package transport.server;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;

@NullMarked
@Slf4j
public class TCPServer {
    private final ExecutorService executorService;
    @Getter private final int port;

    public TCPServer(final ExecutorService executorService, final int port) {
        this.executorService = executorService;
        this.port = port;
    }

    public TCPServer(final ExecutorService executorService) {
        this(executorService, 5432);
    }

    public void listen() throws IOException {
        try (var serverSocket = new ServerSocket(this.port)) {
            log.info("Listening on port {}", this.port);
            while (true) {
                while (!Thread.currentThread().isInterrupted()) {
                    var clientSocket = serverSocket.accept();
                    executorService.submit(new ClientHandler(clientSocket));
                }
            }
        }
    }
}
