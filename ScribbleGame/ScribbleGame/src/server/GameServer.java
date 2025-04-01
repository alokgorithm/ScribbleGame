package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private static List<GameSession> sessions = new ArrayList<>();
    private static int port = 12345;

    public static int getPort() {
        return port;
    }

    // This method starts the server and listens for client connections.
    public static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Game Server started on port: " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // For simplicity, we use a single session.
                if (sessions.isEmpty()) {
                    GameSession session = new GameSession();
                    sessions.add(session);
                }
                GameSession session = sessions.get(0);
                session.addClient(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
