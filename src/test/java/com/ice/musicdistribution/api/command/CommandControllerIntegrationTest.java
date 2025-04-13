package com.ice.musicdistribution.api.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ice.musicdistribution.domain.aggregates.Artist;
import com.ice.musicdistribution.domain.aggregates.LabelRecord;
import com.ice.musicdistribution.domain.aggregates.Release;
import com.ice.musicdistribution.domain.aggregates.Song;
import com.ice.musicdistribution.domain.repositories.ArtistRepository;
import com.ice.musicdistribution.domain.repositories.LabelRepository;
import com.ice.musicdistribution.domain.repositories.ReleaseRepository;
import com.ice.musicdistribution.domain.repositories.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CommandControllerIntegrationTest {

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

    private UUID artistId;
    private UUID labelId;
    private UUID song1Id;
    private UUID song2Id;

    @BeforeEach
    void setup() {
        // Create test data
        artistId = UUID.randomUUID();
        labelId = UUID.randomUUID();
        song1Id = UUID.randomUUID();
        song2Id = UUID.randomUUID();

        // Set up a label
        LabelRecord label = new LabelRecord(labelId, "Test Label");
        labelRepository.save(label);

        // Set up an artist
        Artist artist = new Artist(artistId, "Test Artist", labelId);
        artistRepository.save(artist);

        // Set up songs
        Song song1 = new Song(song1Id, "Test Song 1", artistId, Duration.ofMinutes(3));
        Song song2 = new Song(song2Id, "Test Song 2", artistId, Duration.ofMinutes(4));
        songRepository.save(song1);
        songRepository.save(song2);
    }

    @Test
    void testCreateRelease() throws Exception {
        // Create request data
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", "Test Release");
        requestData.put("artistId", artistId.toString());

        // Perform request
        MvcResult result = mockMvc.perform(post("/commands/releases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Release"))
                .andExpect(jsonPath("$.artistId").value(artistId.toString()))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        // Extract release ID from response
        String responseJson = result.getResponse().getContentAsString();
        UUID releaseId = UUID.fromString(objectMapper.readTree(responseJson).get("id").asText());

        // Verify release was saved to the repository
        Optional<Release> savedRelease = releaseRepository.findById(releaseId);
        assertTrue(savedRelease.isPresent());
        assertEquals("Test Release", savedRelease.get().getTitle());
        assertEquals(artistId, savedRelease.get().getArtistId());
    }

    @Test
    void testAddSongsToRelease() throws Exception {
        // First create a release
        Release release = new Release(UUID.randomUUID(), "Test Release", artistId);
        releaseRepository.save(release);

        // Create request data
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("artistId", artistId.toString());
        requestData.put("songIds", Arrays.asList(song1Id.toString(), song2Id.toString()));

        // Perform request
        mockMvc.perform(put("/commands/releases/" + release.getId() + "/songs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isOk());

        // Verify songs were added to the release
        Release updatedRelease = releaseRepository.findById(release.getId()).orElseThrow();
        assertEquals(2, updatedRelease.getSongIds().size());
        assertTrue(updatedRelease.getSongIds().contains(song1Id));
        assertTrue(updatedRelease.getSongIds().contains(song2Id));
    }

    @Test
    void testProposeReleaseDate() throws Exception {
        // First create a release
        Release release = new Release(UUID.randomUUID(), "Test Release", artistId);
        releaseRepository.save(release);

        // Create request data
        LocalDate proposedDate = LocalDate.now().plusDays(30);
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("artistId", artistId.toString());
        requestData.put("proposedDate", proposedDate.toString());

        // Perform request
        mockMvc.perform(put("/commands/releases/" + release.getId() + "/propose-date")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isOk());

        // Verify release date was proposed
        Release updatedRelease = releaseRepository.findById(release.getId()).orElseThrow();
        assertEquals(proposedDate, updatedRelease.getProposedReleaseDate());
        assertEquals(Release.ReleaseStatus.PROPOSED, updatedRelease.getStatus());
    }

    @Test
    void testApproveReleaseDate() throws Exception {
        // First create a release and propose a date
        Release release = new Release(UUID.randomUUID(), "Test Release", artistId);
        LocalDate proposedDate = LocalDate.now().plusDays(30);
        release.proposeReleaseDate(proposedDate);
        releaseRepository.save(release);

        // Create request data
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("labelId", labelId.toString());
        requestData.put("approvedDate", proposedDate.toString());

        // Perform request
        mockMvc.perform(put("/commands/releases/" + release.getId() + "/approve-date")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isOk());

        // Verify release date was approved
        Release updatedRelease = releaseRepository.findById(release.getId()).orElseThrow();
        assertEquals(proposedDate, updatedRelease.getApprovedReleaseDate());
        assertEquals(Release.ReleaseStatus.APPROVED, updatedRelease.getStatus());
    }

    @Test
    void testPublishRelease() throws Exception {
        // First create a release, propose and approve a date
        Release release = new Release(UUID.randomUUID(), "Test Release", artistId);
        LocalDate approvedDate = LocalDate.now();
        release.proposeReleaseDate(approvedDate);
        release.approveReleaseDate(approvedDate);
        releaseRepository.save(release);

        // Perform request
        mockMvc.perform(post("/commands/releases/" + release.getId() + "/publish"))
                .andExpect(status().isOk());

        // Verify release was published
        Release updatedRelease = releaseRepository.findById(release.getId()).orElseThrow();
        assertEquals(Release.ReleaseStatus.PUBLISHED, updatedRelease.getStatus());
        assertNotNull(updatedRelease.getPublishedDate());
    }

    @Test
    void testWithdrawRelease() throws Exception {
        // First create and publish a release
        Release release = new Release(UUID.randomUUID(), "Test Release", artistId);
        release.proposeReleaseDate(LocalDate.now());
        release.approveReleaseDate(LocalDate.now());
        release.publish(LocalDate.now());
        releaseRepository.save(release);

        // Perform request
        mockMvc.perform(delete("/commands/releases/" + release.getId() + "?artistId=" + artistId))
                .andExpect(status().isOk());

        // Verify release was withdrawn
        Release updatedRelease = releaseRepository.findById(release.getId()).orElseThrow();
        assertEquals(Release.ReleaseStatus.WITHDRAWN, updatedRelease.getStatus());
    }

    @Test
    void testCreateReleaseWithInvalidData() throws Exception {
        // Create request with missing required data
        Map<String, Object> requestData = new HashMap<>();
        // Missing title and artistId

        // Perform request
        mockMvc.perform(post("/commands/releases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddSongsToReleaseOwnedByDifferentArtist() throws Exception {
        // Create a release owned by artist1
        Release release = new Release(UUID.randomUUID(), "Test Release", artistId);
        releaseRepository.save(release);

        // Create different artist
        UUID differentArtistId = UUID.randomUUID();
        Artist differentArtist = new Artist(differentArtistId, "Different Artist", labelId);
        artistRepository.save(differentArtist);

        // Create request data with different artist
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("artistId", differentArtistId.toString());
        requestData.put("songIds", Arrays.asList(song1Id.toString(), song2Id.toString()));

        // Perform request
        mockMvc.perform(put("/commands/releases/" + release.getId() + "/songs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isConflict());
    }

    @Test
    void testPublishReleaseWithoutApproval() throws Exception {
        // Create a release that is not yet approved
        Release release = new Release(UUID.randomUUID(), "Test Release", artistId);
        releaseRepository.save(release);

        // Perform request
        mockMvc.perform(post("/commands/releases/" + release.getId() + "/publish"))
                .andExpect(status().isConflict());
    }
}