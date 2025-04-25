import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Shefrah2 {
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
}else if (message.startsWith("answer:")) {
                        handleAnswer(message.substring(7));
                    } else if (message.equals("GET_PLAYERS")) {
                        // إرسال قائمة اللاعبين عند الطلب
                        out.println("PLAYERS:" + String.join(",", getPlayerNames()));
                    } else if (message.equals("GET_PLAYERS_FOR_SCOREBOARD")) {
                        // إرسال قائمة اللاعبين مع النقاط للوحة النتائج
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
                out.println("GameOver:Time's up! final scores:" + getFinalScores());
                return;
            }
            
            try {
                int playerAnswer = Integer.parseInt(answer);
                int currentLevel;
                synchronized (playerLevels) {
                    currentLevel = playerLevels.get(playerName);
                }
               if (currentLevel >= answers.size()) {
                 out.println("GameOver:final scores:" + getFinalScores());
                   /* out.println("GameOver: Your final score: " + playerScores.get(playerName) + 
                          " final scores:" + getFinalScores()); */
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
    // Player has completed the last level → End the game with a winner
    broadcastMessage("GameOver:Winner! " + playerName + " wins with score: " 
                    + playerScores.get(playerName) + " Final scores: " + getFinalScores());
    endGame(); // Stop timer and reset game
}
                } else {
                    // إرسال رسالة WrongAnswer للعميل
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
            synchronized (playerScores) {
                playerScores.remove(playerName);
            }
            synchronized (playerLevels) {
                playerLevels.remove(playerName);
            }
              synchronized (playerLevels) {
                 if (waitingPlayers.size() == 1) {
            String lastPlayer = waitingPlayers.get(0);
            for (ClientHandler client : waitingRoom) {
                if (client.playerName.equals(lastPlayer)) {
                    client.sendMessage("GameOver:All other players left. You win! final scores:" + getFinalScores());
                }
            }
         
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
              // إذا لم يعد هناك لاعبون، أوقف التايمر
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
            // عرض النقاط فقط للاعبين في قائمة waitingPlayers
            if (waitingPlayers.contains(entry.getKey())) {
                sb.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
            }
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
    // استخدام LinkedHashSet لإزالة التكرارات مع الحفاظ على الترتيب
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
                        // إذا وصلنا لـ5 لاعبين نبدأ مباشرة
                        if (waitingPlayers.size() >= 5) {
                            cancel();
                            checkAndStartGame(false); // false = ليس بسبب انتهاء المؤقت
                            return;
                        }

                        if (countdown <= 0) {
                            cancel();
                            timerRunning = false;
                            checkAndStartGame(true); // true = بسبب انتهاء المؤقت
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
        // تبدأ اللعبة إذا كان هناك 5 لاعبين أو إذا forceStart=true (عند انتهاء المؤقت)
        if (waitingPlayers.size() >= 5 || (forceStart && waitingPlayers.size() >= 2)) {
            if (gameTimer != null) {
                gameTimer.cancel();
                timerRunning = false;
                gameStarted = true;
                countdown = 30;
            }

            System.out.println("Game started with players: " + waitingPlayers);

            // تهيئة بيانات اللاعبين
            synchronized (playerLevels) {
                waitingPlayers.forEach(player -> playerLevels.put(player, 0));
            }

            synchronized (playerScores) {
                waitingPlayers.forEach(player -> playerScores.put(player, 0));
            }

            // إرسال بداية اللعبة لكل اللاعبين
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
        for (Map.Entry<String, Integer> entry : playerScores.entrySet()) {
            if (waitingPlayers.contains(entry.getKey())) { // Only include players who stayed
                sb.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
            }
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
            
            // تحذير عند 30 ثانية المتبقية
            if (remainingGameTime == 30) {
                broadcastMessage("Warning:30 seconds remaining!");
            }
        }
    }, 0, 1000); // تحديث كل ثانية
}
    
private static void endGameByTime() {
    broadcastMessage("GameOver:Time's up! Final scores: " + getFinalScores());
    endGame(); // Reuse the same cleanup method
}
    private static void endGame() {
    // Stop the timer if running
    timerRunning = false;
    countdown = 30; // Reset for next game
    
    if (gameTimer != null) {
        gameTimer.cancel();
    }
    if (totalGameTimer != null) {
        totalGameTimer.cancel();
    }
    
    // Clear waiting players
    synchronized (waitingPlayers) {
        waitingPlayers.clear();
    }
}
         
}
