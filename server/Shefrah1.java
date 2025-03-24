import java.io.*;
import java.net.*;
import java.util.*;

public class Shefrah2 {
    private static final ArrayList<ClientHandler> waitingRoom = new ArrayList<>();
    private static final ArrayList<String> waitingPlayers = new ArrayList<>();
    private static int countdown = 30;
    private static boolean timerRunning = false;
    private static Timer gameTimer;
    private static final Map<String, Integer> playerScores = new HashMap<>();
    private static final List<String> picName = Arrays.asList(
       "pic1", "pic2", "pic3", "pic4", "pic5", 
        "pic6", "pic7", "pic8", "pic9",
        "pic10", "pic11", "pic12", "pic13", "pic14", "pic15"
    );
    
     private static final List<Integer> answers = Arrays.asList(
        25, 15, 30, 40, 35, 5, 45, 50, 20, 10, 65, 55, 30, 25, 10
    );
        private static int currentRound = 0;

        
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
            sendPlayersList(); // Ensure the player list is updated upon new connection
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
        sendPlayersList();
        broadcastWaitingPlayers();

        String message;
        while ((message = in.readLine()) != null) {
            if (message.equals("play")) {
                synchronized (waitingPlayers) {
                    if (!waitingPlayers.contains(playerName)) {
                        waitingPlayers.add(playerName);
                    }
                }
                broadcastWaitingPlayers();
                startCountdownIfNeeded();
                checkAndStartGame();
            } else if (message.startsWith("answer:")) {
                String answer = message.substring(7); // استخراج الإجابة بعد "answer:"
                handleAnswer(answer); // التحقق من الإجابة
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
        int correctAnswer = answers.get(currentRound);
        if (playerAnswer == correctAnswer) {
            // Correct answer, increase score
            playerScores.put(playerName, playerScores.getOrDefault(playerName, 0) + 1);
            out.println("Correct! Your score: " + playerScores.get(playerName));

            // Move to the next round
            currentRound++;
            if (currentRound < picName.size()) {
                out.println("NextRound:" + picName.get(currentRound)); // Send next round image name
            } else {
                out.println("Game over! Your final score: " + playerScores.get(playerName));
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

            for (ClientHandler client : waitingRoom) {
                if (waitingPlayers.contains(client.playerName)) {
                    client.sendMessage("GameStart");
                } else {
                    client.sendMessage("StayInWaitingRoom");
                }
            }
            closePreviousFrames();
        }
    }

    private static void closePreviousFrames() {
        broadcastMessage("ClosePreviousFrames");
    }
    private static void endGame() {
        broadcastMessage("GameEnd");
    }
}


