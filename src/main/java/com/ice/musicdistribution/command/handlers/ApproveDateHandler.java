package com.ice.musicdistribution.command.handlers;

import com.ice.musicdistribution.command.commands.ApproveReleaseDate;
import com.ice.musicdistribution.domain.aggregates.Artist;
import com.ice.musicdistribution.domain.aggregates.LabelRecord;
import com.ice.musicdistribution.domain.aggregates.Release;
import com.ice.musicdistribution.domain.events.ReleaseDateApproved;
import com.ice.musicdistribution.domain.repositories.ArtistRepository;
import com.ice.musicdistribution.domain.repositories.LabelRepository;
import com.ice.musicdistribution.domain.repositories.ReleaseRepository;
import com.ice.musicdistribution.eventstore.EventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for the ApproveReleaseDate command
 */
@Component
@RequiredArgsConstructor
public class ApproveDateHandler {
    private final ReleaseRepository releaseRepository;
    private final LabelRepository labelRepository;
    private final ArtistRepository artistRepository;
    private final EventStore eventStore;

    /**
     * Handle the ApproveReleaseDate command
     */
    @Transactional
    public void handle(ApproveReleaseDate command) {
        // 1. Retrieve the release
        Release release = releaseRepository.findById(command.getReleaseId())
                .orElseThrow(() -> new IllegalArgumentException("Release not found"));

        // 2. Retrieve the artist
        Artist artist = artistRepository.findById(release.getArtistId())
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

        // 3. Verify that the label is responsible for this artist
        if (!artist.getLabelId().equals(command.getLabelId())) {
            throw new IllegalStateException("Label is not responsible for this artist");
        }

        // 4. Retrieve the label
        LabelRecord label = labelRepository.findById(command.getLabelId())
                .orElseThrow(() -> new IllegalArgumentException("Label not found"));

        // 5. Verify that the label can approve this release
        if (!label.canApproveRelease(artist.getId(), release)) {
            throw new IllegalStateException("Label cannot approve this release");
        }

        // 6. Approve the release date
        release.approveReleaseDate(command.getApprovedDate());

        // 7. Save the updated release
        releaseRepository.save(release);

        // 8. Publish the ReleaseDateApproved event
        ReleaseDateApproved event = ReleaseDateApproved.fromRelease(release, label.getId());
        eventStore.store(event);
    }
}