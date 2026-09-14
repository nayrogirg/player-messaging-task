package com.vahan.assignment.app;

import com.vahan.assignment.domain.Player;
import com.vahan.assignment.transport.InMemoryTransport;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Composition root for the same-JVM requirement.
 *
 * <p>It creates two players, gives them connected in-memory endpoints, runs
 * them concurrently, waits for both to finish, and shuts the executor down
 * gracefully.</p>
 */
public final class SameProcessMain {

    private static final int MESSAGE_LIMIT = 10;
    private static final Duration COMPLETION_TIMEOUT = Duration.ofSeconds(10);

    private SameProcessMain() {
    }

    /**
     * Runs the two-player conversation in a single Java process.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        InMemoryTransport.Pair transports = InMemoryTransport.connectedPair();
        Player initiator = new Player("initiator", true, MESSAGE_LIMIT, transports.first());
        Player responder = new Player("responder", false, MESSAGE_LIMIT, transports.second());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<?>> tasks = List.of(
                    executor.submit(initiator),
                    executor.submit(responder)
            );
            for (Future<?> task : tasks) {
                task.get(COMPLETION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Main thread was interrupted", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new IllegalStateException("Conversation did not complete successfully", exception);
        } finally {
            executor.shutdownNow();
        }

        if (initiator.sentMessages() != MESSAGE_LIMIT
                || initiator.receivedMessages() != MESSAGE_LIMIT) {
            throw new IllegalStateException("Initiator did not meet the stop condition");
        }
    }
}
