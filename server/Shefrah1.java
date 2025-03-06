import java.io.*;
import java.net.*;
import java.util.*;

public class Shefrah1 {

    private static ArrayList<ClientHandler> waitingRoom = new ArrayList<>();
    private static ArrayList<ClientHandler> ReadyPlayers = new ArrayList<>();
    private static boolean gameStarted = false;
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
                System.out.println("Player connected: " + playerName);
                sendPlayersList();

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(playerName + " says: " + message);

                    if (message.equals("play")) {
                        startPlayRoom();
                    } else if (message.startsWith("Ready:")) {
                        addReadyPlayer(this);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnections();
                synchronized (waitingRoom) {
                    waitingRoom.remove(this);
                }
                synchronized (ReadyPlayers) {
                    ReadyPlayers.remove(this);
                }
                sendPlayersList();
                sendReadyPlayersList();
            }
        }

        private void startPlayRoom() {
            if (gameStarted) return;
            if (!ReadyPlayers.contains(this)) {
                synchronized (ReadyPlayers) {
                    ReadyPlayers.add(this);
                }
            }

            if (ReadyPlayers.size() == 2) {
                startGameTimer();
            } else if (ReadyPlayers.size() == 3) {
                startGameNow();
            }
        }

        private void startGameTimer() {
            if (gameTimer != null) gameTimer.cancel();
            System.out.println("Starting 20-second countdown...");
            gameTimer = new Timer();
            gameTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startGameNow();
                }
            }, 20000);
        }

        private void startGameNow() {
            if (gameStarted) return;
            gameStarted = true;
            System.out.println("Game started!");
            broadcastMessage("Game Start");
        }

        private void addReadyPlayer(ClientHandler player) {
            synchronized (ReadyPlayers) {
                if (!ReadyPlayers.contains(player)) {
                    ReadyPlayers.add(player);
                    sendReadyPlayersList();
                }
            }
        }

        private void sendPlayersList() {
            StringBuilder playersList = new StringBuilder("Players:");
            synchronized (waitingRoom) {
                for (ClientHandler client : waitingRoom) {
                    playersList.append(client.playerName).append(",");
                }
                if (playersList.length() > 0) {
                    playersList.setLength(playersList.length() - 1);
                }
                for (ClientHandler client : waitingRoom) {
                    client.out.println(playersList.toString());
                }
            }
        }

        private void sendReadyPlayersList() {
            StringBuilder readyPlayersList = new StringBuilder("ReadyPlayers:");
            synchronized (ReadyPlayers) {
                for (ClientHandler client : ReadyPlayers) {
                    readyPlayersList.append(client.playerName).append(",");
                }
                if (readyPlayersList.length() > 0) {
                    readyPlayersList.setLength(readyPlayersList.length() - 1);
                }

                for (ClientHandler client : ReadyPlayers) {
                    client.out.println(readyPlayersList.toString());
                }
            }
        }

        private void broadcastMessage(String message) {
            synchronized (waitingRoom) {
                for (ClientHandler client : waitingRoom) {
                    client.out.println(message);
                }
            }
        }

        private void closeConnections() {
            System.out.println("Player " + playerName + " left!");
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {}
            synchronized (waitingRoom) {
                waitingRoom.remove(this);
            }
            synchronized (ReadyPlayers) {
                ReadyPlayers.remove(this);
            }
            sendPlayersList();
            sendReadyPlayersList();
        }
    }
}
