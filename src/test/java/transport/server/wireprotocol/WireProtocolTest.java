package transport.server.wireprotocol;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class WireProtocolTest {

  @Test
  void encode_properMessage_returnsValidBytes() {
    var message = new WireMessage(WireMessageUtility.WireMessageType.TERMINATE, "");
    var encodedMessage = WireProtocol.encode(message);
    assertEquals(5, encodedMessage.length);
  }

  @Test
  void decode_encodeOriginalValidMessage_returnsOriginalMessage() {
    var originalMessage = new WireMessage(WireMessageUtility.WireMessageType.TERMINATE, "");
    var encodedMessage = WireProtocol.encode(originalMessage);
    try {
      var decodedMessage = WireProtocol.decode(new ByteArrayInputStream(encodedMessage));
      assertEquals(originalMessage, decodedMessage);
    } catch (IOException e) {
      fail("decode failed: %s\n".formatted(e.getMessage()));
    }
  }

  @Test
  void decode_emptyMessage_throwsIOException() {
    assertThrows(
        IOException.class, () -> WireProtocol.decode(new ByteArrayInputStream(new byte[0])));
  }

  @Test
  void decode_incorrectMessageLengthLessThanFour_throwsIOException() {
    var buffer = ByteBuffer.allocate(3);
    buffer.put(WireMessageUtility.typeToBytes(WireMessageUtility.WireMessageType.TERMINATE));
    assertThrows(
        IOException.class, () -> WireProtocol.decode(new ByteArrayInputStream(buffer.array())));
  }

  @Test
  void decode_incorrectMessageLengthGreaterThanFour_throwsIOException() {
    var buffer = ByteBuffer.allocate(9);
    buffer.put(WireMessageUtility.typeToBytes(WireMessageUtility.WireMessageType.TERMINATE));
    buffer.putInt(5);
    buffer.putInt(1);
    assertThrows(
        IOException.class, () -> WireProtocol.decode(new ByteArrayInputStream(buffer.array())));
  }
}
