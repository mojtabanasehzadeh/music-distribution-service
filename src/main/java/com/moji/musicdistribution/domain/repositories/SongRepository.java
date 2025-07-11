package com.moji.musicdistribution.domain.repositories;

import com.moji.musicdistribution.domain.aggregates.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Song entities
 */
@Repository
public interface SongRepository extends JpaRepository<Song, UUID> {
    /**
     * Find all songs by artist ID
     */
    List<Song> findByArtistId(UUID artistId);
}