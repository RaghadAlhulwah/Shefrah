import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ShClient1 extends JFrame {
    private static final Map<String, String> picMap = new HashMap<>();

    static {
        picMap.put("pic1", "imgSh.png");
        picMap.put("pic2", "imgSh2.png");
        picMap.put("pic3", "imgSh3.png");
        picMap.put("pic4", "imgSh4.png");
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
        setSize(800, 600);
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
                } else if (serverMessage.startsWith("SCORES:")) {
                    updateScoreboard(serverMessage.substring(7));
                } else if (serverMessage.startsWith("NextRound:")) {
                    updateCurrentImage(serverMessage.substring(10));
                } else if (serverMessage.startsWith("GameOver:")) {
                    showGameOver(serverMessage.substring(9));
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

    private void updateScoreboard(String scoresData) {
        SwingUtilities.invokeLater(() -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof GameStartFrame) {
                    ((GameStartFrame) window).updateScoreboard(scoresData);
                }
            }
        });
    }

    private void updateCurrentImage(String imageName) {
        SwingUtilities.invokeLater(() -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof GameStartFrame) {
                    ((GameStartFrame) window).updateImage(imageName);
                }
            }
        });
    }

    private void showGameOver(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
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
            new GameStartFrame(socket, imageName, playerName, out).setVisible(true);
            for (Window window : Window.getWindows()) {
                if (window instanceof ReadyPlayersFrame) {
                    window.dispose();
                }
            }
        });
    }

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

    static class GameStartFrame extends JFrame {
        private JLabel displayField;
        private JTextField textField;
        private JButton submitButton;
        private PrintWriter out;
        private String playerName;
        private JPanel scoreboardPanel;
        private JLabel[] playerScoreLabels = new JLabel[4];

        public GameStartFrame(Socket socket, String imageName, String playerName, PrintWriter out) {
            this.playerName = playerName;
            this.out = out;
            
            setTitle("شفرة - بدء اللعبة - " + playerName);
            setSize(800, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());

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

            // Initialize Scoreboard
            initScoreboard();

            // Submit Button Action
            submitButton.addActionListener(e -> {
                String answer = textField.getText().trim();
                if (!answer.isEmpty()) {
                    out.println("answer:" + answer);
                    textField.setText("");
                }
            });
        }

        private void initScoreboard() {
            scoreboardPanel = new JPanel();
            scoreboardPanel.setLayout(new BoxLayout(scoreboardPanel, BoxLayout.Y_AXIS));
            scoreboardPanel.setBorder(BorderFactory.createTitledBorder("نتائج اللاعبين"));
            scoreboardPanel.setPreferredSize(new Dimension(200, 150));

            for (int i = 0; i < 4; i++) {
                playerScoreLabels[i] = new JLabel("...: 0");
                playerScoreLabels[i].setFont(new Font("Arial", Font.PLAIN, 14));
                playerScoreLabels[i].setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                scoreboardPanel.add(playerScoreLabels[i]);
            }

            add(scoreboardPanel, BorderLayout.EAST);
        }

        public void updateImage(String imageName) {
            SwingUtilities.invokeLater(() -> {
                String path = picMap.get(imageName);
                if (path != null) {
                    ImageIcon icon = new ImageIcon(getClass().getResource(path));
                    if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                        Image scaledImage = icon.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH);
                        displayField.setIcon(new ImageIcon(scaledImage));
                    } else {
                        displayField.setText("الصورة غير متوفرة!");
                    }
                } else {
                    displayField.setText("الصورة غير معروفة!");
                }
            });
        }

        public void updateScoreboard(String scoresData) {
            SwingUtilities.invokeLater(() -> {
                // Clear existing
                for (JLabel label : playerScoreLabels) {
                    label.setText("...: 0");
                }

                // Parse and update
                String[] players = scoresData.split(",");
                for (int i = 0; i < Math.min(players.length, 4); i++) {
                    String[] parts = players[i].split(":");
                    if (parts.length == 2) {
                        String name = parts[0];
                        String score = parts[1];
                        
                        String displayText = name + ": " + score + " نقطة";
                        playerScoreLabels[i].setText(displayText);
                        
                        if (name.equals(playerName)) {
                            playerScoreLabels[i].setFont(new Font("Arial", Font.BOLD, 14));
                        } else {
                            playerScoreLabels[i].setFont(new Font("Arial", Font.PLAIN, 14));
                        }
                    }
                }
            });
        }
    }
}