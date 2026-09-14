package com.vahan.assignment.transport;

import com.vahan.assignment.domain.Message;

import java.io.IOException;

/**
 * Communication boundary used by a player.
 *
 * <p>Implementations decide whether messages cross an in-memory queue or a TCP
 * connection. Keeping this detail outside the player lets both deployment modes
 * share exactly the same conversation behavior.</p>
 */
public interface MessageTransport extends AutoCloseable {

    /**
     * Sends one complete message to the peer.
     *
     * @param message message to send
     * @throws IOException when delivery fails
     */
    void send(Message message) throws IOException;

    /**
     * Waits until the next complete peer message is available.
     *
     * @return the received message
     * @throws IOException when receiving fails
     * @throws InterruptedException when an in-memory wait is interrupted
     */
    Message receive() throws IOException, InterruptedException;

    /**
     * Releases transport resources. Implementations may safely be closed more
     * than once.
     *
     * @throws IOException when resource cleanup fails
     */
    @Override
    void close() throws IOException;
}
