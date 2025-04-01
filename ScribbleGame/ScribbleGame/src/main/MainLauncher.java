package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainLauncher extends JFrame {
    public MainLauncher() {
        setTitle("Scribble Game Launcher");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(2, 1, 10, 10));
        setLocationRelativeTo(null);

        JButton hostButton = new JButton("Host Game");
        JButton joinButton = new JButton("Join Game");

        hostButton.addActionListener(e -> hostGame());
        joinButton.addActionListener(e -> joinGame());

        add(hostButton);
        add(joinButton);
    }

    private void hostGame() {
        String username = JOptionPane.showInputDialog(this, "Enter your username (as Host):");
        if (username == null || username.isEmpty()) {
            return;
        }
        // Start server in a new thread
        new Thread(() -> {
            server.GameServer.startServer();
        }).start();

        // Get local IP and port
        String ip;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            ip = "localhost";
        }
        int port = server.GameServer.getPort();
        String link = ip + ":" + port;
        JOptionPane.showMessageDialog(this, "Server started.\nShare this link with players:\n" + link);

        // Start the client as host player, pass isHost = true.
        new client.GameClient(ip, port, username, true);

        // Close the launcher window
        dispose();
    }

    private void joinGame() {
        String serverIp = JOptionPane.showInputDialog(this, "Enter Server IP:");
        if (serverIp == null || serverIp.isEmpty()) {
            return;
        }
        String portStr = JOptionPane.showInputDialog(this, "Enter Port:");
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid port number!");
            return;
        }
        String username = JOptionPane.showInputDialog(this, "Enter your Username:");
        if (username == null || username.isEmpty()) {
            return;
        }
        // For joining, isHost is false.
        new client.GameClient(serverIp, port, username, false);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainLauncher().setVisible(true);
        });
    }
}
