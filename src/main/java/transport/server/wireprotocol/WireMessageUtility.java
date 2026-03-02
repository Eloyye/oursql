package transport.server.wireprotocol;

import org.jspecify.annotations.NullMarked;

import java.nio.charset.StandardCharsets;

@NullMarked
public class WireMessageUtility {
    public enum WireMessageType {
        READY_FOR_QUERY,
        QUERY,
        ROW_DESCRIPTION,
        DATA_ROW,
        COMMAND_COMPLETE,
        TERMINATE, ERROR
    }
/**
*
 * @param type: The WireMessageType enum type to convert to string representation
 * @return Single Character representation of the WireMessageType enum
*/
    public static String typeToString(WireMessageType type) {
        return switch (type) {
            case READY_FOR_QUERY -> "R";
            case QUERY -> "Q";
            case ROW_DESCRIPTION -> "D";
            case DATA_ROW -> "T";
            case COMMAND_COMPLETE -> "C";
          case TERMINATE -> "X";
          case ERROR -> "E";
        };
    }

/**
*
 * @param type: The WireMessageType enum type to convert to a byte array
 * @return byte array with utf-8 encoding
*/
    public static byte[] typeToBytes(WireMessageType type) {
        return typeToString(type).getBytes(StandardCharsets.UTF_8);
    }

    public static WireMessageType stringToType(String type) {
        return switch (type) {
            case "R" -> WireMessageType.READY_FOR_QUERY;
            case "Q" -> WireMessageType.QUERY;
            case "D" -> WireMessageType.ROW_DESCRIPTION;
            case "T" -> WireMessageType.DATA_ROW;
            case "C" -> WireMessageType.COMMAND_COMPLETE;
            case "E" -> WireMessageType.ERROR;
            case "X" -> WireMessageType.TERMINATE;
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };
    }
}

