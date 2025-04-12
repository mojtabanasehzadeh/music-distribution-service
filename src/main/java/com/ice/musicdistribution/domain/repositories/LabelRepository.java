package com.ice.musicdistribution.domain.repositories;

import com.ice.musicdistribution.domain.aggregates.LabelRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for LabelRecord entities
 */
@Repository
public interface LabelRepository extends JpaRepository<LabelRecord, UUID> {
    /**
     * Find a label by name
     */
    LabelRecord findByName(String name);
}