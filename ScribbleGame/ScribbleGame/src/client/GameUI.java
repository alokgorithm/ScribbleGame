package client;

import javax.swing.*;
import java.awt.*;

public class GameUI extends JFrame {
    private GameClient client;
    private WhiteboardPanel whiteboardPanel;
    private ChatPanel chatPanel;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private JButton startGameButton; // Visible only to host.
    private boolean isHost;

    public GameUI(GameClient client, boolean isHost) {
        this.client = client;
        this.isHost = isHost;
        setTitle("Scribble Game - " + (isHost ? "Host" : "Player"));
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
        setVisible(true);
    }

    // Initialize UI components.
    private void initComponents() {
        whiteboardPanel = new WhiteboardPanel(client);
        chatPanel = new ChatPanel(client);
        timerLabel = new JLabel("Timer: 90");
        scoreLabel = new JLabel("Score: 0");

        // Panel for left side (whiteboard and timer)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(whiteboardPanel, BorderLayout.CENTER);
        leftPanel.add(timerLabel, BorderLayout.NORTH);

        // Panel for right side (chat, score, and optionally start button)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(chatPanel, BorderLayout.CENTER);
        rightPanel.add(scoreLabel, BorderLayout.SOUTH);

        // If host, add a Start Game button at the top of the right panel.
        if (isHost) {
            startGameButton = new JButton("Start Game");
            startGameButton.addActionListener(e -> {
                client.sendMessage("START_GAME");
                startGameButton.setEnabled(false);
            });
            rightPanel.add(startGameButton, BorderLayout.NORTH);
        }

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(leftPanel, BorderLayout.CENTER);
        getContentPane().add(rightPanel, BorderLayout.EAST);
    }

    // Process messages received from the server and update the UI.
    public void handleServerMessage(String message) {
        if (message.startsWith("DRAW:")) {
            String drawData = message.substring(5);
            whiteboardPanel.drawFromData(drawData);
        } else if (message.startsWith("CHAT:")) {
            String chatMsg = message.substring(5);
            chatPanel.appendMessage(chatMsg);
        } else if (message.startsWith("TIMER:")) {
            String time = message.substring(6);
            timerLabel.setText("Timer: " + time);
        } else if (message.startsWith("ROUND_END:")) {
            chatPanel.appendMessage(message.substring(10));
            whiteboardPanel.clearBoard();
        } else if (message.startsWith("NEW_ROUND:")) {
            String drawer = message.substring(10);
            chatPanel.appendMessage(drawer + " is drawing.");
        } else if (message.startsWith("YOUR_WORD:")) {
            JOptionPane.showMessageDialog(this, "Your word: " + message.substring(10), "Word to Draw", JOptionPane.INFORMATION_MESSAGE);
        } else if (message.startsWith("GAME_START")) {
            chatPanel.appendMessage("Game Started!");
        } else if (message.startsWith("GAME_OVER")) {
            chatPanel.appendMessage("Game Over!");
        } else if (message.startsWith("CORRECT_GUESS:")) {
            chatPanel.appendMessage(message.substring(14) + " guessed the word correctly!");
        } else if (message.startsWith("USER_JOINED:")) {
            String joinedUser = message.substring(12);
            chatPanel.appendMessage(joinedUser + " joined the game.");
        } else if (message.startsWith("SCORE:")) {
            // Message format: SCORE:username:score
            String[] parts = message.split(":");
            if(parts.length >= 3 && parts[1].equals(client.getUsername())) {
                scoreLabel.setText("Score: " + parts[2]);
            }
        }
    }
}
