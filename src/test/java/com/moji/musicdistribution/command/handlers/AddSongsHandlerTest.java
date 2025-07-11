package com.moji.musicdistribution.command.handlers;

import com.moji.musicdistribution.command.commands.AddSongsToRelease;
import com.moji.musicdistribution.domain.aggregates.Release;
import com.moji.musicdistribution.domain.aggregates.Song;
import com.moji.musicdistribution.domain.events.SongsAddedToRelease;
import com.moji.musicdistribution.domain.repositories.ReleaseRepository;
import com.moji.musicdistribution.domain.repositories.SongRepository;
import com.moji.musicdistribution.eventstore.EventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddSongsHandlerTest {

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private SongRepository songRepository;

    @Mock
    private EventStore eventStore;

    @InjectMocks
    private AddSongsHandler handler;

    @Captor
    private ArgumentCaptor<Release> releaseCaptor;

    @Captor
    private ArgumentCaptor<SongsAddedToRelease> eventCaptor;

    private UUID releaseId;
    private UUID artistId;
    private Set<UUID> songIds;
    private Release release;
    private Song song1;
    private Song song2;
    UUID song1Id;
    UUID song2Id;
    private AddSongsToRelease command;

    @BeforeEach
    void setUp() {
        releaseId = UUID.randomUUID();
        artistId = UUID.randomUUID();

        // Setup song IDs
        song1Id = UUID.randomUUID();
        song2Id = UUID.randomUUID();
        songIds = new HashSet<>();
        songIds.add(song1Id);
        songIds.add(song2Id);

        // Setup release
        release = new Release(releaseId, "Test Release", artistId);

        // Setup songs
        song1 = new Song(song1Id, "Song 1", artistId, Duration.ofMinutes(3));
        song2 = new Song(song2Id, "Song 2", artistId, Duration.ofMinutes(4));

        // Setup command
        command = new AddSongsToRelease(releaseId, songIds, artistId);

        // Setup mock
        when(releaseRepository.findById(releaseId)).thenReturn(Optional.of(release));
    }

    @Test
    void testHandleAddSongsCommand() {

        when(songRepository.findById(song1Id)).thenReturn(Optional.of(song1));
        when(songRepository.findById(song2Id)).thenReturn(Optional.of(song2));
        when(releaseRepository.save(any(Release.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute the handler
        handler.handle(command);

        // Verify release was retrieved
        verify(releaseRepository).findById(releaseId);

        // Verify songs were retrieved
        for (UUID songId : songIds) {
            verify(songRepository).findById(songId);
        }

        // Verify release was saved
        verify(releaseRepository).save(releaseCaptor.capture());
        Release savedRelease = releaseCaptor.getValue();

        // Verify songs were added to the release
        assertEquals(2, savedRelease.getSongIds().size());
        for (UUID songId : songIds) {
            assertTrue(savedRelease.getSongIds().contains(songId));
        }

        // Verify event was stored
        verify(eventStore).store(eventCaptor.capture());
        SongsAddedToRelease storedEvent = eventCaptor.getValue();

        // Verify event properties
        assertEquals(releaseId, storedEvent.getAggregateId());
        assertEquals(artistId, storedEvent.getArtistId());
        assertEquals("Test Release", storedEvent.getReleaseTitle());
        assertEquals(2, storedEvent.getSongIds().size());
        for (UUID songId : songIds) {
            assertTrue(storedEvent.getSongIds().contains(songId));
        }
    }

    @Test
    void testHandleAddSongsCommandReleaseNotFound() {
        // Setup mock to return empty for release
        when(releaseRepository.findById(releaseId)).thenReturn(Optional.empty());

        // Execute handler and verify it throws exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.handle(command)
        );

        // Verify message
        assertEquals("Release not found", exception.getMessage());

        // Verify no release was saved and no event was stored
        verify(releaseRepository, never()).save(any());
        verify(eventStore, never()).store(any());
    }

    @Test
    void testHandleAddSongsCommandArtistDoesNotOwnRelease() {
        // Setup release with different artist
        UUID differentArtistId = UUID.randomUUID();
        Release release = new Release(releaseId, "Test Release", differentArtistId);
        when(releaseRepository.findById(releaseId)).thenReturn(Optional.of(release));

        // Execute handler and verify it throws exception
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> handler.handle(command)
        );

        // Verify message
        assertEquals("Artist does not own this release", exception.getMessage());

        // Verify no release was saved and no event was stored
        verify(releaseRepository, never()).save(any());
        verify(eventStore, never()).store(any());
    }

}