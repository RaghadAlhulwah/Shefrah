import java.io.*;
import java.net.*;
import java.util.*;

public class Shefrah1 {
    private static final ArrayList<ClientHandler> waitingRoom = new ArrayList<>();
    private static final ArrayList<String> waitingPlayers = new ArrayList<>();
    private static int countdown = 30;
    private static boolean timerRunning = false;
    private static Timer gameTimer;
    private static final Map<String, Integer> playerScores = new HashMap<>();
    private static final Map<String, Integer> playerLevels = new HashMap<>();
    private static final List<String> picName = Arrays.asList(
        "pic1", "pic2", "pic3", "pic4", "pic5", 
        "pic6", "pic7", "pic8", "pic9",
        "pic10", "pic11", "pic12", "pic13", "pic14", "pic15"
    );
    private static final List<Integer> answers = Arrays.asList(
        25, 15, 30, 40, 35, 5, 45, 50, 20, 10, 65, 55, 30, 25, 10
    );
    private static int currentRound = 0;
    private static Timer gameDurationTimer;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(3280);
        System.out.println("Server started...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            synchronized (waitingRoom) {
                waitingRoom.add(clientHandler);
            }
            new Thread(clientHandler).start();
            sendPlayersList();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String playerName;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        }

        @Override
        public void run() {
            try {
                playerName = in.readLine();
                if (playerName == null || playerName.trim().isEmpty()) {
                    out.println("Error: Invalid name");
                    socket.close();
                    return;
                }
                System.out.println("Player joined: " + playerName);
                playerScores.put(playerName, 0);
                playerLevels.put(playerName, 0);
                sendPlayersList();
                broadcastWaitingPlayers();

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("play")) {
                        synchronized (waitingPlayers) {
                            if (waitingPlayers.size() < 4 && !waitingPlayers.contains(playerName)) {
                                waitingPlayers.add(playerName);
                            }
                        }
                        broadcastWaitingPlayers();
                        startCountdownIfNeeded();
                        checkAndStartGame();
                    } else if (message.startsWith("answer:")) {
                        String answer = message.substring(7); // Extract the answer
                        handleAnswer(answer);
                    }
                }
            } catch (IOException e) {
                System.out.println("Player " + playerName + " disconnected.");
            } finally {
                removePlayer();
            }
        }

        private void handleAnswer(String answer) {
            try {
                int playerAnswer = Integer.parseInt(answer);
                int playerLevel = playerLevels.get(playerName);
                int correctAnswer = answers.get(playerLevel);
                if (playerAnswer == correctAnswer) {
                    playerScores.put(playerName, playerScores.get(playerName) + 1);
                    playerLevels.put(playerName, playerLevel + 1);
                    out.println("Correct! Your score: " + playerScores.get(playerName));

                    // Broadcast the updated score to all players
                    broadcastScoreUpdate(playerName, playerScores.get(playerName));

                    if (playerLevel + 1 < picName.size()) {
                        out.println("NextRound:" + picName.get(playerLevel + 1));
                    } else {
                        out.println("GameOver: Your final score: " + playerScores.get(playerName));
                        endGame();
                    }
                } else {
                    out.println("Incorrect! Try again.");
                }
            } catch (Exception e) {
                out.println("Error: Invalid answer format.");
            }
        }

        private void removePlayer() {
            synchronized (waitingRoom) {
                waitingRoom.remove(this);
            }
            synchronized (waitingPlayers) {
                waitingPlayers.remove(playerName);
            }
            playerScores.remove(playerName);
            playerLevels.remove(playerName);
            sendPlayersList();
            broadcastWaitingPlayers();
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }

    private static void sendPlayersList() {
        String players = "Players:" + String.join(",", getPlayerNames());
        broadcastMessage(players);
    }

    private static void broadcastWaitingPlayers() {
        String waiting = "WaitingPlayers:" + String.join(",", waitingPlayers);
        broadcastMessage(waiting);
    }

    private static List<String> getPlayerNames() {
        List<String> names = new ArrayList<>();
        synchronized (waitingRoom) {
            for (ClientHandler client : waitingRoom) {
                names.add(client.playerName);
            }
        }
        return names;
    }

    private static void broadcastMessage(String message) {
        synchronized (waitingRoom) {
            for (ClientHandler client : waitingRoom) {
                client.sendMessage(message);
            }
        }
    }

    private static void broadcastScoreUpdate(String playerName, int score) {
        String message = "ScoreUpdate:" + playerName + ":" + score;
        broadcastMessage(message);
    }

    private static void startCountdownIfNeeded() {
        if (waitingPlayers.size() >= 2 && !timerRunning) {
            timerRunning = true;
            gameTimer = new Timer();
            gameTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (waitingPlayers.size() >= 4) {
                        gameTimer.cancel();
                        checkAndStartGame();
                        return;
                    }
                    if (countdown <= 0) {
                        gameTimer.cancel();
                        checkAndStartGame();
                        return;
                    }
                    broadcastMessage("Timer:" + countdown);
                    countdown--;
                }
            }, 0, 1000);
        }
    }

    private static void checkAndStartGame() {
        if (waitingPlayers.size() >= 4 || (timerRunning && countdown <= 0)) {
            if (gameTimer != null) {
                gameTimer.cancel();
            }
            timerRunning = false;
            countdown = 30;
            System.out.println("Game started!");

            // Start the 2-minute game duration timer
            gameDurationTimer = new Timer();
            gameDurationTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    endGame();
                }
            }, 120000); // 2 minutes = 120,000 milliseconds

            for (ClientHandler client : waitingRoom) {
                if (waitingPlayers.contains(client.playerName)) {
                    client.sendMessage("GameStart:" + picName.get(playerLevels.get(client.playerName)));
                } else {
                    client.sendMessage("StayInWaitingRoom");
                }
            }
        }
    }

    private static void endGame() {
        broadcastMessage("GameOver");
        if (gameDurationTimer != null) {
            gameDurationTimer.cancel();
        }
    }
}
