package com.ice.musicdistribution.jobs;

import com.ice.musicdistribution.command.CommandBus;
import com.ice.musicdistribution.command.commands.PublishRelease;
import com.ice.musicdistribution.domain.aggregates.Release;
import com.ice.musicdistribution.domain.repositories.ReleaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Scheduled job to publish releases when their approved date is reached
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReleasePublisherJob {

    private final ReleaseRepository releaseRepository;
    private final CommandBus commandBus;
    private final Clock clock;

    /**
     * Check for releases to publish every day at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void publishReleasesWithReachedDate() {
        LocalDate today = LocalDate.now(clock);
        log.info("Checking for releases to publish on {}", today);

        // Find all releases that are ready for publishing
        List<Release> releasesToPublish = releaseRepository.findReleasesReadyForPublishing(today);
        log.info("Found {} releases ready for publishing", releasesToPublish.size());

        // Publish each release
        for (Release release : releasesToPublish) {
            try {
                PublishRelease command = new PublishRelease(release.getId(), today);
                commandBus.execute(command);
                log.info("Published release: {} ({})", release.getTitle(), release.getId());
            } catch (Exception e) {
                log.error("Failed to publish release: {} ({})", release.getTitle(), release.getId(), e);
            }
        }
    }
}