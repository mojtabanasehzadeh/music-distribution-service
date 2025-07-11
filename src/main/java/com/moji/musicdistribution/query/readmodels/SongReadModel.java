package com.moji.musicdistribution.query.readmodels;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.UUID;

/**
 * Read model for songs
 * Used for song search and other query operations
 */
@Getter
@RequiredArgsConstructor
public class SongReadModel {
    private final UUID id;
    private final String title;
    private final UUID artistId;
    private final String artistName;
    private final Duration duration;
    private final boolean searchable;
}