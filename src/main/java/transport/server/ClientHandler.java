package transport.server;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import queryprocessor.ExecutionQuery;
import queryprocessor.QueryHandlerManager;
import transport.server.wireprotocol.MessageChannel;
import transport.server.wireprotocol.ProtocolException;
import transport.server.wireprotocol.SocketMessageChannel;
import transport.server.wireprotocol.WireMessage;
import transport.server.wireprotocol.WireMessageUtility;
import transport.server.wireprotocol.WireMessageUtility.WireMessageType;

import java.io.IOException;
import java.net.Socket;

@Slf4j
@NullMarked
public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

  @Override
  public void run() {
    try (MessageChannel channel = new SocketMessageChannel(this.clientSocket)) {
      log.info("client connected: {}", clientSocket.getInetAddress());
      while (true) {
        WireMessage initialReadyQueryMessage =
            new WireMessage(WireMessageType.READY_FOR_QUERY, "");
        channel.send(initialReadyQueryMessage);
        //                ensure at some point a client message terminates a session.
        WireMessage message = channel.receive();
        if (message.messageType() == WireMessageType.TERMINATE) {
          log.info("client disconnected: {}", clientSocket.getInetAddress());
          break;
        }

        if (message.messageType() != WireMessageType.QUERY) {
          throw new IllegalArgumentException("Invalid message type: " + message.messageType());
        }

        ExecutionQuery executionQuery = QueryHandlerManager.executeQuery(message.messagePayload());
        channel.send(
            new WireMessage(
                WireMessageType.ROW_DESCRIPTION,
                executionQuery.getColumnsMetadata()));
        executionQuery
            .executeRows()
            .forEachOrdered(
                dataRow -> {
                  var newMessage =
                      new WireMessage(
                          WireMessageType.DATA_ROW, dataRow.toString());
                  try {
                    channel.send(newMessage);
                  } catch (IOException e) {
                    throw new ProtocolException("Failed to send data row", e);
                  }
                });
        channel.send(
            new WireMessage(
                WireMessageUtility.WireMessageType.COMMAND_COMPLETE,
                executionQuery.getCommandCompleteMessage()));
      }
    } catch (IOException e) {
      log.error("Error handling client connection", e);
    }
  }
}
