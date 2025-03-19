import java.io.*;
import java.net.*;
import java.util.*;

public class Shefrah1 {
    private static final ArrayList<ClientHandler> waitingRoom = new ArrayList<>();
    private static final ArrayList<String> waitingPlayers = new ArrayList<>();
    private static int countdown = 30;
    private static boolean timerRunning = false;
    private static Timer gameTimer;

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
                    }
                }
            } catch (IOException e) {
                System.out.println("Player " + playerName + " disconnected.");
            } finally {
                removePlayer();
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
}

