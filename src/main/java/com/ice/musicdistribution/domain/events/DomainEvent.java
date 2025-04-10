package com.ice.musicdistribution.domain.events;

import java.util.UUID;

/**
 * Base interface for all domain events in the system.
 */
public interface DomainEvent {

    /**
     * Get the unique identifier of this event
     * @return The event ID
     */
    UUID getId();

    /**
     * Get the timestamp when this event occurred
     * @return The timestamp in milliseconds since epoch
     */
    long getTimestamp();

    /**
     * Get the ID of the aggregate that this event relates to
     * @return The aggregate ID
     */
    UUID getAggregateId();
}