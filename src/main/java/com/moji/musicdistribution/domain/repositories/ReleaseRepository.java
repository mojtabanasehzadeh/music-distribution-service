package com.moji.musicdistribution.domain.repositories;

import com.moji.musicdistribution.domain.aggregates.Release;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Release aggregates
 */
@Repository
public interface ReleaseRepository extends JpaRepository<Release, UUID> {
    /**
     * Find all releases by artist ID
     */
    List<Release> findByArtistId(UUID artistId);

    /**
     * Find all releases containing a specific song
     */
    @Query("SELECT r FROM Release r WHERE :songId MEMBER OF r.songIds")
    List<Release> findBySongId(@Param("songId") UUID songId);

    /**
     * Find all releases with an approved date that has been reached
     */
    @Query("SELECT r FROM Release r WHERE r.status = 'APPROVED' AND r.approvedReleaseDate <= :currentDate")
    List<Release> findReleasesReadyForPublishing(@Param("currentDate") LocalDate currentDate);
}