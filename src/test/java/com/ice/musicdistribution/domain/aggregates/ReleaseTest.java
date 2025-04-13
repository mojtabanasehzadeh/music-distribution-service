package com.ice.musicdistribution.domain.aggregates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ReleaseTest {

    private UUID releaseId;
    private UUID artistId;
    private Release release;
    private LocalDate today;
    private LocalDate tomorrow;

    @BeforeEach
    void setUp() {
        releaseId = UUID.randomUUID();
        artistId = UUID.randomUUID();
        release = new Release(releaseId, "Test Release", artistId);
        today = LocalDate.now();
        tomorrow = today.plusDays(1);
    }

    @Test
    void testCreateRelease() {
        assertEquals(releaseId, release.getId());
        assertEquals("Test Release", release.getTitle());
        assertEquals(artistId, release.getArtistId());
        assertEquals(Release.ReleaseStatus.DRAFT, release.getStatus());
        assertTrue(release.getSongIds().isEmpty());
        assertNull(release.getProposedReleaseDate());
        assertNull(release.getApprovedReleaseDate());
        assertNull(release.getPublishedDate());
    }

    @Test
    void testAddSongs() {
        UUID song1 = UUID.randomUUID();
        UUID song2 = UUID.randomUUID();
        Set<UUID> songs = new HashSet<>();
        songs.add(song1);
        songs.add(song2);

        release.addSongs(songs);

        assertEquals(2, release.getSongIds().size());
        assertTrue(release.getSongIds().contains(song1));
        assertTrue(release.getSongIds().contains(song2));
    }

    @Test
    void testAddSongsToWithdrawnRelease() {
        // First make the release withdrawn
        release.proposeReleaseDate(today);
        release.approveReleaseDate(today);
        release.publish(today);
        release.withdraw();

        // Then try to add songs
        Set<UUID> songs = new HashSet<>();
        songs.add(UUID.randomUUID());

        assertThrows(IllegalStateException.class, () -> {
            release.addSongs(songs);
        });
    }

    @Test
    void testProposeReleaseDate() {
        release.proposeReleaseDate(tomorrow);

        assertEquals(tomorrow, release.getProposedReleaseDate());
        assertEquals(Release.ReleaseStatus.PROPOSED, release.getStatus());
    }

    @Test
    void testProposeReleaseDateForWithdrawnRelease() {
        // First make the release withdrawn
        release.proposeReleaseDate(today);
        release.approveReleaseDate(today);
        release.publish(today);
        release.withdraw();

        // Then try to propose a date
        assertThrows(IllegalStateException.class, () -> {
            release.proposeReleaseDate(tomorrow);
        });
    }

    @Test
    void testApproveReleaseDate() {
        release.proposeReleaseDate(tomorrow);
        release.approveReleaseDate(tomorrow);

        assertEquals(tomorrow, release.getApprovedReleaseDate());
        assertEquals(Release.ReleaseStatus.APPROVED, release.getStatus());
    }

    @Test
    void testApproveDateWithoutProposal() {
        assertThrows(IllegalStateException.class, () -> {
            release.approveReleaseDate(tomorrow);
        });
    }

    @Test
    void testPublishRelease() {
        release.proposeReleaseDate(today);
        release.approveReleaseDate(today);
        release.publish(today);

        assertEquals(today, release.getPublishedDate());
        assertEquals(Release.ReleaseStatus.PUBLISHED, release.getStatus());
    }

    @Test
    void testPublishWithoutApproval() {
        assertThrows(IllegalStateException.class, () -> {
            release.publish(today);
        });
    }

    @Test
    void testPublishBeforeApprovedDate() {
        release.proposeReleaseDate(tomorrow);
        release.approveReleaseDate(tomorrow);

        assertThrows(IllegalStateException.class, () -> {
            release.publish(today);
        });
    }

    @Test
    void testWithdrawRelease() {
        release.proposeReleaseDate(today);
        release.approveReleaseDate(today);
        release.publish(today);
        release.withdraw();

        assertEquals(Release.ReleaseStatus.WITHDRAWN, release.getStatus());
    }

    @Test
    void testWithdrawUnpublishedRelease() {
        assertThrows(IllegalStateException.class, () -> {
            release.withdraw();
        });

        release.proposeReleaseDate(tomorrow);
        assertThrows(IllegalStateException.class, () -> {
            release.withdraw();
        });

        release.approveReleaseDate(tomorrow);
        assertThrows(IllegalStateException.class, () -> {
            release.withdraw();
        });
    }

    @Test
    void testIsPublished() {
        assertFalse(release.isPublished());

        release.proposeReleaseDate(today);
        release.approveReleaseDate(today);
        release.publish(today);

        assertTrue(release.isPublished());
    }

    @Test
    void testIsWithdrawn() {
        assertFalse(release.isWithdrawn());

        release.proposeReleaseDate(today);
        release.approveReleaseDate(today);
        release.publish(today);
        release.withdraw();

        assertTrue(release.isWithdrawn());
    }
}