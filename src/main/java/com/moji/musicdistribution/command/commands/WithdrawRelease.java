package com.moji.musicdistribution.command.commands;

import lombok.Getter;

import java.util.UUID;

/**
 * Command to withdraw a release from distribution
 * Corresponds to: artist can take out release from distribution meaning songs cannot be streamed
 */
@Getter
public class WithdrawRelease {
    private final UUID releaseId;
    private final UUID artistId;

    /**
     * Create a new WithdrawRelease command
     *
     * @param releaseId The ID of the release to withdraw
     * @param artistId  The ID of the artist who owns the release
     */
    public WithdrawRelease(UUID releaseId, UUID artistId) {
        if (releaseId == null) {
            throw new IllegalArgumentException("Release ID cannot be null");
        }
        if (artistId == null) {
            throw new IllegalArgumentException("Artist ID cannot be null");
        }

        this.releaseId = releaseId;
        this.artistId = artistId;
    }
}