package com.ice.musicdistribution.command.commands;

import lombok.Getter;

import java.util.Set;
import java.util.UUID;

/**
 * Command to add songs to a release
 * artist adds songs to a release
 */
@Getter
public class AddSongsToRelease {
    private final UUID releaseId;
    private final Set<UUID> songIds;
    private final UUID artistId;

    /**
     * Create a new AddSongsToRelease command
     *
     * @param releaseId The ID of the release to add songs to
     * @param songIds   The IDs of the songs to add
     * @param artistId  The ID of the artist who owns the release
     */
    public AddSongsToRelease(UUID releaseId, Set<UUID> songIds, UUID artistId) {
        if (releaseId == null) {
            throw new IllegalArgumentException("Release ID cannot be null");
        }
        if (songIds == null || songIds.isEmpty()) {
            throw new IllegalArgumentException("Song IDs cannot be null or empty");
        }
        if (artistId == null) {
            throw new IllegalArgumentException("Artist ID cannot be null");
        }

        this.releaseId = releaseId;
        this.songIds = Set.copyOf(songIds); // Immutable copy
        this.artistId = artistId;
    }
}