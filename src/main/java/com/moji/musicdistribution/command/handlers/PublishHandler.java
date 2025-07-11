package com.moji.musicdistribution.command.handlers;

import com.moji.musicdistribution.command.commands.PublishRelease;
import com.moji.musicdistribution.domain.aggregates.Release;
import com.moji.musicdistribution.domain.events.ReleasePublished;
import com.moji.musicdistribution.domain.repositories.ReleaseRepository;
import com.moji.musicdistribution.eventstore.EventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for the PublishRelease command
 */
@Component
@RequiredArgsConstructor
public class PublishHandler {
    private final ReleaseRepository releaseRepository;
    private final EventStore eventStore;

    /**
     * Handle the PublishRelease command
     */
    @Transactional
    public void handle(PublishRelease command) {
        // 1. Retrieve the release
        Release release = releaseRepository.findById(command.getReleaseId())
                .orElseThrow(() -> new IllegalArgumentException("Release not found"));

        // 2. Verify that the release has an approved date
        if (release.getApprovedReleaseDate() == null) {
            throw new IllegalStateException("Release has no approved date");
        }

        // 3. Verify that the approved date has been reached
        if (release.getApprovedReleaseDate().isAfter(command.getCurrentDate())) {
            throw new IllegalStateException("Approved release date has not been reached yet");
        }

        // 4. Publish the release
        release.publish(command.getCurrentDate());

        // 5. Save the updated release
        releaseRepository.save(release);

        // 6. Publish the ReleasePublished event
        ReleasePublished event = ReleasePublished.fromRelease(release);
        eventStore.store(event);
    }
}