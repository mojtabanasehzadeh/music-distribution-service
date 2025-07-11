package com.moji.musicdistribution.command.handlers;

import com.moji.musicdistribution.command.commands.CreateRelease;
import com.moji.musicdistribution.domain.aggregates.Artist;
import com.moji.musicdistribution.domain.aggregates.Release;
import com.moji.musicdistribution.domain.events.ReleaseCreated;
import com.moji.musicdistribution.domain.repositories.ArtistRepository;
import com.moji.musicdistribution.domain.repositories.ReleaseRepository;
import com.moji.musicdistribution.eventstore.EventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateReleaseHandlerTest {

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private EventStore eventStore;

    @InjectMocks
    private CreateReleaseHandler handler;

    @Captor
    private ArgumentCaptor<Release> releaseCaptor;

    @Captor
    private ArgumentCaptor<ReleaseCreated> eventCaptor;

    private UUID releaseId;
    private UUID artistId;
    private CreateRelease command;
    private Artist artist;

    @BeforeEach
    void setUp() {
        releaseId = UUID.randomUUID();
        artistId = UUID.randomUUID();

        command = new CreateRelease(releaseId, "Test Release", artistId);

        artist = new Artist(artistId, "Test Artist", UUID.randomUUID());

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
    }

    @Test
    void testHandleCreateReleaseCommand() {

        when(releaseRepository.save(any(Release.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute the handler
        Release result = handler.handle(command);

        // Verify artist was checked
        verify(artistRepository).findById(artistId);

        // Verify release was saved
        verify(releaseRepository).save(releaseCaptor.capture());
        Release savedRelease = releaseCaptor.getValue();

        // Verify release properties
        assertEquals(releaseId, savedRelease.getId());
        assertEquals("Test Release", savedRelease.getTitle());
        assertEquals(artistId, savedRelease.getArtistId());
        assertEquals(Release.ReleaseStatus.DRAFT, savedRelease.getStatus());

        // Verify event was stored
        verify(eventStore).store(eventCaptor.capture());
        ReleaseCreated storedEvent = eventCaptor.getValue();

        // Verify event properties
        assertEquals(releaseId, storedEvent.getAggregateId());
        assertEquals("Test Release", storedEvent.getTitle());
        assertEquals(artistId, storedEvent.getArtistId());

        // Verify result matches saved release
        assertSame(savedRelease, result);
    }

    @Test
    void testHandleCreateReleaseCommandArtistNotFound() {
        // Setup mock to return empty for artist
        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

        // Execute handler and verify it throws exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.handle(command)
        );

        // Verify message
        assertEquals("Artist not found", exception.getMessage());

        // Verify no release was saved and no event was stored
        verify(releaseRepository, never()).save(any());
        verify(eventStore, never()).store(any());
    }
}