package com.ice.musicdistribution.command.handlers;

import com.ice.musicdistribution.command.commands.WithdrawRelease;
import com.ice.musicdistribution.domain.aggregates.Release;
import com.ice.musicdistribution.domain.events.ReleaseWithdrawn;
import com.ice.musicdistribution.domain.repositories.ReleaseRepository;
import com.ice.musicdistribution.eventstore.EventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for the WithdrawRelease command
 */
@Component
@RequiredArgsConstructor
public class WithdrawHandler {
    private final ReleaseRepository releaseRepository;
    private final EventStore eventStore;

    /**
     * Handle the WithdrawRelease command
     */
    @Transactional
    public void handle(WithdrawRelease command) {
        // 1. Retrieve the release
        Release release = releaseRepository.findById(command.getReleaseId())
                .orElseThrow(() -> new IllegalArgumentException("Release not found"));

        // 2. Verify that the artist owns the release
        if (!release.getArtistId().equals(command.getArtistId())) {
            throw new IllegalStateException("Artist does not own this release");
        }

        // 3. Verify that the release is published (only published releases can be withdrawn)
        if (!release.isPublished()) {
            throw new IllegalStateException("Only published releases can be withdrawn");
        }

        // 4. Withdraw the release
        release.withdraw();

        // 5. Save the updated release
        releaseRepository.save(release);

        // 6. Publish the ReleaseWithdrawn event
        ReleaseWithdrawn event = ReleaseWithdrawn.fromRelease(release);
        eventStore.store(event);
    }
}