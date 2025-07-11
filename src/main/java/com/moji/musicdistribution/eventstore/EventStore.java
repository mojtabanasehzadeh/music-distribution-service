package com.moji.musicdistribution.eventstore;

import com.moji.musicdistribution.domain.events.DomainEvent;

import java.util.List;
import java.util.UUID;

/**
 * Interface for the Event Store in the CQRS architecture.
 * Responsible for storing and retrieving domain events.
 */
public interface EventStore {

    /**
     * Store a domain event
     *
     * @param event The event to store
     */
    void store(DomainEvent event);

    /**
     * Get all events for a specific aggregate
     *
     * @param aggregateId The ID of the aggregate
     * @return A list of events related to the aggregate
     */
    List<DomainEvent> getEventsForAggregate(UUID aggregateId);

    /**
     * Get all events of a specific type
     *
     * @param eventType The class of the event type
     * @param <T>       The type of domain event
     * @return A list of events of the specified type
     */
    <T extends DomainEvent> List<T> getEventsByType(Class<T> eventType);

    /**
     * Get all events
     *
     * @return All stored events
     */
    List<DomainEvent> getAllEvents();
}