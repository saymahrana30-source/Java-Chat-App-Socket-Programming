package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;

/**
 * ChatClientGUI.java
 * ==================
 * Optional Swing-based GUI client. Same underlying protocol as
 * client.ChatClient - only the presentation layer changes.
 *
 * IMPORTANT SWING THREADING RULE:
 *   Swing components must only be touched from the "Event Dispatch
 *   Thread" (EDT). Our network listener runs on a background thread,
 *   so whenever it needs to update the chat window it must hop back
 *   onto the EDT using SwingUtilities.invokeLater(...).
 */
public class ChatClientGUI extends JFrame {

    private final JTextArea chatArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final JButton sendButton = new JButton("Send");
    private final JLabel statusLabel = new JLabel("Not connected");

    private Socket socket;
    private BufferedReader serverIn;
    private PrintWriter serverOut;
    private String username;

    public ChatClientGUI(String host, int port) {
        super("Java Chat Client");
        buildUi();
        connectToServer(host, port);
    }

    private void buildUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 460);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(6, 6));
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(statusLabel, BorderLayout.NORTH);

        // Send on button click AND on pressing Enter in the text field.
        sendButton.addActionListener(this::onSend);
        inputField.addActionListener(this::onSend);
    }

    private void onSend(ActionEvent e) {
        String text = inputField.getText().trim();
        if (!text.isEmpty() && serverOut != null) {
            serverOut.println(text);
            inputField.setText("");
            if (text.equalsIgnoreCase("/quit")) {
                dispose();
            }
        }
    }

    private void connectToServer(String host, int port) {
        username = JOptionPane.showInputDialog(this, "Enter your username:", "Join Chat", JOptionPane.PLAIN_MESSAGE);
        if (username == null || username.trim().isEmpty()) {
            username = "Guest" + (int) (Math.random() * 1000);
        }

        try {
            socket = new Socket(host, port);
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverOut = new PrintWriter(socket.getOutputStream(), true);

            String prompt = serverIn.readLine(); // "SUBMITNAME"
            serverOut.println(username);

            String response = serverIn.readLine();
            while (response != null && response.equals("NAMEINUSE")) {
                username = JOptionPane.showInputDialog(this,
                        "That username is taken. Enter another:", "Join Chat", JOptionPane.PLAIN_MESSAGE);
                serverOut.println(username);
                response = serverIn.readLine();
            }

            statusLabel.setText("Connected as " + username + " (" + host + ":" + port + ")");

            // Background listener thread - keeps GUI responsive while waiting
            // for incoming messages from the server.
            Thread listener = new Thread(this::listenForMessages);
            listener.setDaemon(true);
            listener.start();

        } catch (IOException ex) {
            statusLabel.setText("Connection failed: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Could not connect to " + host + ":" + port + "\n" + ex.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void listenForMessages() {
        try {
            String line;
            while ((line = serverIn.readLine()) != null) {
                String finalLine = line;
                // Hop back onto the EDT before touching the Swing component.
                SwingUtilities.invokeLater(() -> chatArea.append(finalLine + "\n"));
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> statusLabel.setText("Disconnected from server."));
        }
    }

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;

        SwingUtilities.invokeLater(() -> {
            ChatClientGUI gui = new ChatClientGUI(host, port);
            gui.setVisible(true);
        });
    }
}
