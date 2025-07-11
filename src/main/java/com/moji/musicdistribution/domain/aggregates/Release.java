package com.moji.musicdistribution.domain.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Release aggregate root - represents a music release with its songs
 */
@Entity
@Table(name = "releases")
@Getter
@NoArgsConstructor // Required by JPA
public class Release {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "artist_id", nullable = false)
    private UUID artistId;

    @ElementCollection
    @CollectionTable(name = "release_songs", joinColumns = @JoinColumn(name = "release_id"))
    @Column(name = "song_id")
    private Set<UUID> songIds = new HashSet<>();

    @Column(name = "proposed_release_date")
    private LocalDate proposedReleaseDate;

    @Column(name = "approved_release_date")
    private LocalDate approvedReleaseDate;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReleaseStatus status;

    /**
     * Create a new release
     */
    public Release(UUID id, String title, UUID artistId) {
        this.id = id;
        this.title = title;
        this.artistId = artistId;
        this.songIds = new HashSet<>();
        this.status = ReleaseStatus.DRAFT;
    }

    /**
     * Add songs to the release
     */
    public void addSongs(Set<UUID> newSongIds) {
        if (status == ReleaseStatus.WITHDRAWN) {
            throw new IllegalStateException("Cannot add songs to a withdrawn release");
        }
        this.songIds.addAll(newSongIds);
    }

    /**
     * Propose a release date
     */
    public void proposeReleaseDate(LocalDate date) {
        if (status == ReleaseStatus.WITHDRAWN) {
            throw new IllegalStateException("Cannot propose release date for a withdrawn release");
        }
        this.proposedReleaseDate = date;
        this.status = ReleaseStatus.PROPOSED;
    }

    /**
     * Approve a release date (by label)
     */
    public void approveReleaseDate(LocalDate date) {
        if (status != ReleaseStatus.PROPOSED) {
            throw new IllegalStateException("Cannot approve date for a release that hasn't been proposed");
        }
        this.approvedReleaseDate = date;
        this.status = ReleaseStatus.APPROVED;
    }

    /**
     * Publish the release once the approved date is reached
     */
    public void publish(LocalDate currentDate) {
        if (status != ReleaseStatus.APPROVED) {
            throw new IllegalStateException("Cannot publish a release that hasn't been approved");
        }
        if (approvedReleaseDate.isAfter(currentDate)) {
            throw new IllegalStateException("Cannot publish a release before its approved date");
        }
        this.publishedDate = currentDate;
        this.status = ReleaseStatus.PUBLISHED;
    }

    /**
     * Withdraw the release from distribution
     */
    public void withdraw() {
        if (status != ReleaseStatus.PUBLISHED) {
            throw new IllegalStateException("Only published releases can be withdrawn");
        }
        this.status = ReleaseStatus.WITHDRAWN;
    }

    /**
     * Check if this release is published
     */
    public boolean isPublished() {
        return status == ReleaseStatus.PUBLISHED;
    }

    /**
     * Check if this release is withdrawn
     */
    public boolean isWithdrawn() {
        return status == ReleaseStatus.WITHDRAWN;
    }

    /**
     * Release status enum
     */
    public enum ReleaseStatus {
        DRAFT,
        PROPOSED,
        APPROVED,
        PUBLISHED,
        WITHDRAWN
    }
}