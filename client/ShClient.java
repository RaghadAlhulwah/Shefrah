import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ShClient extends JFrame {
    private JTextArea connectedPlayers;
    private JButton playButton;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;

    public ShClient(Socket clientSocket) throws IOException {
        socket = clientSocket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        setTitle("شفرة");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("اللاعبون المتصلون", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        connectedPlayers = new JTextArea(20, 50);
        connectedPlayers.setEditable(false);

        playButton = new JButton("انطلق");

        add(title, BorderLayout.NORTH);
        add(connectedPlayers, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(playButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Ask for player's name in Arabic
        playerName = JOptionPane.showInputDialog(this, "أدخل اسمك:", "اسم اللاعب", JOptionPane.PLAIN_MESSAGE);
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "اللاعب " + (int) (Math.random() * 1000);  // Default name if none entered
        }

        // Send the player's name to the server
        out.println(playerName);

        // Play button listener
        playButton.addActionListener(e -> sendPlayCommand());

        // Start a thread to read messages from the server
        new Thread(this::readServerMessages).start();
    }

    private void sendPlayCommand() {
        out.println("play");
    }

    private void readServerMessages() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                System.out.println("Server: " + serverMessage);
                if (serverMessage.startsWith("Players:")) {
                    String playersList = serverMessage.substring(8); // Extract player list
                    updateConnectedPlayers(playersList.split(","));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateConnectedPlayers(String[] players) {
        connectedPlayers.setText("");
        for (String player : players) {
            if (player != null && !player.isEmpty()) {
                connectedPlayers.append(player + "\n");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Socket socket = new Socket("localhost", 1234);
                ShClient client = new ShClient(socket);
                client.setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
