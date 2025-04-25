import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Window;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.Timer;
import java.awt.FlowLayout;

public class ShClient1 extends JFrame {
    private static final Map<String, String> picMap = new HashMap<>();

    static {
        picMap.put("pic1", "/shefrah2/imgSh2.jpg");
        picMap.put("pic2", "/shefrah2/imgSh3.jpg");
        picMap.put("pic3", "/shefrah2/imgSh4.jpg");
        picMap.put("pic4", "/shefrah2/imgSh5.jpg");
        picMap.put("pic5", "/shefrah2/imgSh6.jpg");
        picMap.put("pic6", "/shefrah2/imgSh7.jpg");
        picMap.put("pic7", "/shefrah2/imgSh8.jpg");
        picMap.put("pic8", "/shefrah2/imgSh9.jpg");
        picMap.put("pic9", "/shefrah2/imgSh10.jpg");
        picMap.put("pic10", "/shefrah2/imgSh11.jpg");
        picMap.put("pic11", "/shefrah2/imgSh12.jpg");
        picMap.put("pic12", "/shefrah2/imgSh13.jpg");
        picMap.put("pic13", "/shefrah2/imgSh14.jpg");
        picMap.put("pic14", "/shefrah2/imgSh15.jpg");
    }

    private static JLabel totalGameTimerLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NameInputFrame::new);
    }

    static class NameInputFrame extends JFrame {
        private JTextField nameField;
        private JButton submitButton;
        private JLabel imgfield;
        private JPanel cPanel;
        private JPanel middlePanel;

        public NameInputFrame() {
            setTitle("شفرة");
            setSize(700, 600);
            setLayout(new BorderLayout());
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);            
         
            imgfield = new JLabel("", JLabel.CENTER);
            ImageIcon logo = new ImageIcon(getClass().getResource("/img/ShLOGO.png"));
            Image scaledImage = logo.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            imgfield.setIcon(new ImageIcon(scaledImage));
            
            cPanel = new JPanel(new BorderLayout());
            cPanel.add(imgfield, BorderLayout.CENTER);
            
            JLabel prompt = new JLabel("أدخل اسمك:", SwingConstants.CENTER);
            prompt.setFont(new Font("Arial", Font.BOLD, 20));
            nameField = new JTextField(15);

            submitButton = new JButton("انضم");
            submitButton.addActionListener(e -> submitName());

            JPanel inputPanel = new JPanel();
            inputPanel.add(nameField);

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(submitButton);
            
            middlePanel = new JPanel();
            middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));  
            middlePanel.add(Box.createVerticalStrut(50));
            JPanel promptPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));  
            promptPanel.add(prompt);
            middlePanel.add(promptPanel);
            middlePanel.add(Box.createVerticalStrut(20));
            middlePanel.add(inputPanel);
            
            add(cPanel, BorderLayout.PAGE_START);
            add(middlePanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.PAGE_END);

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
                JOptionPane.showMessageDialog(this, "خطأ في الاتصال بالخادم", "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;
    private final JButton playButton;

    public ShClient1(Socket socket, String playerName) throws IOException {
        this.socket = socket;
        this.playerName = playerName;

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        setTitle("شفرة - " + playerName);
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("اللاعبون المتصلون", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JTextArea connectedPlayers = new JTextArea(10,50);
        connectedPlayers.setEditable(false);

        playButton = new JButton("جاهز");

        add(title, BorderLayout.NORTH);
        add(new JScrollPane(connectedPlayers), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(playButton);
        add(buttonPanel, BorderLayout.SOUTH);

        out.println(playerName);
        
        playButton.addActionListener(e -> {
            playButton.setEnabled(false);
            out.println("play");
            openReadyPlayersFrame();
        });

        new Thread(() -> readServerMessages(connectedPlayers)).start();
    }
   
    private void readServerMessages(JTextArea connectedPlayers) {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                System.out.println("Received: " + serverMessage);
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
                } else if (serverMessage.startsWith("TotalGameTimer:")) {
                    updateTotalGameTimer(Integer.parseInt(serverMessage.substring(15)));
                } else if (serverMessage.startsWith("WrongAnswer")) {
                    for (Window window : Window.getWindows()) {
                        if (window instanceof GameStartFrame) {
                            ((GameStartFrame) window).showErrorMessage("إجابة خاطئة");
                        }
                    }
                } else if (serverMessage.startsWith("Warning:")) {
                    updateTotalGameTimer(30);
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
            Set<String> uniquePlayers = new LinkedHashSet<>(Arrays.asList(players));
            for (Window window : Window.getWindows()) {
                if (window instanceof ReadyPlayersFrame) {
                    ((ReadyPlayersFrame) window).updateReadyPlayers(
                        uniquePlayers.toArray(new String[0])
                    );
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
            System.out.println("GameOver message: " + message);
            if (!message.contains("Final scores:") && !message.contains("No scores available")) {
                System.out.println("Skipping GameOver message without scores");
                return;
            }
            
            Map<String, Integer> finalScores = new HashMap<>();
            
            if (message.toLowerCase().contains("final scores:")) {
                String scoresPart = message.substring(message.indexOf("Final scores:") + 13).trim();
                System.out.println("Scores part: " + scoresPart);
                if (!scoresPart.isEmpty()) {
                    String[] playerEntries = scoresPart.split(",");
                    for (String entry : playerEntries) {
                        if (entry.trim().isEmpty()) {
                            System.out.println("Skipping empty entry");
                            continue;
                        }
                        int lastColonIndex = entry.lastIndexOf(":");
                        if (lastColonIndex <= 0 || lastColonIndex == entry.length() - 1) {
                            System.out.println("Invalid entry format: " + entry);
                            continue;
                        }
                        String playerName = entry.substring(0, lastColonIndex).trim();
                        String scoreStr = entry.substring(lastColonIndex + 1).trim();
                        if (playerName.isEmpty() || scoreStr.isEmpty()) {
                            System.out.println("Empty player name or score in entry: " + entry);
                            continue;
                        }
                        try {
                            int score = Integer.parseInt(scoreStr);
                            finalScores.put(playerName, score);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid score format for entry: " + entry);
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("Scores part is empty");
                }
            } else if (message.contains("No scores available")) {
                System.out.println("No scores available in message");
            } else {
                System.out.println("No 'Final scores:' found in message");
            }

            System.out.println("Parsed finalScores: " + finalScores);
            
            if (!finalScores.isEmpty() || message.contains("No scores available")) {
                for (Window window : Window.getWindows()) {
                    window.dispose();
                }
                new WinnerFrame(finalScores).setVisible(true);
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
            GameStartFrame frame = new GameStartFrame(socket, imageName, playerName, out);
            frame.setVisible(true);
            
            String playersList = getPlayerListFromServer();
            frame.updateScoreboard("SCORES:" + createInitialScores(playersList));
            
            for (Window window : Window.getWindows()) {
                if (window instanceof ReadyPlayersFrame) {
                    window.dispose();
                }
            }
        });
    }
    
    private String createInitialScores(String playersList) {
        StringBuilder scores = new StringBuilder();
        String[] players = playersList.split(",");
        
        for (String player : players) {
            if (!player.trim().isEmpty()) {
                scores.append(player).append(":0,");
            }
        }
        
        if (scores.length() > 0) {
            scores.setLength(scores.length() - 1);
        }
        
        return scores.toString();
    }

    private String getPlayerListFromServer() {
        try {
            out.println("GET_PLAYERS");
            String response = in.readLine();
            if (response != null && response.startsWith("PLAYERS:")) {
                return response.substring(8);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return playerName;
    }

    static class ReadyPlayersFrame extends JFrame {
        private JTextArea readyPlayersArea;
        private JLabel timerLabel;

        public ReadyPlayersFrame(String playerName) {
            setTitle("اللاعبون الجاهزون - " + playerName);
            setSize(700, 600);
            setLayout(new BorderLayout());
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            
            JLabel title = new JLabel("اللاعبون الجاهزون", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 24));

            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

            readyPlayersArea = new JTextArea(15,30);
            readyPlayersArea.setFont(new Font("Arial", Font.PLAIN, 16));
            readyPlayersArea.setEditable(false);
            
            JScrollPane scrollPane = new JScrollPane(readyPlayersArea);
            centerPanel.add(scrollPane);

            timerLabel = new JLabel("الوقت المتبقي: 30 ثانية", SwingConstants.CENTER);
            timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
            
            add(title, BorderLayout.NORTH);
            add(centerPanel, BorderLayout.CENTER);
            add(timerLabel, BorderLayout.SOUTH);
        }

        public void updateReadyPlayers(String[] players) {
            SwingUtilities.invokeLater(() -> {
                readyPlayersArea.setText("");
                Set<String> uniquePlayers = new LinkedHashSet<>(Arrays.asList(players));
                uniquePlayers.forEach(player -> {
                    if (!player.isEmpty()) {
                        readyPlayersArea.append(player + "\n");
                    }
                });
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
        private JLabel[] playerScoreLabels = new JLabel[5];
        private JLabel errorMessageLabel;
        private JPanel topPanel;
        private JPanel centerPanel;
        private JButton closeButton;

        public GameStartFrame(Socket socket, String imageName, String playerName, PrintWriter out) {
            this.playerName = playerName;
            this.out = out;

            setTitle("شفرة - بدء اللعبة - " + playerName);
            setSize(800, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());

            topPanel = new JPanel();
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
            
            totalGameTimerLabel = new JLabel("الوقت المتبقي للعبة: 02:00", JLabel.CENTER);
            totalGameTimerLabel.setFont(new Font("Arial", Font.BOLD, 18));
            totalGameTimerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            errorMessageLabel = new JLabel("", JLabel.CENTER);
            errorMessageLabel.setFont(new Font("Arial", Font.BOLD, 24));
            errorMessageLabel.setForeground(Color.RED);
            errorMessageLabel.setVisible(false);
            errorMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            topPanel.add(totalGameTimerLabel);
            topPanel.add(Box.createVerticalStrut(10));
            topPanel.add(errorMessageLabel);
            
            add(topPanel, BorderLayout.NORTH);

            centerPanel = new JPanel(new BorderLayout());
            displayField = new JLabel("", JLabel.CENTER);
            updateImage(imageName);
            centerPanel.add(displayField, BorderLayout.CENTER);
            
            initScoreboard();
            centerPanel.add(scoreboardPanel, BorderLayout.EAST);
            
            add(centerPanel, BorderLayout.CENTER);

            JPanel inputPanel = new JPanel();
            textField = new JTextField(20);
            submitButton = new JButton("إرسال");
            closeButton = new JButton("خروج");
            inputPanel.add(textField);
            inputPanel.add(submitButton);
            inputPanel.add(closeButton);
            add(inputPanel, BorderLayout.SOUTH);

            submitButton.addActionListener(e -> {
                String answer = textField.getText().trim();
                if (!answer.isEmpty()) {
                    out.println("answer:" + answer);
                    textField.setText("");
                }
            });
            
            closeButton.addActionListener(e -> System.exit(0));
        }
        
        public void updateScoreboard(String scoresData) {
            SwingUtilities.invokeLater(() -> {
                for (JLabel label : playerScoreLabels) {
                    label.setText("");
                }

                if (scoresData == null || scoresData.isEmpty()) return;

                String[] playerEntries = scoresData.split(",");
                for (int i = 0; i < Math.min(playerEntries.length, playerScoreLabels.length); i++) {
                    String[] parts = playerEntries[i].split(":");
                    if (parts.length == 2) {
                        String playerName = parts[0];
                        String score = parts[1];
                        playerScoreLabels[i].setText(playerName + ": " + score + " نقطة");
                    }
                }
            });
        }

        public void showErrorMessage(String message) {
            SwingUtilities.invokeLater(() -> {
                errorMessageLabel.setText(message);
                errorMessageLabel.setVisible(true);
                
                topPanel.revalidate();
                topPanel.repaint();

                Timer timer = new Timer(1500, e -> {
                    errorMessageLabel.setVisible(false);
                    topPanel.revalidate();
                    topPanel.repaint();
                });
                timer.setRepeats(false);
                timer.start();
            });
        }

        private void initScoreboard() {
            scoreboardPanel = new JPanel();
            scoreboardPanel.setLayout(new BoxLayout(scoreboardPanel, BoxLayout.Y_AXIS));
            scoreboardPanel.setBorder(BorderFactory.createTitledBorder("نتائج اللاعبين"));
            
            for (int i = 0; i < 5; i++) {
                playerScoreLabels[i] = new JLabel(" ");
                playerScoreLabels[i].setFont(new Font("Arial", Font.PLAIN, 14));
                scoreboardPanel.add(playerScoreLabels[i]);
            }
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
    }

    static class WinnerFrame extends JFrame {
        private JTextArea winnerArea;
        private JButton exitButton;

        public WinnerFrame(Map<String, Integer> finalScores) {
            setTitle("نتائج النهائية");
            setSize(500, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());

            winnerArea = new JTextArea();
            winnerArea.setEditable(false);
            winnerArea.setFont(new Font("Arial", Font.PLAIN, 18));
            winnerArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

            exitButton = new JButton("خروج");
            exitButton.addActionListener(e -> System.exit(0));

            add(new JScrollPane(winnerArea), BorderLayout.CENTER);
            add(exitButton, BorderLayout.SOUTH);

            determineWinners(finalScores);
        }

        private void determineWinners(Map<String, Integer> scores) {
            if (scores.isEmpty()) {
                winnerArea.setText("لا يوجد نتائج نهائية متاحة\n\nقد يكون السبب انتهاء اللعبة مبكرًا، انسحاب جميع اللاعبين، أو مشكلة في الاتصال");
                return;
            }

            int maxScore = Collections.max(scores.values());
            List<String> winners = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : scores.entrySet()) {
                if (entry.getValue() == maxScore) {
                    winners.add(entry.getKey());
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("النتائج النهائية:\n\n");
            
            List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(scores.entrySet());
            sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

            for (Map.Entry<String, Integer> entry : sortedEntries) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" نقطة\n");
            }

            sb.append("\n");

            if (winners.size() == scores.size() && winners.size() > 1) {
                sb.append("لا يوجد فائز! جميع اللاعبين لديهم نفس النتيجة");
            } else if (winners.size() == 1) {
                sb.append("الفائز: ").append(winners.get(0)).append("!");
            } else {
                sb.append("الفائزون:\n");
                for (String winner : winners) {
                    sb.append(winner).append("\n");
                }
            }

            winnerArea.setText(sb.toString());

            for (String winner : winners) {
                highlightText(winner);
            }
        }

        private void highlightText(String textToHighlight) {
            String content = winnerArea.getText();
            int start = content.indexOf(textToHighlight);
            while (start >= 0) {
                int end = start + textToHighlight.length();
                winnerArea.setSelectionStart(start);
                winnerArea.setSelectionEnd(end);
                winnerArea.replaceSelection(textToHighlight);
                winnerArea.setSelectionColor(Color.YELLOW);
                winnerArea.setSelectionStart(end);
                winnerArea.setSelectionEnd(end);
                start = content.indexOf(textToHighlight, end);
            }
        }
    }

    public void updateTotalGameTimer(int timeLeft) {
        SwingUtilities.invokeLater(() -> {
            int minutes = timeLeft / 60;
            int seconds = timeLeft % 60;
            String timeText = String.format("الوقت المتبقي للعبة: %02d:%02d", minutes, seconds);
            totalGameTimerLabel.setText(timeText);
            
            if (timeLeft <= 30) {
                totalGameTimerLabel.setForeground(Color.RED);
            }
        });
    }
}