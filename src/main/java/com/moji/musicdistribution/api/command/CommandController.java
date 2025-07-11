package com.moji.musicdistribution.api.command;

import com.moji.musicdistribution.command.CommandBus;
import com.moji.musicdistribution.command.commands.*;
import com.moji.musicdistribution.command.handlers.*;
import com.moji.musicdistribution.domain.aggregates.Release;
import lombok.RequiredArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * REST controller for command operations (write side of CQRS)
 */
@RestController
@RequestMapping("/commands")
@RequiredArgsConstructor
public class CommandController {

    private final CommandBus commandBus; // Add this field
    private final AddSongsHandler addSongsHandler;
    private final ProposeDateHandler proposeDateHandler;
    private final ApproveDateHandler approveDateHandler;
    private final PublishHandler publishHandler;
    private final StreamHandler streamHandler;
    private final PaymentReportHandler paymentReportHandler;
    private final WithdrawHandler withdrawHandler;
    private final Clock clock;


    /**
     * Create a new release
     */
    @PostMapping("/releases")
    public ResponseEntity<ReleaseDTO> createRelease(@RequestBody CreateReleaseRequest request) {
        UUID releaseId = request.getId() != null ? request.getId() : UUID.randomUUID();

        // Create the command
        CreateRelease command = new CreateRelease(
                releaseId,
                request.getTitle(),
                request.getArtistId()
        );

        // Execute the command using the CommandBus and get the result
        Release release = commandBus.executeForResult(command);

        // Return the DTO
        return ResponseEntity.ok(new ReleaseDTO(
                release.getId(),
                release.getTitle(),
                release.getArtistId(),
                release.getStatus().toString()
        ));
    }

    /**
     * Add songs to a release
     */
    @PutMapping("/releases/{releaseId}/songs")
    public ResponseEntity<Void> addSongsToRelease(
            @PathVariable UUID releaseId,
            @RequestBody AddSongsRequest request) {

        AddSongsToRelease command = new AddSongsToRelease(
                releaseId,
                request.getSongIds(),
                request.getArtistId()
        );

        addSongsHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    /**
     * Propose a release date
     */
    @PutMapping("/releases/{releaseId}/propose-date")
    public ResponseEntity<Void> proposeReleaseDate(
            @PathVariable UUID releaseId,
            @RequestBody ProposeDateRequest request) {

        ProposeReleaseDate command = new ProposeReleaseDate(
                releaseId,
                request.getArtistId(),
                request.getProposedDate()
        );

        proposeDateHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    /**
     * Approve a release date
     */
    @PutMapping("/releases/{releaseId}/approve-date")
    public ResponseEntity<Void> approveReleaseDate(
            @PathVariable UUID releaseId,
            @RequestBody ApproveDateRequest request) {

        ApproveReleaseDate command = new ApproveReleaseDate(
                releaseId,
                request.getLabelId(),
                request.getApprovedDate()
        );

        approveDateHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    /**
     * Publish a release
     */
    @PostMapping("/releases/{releaseId}/publish")
    public ResponseEntity<Void> publishRelease(@PathVariable UUID releaseId) {
        LocalDate currentDate = LocalDate.now(clock);

        PublishRelease command = new PublishRelease(releaseId, currentDate);

        publishHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    /**
     * Record a stream of a song
     */
    @PostMapping("/streams")
    public ResponseEntity<Void> recordStream(@RequestBody RecordStreamRequest request) {
        RecordStream command = new RecordStream(
                request.getSongId(),
                request.getUserId(),
                request.getDuration(),
                request.getTimestamp()
        );

        streamHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    /**
     * Request a payment report
     */
    @PostMapping("/artists/{artistId}/payment-reports")
    public ResponseEntity<Void> requestPaymentReport(
            @PathVariable UUID artistId,
            @RequestBody PaymentReportRequest request) {

        RequestPaymentReport command = new RequestPaymentReport(
                artistId,
                request.getFromDate(),
                request.getToDate(),
                UUID.randomUUID()
        );

        paymentReportHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    /**
     * Withdraw a release
     */
    @DeleteMapping("/releases/{releaseId}")
    public ResponseEntity<Void> withdrawRelease(
            @PathVariable UUID releaseId,
            @RequestParam UUID artistId) {

        WithdrawRelease command = new WithdrawRelease(releaseId, artistId);

        withdrawHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    // DTO classes for requests and responses

    @Getter
    @Setter
    public static class CreateReleaseRequest {
        private UUID id;
        private String title;
        private UUID artistId;
    }

    @Data
    public static class ReleaseDTO {
        private final UUID id;
        private final String title;
        private final UUID artistId;
        private final String status;
    }

    @Getter
    @Setter
    public static class AddSongsRequest {
        private UUID artistId;
        private Set<UUID> songIds;
    }

    @Getter
    @Setter
    public static class ProposeDateRequest {
        private UUID artistId;
        private LocalDate proposedDate;
    }

    @Getter
    @Setter
    public static class ApproveDateRequest {
        private UUID labelId;
        private LocalDate approvedDate;
    }

    @Getter
    @Setter
    public static class RecordStreamRequest {
        private UUID songId;
        private UUID userId;
        private java.time.Duration duration;
        private java.time.Instant timestamp;
    }

    @Getter
    @Setter
    public static class PaymentReportRequest {
        private java.time.Instant fromDate;
        private java.time.Instant toDate;
    }
}