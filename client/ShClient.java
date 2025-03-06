// Client (ShClient.java)
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

        playButton = new JButton("انا جاهز");

        add(title, BorderLayout.NORTH);
        add(new JScrollPane(connectedPlayers), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(playButton);
        add(buttonPanel, BorderLayout.SOUTH);

        out.println(playerName);

        playButton.addActionListener(e -> {
            sendPlayCommand();
            openWaitingRoom();
        });

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
                    String playersList = serverMessage.substring(8);
                    updateConnectedPlayers(playersList.split(","));
                } else if (serverMessage.startsWith("WaitingPlayers:")) {
                    String waitingList = serverMessage.substring(15);
                    updateWaitingPlayers(waitingList.split(","));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateConnectedPlayers(String[] players) {
        SwingUtilities.invokeLater(() -> {
            connectedPlayers.setText("");
            for (String player : players) {
                if (player != null && !player.isEmpty()) {
                    connectedPlayers.append(player + "\n");
                }
            }
        });
    }

    private void updateWaitingPlayers(String[] players) {
        SwingUtilities.invokeLater(() -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof WaitingRoom) {
                    WaitingRoom waitingRoom = (WaitingRoom) window;
                    waitingRoom.updateWaitingPlayersArea(players);
                }
            }
        });
    }

    private void openWaitingRoom() {
        SwingUtilities.invokeLater(() -> {
            WaitingRoom waitingRoomFrame = new WaitingRoom(playerName, new String[0]);
            waitingRoomFrame.setVisible(true);
            this.setVisible(false);
        });
    }

    private static class NameInputFrame extends JFrame {
        private JTextField nameField;
        private JButton okButton;

        public NameInputFrame() {
            setTitle("ادخل اسمك");
            setSize(300, 150);
            setLayout(new BorderLayout());
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

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
                Socket socket = new Socket("localhost", 1234);
                ShClient client = new ShClient(socket, playerName);
                client.setVisible(true);
                this.dispose();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "خطأ في الاتصال بالخادم", "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class WaitingRoom extends JFrame {
        private JTextArea waitingPlayersArea;

        public WaitingRoom(String playerName, String[] waitingPlayers) {
            setTitle("غرفة الانتظار - " + playerName);
            setSize(400, 300);
            setLayout(new BorderLayout());
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JPanel waitingPanel = new JPanel(new BorderLayout());
            waitingPlayersArea = new JTextArea();
            waitingPlayersArea.setEditable(false);

            updateWaitingPlayersArea(waitingPlayers);

            waitingPanel.add(new JScrollPane(waitingPlayersArea), BorderLayout.CENTER);

            add(waitingPanel, BorderLayout.CENTER);
        }

        public void updateWaitingPlayersArea(String[] waitingPlayers) {
            StringBuilder playersList = new StringBuilder("اللاعبون المنتظرون:\n");
            for (String player : waitingPlayers) {
                if (player != null && !player.isEmpty()) {
                    playersList.append(player).append("\n");
                }
            }
            waitingPlayersArea.setText(playersList.toString());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NameInputFrame());
    }
}