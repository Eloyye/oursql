package transport.server.wireprotocol;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@NullMarked
@Slf4j
public class WireProtocol {
    public static byte[] encode(WireMessage wireMessage) {
        var payloadBytes = wireMessage.messagePayload().getBytes(StandardCharsets.UTF_8);
        var byteBuffer = ByteBuffer.allocate(1 + 4 + payloadBytes.length);
        byteBuffer.put(WireMessageUtility.typeToBytes(wireMessage.messageType()));
        byteBuffer.putInt(payloadBytes.length);
        byteBuffer.put(payloadBytes);
        return byteBuffer.array();
    }

    public static WireMessage decode(InputStream inputStream) throws IOException {
        var typeByte = inputStream.read();
        if (typeByte == -1) {
            log.error("Unexpected end of stream; type: {}, expected: {}", typeByte, -1);
            throw new IOException("Unexpected end of stream");
        }
        var wireMessageType = WireMessageUtility.stringToType(String.valueOf((char) typeByte));
        var payloadBytes = inputStream.readNBytes(4);
        if (payloadBytes.length != 4) {
            log.error("Unexpected end of stream; payload: {}, expected: {}", payloadBytes.length, 4);
            throw new IOException("Unexpected end of stream; could not read payload length");
        }
        var payloadLength = ByteBuffer.wrap(payloadBytes).getInt();
        var payloadBytesArray = inputStream.readNBytes(payloadLength);
        if (payloadBytesArray.length < payloadLength) {
            throw new IOException("Unexpected end of stream: incomplete payload");
        }
        var payload = new String(payloadBytesArray, StandardCharsets.UTF_8);
        return new WireMessage(wireMessageType, payload);
    }
}
