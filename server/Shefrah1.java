import java.io.*;
import java.net.*;
import java.util.*;

public class Shefrah1 {
    private static ArrayList<ClientHandler> waitingRoom = new ArrayList<>();  //array list to handle connected clients 
    private static int playCount = 0;  //variable to count players that 
    private static boolean gameStarted = false;
    private static Timer gameTimer;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(3280);
        System.out.println("Server started...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            waitingRoom.add(clientHandler);
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
                        handlePlayRequest();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnections();
                waitingRoom.remove(this);
                sendPlayersList();
            }
        }

        private void handlePlayRequest() {
            if (gameStarted) return; // Ignore if game already started

            playCount++;
            System.out.println("Play button pressed by " + playerName + " | Total plays: " + playCount);

            if (playCount == 2) {
                startGameTimer();
            } else if (playCount >= 3) {
                startGameNow();
            }
        }

        private void startGameTimer() {
            if (gameTimer != null) {
                gameTimer.cancel();
            }
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
            if (gameStarted) return; // Prevent duplicate start
            gameStarted = true;
            System.out.println("Game started!");
            broadcastMessage("Game Start");
        }

        private void sendPlayersList() {
            StringBuilder playersList = new StringBuilder("Players:");
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

        private void broadcastMessage(String message) {
            for (ClientHandler client : waitingRoom) {
                client.out.println(message);
            }
        }

        private void closeConnections() {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
            }
        }
    }
}
