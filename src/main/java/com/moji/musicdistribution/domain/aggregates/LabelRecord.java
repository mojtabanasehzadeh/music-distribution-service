package com.moji.musicdistribution.domain.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * LabelRecord entity - represents a record label that artists can be signed to
 */
@Entity
@Table(name = "labels")
@Getter
@NoArgsConstructor // Required by JPA
public class LabelRecord {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Create a new record label
     */
    public LabelRecord(UUID id, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Label name cannot be empty");
        }

        this.id = id;
        this.name = name;
    }

    /**
     * Approve a release date for an artist signed to this label
     * In a real system, this would contain more complex approval logic
     */
    public boolean canApproveRelease(UUID artistId, Release release) {
        // Check if the release has a proposed date
        if (release.getProposedReleaseDate() == null) {
            return false;
        }

        // In a real system, we'd check if the artist belongs to this label
        // and perhaps apply other business rules
        return true;
    }
}