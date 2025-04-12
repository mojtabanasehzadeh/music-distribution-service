package com.ice.musicdistribution.command.handlers;

import com.ice.musicdistribution.command.commands.RequestPaymentReport;
import com.ice.musicdistribution.domain.aggregates.Artist;
import com.ice.musicdistribution.domain.events.PaymentReportRequested;
import com.ice.musicdistribution.domain.repositories.ArtistRepository;
import com.ice.musicdistribution.eventstore.EventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handler for the RequestPaymentReport command
 */
@Component
@RequiredArgsConstructor
public class PaymentReportHandler {
    private final ArtistRepository artistRepository;
    private final EventStore eventStore;

    /**
     * Handle the RequestPaymentReport command
     */
    @Transactional
    public void handle(RequestPaymentReport command) {
        // 1. Verify that the artist exists
        Artist artist = artistRepository.findById(command.getArtistId())
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

        // 2. Publish the PaymentReportRequested event
        // This will trigger the query side to generate a report
        PaymentReportRequested event = new PaymentReportRequested(
                command.getRequestId(),
                artist.getId(),
                artist.getName(),
                command.getFromDate(),
                command.getToDate()
        );
        eventStore.store(event);

        // Note: In a more complex system, we might save a record of the request,
        // but for this simplified implementation, we just publish the event
    }
}