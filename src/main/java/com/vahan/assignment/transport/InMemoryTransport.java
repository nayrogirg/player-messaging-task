package com.vahan.assignment.transport;

import com.vahan.assignment.domain.Message;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * In-process transport implemented with two thread-safe blocking queues.
 *
 * <p>Each endpoint writes to its peer's inbound queue and reads from its own.
 * The factory guarantees correct queue wiring and avoids exposing mutable queues
 * to application code.</p>
 */
public final class InMemoryTransport implements MessageTransport {

    private final BlockingQueue<Message> inbound;
    private final BlockingQueue<Message> outbound;

    private InMemoryTransport(BlockingQueue<Message> inbound,
                              BlockingQueue<Message> outbound) {
        this.inbound = inbound;
        this.outbound = outbound;
    }

    /**
     * Creates the two connected endpoints required by two players.
     *
     * @return a connected endpoint pair
     */
    public static Pair connectedPair() {
        BlockingQueue<Message> firstInbox = new LinkedBlockingQueue<>();
        BlockingQueue<Message> secondInbox = new LinkedBlockingQueue<>();
        return new Pair(
                new InMemoryTransport(firstInbox, secondInbox),
                new InMemoryTransport(secondInbox, firstInbox)
        );
    }

    @Override
    public void send(Message message) throws InterruptedIOException {
        try {
            outbound.put(Objects.requireNonNull(message, "message"));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Interrupted while sending", exception);
        }
    }

    @Override
    public Message receive() throws InterruptedException {
        return inbound.take();
    }

    /**
     * Has no external resources to release.
     */
    @Override
    public void close() {
        // Queues are garbage-collected after both players finish.
    }

    /**
     * Holds two connected transport endpoints.
     *
     * @param first first endpoint
     * @param second second endpoint
     */
    public record Pair(MessageTransport first, MessageTransport second) {
    }

    /**
     * Preserves the interruption cause while satisfying the transport's
     * IOException-based send contract.
     */
    public static final class InterruptedIOException extends java.io.IOException {
        private InterruptedIOException(String message, InterruptedException cause) {
            super(message, cause);
        }
    }
}
