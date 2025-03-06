// Server (Shefrah1.java)
import java.io.*;
import java.net.*;
import java.util.*;

public class Shefrah1 {
    private static List<ClientHandler> waitingRoom = Collections.synchronizedList(new ArrayList<>());
    private static List<String> waitingPlayers = Collections.synchronizedList(new ArrayList<>());
    private static int countdown = 60;
    private static boolean timerRunning = false;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("Server started. Waiting for player connections...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Player connected.");
            
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            waitingRoom.add(clientHandler);
            new Thread(clientHandler).start();

            checkAndStartGame();
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
                    }
                    sendPlayersList();
                }
            } catch (IOException e) {
                System.out.println("Player " + playerName + " disconnected.");
            } finally {
                removePlayer();
            }
        }

        private void removePlayer() {
            waitingRoom.remove(this);
            waitingPlayers.remove(playerName);
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
        for (ClientHandler client : waitingRoom) {
            names.add(client.playerName);
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
        if (waitingPlayers.size() == 2 && !timerRunning) {
            timerRunning = true;
            new Thread(() -> {
                while (countdown > 0 && waitingPlayers.size() < 4) {
                    broadcastMessage("Timer:" + countdown);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    countdown--;
                }
                System.out.println("Game Start!");
                broadcastMessage("GameStart");
                resetGameState();
            }).start();
        }
    }

    private static void checkAndStartGame() {
        if (waitingPlayers.size() >= 4) {
            countdown = 0;
            System.out.println("Game Start!");
            broadcastMessage("GameStart");
            resetGameState();
        }
    }

    private static void resetGameState() {
        countdown = 60;
        timerRunning = false;
        waitingPlayers.clear();
    }
}
