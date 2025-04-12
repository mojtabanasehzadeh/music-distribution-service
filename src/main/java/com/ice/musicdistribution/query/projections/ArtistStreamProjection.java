package com.ice.musicdistribution.query.projections;

import com.ice.musicdistribution.domain.aggregates.Artist;
import com.ice.musicdistribution.domain.aggregates.Song;
import com.ice.musicdistribution.domain.aggregates.Stream;
import com.ice.musicdistribution.domain.events.StreamRecorded;
import com.ice.musicdistribution.domain.repositories.ArtistRepository;
import com.ice.musicdistribution.domain.repositories.SongRepository;
import com.ice.musicdistribution.domain.repositories.StreamRepository;
import com.ice.musicdistribution.query.readmodels.ArtistStreamReport;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Projection that builds and maintains artist stream reports
 * Fulfills: artist can request a report of streamed songs
 */
@Component
@RequiredArgsConstructor
public class ArtistStreamProjection {

    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final StreamRepository streamRepository;

    // Optional: In-memory cache for faster lookups
    private final Map<UUID, Map<UUID, String>> artistSongTitlesCache = new HashMap<>();

    /**
     * Listen for StreamRecorded events and update projection data
     */
    @EventListener
    public void on(StreamRecorded event) {
        // For a real implementation, we might update a dedicated read model table in the database
        // For this simplified implementation, we'll rely on the repositories

        // Update the artist song titles cache
        Map<UUID, String> songTitles = artistSongTitlesCache.computeIfAbsent(
                event.getArtistId(),
                artistId -> new HashMap<>()
        );
        songTitles.put(event.getSongId(), event.getSongTitle());
    }

    /**
     * Generate a stream report for an artist
     */
    public ArtistStreamReport generateStreamReport(UUID artistId, Instant fromDate, Instant toDate) {
        // Get the artist
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

        // Get all songs by this artist
        List<Song> artistSongs = songRepository.findByArtistId(artistId);
        Map<UUID, String> songTitles = artistSongs.stream()
                .collect(Collectors.toMap(Song::getId, Song::getTitle));

        // Get all streams for this artist's songs
        List<Stream> streams;
        if (fromDate != null && toDate != null) {
            // Filter by date range if provided
            streams = streamRepository.findByArtistId(artistId).stream()
                    .filter(stream -> !stream.getStreamDate().isBefore(fromDate) &&
                            !stream.getStreamDate().isAfter(toDate))
                    .collect(Collectors.toList());
        } else {
            // Get all streams if no date range
            streams = streamRepository.findByArtistId(artistId);
        }

        // Group streams by song
        Map<UUID, List<Stream>> streamsBySong = new HashMap<>();
        for (Stream stream : streams) {
            streamsBySong.computeIfAbsent(stream.getSongId(), k -> new ArrayList<>())
                    .add(stream);
        }

        // Calculate statistics for each song
        List<ArtistStreamReport.SongStreamStats> songStats = new ArrayList<>();
        int totalMonetizedStreams = 0;
        int totalNonMonetizedStreams = 0;

        for (UUID songId : streamsBySong.keySet()) {
            List<Stream> songStreams = streamsBySong.get(songId);
            int monetizedCount = 0;
            int nonMonetizedCount = 0;

            for (Stream stream : songStreams) {
                if (stream.isMonetizable()) {
                    monetizedCount++;
                } else {
                    nonMonetizedCount++;
                }
            }

            totalMonetizedStreams += monetizedCount;
            totalNonMonetizedStreams += nonMonetizedCount;

            songStats.add(new ArtistStreamReport.SongStreamStats(
                    songId,
                    songTitles.getOrDefault(songId, "Unknown Song"),
                    songStreams.size(),
                    monetizedCount,
                    nonMonetizedCount
            ));
        }

        // Sort song stats by total streams (most to least)
        songStats.sort(Comparator.comparing(ArtistStreamReport.SongStreamStats::getTotalStreams).reversed());

        // Create and return the report
        return new ArtistStreamReport(
                artistId,
                artist.getName(),
                streams.size(),
                totalMonetizedStreams,
                totalNonMonetizedStreams,
                fromDate,
                toDate,
                songStats
        );
    }
}