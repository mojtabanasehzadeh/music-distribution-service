package com.moji.musicdistribution.domain.events;

import lombok.Getter;

import java.util.UUID;

/**
 * Base implementation of DomainEvent that provides common fields for all events.
 */
@Getter
public abstract class BaseDomainEvent implements DomainEvent {

    private final UUID id;
    private final long timestamp;
    private final UUID aggregateId;

    protected BaseDomainEvent(UUID aggregateId) {
        this.id = UUID.randomUUID();
        this.timestamp = System.currentTimeMillis();
        this.aggregateId = aggregateId;
    }
}