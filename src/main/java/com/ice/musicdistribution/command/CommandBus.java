package com.ice.musicdistribution.command;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A simple command bus that routes commands to their handlers
 * Supports both void handlers and handlers that return results
 */
//@Component
public class CommandBus {

    private final Map<Class<?>, Object> handlers = new HashMap<>();

    /**
     * Register a handler for a specific command type (void return)
     */
    public <T> void register(Class<T> commandType, Consumer<T> handler) {
        handlers.put(commandType, handler);
    }

    /**
     * Register a handler for a specific command type (with result)
     */
    public <T, R> void register(Class<T> commandType, Function<T, R> handler) {
        handlers.put(commandType, handler);
    }

    /**
     * Execute a command by routing it to its registered handler
     * This overload is used when no result is needed
     */
    public <T> void execute(T command) {
        Object handler = handlers.get(command.getClass());

        if (handler == null) {
            throw new IllegalArgumentException("No handler registered for command: " + command.getClass().getName());
        }

        if (handler instanceof Consumer) {
            @SuppressWarnings("unchecked")
            Consumer<T> consumer = (Consumer<T>) handler;
            consumer.accept(command);
        } else if (handler instanceof Function) {
            @SuppressWarnings("unchecked")
            Function<T, ?> function = (Function<T, ?>) handler;
            function.apply(command);
        } else {
            throw new IllegalStateException("Unsupported handler type: " + handler.getClass().getName());
        }
    }

    /**
     * Execute a command by routing it to its registered handler and return the result
     * This overload is used when a result is needed
     */
    @SuppressWarnings("unchecked")
    public <T, R> R executeForResult(T command) {
        Object handler = handlers.get(command.getClass());

        if (handler == null) {
            throw new IllegalArgumentException("No handler registered for command: " + command.getClass().getName());
        }

        if (handler instanceof Function) {
            Function<T, R> function = (Function<T, R>) handler;
            return function.apply(command);
        } else {
            throw new IllegalStateException("Handler does not return a result: " + handler.getClass().getName());
        }
    }
}