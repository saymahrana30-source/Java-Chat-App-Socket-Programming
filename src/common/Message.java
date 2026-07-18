package common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Message.java
 * ------------
 * A simple data class (POJO) that represents a single chat message.
 * We keep it in "common" because BOTH the server and the client need
 * to understand what a "message" looks like.
 *
 * Even though our current transport uses plain text lines (see protocol
 * notes in ChatServer.java), having this class is useful for:
 *  - Formatting a message consistently (timestamp + sender + content)
 *  - Making it easy to switch to an object-based protocol later
 *    (e.g. serializing this class instead of raw strings)
 */
public class Message implements Serializable {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String sender;
    private final String content;
    private final LocalDateTime timestamp;
    private final MessageType type;

    public enum MessageType {
        PUBLIC,     // normal broadcast message
        PRIVATE,    // whisper / direct message
        JOIN,       // system message: user joined
        LEAVE,      // system message: user left
        SYSTEM      // any other server-generated notice
    }

    public Message(String sender, String content, MessageType type) {
        this.sender = sender;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Formats the message the way it should appear on screen / in logs.
     * Example: [14:03:22] Sam: hello everyone
     */
    @Override
    public String toString() {
        String time = timestamp.format(TIME_FORMAT);
        switch (type) {
            case JOIN:
                return String.format("[%s] *** %s has joined the chat ***", time, sender);
            case LEAVE:
                return String.format("[%s] *** %s has left the chat ***", time, sender);
            case PRIVATE:
                return String.format("[%s] (private) %s: %s", time, sender, content);
            case SYSTEM:
                return String.format("[%s] SERVER: %s", time, content);
            case PUBLIC:
            default:
                return String.format("[%s] %s: %s", time, sender, content);
        }
    }
}
