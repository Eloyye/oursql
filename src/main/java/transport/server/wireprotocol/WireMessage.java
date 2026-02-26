package transport.server.wireprotocol;

public record WireMessage(WireMessageUtility.WireMessageType messageType, String messagePayload) {
}
