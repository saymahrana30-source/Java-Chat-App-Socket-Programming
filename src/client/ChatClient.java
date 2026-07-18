package client;

import java.io.*;
import java.net.Socket;

/**
 * ChatClient.java
 * ===============
 * Console-based client. Demonstrates the CLIENT side of client-server
 * socket programming.
 *
 * WHY WE NEED TWO THREADS ON THE CLIENT TOO:
 *   Reading from the keyboard (System.in) and reading from the network
 *   socket are BOTH blocking operations. If we did them one after another
 *   on a single thread, we could never receive a message from another
 *   user while we were waiting for ourselves to type something.
 *   So we split the work:
 *     - Main thread   -> reads what YOU type and sends it to the server
 *     - Listener thread -> continuously reads incoming messages from the
 *                          server and prints them to the console
 */
public class ChatClient {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;

        System.out.println("Connecting to chat server at " + host + ":" + port + " ...");

        try (Socket socket = new Socket(host, port)) {
            System.out.println("Connected! Type '/help' to see all commands.\n");

            BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader keyboardIn = new BufferedReader(new InputStreamReader(System.in));

            // --- Username handshake ---
            String serverPrompt = serverIn.readLine(); // expect "SUBMITNAME"
            System.out.print("Enter your username: ");
            String username = keyboardIn.readLine();
            serverOut.println(username);

            String response = serverIn.readLine();
            while (response != null && response.equals("NAMEINUSE")) {
                System.out.print("That username is taken. Try another: ");
                username = keyboardIn.readLine();
                serverOut.println(username);
                response = serverIn.readLine();
            }
            System.out.println("Joined as: " + username + "\n");

            // --- Start the background listener thread ---
            Thread listener = new Thread(new ServerListener(serverIn));
            listener.setDaemon(true); // dies automatically when main thread exits
            listener.start();

            // --- Main thread: read keyboard input, send to server ---
            String userLine;
            while ((userLine = keyboardIn.readLine()) != null) {
                serverOut.println(userLine);
                if (userLine.equalsIgnoreCase("/quit")) {
                    break;
                }
            }

            System.out.println("Disconnecting...");

        } catch (IOException e) {
            System.err.println("Could not connect to server at " + host + ":" + port
                    + " -> " + e.getMessage());
        }
    }

    /**
     * Runnable that continuously listens for messages coming FROM the server
     * and prints them to the console. Runs on its own thread so it never
     * blocks the user from typing.
     */
    static class ServerListener implements Runnable {
        private final BufferedReader serverIn;

        ServerListener(BufferedReader serverIn) {
            this.serverIn = serverIn;
        }

        @Override
        public void run() {
            try {
                String line;
                while ((line = serverIn.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.out.println("Disconnected from server.");
            }
        }
    }
}
