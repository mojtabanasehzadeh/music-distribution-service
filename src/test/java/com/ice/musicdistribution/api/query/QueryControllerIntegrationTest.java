package com.ice.musicdistribution.api.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.musicdistribution.domain.aggregates.*;
import com.ice.musicdistribution.domain.repositories.*;
import com.ice.musicdistribution.eventstore.EventStore;
import com.ice.musicdistribution.query.projections.ArtistStreamProjection;
import com.ice.musicdistribution.query.readmodels.ArtistStreamReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class QueryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private StreamRepository streamRepository;

    @Autowired
    private ArtistStreamProjection artistStreamProjection;

    @Autowired
    private EventStore eventStore;

    @Autowired
    private Clock clock;

    private UUID artistId;
    private UUID labelId;
    private UUID song1Id;
    private UUID song2Id;
    private UUID releaseId;
    private UUID userId;

    @BeforeEach
    void setup() {
        // Generate IDs
        artistId = UUID.randomUUID();
        labelId = UUID.randomUUID();
        song1Id = UUID.randomUUID();
        song2Id = UUID.randomUUID();
        releaseId = UUID.randomUUID();
        userId = UUID.randomUUID();

        // Create test data
        setupCompleteTestScenario();
    }

    @Test
    void testSearchSongsByTitle() throws Exception {
        // Search for songs with title similar to "Bad Habit"
        MvcResult result = mockMvc.perform(get("/queries/songs/search")
                        .param("searchTerm", "Bad Habi")
                        .param("maxDistance", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", containsString("Bad Habit")))
                .andReturn();

        // Verify the response contains expected data
        String responseJson = result.getResponse().getContentAsString();
        assertNotNull(responseJson);
        assertTrue(responseJson.contains("Bad Habit"));
    }

    @Test
    void testGetArtistStreamReport() throws Exception {
        // Get the artist stream report
        MvcResult result = mockMvc.perform(get("/queries/artists/{artistId}/stream-report", artistId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artistId").value(artistId.toString()))
                .andExpect(jsonPath("$.totalStreams").value(2))
                .andExpect(jsonPath("$.monetizedStreams").value(1))
                .andExpect(jsonPath("$.nonMonetizedStreams").value(1))
                .andExpect(jsonPath("$.songStats", hasSize(2)))
                .andReturn();

        // Verify that the report contains data for both songs
        String responseJson = result.getResponse().getContentAsString();
        assertTrue(responseJson.contains(song1Id.toString()));
        assertTrue(responseJson.contains(song2Id.toString()));
    }

    @Test
    void testGetArtistPaymentReport() throws Exception {
        // Get the payment report
        Instant fromDate = Instant.now(clock).minus(1, ChronoUnit.DAYS);
        Instant toDate = Instant.now(clock);

        MvcResult result = mockMvc.perform(get("/queries/artists/{artistId}/payment-report", artistId)
                        .param("fromDate", fromDate.toString())
                        .param("toDate", toDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artistId").value(artistId.toString()))
                .andExpect(jsonPath("$.totalMonetizedStreams").value(1))
                .andExpect(jsonPath("$.totalAmount").isNumber())
                .andReturn();

        // Verify the response contains expected data
        String responseJson = result.getResponse().getContentAsString();
        assertTrue(responseJson.contains("totalAmount"));
        assertTrue(responseJson.contains("songPayments"));
    }

    @Test
    void testGetArtistMonetizationReport() throws Exception {
        // Get the monetization report
        MvcResult result = mockMvc.perform(get("/queries/artists/{artistId}/monetization-report", artistId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artistId").value(artistId.toString()))
                .andExpect(jsonPath("$.totalStreams").value(2))
                .andExpect(jsonPath("$.monetizableStreams").value(1))
                .andExpect(jsonPath("$.estimatedRevenue").isNumber())
                .andReturn();

        // Verify the response contains expected data
        String responseJson = result.getResponse().getContentAsString();
        assertTrue(responseJson.contains("estimatedRevenue"));
    }

    /**
     * Set up a complete test scenario with published release and streams
     */
    private void setupCompleteTestScenario() {
        // Create label
        LabelRecord label = new LabelRecord(labelId, "Test Label");
        labelRepository.save(label);

        // Create artist
        Artist artist = new Artist(artistId, "Test Artist", labelId);
        artistRepository.save(artist);

        // Create songs
        Song song1 = new Song(song1Id, "Bad Habits", artistId, Duration.ofMinutes(4));
        Song song2 = new Song(song2Id, "Galway Girl", artistId, Duration.ofMinutes(3));
        songRepository.save(song1);
        songRepository.save(song2);

        // Create and publish a release
        Release release = new Release(releaseId, "Test Release", artistId);
        release.addSongs(Set.of(song1Id, song2Id));
        release.proposeReleaseDate(LocalDate.now(clock));
        release.approveReleaseDate(LocalDate.now(clock));
        release.publish(LocalDate.now(clock));
        releaseRepository.save(release);

        // Create two streams - one monetizable, one not
        Stream stream1 = new Stream(
                UUID.randomUUID(),
                song1Id,
                userId,
                Instant.now(clock),
                Duration.ofSeconds(45)
        );
        Stream stream2 = new Stream(
                UUID.randomUUID(),
                song2Id,
                userId,
                Instant.now(clock),
                Duration.ofSeconds(25)
        );
        streamRepository.save(stream1);
        streamRepository.save(stream2);

        // Make sure the projections are up to date - in a real system this would
        // happen automatically via event listeners, but for the test we need to
        // ensure the data is available for queries
        ArtistStreamReport report = artistStreamProjection.generateStreamReport(
                artistId,
                Instant.now(clock).minus(1, ChronoUnit.DAYS),
                Instant.now(clock)
        );
    }
}