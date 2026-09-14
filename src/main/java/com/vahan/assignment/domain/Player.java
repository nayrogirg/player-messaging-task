package com.vahan.assignment.domain;

import com.vahan.assignment.transport.MessageTransport;

import java.io.IOException;
import java.util.Objects;

/**
 * Executes the player conversation protocol and owns its message counters.
 *
 * <p>The initiator starts the exchange. After receiving each data message, a
 * player sends a new message containing the received content followed by its
 * own next sent-message counter. After the initiator has sent and received the
 * configured number of messages, it sends a control stop message. Control
 * messages are not counted as application messages.</p>
 */
public final class Player implements Runnable {

    private final String name;
    private final boolean initiator;
    private final int messageLimit;
    private final MessageTransport transport;

    private int sentMessages;
    private int receivedMessages;

    /**
     * Creates a player.
     *
     * @param name human-readable name used in payloads and logs
     * @param initiator whether this player starts and terminates the conversation
     * @param messageLimit number of application messages the initiator must send and receive
     * @param transport communication endpoint owned by this player
     */
    public Player(String name, boolean initiator, int messageLimit, MessageTransport transport) {
        this.name = Objects.requireNonNull(name, "name");
        this.initiator = initiator;
        if (messageLimit < 1) {
            throw new IllegalArgumentException("messageLimit must be positive");
        }
        this.messageLimit = messageLimit;
        this.transport = Objects.requireNonNull(transport, "transport");
    }

    /**
     * Runs the complete blocking conversation and closes the transport on exit.
     *
     * @throws PlayerExecutionException when communication fails or the thread is interrupted
     */
    @Override
    public void run() {
        try (transport) {
            if (initiator) {
                sendApplicationMessage("hello");
            }

            while (true) {
                Message received = transport.receive();
                if (received.isStop()) {
                    log("received STOP");
                    return;
                }

                receivedMessages++;
                log("received #" + receivedMessages + ": " + received.content());

                if (initiator && receivedMessages == messageLimit) {
                    transport.send(Message.stop());
                    log("sent STOP; completed with sent=" + sentMessages
                            + ", received=" + receivedMessages);
                    return;
                }

                sendApplicationMessage(received.content());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new PlayerExecutionException(name + " was interrupted", exception);
        } catch (IOException exception) {
            throw new PlayerExecutionException(name + " communication failed", exception);
        }
    }

    private void sendApplicationMessage(String receivedContent) throws IOException {
        sentMessages++;
        String content = receivedContent + " | " + name + "-sent-" + sentMessages;
        transport.send(Message.data(content));
        log("sent #" + sentMessages + ": " + content);
    }

    private void log(String event) {
        System.out.printf("[%s, pid=%d] %s%n", name, ProcessHandle.current().pid(), event);
    }

    /**
     * Returns the number of application messages sent after execution.
     *
     * @return sent application-message count
     */
    public int sentMessages() {
        return sentMessages;
    }

    /**
     * Returns the number of application messages received after execution.
     *
     * @return received application-message count
     */
    public int receivedMessages() {
        return receivedMessages;
    }

    /**
     * Converts checked transport failures into a failure visible to an executor
     * or a process entry point.
     */
    public static final class PlayerExecutionException extends RuntimeException {
        public PlayerExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
