package com.moji.musicdistribution.domain.repositories;

import com.moji.musicdistribution.domain.aggregates.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Stream entities
 */
@Repository
public interface StreamRepository extends JpaRepository<Stream, UUID> {
    /**
     * Find all streams for a specific song
     */
    List<Stream> findBySongId(UUID songId);

    /**
     * Find all streams for songs by a specific artist
     */
    @Query("SELECT s FROM Stream s JOIN Song song ON s.songId = song.id WHERE song.artistId = :artistId")
    List<Stream> findByArtistId(@Param("artistId") UUID artistId);

    /**
     * Find all monetizable streams (longer than 30 seconds) for a specific artist within a date range
     */
    @Query("SELECT s FROM Stream s JOIN Song song ON s.songId = song.id " +
            "WHERE song.artistId = :artistId " +
            "AND s.monetized = true " +
            "AND s.timestamp BETWEEN :fromDate AND :toDate")
    List<Stream> findMonetizableStreamsByArtistAndDateRange(
            @Param("artistId") UUID artistId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate);
}