package transport.server;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import queryprocessor.ExecutionQuery;
import queryprocessor.QueryHandlerManager;
import transport.server.wireprotocol.WireMessage;
import transport.server.wireprotocol.WireMessageUtility;
import transport.server.wireprotocol.WireProtocolManager;

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
    try (this.clientSocket) {
      var wireProtocolManager = new WireProtocolManager(this.clientSocket);
      log.info("client connected: {}", clientSocket.getInetAddress());
      while (true) {
        WireMessage initialReadyQueryMessage =
            new WireMessage(WireMessageUtility.WireMessageType.READY_FOR_QUERY, "");
        wireProtocolManager.send(initialReadyQueryMessage);
        //                ensure at some point a client message terminates a session.
        WireMessage message = wireProtocolManager.receive();
        if (message.messageType() == WireMessageUtility.WireMessageType.TERMINATE) {
          break;
        }

        if (message.messageType() != WireMessageUtility.WireMessageType.QUERY) {
          throw new IllegalArgumentException("Invalid message type: " + message.messageType());
        }

        ExecutionQuery executionQuery = QueryHandlerManager.executeQuery(message.messagePayload());
        wireProtocolManager.send(
            new WireMessage(
                WireMessageUtility.WireMessageType.ROW_DESCRIPTION,
                executionQuery.getColumnsMetadata()));
        executionQuery
            .executeRows()
            .forEachOrdered(
                dataRow -> {
                  var newMessage =
                      new WireMessage(WireMessageUtility.WireMessageType.DATA_ROW, dataRow.toString());
                  wireProtocolManager.send(newMessage);
                });
        wireProtocolManager.send(
            new WireMessage(
                WireMessageUtility.WireMessageType.COMMAND_COMPLETE,
                executionQuery.getCommandCompleteMessage()));
      }
    } catch (IOException e) {
      log.error("Error handling client connection", e);
    }
  }
}
