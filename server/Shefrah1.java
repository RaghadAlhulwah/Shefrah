import java.io.*;
import java.net.*;
import java.util.*;

public class Shefrah1 {
    private static ArrayList<ClientHandler> waitingRoom = new ArrayList<>();

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

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Player says: " + message);
                    if (message.equals("play")) {
                        broadcastMessage("Game has started!");
                    }
                    sendPlayersList();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnections();
                waitingRoom.remove(this);
                sendPlayersList();
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
                e.printStackTrace();
            }
        }
    }
}
