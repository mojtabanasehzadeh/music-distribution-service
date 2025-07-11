package com.moji.musicdistribution.query.projections;

import com.moji.musicdistribution.domain.aggregates.Artist;
import com.moji.musicdistribution.domain.aggregates.Song;
import com.moji.musicdistribution.domain.aggregates.Stream;
import com.moji.musicdistribution.domain.events.StreamRecorded;
import com.moji.musicdistribution.domain.repositories.ArtistRepository;
import com.moji.musicdistribution.domain.repositories.SongRepository;
import com.moji.musicdistribution.domain.repositories.StreamRepository;
import com.moji.musicdistribution.query.readmodels.ArtistStreamReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArtistStreamProjectionTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private SongRepository songRepository;

    @Mock
    private StreamRepository streamRepository;

    @InjectMocks
    private ArtistStreamProjection projection;

    private UUID artistId;
    private UUID song1Id;
    private UUID song2Id;
    private UUID user1Id;
    private UUID user2Id;
    private Artist artist;
    private List<Song> songs;
    private List<Stream> streams;
    private Instant now;
    private Instant yesterday;
    private Instant lastWeek;

    @BeforeEach
    void setUp() {
        artistId = UUID.randomUUID();
        song1Id = UUID.randomUUID();
        song2Id = UUID.randomUUID();
        user1Id = UUID.randomUUID();
        user2Id = UUID.randomUUID();
        now = Instant.now();
        yesterday = now.minus(1, ChronoUnit.DAYS);
        lastWeek = now.minus(7, ChronoUnit.DAYS);

        // Create artist
        artist = new Artist(artistId, "Test Artist", UUID.randomUUID());

        // Create songs
        Song song1 = new Song(song1Id, "Song 1", artistId, Duration.ofMinutes(3));
        Song song2 = new Song(song2Id, "Song 2", artistId, Duration.ofMinutes(4));
        songs = Arrays.asList(song1, song2);

        // Create streams - 2 monetizable, 1 not monetizable
        Stream stream1 = new Stream(UUID.randomUUID(), song1Id, user1Id, now, Duration.ofSeconds(45));
        Stream stream2 = new Stream(UUID.randomUUID(), song1Id, user2Id, yesterday, Duration.ofSeconds(20));
        Stream stream3 = new Stream(UUID.randomUUID(), song2Id, user1Id, yesterday, Duration.ofSeconds(60));
        streams = Arrays.asList(stream1, stream2, stream3);
    }

    // Helper to set up default mocks for report generation tests
    private void setupDefaultReportMocks() {
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(songRepository.findByArtistId(artistId)).thenReturn(songs);
        when(streamRepository.findByArtistId(artistId)).thenReturn(streams);
    }


    @Test
    void testOnStreamRecordedEvent() {
        // Create a StreamRecorded event
        UUID streamId = UUID.randomUUID();
        StreamRecorded event = new StreamRecorded(
                streamId,
                song1Id,
                user1Id,
                now,
                Duration.ofSeconds(45),
                artistId,
                "Song 1"
        );

        // Call the event handler
        projection.on(event);

        // Not much to verify since our implementation just updates an in-memory cache
        // In a real test with a persistent projection, we would verify repository calls
    }

    @Test
    void testGenerateStreamReport_AllStreams() {

        setupDefaultReportMocks();

        // Generate report for all time
        ArtistStreamReport report = projection.generateStreamReport(artistId, null, null);

        // Verify the report
        assertNotNull(report);
        assertEquals(artistId, report.getArtistId());
        assertEquals("Test Artist", report.getArtistName());
        assertEquals(3, report.getTotalStreams());
        assertEquals(2, report.getMonetizedStreams());
        assertEquals(1, report.getNonMonetizedStreams());

        // Verify song stats
        assertNotNull(report.getSongStats());
        assertEquals(2, report.getSongStats().size());

        // Verify first song stats
        ArtistStreamReport.SongStreamStats song1Stats = findSongStats(report.getSongStats(), song1Id);
        assertNotNull(song1Stats);
        assertEquals("Song 1", song1Stats.getSongTitle());
        assertEquals(2, song1Stats.getTotalStreams());
        assertEquals(1, song1Stats.getMonetizedStreams());
        assertEquals(1, song1Stats.getNonMonetizedStreams());

        // Verify second song stats
        ArtistStreamReport.SongStreamStats song2Stats = findSongStats(report.getSongStats(), song2Id);
        assertNotNull(song2Stats);
        assertEquals("Song 2", song2Stats.getSongTitle());
        assertEquals(1, song2Stats.getTotalStreams());
        assertEquals(1, song2Stats.getMonetizedStreams());
        assertEquals(0, song2Stats.getNonMonetizedStreams());
    }

    @Test
    void testGenerateStreamReport_DateRange() {

        setupDefaultReportMocks();

        // Generate report for just today
        ArtistStreamReport report = projection.generateStreamReport(artistId, now.minus(12, ChronoUnit.HOURS), now.plus(12, ChronoUnit.HOURS));

        // Verify the report
        assertNotNull(report);
        assertEquals(1, report.getTotalStreams());
        assertEquals(1, report.getMonetizedStreams());
        assertEquals(0, report.getNonMonetizedStreams());

        // Verify song stats - should only include stream1
        assertEquals(1, report.getSongStats().size());
        ArtistStreamReport.SongStreamStats songStats = report.getSongStats().get(0);
        assertEquals(song1Id, songStats.getSongId());
        assertEquals(1, songStats.getTotalStreams());
        assertEquals(1, songStats.getMonetizedStreams());
        assertEquals(0, songStats.getNonMonetizedStreams());
    }

    @Test
    void testGenerateStreamReport_NoStreams() {

        setupDefaultReportMocks();
        // Setup empty streams
        when(streamRepository.findByArtistId(artistId)).thenReturn(Collections.emptyList());

        // Generate report
        ArtistStreamReport report = projection.generateStreamReport(artistId, null, null);

        // Verify the report
        assertNotNull(report);
        assertEquals(0, report.getTotalStreams());
        assertEquals(0, report.getMonetizedStreams());
        assertEquals(0, report.getNonMonetizedStreams());
        assertTrue(report.getSongStats().isEmpty());
    }

    @Test
    void testGenerateStreamReport_ArtistNotFound() {
        // Setup artist not found
        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

        // Verify exception is thrown
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> projection.generateStreamReport(artistId, null, null)
        );

        assertEquals("Artist not found", exception.getMessage());
    }

    @Test
    void testGenerateStreamReport_SortedByMostStreams() {

        setupDefaultReportMocks();

        // Add more streams to song2 to make it have the most streams
        Stream stream4 = new Stream(UUID.randomUUID(), song2Id, user2Id, yesterday, Duration.ofSeconds(45));
        Stream stream5 = new Stream(UUID.randomUUID(), song2Id, user1Id, lastWeek, Duration.ofSeconds(45));
        when(streamRepository.findByArtistId(artistId)).thenReturn(Arrays.asList(
                streams.get(0), streams.get(1), streams.get(2), stream4, stream5
        ));

        // Generate report
        ArtistStreamReport report = projection.generateStreamReport(artistId, null, null);

        // Verify song stats are sorted by total streams (most first)
        assertEquals(2, report.getSongStats().size());
        assertEquals(song2Id, report.getSongStats().get(0).getSongId());
        assertEquals(3, report.getSongStats().get(0).getTotalStreams());
        assertEquals(song1Id, report.getSongStats().get(1).getSongId());
        assertEquals(2, report.getSongStats().get(1).getTotalStreams());
    }

    private ArtistStreamReport.SongStreamStats findSongStats(List<ArtistStreamReport.SongStreamStats> stats, UUID songId) {
        return stats.stream()
                .filter(s -> s.getSongId().equals(songId))
                .findFirst()
                .orElse(null);
    }
}