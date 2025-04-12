package com.ice.musicdistribution.command.handlers;

import com.ice.musicdistribution.command.commands.AddSongsToRelease;
import com.ice.musicdistribution.domain.aggregates.Release;
import com.ice.musicdistribution.domain.aggregates.Song;
import com.ice.musicdistribution.domain.events.SongsAddedToRelease;
import com.ice.musicdistribution.domain.repositories.ReleaseRepository;
import com.ice.musicdistribution.domain.repositories.SongRepository;
import com.ice.musicdistribution.eventstore.EventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handler for the AddSongsToRelease command
 */
@Component
@RequiredArgsConstructor
public class AddSongsHandler {
    private final ReleaseRepository releaseRepository;
    private final SongRepository songRepository;
    private final EventStore eventStore;

    /**
     * Handle the AddSongsToRelease command
     */
    @Transactional
    public void handle(AddSongsToRelease command) {
        // 1. Retrieve the release
        Release release = releaseRepository.findById(command.getReleaseId())
                .orElseThrow(() -> new IllegalArgumentException("Release not found"));

        // 2. Verify that the artist owns the release
        if (!release.getArtistId().equals(command.getArtistId())) {
            throw new IllegalStateException("Artist does not own this release");
        }

        // 3. Verify that the songs exist and belong to the artist
        Set<Song> songs = command.getSongIds().stream()
                .map(songId -> songRepository.findById(songId)
                        .orElseThrow(() -> new IllegalArgumentException("Song not found: " + songId)))
                .collect(Collectors.toSet());

        for (Song song : songs) {
            if (!song.getArtistId().equals(command.getArtistId())) {
                throw new IllegalStateException("Artist does not own all the songs");
            }
        }

        // 4. Add the songs to the release
        release.addSongs(command.getSongIds());

        // 5. Save the updated release
        releaseRepository.save(release);

        // 6. Publish the SongsAddedToRelease event
        SongsAddedToRelease event = SongsAddedToRelease.fromRelease(release, command.getSongIds());
        eventStore.store(event);
    }
}