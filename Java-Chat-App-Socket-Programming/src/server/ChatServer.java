package server;

import common.ChatLogger;
import common.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatServer.java
 * ===============
 * This is the "central hub" of the whole application.
 *
 * WHAT A SOCKET IS:
 *   A socket is just an endpoint for network communication - think of it
 *   as a "phone line" between two programs. A ServerSocket sits and
 *   LISTENS on a port for anyone dialing in. When a client connects,
 *   the ServerSocket hands back a plain Socket representing that one
 *   specific connection.
 *
 * WHY MULTITHREADING IS REQUIRED:
 *   ServerSocket.accept() is a BLOCKING call - it waits until a client
 *   connects. If we only ever handled one client at a time on the main
 *   thread, the 2nd client could never connect while we were busy
 *   talking to the 1st. So every time a new client connects, we spin
 *   up a brand-new Thread (a ClientHandler) dedicated to that client.
 *   The main thread's ONLY job becomes: loop forever, accept new
 *   connections, hand each one off to its own thread.
 *
 * PROTOCOL (keep it simple - this is a beginner/industry-style project):
 *   Every message sent over the socket is a single line of plain text,
 *   terminated by a newline (PrintWriter.println / BufferedReader.readLine).
 *   Special client commands:
 *     /w <username> <message>   -> private message ("whisper")
 *     /list                     -> ask server for online users
 *     /quit                     -> disconnect gracefully
 */
public class ChatServer {

    public static final int PORT = 5000;

    // Thread-safe set of all currently connected client handlers.
    // ConcurrentHashMap-backed set avoids ConcurrentModificationException
    // when one thread is broadcasting while another is joining/leaving.
    private static final Set<ClientHandler> clients =
            java.util.Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void main(String[] args) {
        int port = PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port argument, using default " + PORT);
            }
        }

        System.out.println("=========================================");
        System.out.println(" Java Chat Server - Socket Programming ");
        System.out.println("=========================================");
        System.out.println("Starting server on port " + port + " ...");

        // try-with-resources ensures the ServerSocket is closed automatically
        // if something goes wrong (good exception-handling practice).
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is up and listening on port " + port);
            System.out.println("Waiting for clients to connect...\n");
            ChatLogger.log("SERVER STARTED on port " + port);

            // Main accept loop - runs forever until the server process is killed.
            while (true) {
                Socket clientSocket = serverSocket.accept(); // BLOCKS here
                System.out.println("New connection from " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, clients);
                clients.add(handler);

                // Each client gets its OWN thread so the server can serve
                // many clients concurrently.
                Thread thread = new Thread(handler);
                thread.start();
            }

        } catch (IOException e) {
            System.err.println("Could not start server on port " + port
                    + ". Is the port already in use? " + e.getMessage());
        }
    }

    /**
     * Sends a message to every connected client except (optionally) the sender.
     * Called by ClientHandler instances whenever a broadcast is needed.
     */
    static void broadcast(Message message, ClientHandler excludeSelf) {
        String formatted = message.toString();
        for (ClientHandler client : clients) {
            client.send(formatted);
        }
        ChatLogger.log(formatted);
    }

    /** Removes a client handler from the active set (called on disconnect). */
    static void removeClient(ClientHandler handler) {
        clients.remove(handler);
    }

    /** Returns a snapshot list of currently connected usernames (for /list). */
    static java.util.List<String> getOnlineUsernames() {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (ClientHandler client : clients) {
            if (client.getUsername() != null) {
                names.add(client.getUsername());
            }
        }
        return names;
    }

    /** Finds a connected client handler by username (used for private messages). */
    static ClientHandler findByUsername(String username) {
        for (ClientHandler client : clients) {
            if (username.equalsIgnoreCase(client.getUsername())) {
                return client;
            }
        }
        return null;
    }
}
