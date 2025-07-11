package com.moji.musicdistribution.domain.repositories;

import com.moji.musicdistribution.domain.aggregates.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Artist entities
 */
@Repository
public interface ArtistRepository extends JpaRepository<Artist, UUID> {
    /**
     * Find all artists by label ID
     */
    List<Artist> findByLabelId(UUID labelId);

    /**
     * Find an artist by name
     */
    Artist findByName(String name);
}