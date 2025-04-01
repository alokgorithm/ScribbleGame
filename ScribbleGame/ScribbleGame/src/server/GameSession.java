package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameSession {
    private List<ClientHandler> clients;
    private Queue<ClientHandler> drawingQueue;
    private ExecutorService executor;
    private boolean gameStarted;
    private Timer roundTimer;
    private int currentRoundTime;
    private final int INITIAL_TIME = 90; // seconds

    // New: Score map for each client
    private Map<ClientHandler, Integer> scores;
    // New: Set to track which clients have guessed correctly in the current round
    private Set<ClientHandler> guessedThisRound;

    public GameSession() {
        clients = new ArrayList<>();
        drawingQueue = new LinkedList<>();
        executor = Executors.newCachedThreadPool();
        scores = new HashMap<>();
        guessedThisRound = new HashSet<>();
        gameStarted = false;
    }

    // Add a new client to the session.
    public synchronized void addClient(Socket socket) {
        try {
            ClientHandler clientHandler = new ClientHandler(socket, this);
            clients.add(clientHandler);
            // Initialize the client's score.
            scores.put(clientHandler, 0);
            drawingQueue.add(clientHandler);
            executor.execute(clientHandler);
            // Notify all clients that a new player has joined.
            broadcast("USER_JOINED:" + clientHandler.getUsername());
            // Send initial score update for this client.
            broadcastScore(clientHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Start game when the host sends the command.
    public synchronized void startGame() {
        if (gameStarted)
            return;
        gameStarted = true;
        // Reset scores or guessedThisRound if needed
        broadcast("GAME_START");
        startRound();
    }

    // Begin a new round.
    private void startRound() {
        currentRoundTime = INITIAL_TIME;
        guessedThisRound.clear(); // clear guesses for new round
        // Get current drawer from queue.
        ClientHandler currentDrawer = drawingQueue.peek();
        broadcast("NEW_ROUND:" + currentDrawer.getUsername());

        // Get a word from the words list.
        String word = WordManager.getRandomWord();
        currentDrawer.setLastWord(word); // Save the word so it can be compared later.
        // Only the current drawer sees the word.
        currentDrawer.sendMessage("YOUR_WORD:" + word);

        // Start the timer for this round.
        roundTimer = new Timer();
        roundTimer.scheduleAtFixedRate(new TimerTask(){
            public void run() {
                currentRoundTime--;
                broadcast("TIMER:" + currentRoundTime);
                if (currentRoundTime <= 0) {
                    roundTimer.cancel();
                    endRound();
                }
            }
        }, 1000, 1000);
    }

    // End the current round.
    public synchronized void endRound() {
        ClientHandler currentDrawer = drawingQueue.peek();
        broadcast("ROUND_END:The word was " + currentDrawer.getLastWord());

        // Rotate the drawing order.
        drawingQueue.poll();
        drawingQueue.add(currentDrawer);

        // Placeholder: Check if game should end (every player has drawn).
        if (hasAllPlayersDrawn()) {
            broadcast("GAME_OVER");
            // Save high scores etc. using DatabaseHandler.
        } else {
            // Delay before starting the next round.
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {}
            startRound();
        }
    }

    // Placeholder method: Implement logic to determine if every player has drawn.
    private boolean hasAllPlayersDrawn() {
        return false;
    }

    // Broadcast a message to all connected clients.
    public synchronized void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    // Broadcast score update for a specific client.
    public synchronized void broadcastScore(ClientHandler client) {
        int score = scores.get(client);
        // Message format: SCORE:username:score
        broadcast("SCORE:" + client.getUsername() + ":" + score);
    }

    // Called when a client guesses correctly.
    public synchronized void processGuess(ClientHandler client, String guess) {
        ClientHandler currentDrawer = drawingQueue.peek();
        // Do not allow the drawer to guess.
        if (client == currentDrawer) {
            client.sendMessage("CHAT:You cannot guess your own word.");
            return;
        }
        // Check if this client already guessed correctly.
        if (guessedThisRound.contains(client)) {
            client.sendMessage("CHAT:You have already guessed correctly.");
            return;
        }
        // Check if the guess is correct.
        if (guess.equalsIgnoreCase(currentDrawer.getLastWord())) {
            guessedThisRound.add(client);
            // Update score: For example, add points equal to current round time.
            int points = currentRoundTime;
            int newScore = scores.get(client) + points;
            scores.put(client, newScore);
            broadcast("CORRECT_GUESS:" + client.getUsername());
            broadcastScore(client);
            // Reduce timer by 10 seconds.
            currentRoundTime = Math.max(0, currentRoundTime - 10);
            broadcast("TIMER:" + currentRoundTime);
        } else {
            // Broadcast the chat message (even if it's wrong).
            broadcast("CHAT:" + client.getUsername() + ": " + guess);
        }
    }

    // Nested class to handle each client connection.
    class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private GameSession session;
        private String username;
        private String lastWord; // The word assigned to the drawer.

        public ClientHandler(Socket socket, GameSession session) throws IOException {
            this.socket = socket;
            this.session = session;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        public String getUsername() {
            return username;
        }

        public void setLastWord(String word) {
            this.lastWord = word;
        }

        public String getLastWord() {
            return lastWord;
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }

        @Override
        public void run() {
            try {
                // The first message from the client is expected to be the username.
                username = in.readLine();
                if (username == null) {
                    username = "Unknown";
                }
                session.broadcast("USER_JOINED:" + username);

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("GUESS:")) {
                        String guess = message.substring(6).trim();
                        session.processGuess(this, guess);
                    } else if (message.equals("START_GAME")) {
                        session.startGame();
                    } else if (message.startsWith("DRAW:")) {
                        session.broadcast("DRAW:" + message.substring(5));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
