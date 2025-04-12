package com.ice.musicdistribution.command.handlers;

import com.ice.musicdistribution.command.commands.RecordStream;
import com.ice.musicdistribution.domain.aggregates.Release;
import com.ice.musicdistribution.domain.aggregates.Song;
import com.ice.musicdistribution.domain.aggregates.Stream;
import com.ice.musicdistribution.domain.events.StreamMonetized;
import com.ice.musicdistribution.domain.events.StreamRecorded;
import com.ice.musicdistribution.domain.repositories.ReleaseRepository;
import com.ice.musicdistribution.domain.repositories.SongRepository;
import com.ice.musicdistribution.domain.repositories.StreamRepository;
import com.ice.musicdistribution.eventstore.EventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Handler for the RecordStream command
 */
@Component
@RequiredArgsConstructor
public class StreamHandler {
    private final SongRepository songRepository;
    private final ReleaseRepository releaseRepository;
    private final StreamRepository streamRepository;
    private final EventStore eventStore;

    /**
     * Handle the RecordStream command
     */
    @Transactional
    public void handle(RecordStream command) {
        // 1. Retrieve the song
        Song song = songRepository.findById(command.getSongId())
                .orElseThrow(() -> new IllegalArgumentException("Song not found"));

        // 2. Find releases containing the song
        List<Release> releases = releaseRepository.findBySongId(command.getSongId());

        // 3. Verify that the song is available for streaming (in a published release)
        boolean isAvailableForStreaming = releases.stream()
                .anyMatch(Release::isPublished);

        if (!isAvailableForStreaming) {
            throw new IllegalStateException("Song is not available for streaming");
        }

        // 4. Create and save the stream
        Stream stream = new Stream(
                UUID.randomUUID(),
                command.getSongId(),
                command.getUserId(),
                command.getStreamTimestamp(),
                command.getDuration()
        );
        streamRepository.save(stream);

        // 5. Publish the StreamRecorded event
        StreamRecorded streamRecordedEvent = StreamRecorded.fromStream(
                stream,
                song.getArtistId(),
                song.getTitle()
        );
        eventStore.store(streamRecordedEvent);

        // 6. If the stream is monetizable, publish the StreamMonetized event
        if (command.isMonetizable()) {
            // In a real system, the monetization amount would be calculated based on business rules
            BigDecimal monetizationAmount = calculateMonetizationAmount(stream.getDuration());

            StreamMonetized streamMonetizedEvent = StreamMonetized.fromStreamRecorded(
                    streamRecordedEvent,
                    monetizationAmount
            );
            eventStore.store(streamMonetizedEvent);
        }
    }

    /**
     * Calculate the monetization amount for a stream
     * In a real system, this would implement complex business rules
     */
    private BigDecimal calculateMonetizationAmount(java.time.Duration duration) {
        // Simplified calculation: $0.004 per minute or part thereof
        long minutes = (duration.getSeconds() + 59) / 60; // Round up
        return new BigDecimal("0.004").multiply(new BigDecimal(minutes));
    }
}