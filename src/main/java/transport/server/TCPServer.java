package transport.server;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ExecutorService;

@NullMarked
@Slf4j
public class TCPServer implements AutoCloseable {
    private final ExecutorService executorService;
    @Getter private final int port;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile @Nullable ServerSocket serverSocket;

    public TCPServer(final ExecutorService executorService, final int port) {
        this.executorService = executorService;
        this.port = port;
    }

    public TCPServer(final ExecutorService executorService) {
        this(executorService, 5432);
    }

    public void listen() throws IOException {
        running.set(true);
        try (var localServerSocket = new ServerSocket(this.port)) {
            this.serverSocket = localServerSocket;
            log.info("Listening on port {}", this.port);
            while (running.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    var clientSocket = localServerSocket.accept();
                    executorService.submit(new ClientHandler(clientSocket));
                } catch (SocketException socketException) {
                    if (running.get()) {
                        throw socketException;
                    }
                    break;
                }
            }
        } finally {
            running.set(false);
            serverSocket = null;
        }
    }

    @Override
    public void close() throws IOException {
        running.set(false);
        var localServerSocket = this.serverSocket;
        if (localServerSocket != null && !localServerSocket.isClosed()) {
            localServerSocket.close();
        }
    }
}
