# Music Distribution Service (CQRS/Event-Driven Example)

This project demonstrates a Music Distribution backend service built using Spring Boot, applying Command Query Responsibility Segregation (CQRS) and event-driven principles potentially derived from Event Storming. It manages entities like Labels, Artists, Songs, Releases, and Streams, focusing primarily on the release lifecycle and stream reporting.

## Architecture Overview

The application's design is heavily influenced by CQRS and event-driven patterns.

### CQRS (Command Query Responsibility Segregation)

The core idea is to separate operations that change state (Commands) from operations that read state (Queries).

*   **Commands:** Represent an intent to change the system's state (e.g., `CreateRelease`, `AddSongsToRelease`, `ApproveReleaseDate`, `PublishRelease`). They are dispatched via a `CommandBus` (though some handlers might be invoked directly in this implementation) and handled by dedicated `CommandHandlers`.
*   **Command Handlers:** Contain the logic to process a specific command. They typically load an **Aggregate Root** (like `Release`), execute business logic on it, and persist the changes (often by saving the aggregate and/or publishing events).
*   **Aggregates:** Encapsulate state and business rules (e.g., `Release`, `Song`, `Artist`). They validate commands and generate **Domain Events** upon successful state changes.
*   **Domain Events:** Represent significant occurrences in the past (e.g., `ReleaseCreated`, `SongsAddedToRelease`, `ReleaseDateApproved`, `ReleasePublished`, `StreamRecorded`). They are the primary output of the command side after state changes.
*   **Event Store/Bus:** Events generated by aggregates are persisted (using `EventStore` in this example) and/or published for projections to consume.
*   **Projections:** Listen to domain events (using Spring's `@EventListener` or similar) and build/update specialized **Read Models** optimized for specific query needs (e.g., `ArtistStreamProjection`, `SongSearchProjection`). They are decoupled from the command side.
*   **Read Models:** Denormalized data structures tailored for efficient querying (e.g., `ArtistStreamReport`, `SongReadModel`).
*   **Queries:** Represent requests for data. They directly query the Read Models via `QueryControllers` or the Projections themselves, bypassing the command side and aggregates.

This separation allows for:
*   Independent scaling of command and query workloads.
*   Optimized data models for both writing and reading.
*   Increased flexibility and resilience.

### Event Storming & Event-Driven Approach

The design likely originated from identifying key domain events through techniques like Event Storming. The application follows an event-driven approach where:

1.  Commands are processed by Aggregates via Handlers.
2.  Aggregates produce Domain Events upon state change.
3.  These Events are published/stored via the `EventStore`.
4.  Projections subscribe to these Events and update their respective Read Models.
5.  Queries read directly from these eventually consistent Read Models.

This makes the system reactive and facilitates decoupling between different parts of the application.

## System Diagrams

**Overall CQRS Flow**
![img_3.png](images/img_3.png)

**Component Diagram**

![img_4.png](images/img_4.png)

**Context Diagram**

![img_5.png](images/img_5.png)

**Flow Diagram**

![img_6.png](images/img_6.png)

**Simplified Calss Diagram**

![img_7.png](images/img_7.png)

## Scope and Setup


## Demonstration Test (`MusicDistributionDemoTest`)


## Prerequisites


## Database Setup
