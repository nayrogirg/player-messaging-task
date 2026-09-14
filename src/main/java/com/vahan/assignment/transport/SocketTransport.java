package com.vahan.assignment.transport;

import com.vahan.assignment.domain.Message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

/**
 * Cross-process transport that frames messages on an established TCP socket.
 *
 * <p>The protocol writes the message type followed by a UTF-8-compatible
 * length-prefixed string. Each send is flushed immediately because the
 * conversation is request/response oriented.</p>
 */
public final class SocketTransport implements MessageTransport {

    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream output;

    /**
     * Wraps an already connected socket.
     *
     * @param socket connected TCP socket owned by this transport
     * @throws IOException when streams cannot be opened
     */
    public SocketTransport(Socket socket) throws IOException {
        this.socket = Objects.requireNonNull(socket, "socket");
        this.input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    @Override
    public synchronized void send(Message message) throws IOException {
        output.writeUTF(message.type().name());
        output.writeUTF(message.content());
        output.flush();
    }

    @Override
    public Message receive() throws IOException {
        Message.Type type = Message.Type.valueOf(input.readUTF());
        String content = input.readUTF();
        return new Message(type, content);
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
