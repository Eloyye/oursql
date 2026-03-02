package transport.server.wireprotocol;

import org.jspecify.annotations.NullMarked;

import java.io.Closeable;
import java.io.IOException;

/**
 * Abstraction for bidirectional message communication using the wire protocol.
 * Implementations can provide different transport mechanisms (socket, WebSocket, in-memory, etc.).
 */
@NullMarked
public interface MessageChannel extends Closeable {
    /**
     * Sends a wire message through this channel.
     *
     * @param message the message to send
     * @throws IOException if an I/O error occurs during sending
     */
    void send(WireMessage message) throws IOException;

    /**
     * Receives a wire message from this channel.
     * Blocks until a complete message is available.
     *
     * @return the received message
     * @throws IOException if an I/O error occurs during receiving
     */
    WireMessage receive() throws IOException;
}
