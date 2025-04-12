package com.ice.musicdistribution.query.projections;

import com.ice.musicdistribution.domain.aggregates.Artist;
import com.ice.musicdistribution.domain.aggregates.Song;
import com.ice.musicdistribution.domain.aggregates.Stream;
import com.ice.musicdistribution.domain.events.PaymentReportRequested;
import com.ice.musicdistribution.domain.events.StreamMonetized;
import com.ice.musicdistribution.domain.repositories.ArtistRepository;
import com.ice.musicdistribution.domain.repositories.SongRepository;
import com.ice.musicdistribution.domain.repositories.StreamRepository;
import com.ice.musicdistribution.query.readmodels.MonetizationReport;
import com.ice.musicdistribution.query.readmodels.PaymentReport;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Projection that builds and maintains payment reports for monetized streams
 * Fulfills: artist can file for payment for monetized streams
 */
@Component
@RequiredArgsConstructor
public class PaymentReportProjection {

    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final StreamRepository streamRepository;
    private final Clock clock;

    // Map to track the last payment date for each artist
    private final Map<UUID, Instant> lastPaymentDates = new HashMap<>();

    /**
     * Listen for PaymentReportRequested events
     */
    @EventListener
    public void on(PaymentReportRequested event) {
        // When a payment report is requested, we generate it and could store it
        // For this implementation, we'll just update the last payment date
        lastPaymentDates.put(event.getArtistId(), Instant.now(clock));
    }

    /**
     * Listen for StreamMonetized events to update statistics
     */
    @EventListener
    public void on(StreamMonetized event) {
        // In a real implementation, we would update a dedicated database table
        // For this simplified version, we rely on the repositories
    }

    /**
     * Generate a payment report for an artist
     */
    public PaymentReport generatePaymentReport(UUID artistId, Instant fromDate, Instant toDate) {
        // Get the artist
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

        // Get all songs by this artist
        List<Song> artistSongs = songRepository.findByArtistId(artistId);
        Map<UUID, String> songTitles = artistSongs.stream()
                .collect(Collectors.toMap(Song::getId, Song::getTitle));

        // Get all monetizable streams in the date range
        List<Stream> monetizableStreams = streamRepository
                .findMonetizableStreamsByArtistAndDateRange(artistId, fromDate, toDate);

        // Group streams by song
        Map<UUID, List<Stream>> streamsBySong = monetizableStreams.stream()
                .collect(Collectors.groupingBy(Stream::getSongId));

        // Calculate payment for each song
        List<PaymentReport.SongPayment> songPayments = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (UUID songId : streamsBySong.keySet()) {
            List<Stream> songStreams = streamsBySong.get(songId);
            int streamCount = songStreams.size();

            // Calculate payment amount (in a real system, this would use complex business rules)
            BigDecimal songAmount = calculatePaymentAmount(songStreams);
            totalAmount = totalAmount.add(songAmount);

            songPayments.add(new PaymentReport.SongPayment(
                    songId,
                    songTitles.getOrDefault(songId, "Unknown Song"),
                    streamCount,
                    songAmount
            ));
        }

        // Sort song payments by amount (highest to lowest)
        songPayments.sort(Comparator.comparing(PaymentReport.SongPayment::getAmount).reversed());

        // Create and return the report
        return new PaymentReport(
                artistId,
                artist.getName(),
                monetizableStreams.size(),
                totalAmount,
                fromDate,
                toDate,
                Instant.now(clock),
                UUID.randomUUID(),
                songPayments
        );
    }

    /**
     * Generate a monetization report for an artist
     */
    public MonetizationReport generateMonetizationReport(UUID artistId, Instant fromDate, Instant toDate) {
        // Get the artist
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Artist not found"));

        // Use default date range if not provided
        Instant effectiveFromDate = fromDate != null ? fromDate :
                lastPaymentDates.getOrDefault(artistId, Instant.EPOCH);
        Instant effectiveToDate = toDate != null ? toDate : Instant.now(clock);

        // Get all streams in the date range
        List<Stream> allStreams = streamRepository.findByArtistId(artistId).stream()
                .filter(stream -> !stream.getStreamDate().isBefore(effectiveFromDate) &&
                        !stream.getStreamDate().isAfter(effectiveToDate))
                .collect(Collectors.toList());

        // Count monetizable streams
        List<Stream> monetizableStreams = allStreams.stream()
                .filter(Stream::isMonetizable)
                .collect(Collectors.toList());

        // Calculate estimated revenue
        BigDecimal estimatedRevenue = calculatePaymentAmount(monetizableStreams);

        // Create and return the report
        return new MonetizationReport(
                artistId,
                artist.getName(),
                allStreams.size(),
                monetizableStreams.size(),
                estimatedRevenue,
                lastPaymentDates.get(artistId),
                effectiveFromDate,
                effectiveToDate,
                Instant.now(clock)
        );
    }

    /**
     * Calculate payment amount for a list of streams
     * In a real system, this would implement complex business rules
     */
    private BigDecimal calculatePaymentAmount(List<Stream> streams) {
        // Simplified calculation: $0.004 per monetizable stream
        return new BigDecimal("0.004").multiply(new BigDecimal(streams.size()));
    }
}