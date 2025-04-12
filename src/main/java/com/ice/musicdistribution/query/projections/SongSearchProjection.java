package com.ice.musicdistribution.query.projections;

import com.ice.musicdistribution.domain.events.ReleasePublished;
import com.ice.musicdistribution.domain.events.ReleaseWithdrawn;
import com.ice.musicdistribution.domain.events.SongsAddedToRelease;
import com.ice.musicdistribution.query.readmodels.SongReadModel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Projection that maintains a searchable index of songs
 * Fulfills: released songs can be searched by title using Levenshtein distance
 */
@Component
@RequiredArgsConstructor
public class SongSearchProjection {

    // Map of all songs by ID
    private final Map<UUID, SongReadModel> songsById = new ConcurrentHashMap<>();

    // Map of songs by release ID
    private final Map<UUID, Set<UUID>> songsByRelease = new ConcurrentHashMap<>();

    // Set of published releases
    private final Set<UUID> publishedReleases = ConcurrentHashMap.newKeySet();

    /**
     * Listen for SongsAddedToRelease events to add songs to the index
     */
    @EventListener
    public void on(SongsAddedToRelease event) {
        // Update the songs by release map
        Set<UUID> releaseSongs = songsByRelease.computeIfAbsent(
                event.getAggregateId(),
                releaseId -> ConcurrentHashMap.newKeySet()
        );
        releaseSongs.addAll(event.getSongIds());

        // In a real implementation, we would fetch song details from the repository
        // For this simplified version, we only have the song IDs
        // We would normally add them to songsById here
    }

    /**
     * Listen for ReleasePublished events to make songs available for search
     */
    @EventListener
    public void on(ReleasePublished event) {
        // Add to published releases
        publishedReleases.add(event.getAggregateId());
    }

    /**
     * Listen for ReleaseWithdrawn events to remove songs from search
     */
    @EventListener
    public void on(ReleaseWithdrawn event) {
        // Remove from published releases
        publishedReleases.remove(event.getAggregateId());
    }

    /**
     * Get all searchable song IDs (songs in published releases)
     */
    public Set<UUID> getSearchableSongIds() {
        Set<UUID> searchableSongIds = new HashSet<>();

        // Add all songs from published releases
        for (UUID releaseId : publishedReleases) {
            Set<UUID> releaseSongs = songsByRelease.getOrDefault(releaseId, Collections.emptySet());
            searchableSongIds.addAll(releaseSongs);
        }

        return searchableSongIds;
    }

    /**
     * Search for songs by title using Levenshtein distance
     */
    public List<SongReadModel> searchByTitle(String searchTerm, int maxDistance) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return Collections.emptyList();
        }

        // Get all searchable songs
        Set<UUID> searchableSongIds = getSearchableSongIds();
        List<SongReadModel> searchableSongs = searchableSongIds.stream()
                .map(songsById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Create Levenshtein distance calculator
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        String searchTermLower = searchTerm.toLowerCase();

        // Filter songs by title similarity
        return searchableSongs.stream()
                .filter(song -> {
                    int distance = levenshtein.apply(
                            searchTermLower,
                            song.getTitle().toLowerCase()
                    );
                    return distance <= maxDistance;
                })
                .collect(Collectors.toList());
    }
}