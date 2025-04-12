package com.ice.musicdistribution.command.handlers;

import com.ice.musicdistribution.command.commands.CreateRelease;
import com.ice.musicdistribution.domain.aggregates.Artist;
import com.ice.musicdistribution.domain.aggregates.Release;
import com.ice.musicdistribution.domain.events.ReleaseCreated;
import com.ice.musicdistribution.domain.repositories.ArtistRepository;
import com.ice.musicdistribution.domain.repositories.ReleaseRepository;
import com.ice.musicdistribution.eventstore.EventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for the CreateRelease command
 */
@Component
@RequiredArgsConstructor
public class CreateReleaseHandler {
    private final ReleaseRepository releaseRepository;
    private final ArtistRepository artistRepository;
    private final EventStore eventStore;

    /**
     * Handle the CreateRelease command
     * @return The created release
     */
    @Transactional
    public Release handle(CreateRelease command) {
        // Check if the artist exists
        Artist artist = artistRepository.findById(command.getArtistId())
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

        // Create the release
        Release release = new Release(
                command.getReleaseId(),
                command.getTitle(),
                command.getArtistId()
        );

        // Save the release
        releaseRepository.save(release);

        // Publish the ReleaseCreated event
        ReleaseCreated event = new ReleaseCreated(
                release.getId(),
                release.getTitle(),
                release.getArtistId()
        );
        eventStore.store(event);

        // Return the created release
        return release;
    }
}