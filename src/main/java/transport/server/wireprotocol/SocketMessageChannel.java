package transport.server.wireprotocol;

import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Socket-based implementation of MessageChannel.
 * Manages wire protocol communication over a TCP socket.
 */
@NullMarked
public class SocketMessageChannel implements MessageChannel {
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public SocketMessageChannel(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    @Override
    public void send(WireMessage message) throws IOException {
        byte[] encodedMessage = WireProtocol.encode(message);
        outputStream.write(encodedMessage);
        outputStream.flush();
    }

    @Override
    public WireMessage receive() throws IOException {
        return WireProtocol.decode(inputStream);
    }

    @Override
    public void close() throws IOException {
        // Closing the socket also closes its input/output streams
        socket.close();
    }
}
