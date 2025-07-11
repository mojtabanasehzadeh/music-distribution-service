package com.moji.musicdistribution.command.commands;

import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Command to approve a proposed release date
 * Corresponds to: proposed release date has to be agreed by record label
 */
@Getter
public class ApproveReleaseDate {
    private final UUID releaseId;
    private final UUID labelId;
    private final LocalDate approvedDate;

    /**
     * Create a new ApproveReleaseDate command
     *
     * @param releaseId    The ID of the release
     * @param labelId      The ID of the label approving the date
     * @param approvedDate The approved release date (may differ from proposed date)
     */
    public ApproveReleaseDate(UUID releaseId, UUID labelId, LocalDate approvedDate) {
        if (releaseId == null) {
            throw new IllegalArgumentException("Release ID cannot be null");
        }
        if (labelId == null) {
            throw new IllegalArgumentException("Label ID cannot be null");
        }
        if (approvedDate == null) {
            throw new IllegalArgumentException("Approved date cannot be null");
        }
        if (approvedDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Approved date cannot be in the past");
        }

        this.releaseId = releaseId;
        this.labelId = labelId;
        this.approvedDate = approvedDate;
    }
}