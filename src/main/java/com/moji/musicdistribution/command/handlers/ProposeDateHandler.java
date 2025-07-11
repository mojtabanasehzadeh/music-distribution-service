package com.moji.musicdistribution.command.handlers;

import com.moji.musicdistribution.command.commands.ProposeReleaseDate;
import com.moji.musicdistribution.domain.aggregates.Artist;
import com.moji.musicdistribution.domain.aggregates.Release;
import com.moji.musicdistribution.domain.events.ReleaseDateProposed;
import com.moji.musicdistribution.domain.repositories.ArtistRepository;
import com.moji.musicdistribution.domain.repositories.ReleaseRepository;
import com.moji.musicdistribution.eventstore.EventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for the ProposeReleaseDate command
 */
@Component
@RequiredArgsConstructor
public class ProposeDateHandler {
    private final ReleaseRepository releaseRepository;
    private final ArtistRepository artistRepository;
    private final EventStore eventStore;

    /**
     * Handle the ProposeReleaseDate command
     */
    @Transactional
    public void handle(ProposeReleaseDate command) {
        // 1. Retrieve the release
        Release release = releaseRepository.findById(command.getReleaseId())
                .orElseThrow(() -> new IllegalArgumentException("Release not found"));

        // 2. Verify that the artist owns the release
        if (!release.getArtistId().equals(command.getArtistId())) {
            throw new IllegalStateException("Artist does not own this release");
        }

        // 3. Get the artist's label ID
        Artist artist = artistRepository.findById(command.getArtistId())
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

        // 4. Propose the release date
        release.proposeReleaseDate(command.getProposedDate());

        // 5. Save the updated release
        releaseRepository.save(release);

        // 6. Publish the ReleaseDateProposed event
        ReleaseDateProposed event = ReleaseDateProposed.fromRelease(release, artist.getLabelId());
        eventStore.store(event);
    }
}