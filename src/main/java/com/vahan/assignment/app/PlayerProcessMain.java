package com.vahan.assignment.app;

import com.vahan.assignment.domain.Player;
import com.vahan.assignment.transport.SocketTransport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Process entry point for the separate-JVM/PID requirement.
 *
 * <p>The responder owns a loopback-only server socket. The initiator connects
 * as a client. After connection establishment, both roles use the same
 * transport-independent {@link Player} implementation as the same-process
 * mode.</p>
 */
public final class PlayerProcessMain {

    private static final String LOOPBACK = "127.0.0.1";
    private static final int MESSAGE_LIMIT = 10;
    private static final int CONNECT_TIMEOUT_MILLIS = 5_000;

    private PlayerProcessMain() {
    }

    /**
     * Starts one player process.
     *
     * @param args role ({@code initiator} or {@code responder}) and optional port
     * @throws IOException when socket setup fails
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException(
                    "Usage: PlayerProcessMain {initiator|responder} [port]");
        }

        String role = args[0];
        int port = args.length == 2 ? Integer.parseInt(args[1]) : 5050;

        switch (role) {
            case "initiator" -> runInitiator(port);
            case "responder" -> runResponder(port);
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        }
    }

    private static void runInitiator(int port) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(LOOPBACK, port), CONNECT_TIMEOUT_MILLIS);
        new Player("initiator", true, MESSAGE_LIMIT, new SocketTransport(socket)).run();
    }

    private static void runResponder(int port) throws IOException {
        InetAddress loopback = InetAddress.getByName(LOOPBACK);
        try (ServerSocket server = new ServerSocket(port, 1, loopback)) {
            System.out.printf("[responder, pid=%d] listening on %s:%d%n",
                    ProcessHandle.current().pid(), LOOPBACK, port);
            Socket socket = server.accept();
            new Player("responder", false, MESSAGE_LIMIT, new SocketTransport(socket)).run();
        }
    }
}
