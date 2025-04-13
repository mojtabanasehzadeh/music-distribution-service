package com.ice.musicdistribution.domain.aggregates;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SongTest {

    @Test
    void testCreateSong() {
        UUID id = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();
        String title = "Test Song";
        Duration duration = Duration.ofMinutes(3).plusSeconds(30);

        Song song = new Song(id, title, artistId, duration);

        assertEquals(id, song.getId());
        assertEquals(title, song.getTitle());
        assertEquals(artistId, song.getArtistId());
        assertEquals(duration, song.getDuration());
        assertEquals(210, song.getDuration().getSeconds());
    }

    @Test
    void testCreateSongWithEmptyTitle() {
        UUID id = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();
        Duration duration = Duration.ofMinutes(3);

        assertThrows(IllegalArgumentException.class, () -> {
            new Song(id, "", artistId, duration);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Song(id, null, artistId, duration);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Song(id, "  ", artistId, duration);
        });
    }

    @Test
    void testCreateSongWithInvalidDuration() {
        UUID id = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();
        String title = "Test Song";

        assertThrows(IllegalArgumentException.class, () -> {
            new Song(id, title, artistId, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Song(id, title, artistId, Duration.ZERO);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Song(id, title, artistId, Duration.ofSeconds(-1));
        });
    }

    @Test
    void testIsCreatedBy() {
        UUID songId = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();
        UUID otherArtistId = UUID.randomUUID();
        String title = "Test Song";
        Duration duration = Duration.ofMinutes(3);

        Song song = new Song(songId, title, artistId, duration);

        assertTrue(song.isCreatedBy(artistId));
        assertFalse(song.isCreatedBy(otherArtistId));
    }

    @Test
    void testIsMonetizable() {
        UUID songId = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();
        String title = "Test Song";
        Duration duration = Duration.ofMinutes(3);

        Song song = new Song(songId, title, artistId, duration);

        // Streams over 30 seconds should be monetizable
        assertTrue(song.isMonetizable(Duration.ofSeconds(31)));
        assertTrue(song.isMonetizable(Duration.ofMinutes(1)));

        // Streams of 30 seconds or less should not be monetizable
        assertFalse(song.isMonetizable(Duration.ofSeconds(30)));
        assertFalse(song.isMonetizable(Duration.ofSeconds(10)));
    }

}