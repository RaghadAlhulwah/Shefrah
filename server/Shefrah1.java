import java.io.*;
import java.net.*;
import java.util.*;

public class Shefrah1 {
    
    private static ArrayList<ClientHandler> waitingRoom = new ArrayList<>();  //array list to handle connected players 
    private static ArrayList<ClientHandler> ReadyPlayers = new ArrayList<>();
    private static int playersCount = 0;                                         //Variable to count the number of players ready to play
    private static boolean gameStarted = false;                               //variable to handle game state 
    private static Timer gameTimer;                                           // timer 

    // ************** server main ************** //
    
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(3280);                  //creating socket with port # 3280
        System.out.println("Server started...");

        while (true) {
            Socket clientSocket = serverSocket.accept();                     //accepting client connection
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            waitingRoom.add(clientHandler);
            ReadyPlayers.add(clientHandler);
            new Thread(clientHandler).start();
        }
    }

    // ************** ClientHandler ************** //
    
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String playerName;

        public ClientHandler(Socket socket) throws IOException {                        // this method to handle client I/O 
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

        private void startPlayRoom() {
    if (gameStarted) return;

    playersCount++;
    System.out.println("Play button pressed by " + playerName + " | Total players: " + playersCount);

    if (!ReadyPlayers.contains(this)) {
        ReadyPlayers.add(this);
    }

    sendReadyPlayersList(); // Send ready players list update

    if (playersCount == 2) {
        startGameTimer();
    } else if (playersCount == 3) {
        startGameNow();
    }
}

        private void startGameTimer() {                             //
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
        
        private void sendReadyPlayersList() {
            StringBuilder ReadyplayersList = new StringBuilder("Players:");
            for (ClientHandler client : ReadyPlayers) {
                ReadyplayersList.append(client.playerName).append(",");
            }
            if (ReadyplayersList.length() > 0) {
                ReadyplayersList.setLength(ReadyplayersList.length() - 1);
            }
            for (ClientHandler client : waitingRoom) {
    client.out.println(ReadyPlayers.toString());
}

        }

        private void broadcastMessage(String message) {
            for (ClientHandler client : waitingRoom) {
                client.out.println(message);
            }
        }
        
        private void broadcastMessage2(String message) {
            for (ClientHandler client : ReadyPlayers) {
                client.out.println(message);
            }
        }

        private void closeConnections() {
            System.out.println("Player "+playerName+" left the game!");
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
            }
        }
    }}

        
