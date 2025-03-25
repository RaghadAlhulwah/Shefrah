import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ShClient1 extends JFrame {
    private static final Map<String, String> picMap = new HashMap<>();

    static {
        picMap.put("pic1", "/shefrah1/1.png");
        picMap.put("pic2", "/shefrah1/2.png");
        picMap.put("pic3", "/shefrah1/3.png");
        picMap.put("pic4", "/shefrah1/4.png");
        picMap.put("pic5", "/shefrah2/imgSh5.png");
        picMap.put("pic6", "/shefrah2/imgSh6.png");
        picMap.put("pic7", "/shefrah2/imgSh7.png");
        picMap.put("pic8", "/shefrah2/imgSh8.png");
        picMap.put("pic9", "/shefrah2/imgSh9.png");
        picMap.put("pic10", "/shefrah2/imgSh10.png");
        picMap.put("pic11", "/shefrah2/imgSh11.png");
        picMap.put("pic12", "/shefrah2/imgSh12.png");
        picMap.put("pic13", "/shefrah2/imgSh13.png");
        picMap.put("pic14", "/shefrah2/imgSh14.png");
        picMap.put("pic15", "/shefrah2/imgSh15.png");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NameInputFrame::new);
    }

    // Name Input Frame
    static class NameInputFrame extends JFrame {
        private JTextField nameField;
        private JButton submitButton;

        public NameInputFrame() {
            setTitle("شفرة");
            setSize(300, 150);
            setLayout(new BorderLayout());
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JLabel prompt = new JLabel("أدخل اسمك:", SwingConstants.CENTER);
            nameField = new JTextField(15);

            submitButton = new JButton("انضم");
            submitButton.addActionListener(e -> submitName());

            JPanel inputPanel = new JPanel();
            inputPanel.add(nameField);

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(submitButton);

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
                new ShClient1(socket, playerName).setVisible(true);
                this.dispose();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "خطأ في الاتصال بالسيرفر", "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Client Main Class
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;

    public ShClient1(Socket socket, String playerName) throws IOException {
        this.socket = socket;
        this.playerName = playerName;

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        setTitle("شفرة - " + playerName);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("اللاعبون المتصلون", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JTextArea connectedPlayers = new JTextArea(20, 50);
        connectedPlayers.setEditable(false);

        JButton playButton = new JButton("جاهز");

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

        new Thread(() -> readServerMessages(connectedPlayers)).start();
    }

    private void readServerMessages(JTextArea connectedPlayers) {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                System.out.println("Server: " + serverMessage);
                if (serverMessage.startsWith("Players:")) {
                    updateConnectedPlayers(connectedPlayers, serverMessage.substring(8).split(","));
                } else if (serverMessage.startsWith("WaitingPlayers:")) {
                    updateWaitingPlayers(serverMessage.substring(15).split(","));
                } else if (serverMessage.startsWith("Timer:")) {
                    updateTimer(Integer.parseInt(serverMessage.substring(6)));
                } else if (serverMessage.startsWith("GameStart:")) {
                    openGameStartFrame(serverMessage.substring(10));
                } else if (serverMessage.startsWith("ScoreUpdate:")) {
                    String[] parts = serverMessage.substring(12).split(":");
                    String player = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    updateScore(player, score);
                } else if (serverMessage.equals("GameOver")) {
                    JOptionPane.showMessageDialog(this, "اللعبة انتهت!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateConnectedPlayers(JTextArea connectedPlayers, String[] players) {
        SwingUtilities.invokeLater(() -> {
            connectedPlayers.setText("");
            for (String player : players) {
                if (!player.isEmpty()) {
                    connectedPlayers.append(player + "\n");
                }
            }
        });
    }

    private void updateWaitingPlayers(String[] players) {
        SwingUtilities.invokeLater(() -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof ReadyPlayersFrame) {
                    ((ReadyPlayersFrame) window).updateReadyPlayers(players);
                }
            }
        });
    }

    private void updateTimer(int timeLeft) {
        SwingUtilities.invokeLater(() -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof ReadyPlayersFrame) {
                    ((ReadyPlayersFrame) window).updateTimer(timeLeft);
                }
            }
        });
    }

    private void updateScore(String player, int score) {
        SwingUtilities.invokeLater(() -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof GameStartFrame) {
                    ((GameStartFrame) window).updateScore(player, score);
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
    }

    private void openGameStartFrame(String imageName) {
        SwingUtilities.invokeLater(() -> {
            new GameStartFrame(socket, imageName, playerName).setVisible(true);
            for (Window window : Window.getWindows()) {
                if (window instanceof ReadyPlayersFrame) {
                    window.dispose();
                }
            }
        });
    }

    // Ready Players Frame
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

            timerLabel = new JLabel("الوقت المتبقي: 30 ثانية", SwingConstants.CENTER);
            timerLabel.setFont(new Font("Arial", Font.BOLD, 18));

            add(new JScrollPane(readyPlayersArea), BorderLayout.CENTER);
            add(timerLabel, BorderLayout.SOUTH);
        }

        public void updateReadyPlayers(String[] players) {
            SwingUtilities.invokeLater(() -> {
                readyPlayersArea.setText("اللاعبون الجاهزون:\n");
                for (String player : players) {
                    if (!player.isEmpty()) {
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

    // Game Start Frame
    static class GameStartFrame extends JFrame {
    private JLabel displayField;
    private JLabel scoreLabel;
    private JTextField textField;
    private JButton submitButton;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;
    private int score = 0;

    public GameStartFrame(Socket socket, String imageName, String playerName) {
        this.playerName = playerName;
        setTitle("شفرة - بدء اللعبة");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Score Label
        scoreLabel = new JLabel("نقاطك: 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(scoreLabel, BorderLayout.NORTH);

        // Image Display
        displayField = new JLabel();
        updateImage(imageName);
        add(displayField, BorderLayout.CENTER);

        // Input Panel
        JPanel inputPanel = new JPanel();
        textField = new JTextField(20);
        submitButton = new JButton("إرسال");
        inputPanel.add(textField);
        inputPanel.add(submitButton);
        add(inputPanel, BorderLayout.SOUTH);

        // Set up socket communication
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Submit Button Action
        submitButton.addActionListener(e -> {
            String answer = textField.getText().trim();
            if (!answer.isEmpty()) {
                out.println("answer:" + answer);
                textField.setText(""); // Clear the text field after submission
            }
        });

        // Start a thread to listen for server messages
        new Thread(this::listenForServerMessages).start();
    }

    private void updateImage(String imageName) {
        SwingUtilities.invokeLater(() -> {
            String path = picMap.get(imageName);
            if (path != null) {
                ImageIcon icon = new ImageIcon(getClass().getResource(path));
                Image scaledImage = icon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH);
                displayField.setIcon(new ImageIcon(scaledImage));
            } else {
                displayField.setText("الصورة غير متوفرة!");
            }
        });
    }

    private void listenForServerMessages() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                System.out.println("Server: " + serverMessage);
                if (serverMessage.startsWith("NextRound:")) {
                    String nextImageName = serverMessage.substring(10);
                    updateImage(nextImageName);
                } else if (serverMessage.startsWith("ScoreUpdate:")) {
                    String[] parts = serverMessage.substring(12).split(":");
                    String player = parts[0];
                    int newScore = Integer.parseInt(parts[1]);
                    updateScore(player, newScore); // Update the score
                } else if (serverMessage.equals("GameOver")) {
                    JOptionPane.showMessageDialog(this, "اللعبة انتهت! نقاطك النهائية: " + score, "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                } else if (serverMessage.equals("Incorrect")) {
                    showIncorrectFeedback();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateScore(String player, int score) {
        SwingUtilities.invokeLater(() -> {
            if (player.equals(playerName)) {
                this.score = score;
                scoreLabel.setText("نقاطك: " + score);
            }
        });
    }

    private void showIncorrectFeedback() {
        SwingUtilities.invokeLater(() -> {
            JLabel feedbackLabel = new JLabel("إجابة خاطئة!", SwingConstants.CENTER);
            feedbackLabel.setFont(new Font("Arial", Font.BOLD, 24));
            feedbackLabel.setForeground(Color.RED);
            JPanel feedbackPanel = new JPanel(new BorderLayout());
            feedbackPanel.add(feedbackLabel, BorderLayout.CENTER);
            add(feedbackPanel, BorderLayout.NORTH);
            revalidate();
            repaint();

            // Use javax.swing.Timer for the feedback
            new javax.swing.Timer(1500, e -> {
                remove(feedbackPanel);
                revalidate();
                repaint();
            }).start();
        });
    }
}
}
