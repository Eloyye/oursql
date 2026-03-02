package transport.server.client;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import transport.server.wireprotocol.SocketMessageChannel;
import transport.server.wireprotocol.WireMessage;
import transport.server.wireprotocol.WireMessageUtility;

import java.io.IOException;
import java.net.Socket;

@NullMarked
@Slf4j
public class TCPClient implements AutoCloseable {

  @Getter private final int port;
  @Getter private final String host;

  private @Nullable Socket clientSocket;

  public TCPClient(int port, String host) {
    this.port = port;
    this.host = host;
  }

  public void connect() throws IOException {
    try (var socket = new Socket(this.host, this.port);
        var channel = new SocketMessageChannel(socket)) {
      this.clientSocket = socket;
      log.info("connected to {}", socket.getInetAddress());
      var initialMessage = channel.receive();
      log.info("received: {}", initialMessage);
      var secondMessage = new WireMessage(WireMessageUtility.WireMessageType.TERMINATE, "");
      channel.send(secondMessage);
    }
  }

  @Override
  public void close() throws IOException {
    if (this.clientSocket != null) {
      this.clientSocket.close();
    }
  }

  static void main() {
    var client = new TCPClient(5432, "127.0.0.1");
    try {
      client.connect();
    } catch (IOException e) {
      log.error("failed to connect to server", e);
    }
  }
}
