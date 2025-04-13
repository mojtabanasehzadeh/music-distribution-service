package com.ice.musicdistribution.demo;

import com.ice.musicdistribution.command.CommandBus;
import com.ice.musicdistribution.command.commands.*;
import com.ice.musicdistribution.domain.aggregates.*;
import com.ice.musicdistribution.domain.events.DomainEvent;
import com.ice.musicdistribution.domain.repositories.*;
import com.ice.musicdistribution.eventstore.EventStore;
import com.ice.musicdistribution.query.projections.ArtistStreamProjection;
import com.ice.musicdistribution.query.projections.PaymentReportProjection;
import com.ice.musicdistribution.query.readmodels.ArtistStreamReport;
import com.ice.musicdistribution.query.readmodels.PaymentReport;
import com.ice.musicdistribution.query.services.LevenshteinSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Demo test of the Music Distribution Service
 * This test showcases the complete flow from release creation to withdrawal
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MusicDistributionDemoTest {

    @Autowired
    private CommandBus commandBus;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private ReleaseRepository releaseRepository;

    @Autowired
    private LevenshteinSearchService searchService;

    @Autowired
    private ArtistStreamProjection artistStreamProjection;

    @Autowired
    private PaymentReportProjection paymentReportProjection;

    @Autowired
    private EventStore eventStore;

    @Autowired
    private Clock clock;

    // Test entities
    private UUID artistId;
    private UUID labelId;
    private UUID song1Id;
    private UUID song2Id;
    private UUID releaseId;
    private UUID userId;

    @BeforeEach
    public void setup() {
        // Create test entities
        setupTestData();
    }

    @Test
    public void fullReleaseLifecycleTest() {
        // Log test entities for debugging
        System.out.println("Artist ID: " + artistId);
        System.out.println("Label ID: " + labelId);
        System.out.println("Song IDs: " + song1Id + ", " + song2Id);
        System.out.println("Release ID: " + releaseId);

        // Step 1: Create a release
        System.out.println("\n--- STEP 1: Create a release ---");
        CreateRelease createReleaseCommand = new CreateRelease(releaseId, "Sheeran Collection", artistId);
        Release release = commandBus.executeForResult(createReleaseCommand);

        assertNotNull(release);
        assertEquals("Sheeran Collection", release.getTitle());
        assertEquals(artistId, release.getArtistId());
        assertEquals(Release.ReleaseStatus.DRAFT, release.getStatus());
        System.out.println("Release created: " + release.getTitle() + " (" + release.getId() + ")");

        // Step 2: Add songs to the release
        System.out.println("\n--- STEP 2: Add songs to the release ---");
        Set<UUID> songIds = new HashSet<>();
        songIds.add(song1Id);
        songIds.add(song2Id);

        AddSongsToRelease addSongsCommand = new AddSongsToRelease(releaseId, songIds, artistId);
        commandBus.execute(addSongsCommand);

        // Verify songs were added
        release = releaseRepository.findById(releaseId).orElseThrow();
        assertEquals(2, release.getSongIds().size());
        assertTrue(release.getSongIds().contains(song1Id));
        assertTrue(release.getSongIds().contains(song2Id));
        System.out.println("Songs added to release. Total songs: " + release.getSongIds().size());

        // Step 3: Propose a release date
        System.out.println("\n--- STEP 3: Propose a release date ---");
        LocalDate tomorrow = LocalDate.now(clock).plus(1, ChronoUnit.DAYS);
        ProposeReleaseDate proposeCommand = new ProposeReleaseDate(releaseId, artistId, tomorrow);
        commandBus.execute(proposeCommand);

        // Verify date was proposed
        release = releaseRepository.findById(releaseId).orElseThrow();
        assertEquals(Release.ReleaseStatus.PROPOSED, release.getStatus());
        assertEquals(tomorrow, release.getProposedReleaseDate());
        System.out.println("Release date proposed: " + release.getProposedReleaseDate());

        // Step 4: Label approves the release date
        System.out.println("\n--- STEP 4: Label approves the release date ---");
        ApproveReleaseDate approveCommand = new ApproveReleaseDate(releaseId, labelId, tomorrow);
        commandBus.execute(approveCommand);

        // Verify date was approved
        release = releaseRepository.findById(releaseId).orElseThrow();
        assertEquals(Release.ReleaseStatus.APPROVED, release.getStatus());
        assertEquals(tomorrow, release.getApprovedReleaseDate());
        System.out.println("Release date approved: " + release.getApprovedReleaseDate());

        // Step 5: Publish the release (simulate that tomorrow has arrived)
        System.out.println("\n--- STEP 5: Publish the release ---");
        PublishRelease publishCommand = new PublishRelease(releaseId, tomorrow);
        commandBus.execute(publishCommand);

        // Verify release was published
        release = releaseRepository.findById(releaseId).orElseThrow();
        assertEquals(Release.ReleaseStatus.PUBLISHED, release.getStatus());
        assertNotNull(release.getPublishedDate());
        System.out.println("Release published on: " + release.getPublishedDate());

        // Step 6: Search for songs
        System.out.println("\n--- STEP 6: Search for songs by title ---");
        List<Song> searchResults = searchService.searchSongsByTitle("Bad Habi", 2);

        // Verify search works
        assertFalse(searchResults.isEmpty());
        boolean foundSong1 = searchResults.stream()
                .anyMatch(song -> song.getId().equals(song1Id));
        assertTrue(foundSong1, "Search should find Bad Habits");
        System.out.println("Search found " + searchResults.size() + " songs");
        for (Song song : searchResults) {
            System.out.println("  - " + song.getTitle());
        }

        // Step 7: Record streams
        System.out.println("\n--- STEP 7: Record streams ---");

        // Long stream (monetizable)
        RecordStream stream1Command = new RecordStream(
                song1Id,
                userId,
                Duration.ofSeconds(45),
                Instant.now(clock)
        );
        commandBus.execute(stream1Command);
        System.out.println("Recorded monetizable stream (45s) for: Bad Habits");

        // Short stream (not monetizable)
        RecordStream stream2Command = new RecordStream(
                song2Id,
                userId,
                Duration.ofSeconds(25),
                Instant.now(clock)
        );
        commandBus.execute(stream2Command);
        System.out.println("Recorded non-monetizable stream (25s) for: Galway Girl");

        // Step 8: Request artist stream report
        System.out.println("\n--- STEP 8: Request artist stream report ---");
        ArtistStreamReport streamReport = artistStreamProjection.generateStreamReport(
                artistId,
                Instant.now(clock).minus(1, ChronoUnit.DAYS),
                Instant.now(clock)
        );

        // Verify stream report
        assertNotNull(streamReport);
        assertEquals(2, streamReport.getTotalStreams());
        assertEquals(1, streamReport.getMonetizedStreams());
        assertEquals(1, streamReport.getNonMonetizedStreams());
        System.out.println("Stream Report:");
        System.out.println("  Total Streams: " + streamReport.getTotalStreams());
        System.out.println("  Monetized Streams: " + streamReport.getMonetizedStreams());
        System.out.println("  Non-monetized Streams: " + streamReport.getNonMonetizedStreams());

        // Step 9: Request payment report
        System.out.println("\n--- STEP 9: Request payment report ---");
        Instant fromDate = Instant.now(clock).minus(1, ChronoUnit.DAYS);
        Instant toDate = Instant.now(clock);

        RequestPaymentReport paymentCommand = new RequestPaymentReport(
                artistId,
                fromDate,
                toDate,
                UUID.randomUUID()
        );
        commandBus.execute(paymentCommand);

        PaymentReport paymentReport = paymentReportProjection.generatePaymentReport(
                artistId,
                fromDate,
                toDate
        );

        // Verify payment report
        assertNotNull(paymentReport);
        assertEquals(1, paymentReport.getTotalMonetizedStreams());
        System.out.println("Payment Report:");
        System.out.println("  Total Monetized Streams: " + paymentReport.getTotalMonetizedStreams());
        System.out.println("  Total Amount: " + paymentReport.getTotalAmount());

        // Step 10: Withdraw the release
        System.out.println("\n--- STEP 10: Withdraw the release ---");
        WithdrawRelease withdrawCommand = new WithdrawRelease(releaseId, artistId);
        commandBus.execute(withdrawCommand);

        // Verify release was withdrawn
        release = releaseRepository.findById(releaseId).orElseThrow();
        assertEquals(Release.ReleaseStatus.WITHDRAWN, release.getStatus());
        System.out.println("Release withdrawn successfully");

        // Step 11: Verify songs are no longer searchable
        System.out.println("\n--- STEP 11: Verify songs are no longer searchable ---");
        searchResults = searchService.searchSongsByTitle("Bad", 2);

        // Verify songs cannot be found
        assertTrue(searchResults.isEmpty(), "Songs from withdrawn releases should not be searchable");
        System.out.println("Search found " + searchResults.size() + " songs (expected: 0)");

        // Get all events for the release
        List<DomainEvent> releaseEvents = eventStore.getEventsForAggregate(releaseId);

        // Expected event sequence for a complete release lifecycle
        List<String> expectedEventSequence = Arrays.asList(
                "ReleaseCreated",
                "SongsAddedToRelease",
                "ReleaseDateProposed",
                "ReleaseDateApproved",
                "ReleasePublished",
                "ReleaseWithdrawn"
        );

        // Verify we have the right number of events
        assertTrue(releaseEvents.size() >= expectedEventSequence.size(),
                "Expected at least " + expectedEventSequence.size() + " events, but found " + releaseEvents.size());

        // Create a list of actual event types in order
        List<String> actualEventSequence = releaseEvents.stream()
                .map(event -> event.getClass().getSimpleName())
                .toList();

        System.out.println("Complete event sequence for release " + releaseId + ":");
        for (int i = 0; i < actualEventSequence.size(); i++) {
            System.out.println((i + 1) + ". " + actualEventSequence.get(i));
        }

        // Verify that all expected events are present in the correct order
        int lastFoundIndex = -1;
        for (String expectedEvent : expectedEventSequence) {
            int currentIndex = -1;
            for (int i = lastFoundIndex + 1; i < actualEventSequence.size(); i++) {
                if (actualEventSequence.get(i).equals(expectedEvent)) {
                    currentIndex = i;
                    break;
                }
            }

            assertTrue(currentIndex > lastFoundIndex,
                    "Event " + expectedEvent + " not found or not in correct sequence");
            lastFoundIndex = currentIndex;
        }
        System.out.println("✓ Complete event sequence verified successfully");

        System.out.println("\nTest completed successfully!");
    }

    /**
     * Set up test data before running the test
     */
    private void setupTestData() {
        // Generate IDs
        artistId = UUID.randomUUID();
        labelId = UUID.randomUUID();
        song1Id = UUID.randomUUID();
        song2Id = UUID.randomUUID();
        releaseId = UUID.randomUUID();
        userId = UUID.randomUUID();

        // Create label
        LabelRecord label = new LabelRecord(labelId, "XYZ Records");
        labelRepository.save(label);

        // Create artist
        Artist artist = new Artist(artistId, "Ed Sheeran", labelId);
        artistRepository.save(artist);

        // Create songs
        Song song1 = new Song(song1Id, "Bad Habits", artistId, Duration.ofMinutes(4));
        Song song2 = new Song(song2Id, "Galway Girl", artistId, Duration.ofMinutes(3));
        songRepository.save(song1);
        songRepository.save(song2);
    }
}