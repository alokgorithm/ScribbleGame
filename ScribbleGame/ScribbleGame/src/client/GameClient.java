package client;

import java.io.*;
import java.net.*;
import javax.swing.*;

public class GameClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private GameUI gameUI;
    private String username;
    private boolean isHost;

    public String getUsername() {
        return username;
    }


    // Updated constructor with isHost flag.
    public GameClient(String serverIp, int port, String username, boolean isHost) {
        this.username = username;
        this.isHost = isHost;
        try {
            socket = new Socket(serverIp, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Send the username immediately upon connecting.
            out.println(username);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create the UI first, then start listening.
        SwingUtilities.invokeLater(() -> {
            gameUI = new GameUI(GameClient.this, isHost);
            // Now that gameUI is created, start the listening thread.
            new Thread(() -> {
                listenToServer();
            }).start();
        });
    }

    // Listen for and process messages from the server.
    public void listenToServer() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received: " + message);
                // Safety: if gameUI is not yet assigned (shouldn't happen), wait a little.
                while (gameUI == null) {
                    try { Thread.sleep(50); } catch (InterruptedException e) {}
                }
                gameUI.handleServerMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Send a message to the server.
    public void sendMessage(String msg) {
        out.println(msg);
    }
}
