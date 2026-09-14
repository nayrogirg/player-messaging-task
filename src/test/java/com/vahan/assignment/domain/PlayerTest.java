package com.vahan.assignment.domain;

import com.vahan.assignment.transport.InMemoryTransport;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/**
 * Verifies the complete conversation, exact stop condition, and responder
 * shutdown without depending on sockets or external services.
 */
class PlayerTest {

    @Test
    void initiatorSendsAndReceivesExactlyTenMessages() {
        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            InMemoryTransport.Pair transports = InMemoryTransport.connectedPair();
            Player initiator = new Player("initiator", true, 10, transports.first());
            Player responder = new Player("responder", false, 10, transports.second());

            ExecutorService executor = Executors.newFixedThreadPool(2);
            try {
                Future<?> initiatorTask = executor.submit(initiator);
                Future<?> responderTask = executor.submit(responder);
                initiatorTask.get();
                responderTask.get();
            } finally {
                executor.shutdownNow();
            }

            assertEquals(10, initiator.sentMessages());
            assertEquals(10, initiator.receivedMessages());
            assertEquals(10, responder.sentMessages());
            assertEquals(10, responder.receivedMessages());
        });
    }
}
