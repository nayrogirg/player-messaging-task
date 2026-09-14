package com.vahan.assignment.domain;

import java.util.Objects;

/**
 * Immutable value exchanged by players.
 *
 * <p>A message is either application data, which participates in the ten-message
 * conversation, or a stop signal used only for graceful shutdown.</p>
 *
 * @param type message category
 * @param content application payload; empty for a stop signal
 */
public record Message(Type type, String content) {

    /**
     * Defines the two messages understood by the protocol.
     */
    public enum Type {
        DATA,
        STOP
    }

    public Message {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(content, "content");
        if (type == Type.STOP && !content.isEmpty()) {
            throw new IllegalArgumentException("A stop message must not have content");
        }
    }

    /**
     * Creates an application message.
     *
     * @param content payload to exchange
     * @return a data message
     */
    public static Message data(String content) {
        return new Message(Type.DATA, Objects.requireNonNull(content, "content"));
    }

    /**
     * Creates the control message that terminates the receiver.
     *
     * @return a stop message
     */
    public static Message stop() {
        return new Message(Type.STOP, "");
    }

    /**
     * Indicates whether this is the graceful-shutdown signal.
     *
     * @return {@code true} for a stop message
     */
    public boolean isStop() {
        return type == Type.STOP;
    }
}
