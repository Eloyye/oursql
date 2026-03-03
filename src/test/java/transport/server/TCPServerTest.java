package transport.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

@NullMarked
@Slf4j
class TCPServerTest {

    @Test
    void listen_acceptsConnection_submitsClientHandler() throws Exception {
        ExecutorService executor = mock(ExecutorService.class);
        int port = findFreePort();
        TCPServer server = new TCPServer(executor, port);

        AtomicReference<@Nullable Throwable> listenFailure = new AtomicReference<>();
        Thread serverThread = startServerThread(server, listenFailure);

        try (Socket ignored = connectWithRetry(port)) {
            verify(executor, timeout(2_000).times(1)).submit(any(Runnable.class));
        } finally {
            server.close();
            serverThread.join(2_000);
        }

        assertFalse(serverThread.isAlive());
        assertNull(listenFailure.get());
    }

    @Test
    void listen_acceptsMultipleConnections_submitsPerClient() throws Exception {
        ExecutorService executor = mock(ExecutorService.class);
        int port = findFreePort();
        TCPServer server = new TCPServer(executor, port);

        AtomicReference<@Nullable Throwable> listenFailure = new AtomicReference<>();
        Thread serverThread = startServerThread(server, listenFailure);

        try (Socket ignored1 = connectWithRetry(port);
             Socket ignored2 = connectWithRetry(port);
             Socket ignored3 = connectWithRetry(port)) {
            verify(executor, timeout(2_000).times(3)).submit(any(Runnable.class));
        } finally {
            server.close();
            serverThread.join(2_000);
        }

        assertFalse(serverThread.isAlive());
        assertNull(listenFailure.get());
    }

    @Test
    void listen_whenPortInUse_throwsIOException() throws IOException {
        ExecutorService executor = mock(ExecutorService.class);

        try (ServerSocket occupiedSocket = new ServerSocket(0)) {
            TCPServer server = new TCPServer(executor, occupiedSocket.getLocalPort());

            assertThrows(IOException.class, server::listen);
        }
    }

    @Test
    void close_stopsListeningThreadGracefully() throws Exception {
        ExecutorService executor = mock(ExecutorService.class);
        int port = findFreePort();
        TCPServer server = new TCPServer(executor, port);

        AtomicReference<@Nullable Throwable> listenFailure = new AtomicReference<>();
        Thread serverThread = startServerThread(server, listenFailure);

        try (Socket ignored = connectWithRetry(port)) {
            verify(executor, timeout(2_000).times(1)).submit(any(Runnable.class));
        }

        server.close();
        serverThread.join(2_000);

        assertFalse(serverThread.isAlive());
        assertNull(listenFailure.get());
    }

    private static Thread startServerThread(TCPServer server, AtomicReference<Throwable> failureRef) {
        Thread thread = new Thread(() -> {
            try {
                server.listen();
            } catch (Throwable throwable) {
                failureRef.set(throwable);
            }
        });
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private static Socket connectWithRetry(int port) throws Exception {
        Exception lastException = null;
        for (int attempt = 0; attempt < 50; attempt++) {
            try {
                return new Socket("127.0.0.1", port);
            } catch (IOException connectException) {
                lastException = connectException;
                Thread.sleep(50);
            }
        }

        assertNotNull(lastException);
        throw lastException;
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
