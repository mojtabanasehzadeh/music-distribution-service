package com.ice.musicdistribution.query.services;

import com.ice.musicdistribution.domain.aggregates.Release;
import com.ice.musicdistribution.domain.aggregates.Song;
import com.ice.musicdistribution.domain.repositories.ReleaseRepository;
import com.ice.musicdistribution.domain.repositories.SongRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for searching songs by title using Levenshtein distance algorithm
 */
@Service
@RequiredArgsConstructor
public class LevenshteinSearchService {

    private final SongRepository songRepository;
    private final ReleaseRepository releaseRepository;

    /**
     * Search for songs by title using Levenshtein distance
     * Only returns songs from published releases
     *
     * @param searchTerm  The search term to match against song titles
     * @param maxDistance The maximum Levenshtein distance allowed for a match
     * @return List of songs that match the search criteria
     */
    public List<Song> searchSongsByTitle(String searchTerm, int maxDistance) {

        // First, get all songs
        List<Song> allSongs = songRepository.findAll();

        // Get all published releases
        List<Release> publishedReleases = releaseRepository.findAll().stream()
                .filter(Release::isPublished)
                .collect(Collectors.toList());

        // Extract all song IDs from published releases
        Set<UUID> availableSongIds = new HashSet<>();
        for (Release release : publishedReleases) {
            availableSongIds.addAll(release.getSongIds());
        }

        // Create Levenshtein distance calculator
        LevenshteinDistance levenshtein = new LevenshteinDistance();

        // Filter songs by title similarity and availability
        return allSongs.stream()
                .filter(song -> availableSongIds.contains(song.getId())) // Only include available songs
                .filter(song -> {
                    // Calculate Levenshtein distance between search term and song title
                    int distance = levenshtein.apply(
                            searchTerm.toLowerCase(),
                            song.getTitle().toLowerCase()
                    );
                    return distance <= maxDistance;
                })
                .collect(Collectors.toList());
    }
}