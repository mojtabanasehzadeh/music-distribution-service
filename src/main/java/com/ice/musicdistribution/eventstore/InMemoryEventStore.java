package com.ice.musicdistribution.eventstore;

import com.ice.musicdistribution.domain.events.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the EventStore.
 * Stores events in memory and also publishes them to the Spring application event system.
 */
@Component
public class InMemoryEventStore implements EventStore {

    private final Map<UUID, List<DomainEvent>> eventsByAggregate = new ConcurrentHashMap<>();
    private final List<DomainEvent> allEvents = new CopyOnWriteArrayList<>();
    private final ApplicationEventPublisher eventPublisher;

    public InMemoryEventStore(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void store(DomainEvent event) {
        // Store by aggregate ID
        eventsByAggregate.computeIfAbsent(event.getAggregateId(), k -> new ArrayList<>())
                .add(event);

        // Store in all events list
        allEvents.add(event);

        // Publish the event to the Spring application context
        eventPublisher.publishEvent(event);
    }

    @Override
    public List<DomainEvent> getEventsForAggregate(UUID aggregateId) {
        return eventsByAggregate.getOrDefault(aggregateId, List.of());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> List<T> getEventsByType(Class<T> eventType) {
        return allEvents.stream()
                .filter(event -> eventType.isInstance(event))
                .map(event -> (T) event)
                .collect(Collectors.toList());
    }

    @Override
    public List<DomainEvent> getAllEvents() {
        return new ArrayList<>(allEvents);
    }
}