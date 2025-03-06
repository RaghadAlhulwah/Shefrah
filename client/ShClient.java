import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ShClient1 {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NameFrame().setVisible(true));
    }

    // ********** First Frame: Name input ********** //
    public static class NameFrame extends JFrame {

        private JTextField nameField;
        private JButton okButton;

        public NameFrame() {
            setTitle("شفرة");
            setSize(600, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());

            JLabel lbl1EnterName = new JLabel("أدخل اسمك:", SwingConstants.CENTER);
            nameField = new JTextField(15);
            okButton = new JButton("موافق");

            // Button to submit the name
            okButton.addActionListener(e -> submitName());

            JPanel inputPanel = new JPanel();
            inputPanel.add(nameField);

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);

            add(lbl1EnterName, BorderLayout.NORTH);
            add(inputPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        private void submitName() {
            String pName = nameField.getText().trim();

            if (pName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "يرجى إدخال اسم صحيح!", "خطأ", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Socket socket = new Socket("localhost", 3280);
                // Move to connected players frame
                new ConnectedPlayersFrame(socket, pName).setVisible(true);
                this.dispose();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "خطأ في الاتصال بالخادم", "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ********** Second Frame: Connected players ********** //
    public static class ConnectedPlayersFrame extends JFrame {

        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private JTextArea connectedPlayers;
        private JButton playButton;
        private String playerName;

        public ConnectedPlayersFrame(Socket socket, String playerName) throws IOException {
            this.socket = socket;
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

            connectedPlayers = new JTextArea(10, 30);
            connectedPlayers.setEditable(false);

            playButton = new JButton("انطلق");
            playButton.addActionListener(e -> sendPlayCommand());

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(playButton);

            add(title, BorderLayout.NORTH);
            add(new JScrollPane(connectedPlayers), BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            // Send player name to server
            out.println(playerName);

            new Thread(this::readServerMessages).start();
        }

        private void sendPlayCommand() {
            out.println("play");
            playButton.setEnabled(false);

            SwingUtilities.invokeLater(() -> {
                try {
                    new ReadyPlayersFrame(socket, playerName).setVisible(true);
                    this.dispose();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        private void readServerMessages() {
            try {
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    System.out.println("Received in ConnectedPlayersFrame: " + serverMessage); //Debug line.
                    if (serverMessage.startsWith("Players:")) {
                        String playersList = serverMessage.substring(8);
                        updateConnectedPlayers(playersList.split(","));
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
                    if (!player.isEmpty()) {
                        connectedPlayers.append(player + "\n");
                    }
                }
            });
        }
    }
    

    // ********** Third Frame: Ready players ********** //
    public static class ReadyPlayersFrame extends JFrame {

        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private JTextArea readyPlayers;
        private String playerName;

        public ReadyPlayersFrame(Socket socket, String playerName) throws IOException {
            this.socket = socket;
            this.playerName = playerName;

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            setTitle("غرفة الانتظار");
            setSize(600, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());

            JLabel title = new JLabel("اللاعبون المستعدون", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 24));

            readyPlayers = new JTextArea(10, 30);
            readyPlayers.setEditable(false);

            JButton waitingMessage = new JButton("انتظر بدء اللعبة...");
            waitingMessage.setEnabled(false);

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(waitingMessage);

            add(title, BorderLayout.NORTH);
            add(new JScrollPane(readyPlayers), BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            out.println("Ready: " + playerName);

            new Thread(this::readServerMessages).start();
        }

        private void readServerMessages() {
            try {
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    System.out.println("Received: " + serverMessage); //Debug line.
                    if (serverMessage.startsWith("ReadyPlayers:")) {
                        String readyList = serverMessage.substring(13);
                        updateReadyPlayers(readyList.split(","));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void updateReadyPlayers(String[] players) {
            SwingUtilities.invokeLater(() -> {
                readyPlayers.setText("");
                if (players != null && players.length > 0 && !(players.length == 1 && players[0].trim().isEmpty())) {
                    for (String player : players) {
                        if (!player.trim().isEmpty()) {
                            readyPlayers.append(player.trim() + "\n");
                        }
                    }
                }
                readyPlayers.revalidate();
                readyPlayers.repaint();
            });
        }
    }
}
