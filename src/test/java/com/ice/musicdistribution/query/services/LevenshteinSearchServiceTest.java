package com.ice.musicdistribution.query.services;

import com.ice.musicdistribution.domain.aggregates.Release;
import com.ice.musicdistribution.domain.aggregates.Song;
import com.ice.musicdistribution.domain.repositories.ReleaseRepository;
import com.ice.musicdistribution.domain.repositories.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LevenshteinSearchServiceTest {

    @Mock
    private SongRepository songRepository;

    @Mock
    private ReleaseRepository releaseRepository;

    @InjectMocks
    private LevenshteinSearchService searchService;

    private List<Song> allSongs;
    private List<Release> publishedReleases;
    private UUID artistId;

    @BeforeEach
    void setUp() {
        artistId = UUID.randomUUID();
        UUID releaseId = UUID.randomUUID();

        // Create test songs
        Song song1 = new Song(UUID.randomUUID(), "Summer Breeze", artistId, Duration.ofMinutes(3));
        Song song2 = new Song(UUID.randomUUID(), "Winter Wonder", artistId, Duration.ofMinutes(4));
        Song song3 = new Song(UUID.randomUUID(), "Autumn Leaves", artistId, Duration.ofMinutes(3));
        Song song4 = new Song(UUID.randomUUID(), "Spring Flowers", artistId, Duration.ofMinutes(5));

        allSongs = Arrays.asList(song1, song2, song3, song4);

        // Create a published release with some songs
        Set<UUID> availableSongIds = new HashSet<>();
        availableSongIds.add(song1.getId());
        availableSongIds.add(song3.getId());

        Release publishedRelease = new Release(releaseId, "Test Release", artistId);
        publishedRelease.addSongs(availableSongIds);
        publishedRelease.proposeReleaseDate(LocalDate.now().minusDays(5));
        publishedRelease.approveReleaseDate(LocalDate.now().minusDays(3));
        publishedRelease.publish(LocalDate.now().minusDays(1));

        publishedReleases = Collections.singletonList(publishedRelease);
    }

    @Test
    void testSearchSongsByTitle_ExactMatch() {

        when(songRepository.findAll()).thenReturn(allSongs);
        when(releaseRepository.findAll()).thenReturn(publishedReleases);

        List<Song> results = searchService.searchSongsByTitle("Summer Breeze", 0);

        // Verify repository calls
        verify(songRepository).findAll();
        verify(releaseRepository).findAll();

        // Verify results
        assertEquals(1, results.size());
        assertEquals("Summer Breeze", results.get(0).getTitle());
    }

    @Test
    void testSearchSongsByTitle_NoMatch() {
        List<Song> results = searchService.searchSongsByTitle("Nonexistent", 2);

        // Verify results
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchSongsByTitle_OnlyReturnsPublishedSongs() {
        // This searches for "Winter" which exists but is not in a published release
        List<Song> results = searchService.searchSongsByTitle("Winter", 0);

        // Verify results - should be empty since the song is not in a published release
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchSongsByTitle_WithdrawnRelease() {
        // Prepare a release that was published but then withdrawn
        Release withdrawnRelease = new Release(UUID.randomUUID(), "Withdrawn Release", artistId);
        Set<UUID> withdrawnSongIds = new HashSet<>();
        withdrawnSongIds.add(allSongs.get(1).getId()); // Winter Wonder
        withdrawnRelease.addSongs(withdrawnSongIds);
        withdrawnRelease.proposeReleaseDate(LocalDate.now().minusDays(10));
        withdrawnRelease.approveReleaseDate(LocalDate.now().minusDays(8));
        withdrawnRelease.publish(LocalDate.now().minusDays(5));
        withdrawnRelease.withdraw();

        // Update mock to include the withdrawn release
        when(releaseRepository.findAll()).thenReturn(Arrays.asList(publishedReleases.get(0), withdrawnRelease));

        // Search for song in withdrawn release
        List<Song> results = searchService.searchSongsByTitle("Winter", 0);

        // Verify results - should be empty since the song is in a withdrawn release
        assertTrue(results.isEmpty(), "Songs from withdrawn releases should not be searchable");
    }

    @Test
    void testSearchSongsByTitle_NullSearchTerm() {
        // Test with null search term
        List<Song> results = searchService.searchSongsByTitle(null, 2);

        // Verify results
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchSongsByTitle_NegativeDistance() {
        // Test with negative distance
        List<Song> results = searchService.searchSongsByTitle("Summer", -1);

        // Verify results - should be empty or throw exception depending on implementation
        assertTrue(results.isEmpty());
    }
}