package server;

import common.ChatLogger;
import common.Message;
import common.Message.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Set;

/**
 * ClientHandler.java
 * ===================
 * Implements Runnable so each instance can run on its own Thread
 * (see ChatServer.main -> `new Thread(handler).start()`).
 *
 * One instance of this class = one connected client. It:
 *   1. Reads the username the client sends first.
 *   2. Announces the join to everyone.
 *   3. Loops forever reading lines from that client and reacting:
 *        - plain text        -> broadcast to everyone
 *        - "/w user msg"     -> private message
 *        - "/list"           -> send back online users
 *        - "/nick newname"   -> change username
 *        - "/help"           -> show available commands
 *        - "/quit"           -> disconnect gracefully
 *   4. On disconnect (voluntary or accidental), cleans up and announces
 *      the leave to everyone else.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Set<ClientHandler> allClients;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private volatile boolean running = true;

    public ClientHandler(Socket socket, Set<ClientHandler> allClients) {
        this.socket = socket;
        this.allClients = allClients;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void run() {
        try {
            // BufferedReader wraps the socket's InputStream so we can read
            // whole lines of text easily (readLine()) instead of raw bytes.
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // PrintWriter wraps the socket's OutputStream, auto-flush=true
            // means every println() is sent immediately, not buffered.
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("SUBMITNAME"); // tell the client: "please send your username"
            username = in.readLine();

            if (username == null || username.trim().isEmpty()) {
                username = "Guest" + (int) (Math.random() * 1000);
            }

            // Prevent duplicate usernames (simple check).
            while (ChatServer.findByUsername(username) != null
                    && ChatServer.findByUsername(username) != this) {
                out.println("NAMEINUSE");
                username = in.readLine();
                if (username == null) return;
            }

            out.println("NAMEACCEPTED " + username);
            System.out.println(username + " has joined the chat.");

            // Announce to everyone (including the new client).
            ChatServer.broadcast(new Message(username, "", MessageType.JOIN), this);

            String line;
            while (running && (line = in.readLine()) != null) {
                handleClientLine(line);
            }

        } catch (IOException e) {
            System.out.println(username + " disconnected unexpectedly: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    /**
     * Parses one line of input from the client and reacts appropriately.
     * This is essentially our tiny "chat protocol" parser.
     */
    private void handleClientLine(String line) {
        if (line.equalsIgnoreCase("/quit")) {
            running = false;

        } else if (line.equalsIgnoreCase("/list")) {
            List<String> online = ChatServer.getOnlineUsernames();
            send("Online users (" + online.size() + "): " + String.join(", ", online));

        } else if (line.equalsIgnoreCase("/help")) {
            send("Available Commands:");
            send("/help                    Show this help message");
            send("/list                    Show online users");
            send("/nick <newname>          Change your username");
            send("/quit                    Leave the chat");
            send("/w <username> <message>  Send a private message");

        } else if (line.startsWith("/nick ")) {
            String newName = line.substring(6).trim();
            if (newName.isEmpty()) {
                send("Usage: /nick <newname>");
            } else if (ChatServer.findByUsername(newName) != null) {
                send("Nickname '" + newName + "' is already taken.");
            } else {
                String oldName = username;
                username = newName;
                send("Nickname changed to " + newName);
                ChatServer.broadcast(new Message(oldName, "is now known as " + newName, MessageType.SYSTEM), this);
            }

        } else if (line.startsWith("/w ")) {
            // Format: /w targetUsername message text here
            String rest = line.substring(3).trim();
            int spaceIdx = rest.indexOf(' ');
            if (spaceIdx == -1) {
                send("Usage: /w <username> <message>");
                return;
            }
            String targetName = rest.substring(0, spaceIdx);
            String content = rest.substring(spaceIdx + 1);

            ClientHandler target = ChatServer.findByUsername(targetName);
            if (target == null) {
                send("User '" + targetName + "' is not online.");
            } else {
                Message pm = new Message(username, content, MessageType.PRIVATE);
                target.send(pm.toString());
                send(pm.toString()); // echo back to sender too
                ChatLogger.log("[PRIVATE] " + pm);
            }

        } else {
            // Normal public message -> broadcast to everyone.
            ChatServer.broadcast(new Message(username, line, MessageType.PUBLIC), this);
        }
    }

    /** Sends a raw line of text to THIS client only. */
    public void send(String text) {
        if (out != null) {
            out.println(text);
        }
    }

    /** Cleans up sockets/streams and notifies everyone that this user left. */
    private void disconnect() {
        ChatServer.removeClient(this);
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket for " + username + ": " + e.getMessage());
        }

        if (username != null) {
            System.out.println(username + " has left the chat.");
            ChatServer.broadcast(new Message(username, "", MessageType.LEAVE), this);
        }
    }
}
