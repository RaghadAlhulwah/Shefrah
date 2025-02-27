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

    // 888888888 Constructor for the main game window
    public ShClient(Socket clientSocket, String playerName) throws IOException {
        this.socket = clientSocket;
        this.playerName = playerName;

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
        add(new JScrollPane(connectedPlayers), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(playButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 888888888 Send the player's name to the server
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

    // 888888888 Separate frame for entering player name
    private static class NameInputFrame extends JFrame {
        private JTextField nameField;
        private JButton okButton;

        public NameInputFrame() {
            setTitle("ادخل اسمك");
            setSize(600, 300);
            setLayout(new BorderLayout());
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null); //يحط المكان الفريم بنص الشاشه بدل الزاوية

            JLabel prompt = new JLabel("أدخل اسمك:", SwingConstants.CENTER);
            nameField = new JTextField(15);

            okButton = new JButton("موافق");
            okButton.addActionListener(e -> submitName());

            JPanel inputPanel = new JPanel();
            inputPanel.add(nameField);

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);

            add(prompt, BorderLayout.NORTH);
            add(inputPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            setVisible(true);
        }

        private void submitName() {
            String playerName = nameField.getText().trim();
            if (playerName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "يرجى إدخال اسم", "خطأ", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Socket socket = new Socket("localhost", 3280);
                ShClient client = new ShClient(socket, playerName);
                client.setVisible(true);
                this.dispose(); // Close the name input window
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "خطأ في الاتصال بالخادم", "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 888888888 Main method to start the program
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NameInputFrame());
    }
}