import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ShClient1 extends JFrame {
    private JTextArea connectedPlayers; 
    private JButton playButton; 
    private Socket socket; 
    private PrintWriter out; 
    private BufferedReader in; 
    private String playerName;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NameInputFrame::new);
    }

    public ShClient1(Socket clientSocket, String playerName) throws IOException {
        this.socket = clientSocket;
        this.playerName = playerName;

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        setTitle("شفرة");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("اللاعبون المنتظرون", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        connectedPlayers = new JTextArea(20, 50);
        connectedPlayers.setEditable(false);

        playButton = new JButton("جاهز");

        add(title, BorderLayout.NORTH);
        add(new JScrollPane(connectedPlayers), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(playButton);
        add(buttonPanel, BorderLayout.SOUTH);

        out.println(playerName);

        playButton.addActionListener(e -> {
            out.println("play");
            openReadyPlayersFrame();
        });

        new Thread(this::readServerMessages).start();
    }

    private void readServerMessages() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                System.out.println("السيرفر: " + serverMessage);
                if (serverMessage.startsWith("Players:")) {
                    String playersList = serverMessage.substring(8);
                    updateConnectedPlayers(playersList.split(","));
                } else if (serverMessage.startsWith("WaitingPlayers:")) {
                    String waitingList = serverMessage.substring(15);
                    updateWaitingPlayers(waitingList.split(","));
                } else if (serverMessage.startsWith("Timer:")) {
                    int timeLeft = Integer.parseInt(serverMessage.substring(6));
                    updateTimer(timeLeft);
                } else if (serverMessage.equals("GameStart")) {
                    openGameStartFrame();
                } else if (serverMessage.equals("ClosePreviousFrames")) {
                    closePreviousFrames();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closePreviousFrames() {
        SwingUtilities.invokeLater(() -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof ReadyPlayersFrame) {
                    window.dispose();
                }
            }
        });
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
                if (window instanceof ReadyPlayersFrame) {
                    ReadyPlayersFrame readyPlayersFrame = (ReadyPlayersFrame) window;
                    readyPlayersFrame.updateReadyPlayers(players);
                }
            }
        });
    }

    private void updateTimer(int timeLeft) {
        SwingUtilities.invokeLater(() -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof ReadyPlayersFrame) {
                    ReadyPlayersFrame readyPlayersFrame = (ReadyPlayersFrame) window;
                    readyPlayersFrame.updateTimer(timeLeft);
                }
            }
        });
    }

    private void openReadyPlayersFrame() {
        SwingUtilities.invokeLater(() -> {
            ReadyPlayersFrame readyPlayersFrame = new ReadyPlayersFrame(playerName);
            readyPlayersFrame.setVisible(true);
            this.setVisible(false);
        });
        this.dispose();
    }

    private void openGameStartFrame() {
        SwingUtilities.invokeLater(GameStartFrame::new);
    }

    // الفئة الخاصة بإدخال الاسم
    static class NameInputFrame extends JFrame {
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
                Socket socket = new Socket("localhost", 3280);
                ShClient1 client = new ShClient1(socket, playerName);
                client.setVisible(true);
                this.dispose();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "خطأ في الاتصال بالسيرفر", "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // الفئة الخاصة باللاعبين الجاهزين
    static class ReadyPlayersFrame extends JFrame {
        private JTextArea readyPlayersArea; 
        private JLabel timerLabel;

        public ReadyPlayersFrame(String playerName) {
            setTitle("اللاعبون الجاهزون - " + playerName);
            setSize(400, 300);
            setLayout(new BorderLayout());
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            readyPlayersArea = new JTextArea();
            readyPlayersArea.setEditable(false);
            readyPlayersArea.setText("اللاعبون الجاهزون:\n" + playerName);

            timerLabel = new JLabel("الوقت المتبقي: 30 ثانية", SwingConstants.CENTER);
            timerLabel.setFont(new Font("Arial", Font.BOLD, 18));

            add(new JScrollPane(readyPlayersArea), BorderLayout.CENTER);
            add(timerLabel, BorderLayout.SOUTH);
        }

        public void updateReadyPlayers(String[] players) {
            SwingUtilities.invokeLater(() -> {
                readyPlayersArea.setText("اللاعبون الجاهزون:\n");
                for (String player : players) {
                    if (player != null && !player.isEmpty()) {
                        readyPlayersArea.append(player + "\n");
                    }
                }
            });
        }

        public void updateTimer(int timeLeft) {
            SwingUtilities.invokeLater(() -> {
                timerLabel.setText("الوقت المتبقي: " + timeLeft + " ثانية");
            });
        }
    }

    // الفئة الخاصة ببدء اللعبة
    static class GameStartFrame extends JFrame {
        public GameStartFrame() {
            setTitle("بدء اللعبة");
            setSize(600, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());

            JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

            try {
                ImageIcon originalImage = new ImageIcon(getClass().getResource("/shefrah2/imgSh.png"));
                Image scaledImage = originalImage.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH);
                JLabel displayField = new JLabel(new ImageIcon(scaledImage));
                imagePanel.add(displayField);
            } catch (Exception e) {
                imagePanel.add(new JLabel("الصورة غير موجودة!", SwingConstants.CENTER));
            }

            JTextField textField = new JTextField(20);
            JButton submitButton = new JButton("إرسال");

            submitButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "لقد أدخلت: " + textField.getText()));

            inputPanel.add(textField);
            inputPanel.add(submitButton);

            add(imagePanel, BorderLayout.CENTER);
            add(inputPanel, BorderLayout.SOUTH);
            setVisible(true);
        }
    }
}
