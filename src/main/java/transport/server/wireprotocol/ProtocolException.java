package transport.server.wireprotocol;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Exception thrown when there is an error in wire protocol handling.
 * This wraps lower-level IOExceptions with protocol-specific context.
 */
@NullMarked
public class ProtocolException extends RuntimeException {
    public ProtocolException(String message) {
        super(message);
    }

    public ProtocolException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
