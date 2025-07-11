package com.moji.musicdistribution.domain.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Artist entity - represents an artist who creates songs and releases
 */
@Entity
@Table(name = "artists")
@Getter
@NoArgsConstructor // Required by JPA
public class Artist {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "label_id", nullable = false)
    private UUID labelId; // Optional label ID (unlabeled artists are out of scope)

    /**
     * Create a new artist
     */
    public Artist(UUID id, String name, UUID labelId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Artist name cannot be empty");
        }
        if (labelId == null) {
            throw new IllegalArgumentException("Label ID is required (unlabeled artists are out of scope)");
        }

        this.id = id;
        this.name = name;
        this.labelId = labelId;
    }

    /**
     * Check if this artist belongs to the specified label
     */
    public boolean belongsToLabel(UUID labelId) {
        return this.labelId.equals(labelId);
    }
}