package com.ice.musicdistribution.api.query;

import com.ice.musicdistribution.domain.aggregates.Song;
import com.ice.musicdistribution.query.projections.ArtistStreamProjection;
import com.ice.musicdistribution.query.projections.PaymentReportProjection;
import com.ice.musicdistribution.query.readmodels.ArtistStreamReport;
import com.ice.musicdistribution.query.readmodels.MonetizationReport;
import com.ice.musicdistribution.query.readmodels.PaymentReport;
import com.ice.musicdistribution.query.services.LevenshteinSearchService;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for query operations (read side of CQRS)
 */
@RestController
@RequestMapping("/queries")
@RequiredArgsConstructor
public class QueryController {

    private final LevenshteinSearchService searchService;
    private final ArtistStreamProjection artistStreamProjection;
    private final PaymentReportProjection paymentReportProjection;

    /**
     * Search for songs by title using Levenshtein distance
     */
    @GetMapping("/songs/search")
    public ResponseEntity<List<SongDTO>> searchSongsByTitle(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "2") int maxDistance) {

        List<Song> songs = searchService.searchSongsByTitle(searchTerm, maxDistance);

        List<SongDTO> songDTOs = songs.stream()
                .map(song -> new SongDTO(
                        song.getId(),
                        song.getTitle(),
                        song.getArtistId(),
                        song.getDuration().getSeconds()
                ))
                .toList();

        return ResponseEntity.ok(songDTOs);
    }

    /**
     * Get stream report for an artist
     */
    @GetMapping("/artists/{artistId}/stream-report")
    public ResponseEntity<ArtistStreamReport> getArtistStreamReport(
            @PathVariable UUID artistId,
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate) {

        ArtistStreamReport report = artistStreamProjection.generateStreamReport(
                artistId,
                fromDate,
                toDate
        );

        return ResponseEntity.ok(report);
    }

    /**
     * Get payment report for an artist
     */
    @GetMapping("/artists/{artistId}/payment-report")
    public ResponseEntity<PaymentReport> getArtistPaymentReport(
            @PathVariable UUID artistId,
            @RequestParam Instant fromDate,
            @RequestParam Instant toDate) {

        PaymentReport report = paymentReportProjection.generatePaymentReport(
                artistId,
                fromDate,
                toDate
        );

        return ResponseEntity.ok(report);
    }

    /**
     * Get monetization report for an artist
     */
    @GetMapping("/artists/{artistId}/monetization-report")
    public ResponseEntity<MonetizationReport> getArtistMonetizationReport(
            @PathVariable UUID artistId,
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate) {

        MonetizationReport report = paymentReportProjection.generateMonetizationReport(
                artistId,
                fromDate,
                toDate
        );

        return ResponseEntity.ok(report);
    }

    // DTO classes

    @Value
    public static class SongDTO {
        // Fields are automatically private final with @Value
        UUID id;
        String title;
        UUID artistId;
        long durationSeconds;

    }
}