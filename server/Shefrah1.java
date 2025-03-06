// Server (Shefrah1.java)
import java.io.*;
import java.net.*;
import java.util.*;

public class Shefrah1 {
    private static ArrayList<ClientHandler> waitingRoom = new ArrayList<>();
    private static ArrayList<String> waitingPlayers = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);

        while (true) {
            System.out.println("Waiting for player connection...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Player connected");

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
                broadcastWaitingPlayers();

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Player says: " + message);
                    if (message.equals("play")) {
                        if (!waitingPlayers.contains(playerName)) {
                            waitingPlayers.add(playerName);
                        }
                        broadcastWaitingPlayers();
                    }
                    sendPlayersList();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnections();
                waitingRoom.remove(this);
                waitingPlayers.remove(playerName);
                sendPlayersList();
                broadcastWaitingPlayers();
            }
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

        private void broadcastWaitingPlayers() {
            StringBuilder waitingList = new StringBuilder("WaitingPlayers:");
            for (String player : waitingPlayers) {
                waitingList.append(player).append(",");
            }
            if (waitingList.length() > 0) {
                waitingList.setLength(waitingList.length() - 1);
            }
            for (ClientHandler client : waitingRoom) {
                client.out.println(waitingList.toString());
            }
        }

        private void closeConnections() {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}