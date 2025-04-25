import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Shefrah1 {
    private static final ArrayList<ClientHandler> waitingRoom = new ArrayList<>();
    private static final ArrayList<String> waitingPlayers = new ArrayList<>();
    private static int countdown = 30;
    private static boolean timerRunning = false;
    private static Timer gameTimer;
    private static final Map<String, Integer> playerScores = new HashMap<>();
    private static final Map<String, Integer> playerLevels = new HashMap<>();
    private static final List<String> picName = Arrays.asList(
        "pic1", "pic2", "pic3", "pic4", "pic5", "pic6", "pic7", "pic8", "pic9",
        "pic10", "pic11", "pic12", "pic13", "pic14"
    );
    private static final List<Integer> answers = Arrays.asList(
       15, 5, 2, 3, 12, 6, 7, 5, 10, 0, 0, 0, 0, 0, 0
    );
    private static final int TOTAL_GAME_TIME = 120; // 120 ثانية = دقيقتين
    private static int remainingGameTime = TOTAL_GAME_TIME;
    private static Timer totalGameTimer; 
    private static volatile boolean gameStarted = false;

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
                
                synchronized (playerScores) {
                    playerScores.put(playerName, 0);
                }
                synchronized (playerLevels) {
                    playerLevels.put(playerName, 0);
                }
                broadcastScores();
                
                System.out.println("Player joined: " + playerName);
                sendPlayersList();
                broadcastWaitingPlayers();

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("play")) {
                        synchronized (waitingPlayers) {
                            if (!gameStarted && !waitingPlayers.contains(playerName) && waitingPlayers.size() < 5) {
                                waitingPlayers.add(playerName);
                                startCountdownIfNeeded();
                            }
                        }
                        broadcastWaitingPlayers();
                    } else if (message.startsWith("answer:")) {
                        handleAnswer(message.substring(7));
                    } else if (message.equals("GET_PLAYERS")) {
                        out.println("PLAYERS:" + String.join(",", getPlayerNames()));
                    } else if (message.equals("GET_PLAYERS_FOR_SCOREBOARD")) {
                        StringBuilder sb = new StringBuilder("SCORES:");
                        synchronized (playerScores) {
                            for (Map.Entry<String, Integer> entry : playerScores.entrySet()) {
                                sb.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
                            }
                        }
                        if (sb.length() > 7) {
                            sb.setLength(sb.length() - 1);
                        }
                        out.println(sb.toString());
                    }
                }
            } catch (IOException e) {
                System.out.println("Player " + playerName + " disconnected.");
            } finally {
                removePlayer();
            }
        }

        private void handleAnswer(String answer) {
            if (remainingGameTime <= 0) {
                String finalScores = getFinalScores();
                System.out.println("Final scores at time out: " + finalScores);
                out.println("GameOver:Time's up! Final scores:" + finalScores);
                return;
            }
            
            try {
                int playerAnswer = Integer.parseInt(answer);
                int currentLevel;
                synchronized (playerLevels) {
                    currentLevel = playerLevels.get(playerName);
                }
                
                if (currentLevel >= answers.size()) {
                    String finalScores = getFinalScores();
                    System.out.println("Final scores for player completion: " + finalScores);
                    out.println("GameOver:Your final score: " + playerScores.get(playerName) + 
                                " Final scores:" + finalScores);
                    return;
                }
                
                int correctAnswer = answers.get(currentLevel);
                if (playerAnswer == correctAnswer) {
                    synchronized (playerScores) {
                        int newScore = playerScores.get(playerName) + 1;
                        playerScores.put(playerName, newScore);
                    }
                    broadcastScores();
                    
                    synchronized (playerLevels) {
                        playerLevels.put(playerName, currentLevel + 1);
                    }
                    
                    out.println("Correct!");
                    
                    if (currentLevel + 1 < picName.size()) {
                        out.println("NextRound:" + picName.get(currentLevel + 1));
                    } else {
                        String finalScores = getFinalScores();
                        System.out.println("Final scores for winner: " + finalScores);
                        String message = finalScores.isEmpty() 
                            ? "GameOver:Winner! " + playerName + " wins with score: " + playerScores.get(playerName) + " No scores available"
                            : "GameOver:Winner! " + playerName + " wins with score: " + playerScores.get(playerName) + " Final scores:" + finalScores;
                        broadcastMessage(message);
                        endGame();
                    }
                } else {
                    out.println("WrongAnswer");
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
            // Do not remove from playerScores or playerLevels to preserve scores
            
            synchronized (waitingPlayers) {
                if (waitingPlayers.size() == 1 && gameStarted) {
                    String lastPlayer = waitingPlayers.get(0);
                    String finalScores = getFinalScores();
                    System.out.println("Final scores for last player: " + finalScores);
                    String message = finalScores.isEmpty() 
                        ? "GameOver:All other players left. You win! No scores available"
                        : "GameOver:All other players left. You win! Final scores:" + finalScores;
                    for (ClientHandler client : waitingRoom) {
                        if (client.playerName.equals(lastPlayer)) {
                            client.sendMessage(message);
                        }
                    }
                    endGame();
                }
            }
            
            broadcastScores();
            sendPlayersList();
            broadcastWaitingPlayers();
            
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket for " + playerName);
            }
            
            if (waitingRoom.isEmpty() && totalGameTimer != null) {
                totalGameTimer.cancel();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }

    private static void broadcastScores() {
        StringBuilder sb = new StringBuilder("SCORES:");
        synchronized (playerScores) {
            for (Map.Entry<String, Integer> entry : playerScores.entrySet()) {
                sb.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
            }
        }
        if (sb.length() > 7) {
            sb.setLength(sb.length() - 1); // Remove trailing comma
        }
        broadcastMessage(sb.toString());
    }

    private static void sendPlayersList() {
        String players = "Players:" + String.join(",", getPlayerNames());
        broadcastMessage(players);
    }

    private static void broadcastWaitingPlayers() {
        Set<String> uniquePlayers = new LinkedHashSet<>(waitingPlayers);
        String waiting = "WaitingPlayers:" + String.join(",", uniquePlayers);
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
        System.out.println("Broadcasting: " + message);
        synchronized (waitingRoom) {
            for (ClientHandler client : waitingRoom) {
                client.sendMessage(message);
            }
        }
    }

    private static void startCountdownIfNeeded() {
        synchronized (waitingPlayers) {
            if (waitingPlayers.size() >= 2 && !timerRunning) {
                timerRunning = true;
                gameTimer = new Timer();
                gameTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        synchronized (waitingPlayers) {
                            if (waitingPlayers.size() >= 5) {
                                cancel();
                                checkAndStartGame(false);
                                return;
                            }

                            if (countdown <= 0) {
                                cancel();
                                timerRunning = false;
                                checkAndStartGame(true);
                                return;
                            }

                            broadcastMessage("Timer:" + countdown);
                            countdown--;
                        }
                    }
                }, 0, 1000);
            }
        }
    }

    private static void checkAndStartGame(boolean forceStart) {
        synchronized (waitingPlayers) {
            if (waitingPlayers.size() >= 5 || (forceStart && waitingPlayers.size() >= 2)) {
                if (gameTimer != null) {
                    gameTimer.cancel();
                    timerRunning = false;
                    gameStarted = true;
                    countdown = 30;
                }

                System.out.println("Game started with players: " + waitingPlayers);

                synchronized (playerLevels) {
                    waitingPlayers.forEach(player -> playerLevels.put(player, 0));
                }

                synchronized (playerScores) {
                    waitingPlayers.forEach(player -> playerScores.put(player, 0));
                }

                synchronized (waitingRoom) {
                    waitingRoom.forEach(client -> {
                        if (waitingPlayers.contains(client.playerName)) {
                            client.sendMessage("GameStart:" + picName.get(0));
                            client.sendMessage("SCORES:" + 
                                waitingPlayers.stream()
                                    .map(p -> p + ":0")
                                    .collect(Collectors.joining(",")));
                        }
                    });
                }

                startTotalGameTimer();
            } else if (forceStart) {
                broadcastMessage("NotEnoughPlayers:Need at least 2 players to start");
            }
        }
    }
    
    private static String getFinalScores() {
        StringBuilder sb = new StringBuilder();
        synchronized (playerScores) {
            System.out.println("playerScores: " + playerScores);
            for (Map.Entry<String, Integer> entry : playerScores.entrySet()) {
                sb.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1); // Remove trailing comma
        }
        return sb.toString();
    }

    private static void startTotalGameTimer() {
        remainingGameTime = TOTAL_GAME_TIME;
        totalGameTimer = new Timer();
        totalGameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (remainingGameTime <= 0) {
                    endGameByTime();
                    totalGameTimer.cancel();
                    return;
                }
                
                broadcastMessage("TotalGameTimer:" + remainingGameTime);
                remainingGameTime--;
                
                if (remainingGameTime == 30) {
                    broadcastMessage("Warning:30 seconds remaining!");
                }
            }
        }, 0, 1000);
    }
    
    private static void endGameByTime() {
        String finalScores = getFinalScores();
        System.out.println("Final scores at time's up: " + finalScores);
        String message = finalScores.isEmpty() 
            ? "GameOver:Time's up! No scores available"
            : "GameOver:Time's up! Final scores:" + finalScores;
        broadcastMessage(message);
        endGame();
    }

    private static void endGame() {
        timerRunning = false;
        countdown = 30;
        
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        if (totalGameTimer != null) {
            totalGameTimer.cancel();
        }
        
        synchronized (waitingPlayers) {
            waitingPlayers.clear();
        }
        
        gameStarted = false;
    }
}