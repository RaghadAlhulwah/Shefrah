package com.mycompany.shefrah1;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Window;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.stream.Collectors;

public class ShClient1 extends JFrame {
    private static final Map<String, String> picMap = new HashMap<>();

    static {
        picMap.put("pic1", "/img/1.png");
        picMap.put("pic2", "/img/2.png");
        picMap.put("pic3", "/img/3.png");
        picMap.put("pic4", "/img/4.png");
        picMap.put("pic5", "/img/5.png");
        picMap.put("pic6", "/img/6.png");
        picMap.put("pic7", "/img/7.png");
        picMap.put("pic8", "/img/8.png");
        picMap.put("pic9", "/img/9.png");
        picMap.put("pic10", "/img/10.png");
        picMap.put("pic11", "/img/11.png");
        picMap.put("pic12", "/img/12.png");
        picMap.put("pic13", "/img/13.png");
        picMap.put("pic14", "/img/14.png");
    }

    private static JLabel totalGameTimerLabel;
    private static boolean gameStarted = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NameInputFrame::new);
    }

    static class NameInputFrame extends JFrame {
        private JTextField nameField;
        private JButton submitButton;
        private JButton exitButton;

        public NameInputFrame() {
            setTitle("شفرة");
            setSize(700, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            // Background Panel with template image
            BackgroundPanel bgPanel = new BackgroundPanel("/img/pg1.png");
            bgPanel.setLayout(null); // Using absolute positioning for precise placement
            setContentPane(bgPanel);

            // Logo with flower design (already in template, so we skip it)

            // Name input field - positioned in center as per template
            nameField = new JTextField();
            nameField.setFont(new Font("Al Nile", Font.PLAIN, 18));
            nameField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            nameField.setBackground(new Color(91, 70, 78));
            nameField.setForeground(Color.WHITE);
            nameField.setCaretColor(Color.WHITE);
            nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 100, 110), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            nameField.setBounds(278, 432, 145, 35);

            // Submit button - "انضمام"
            submitButton = new JButton("انضمام");
            submitButton.setFont(new Font("Al Nile", Font.BOLD, 16));
            submitButton.setBackground(new Color(91, 70, 78));
            submitButton.setForeground(Color.WHITE);
            submitButton.setFocusPainted(false);
            submitButton.setBorder(BorderFactory.createLineBorder(new Color(120, 100, 110), 2));
            submitButton.setBounds(306, 506, 90, 30);
            submitButton.addActionListener(e -> submitName());

            // Exit button - bottom left
            exitButton = new JButton("خروج");
            exitButton.setFont(new Font("Al Nile", Font.BOLD, 14));
            exitButton.setBackground(new Color(91, 70, 78));
            exitButton.setForeground(Color.WHITE);
            exitButton.setFocusPainted(false);
            exitButton.setBorder(BorderFactory.createLineBorder(new Color(120, 100, 110), 2));
            exitButton.setBounds(38, 515, 90, 30);
            exitButton.addActionListener(e -> System.exit(0));

            bgPanel.add(nameField);
            bgPanel.add(submitButton);
            bgPanel.add(exitButton);

            nameField.addActionListener(e -> submitButton.doClick());

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
    private JButton exitButton;

    public ShClient1(Socket socket, String playerName) throws IOException {
        this.socket = socket;
        this.playerName = playerName;

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        setTitle("شفرة");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Background Panel with template image
        BackgroundPanel bgPanel = new BackgroundPanel("/img/pg2.png");
        bgPanel.setLayout(null);
        setContentPane(bgPanel);

                    // Connected Players Text Area - positioned in the pink box
        JTextArea connectedPlayers = new JTextArea();
        connectedPlayers.setFont(new Font("Al Nile", Font.PLAIN, 16));
        connectedPlayers.setEditable(false);
        connectedPlayers.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        connectedPlayers.setOpaque(false);
        connectedPlayers.setForeground(new Color(80, 60, 70));

        JScrollPane scrollPane = new JScrollPane(connectedPlayers);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setBounds(180, 175, 210, 300); // تعديل الموضع ليكون وسط الصندوق الوردي

        // Ready button - "جاهز"
        playButton = new JButton("جاهز");
        playButton.setFont(new Font("Al Nile", Font.BOLD, 16));
        playButton.setBackground(new Color(91, 70, 78));
        playButton.setForeground(Color.WHITE);
        playButton.setFocusPainted(false);
        playButton.setBorder(BorderFactory.createLineBorder(new Color(120, 100, 110), 2));
        playButton.setBounds(306, 506, 90, 30);

        // Exit button
        exitButton = new JButton("خروج");
        exitButton.setFont(new Font("Al Nile", Font.BOLD, 14));
        exitButton.setBackground(new Color(91, 70, 78));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
        exitButton.setBorder(BorderFactory.createLineBorder(new Color(120, 100, 110), 2));
        exitButton.setBounds(38, 515, 90, 30);
        exitButton.addActionListener(e -> System.exit(0));

        bgPanel.add(scrollPane);
        bgPanel.add(playButton);
        bgPanel.add(exitButton);

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
                    gameStarted = true;
                    SwingUtilities.invokeLater(() -> {
                        playButton.setEnabled(false);
                        playButton.setText("اللعبة بدأت");
                    });
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
            Map<String, Integer> finalScores = new HashMap<>();
            System.out.println("GameOver message: " + message);

            if (!message.contains("Final scores:") && !message.contains("No scores available")) {
                System.out.println("Skipping GameOver message without scores");
                return;
            }

            if (message.contains("Final scores:")) {
                String scoresPart = message.substring(message.indexOf("Final scores:") + 13).trim();
                if (!scoresPart.isEmpty()) {
                    String[] playerEntries = scoresPart.split(",");
                    for (String entry : playerEntries) {
                        if (entry.trim().isEmpty()) continue;
                        int lastColonIndex = entry.lastIndexOf(":");
                        if (lastColonIndex <= 0 || lastColonIndex == entry.length() - 1) continue;
                        String playerName = entry.substring(0, lastColonIndex).trim();
                        String scoreStr = entry.substring(lastColonIndex + 1).trim();
                        if (playerName.isEmpty() || scoreStr.isEmpty()) continue;
                        try {
                            int score = Integer.parseInt(scoreStr);
                            finalScores.put(playerName, score);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid score format for entry: " + entry);
                        }
                    }
                }
            }

            if (!finalScores.containsKey(playerName) && !message.contains("No scores available")) {
                JOptionPane.showMessageDialog(this, "لم تشارك في اللعبة!", "معلومات", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            for (Window window : Window.getWindows()) {
                window.dispose();
            }
            new WinnerFrame(finalScores).setVisible(true);
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

    private void updateTotalGameTimer(int timeLeft) {
        SwingUtilities.invokeLater(() -> {
            int minutes = timeLeft / 60;
            int seconds = timeLeft % 60;
            String timeText = String.format("%02d:%02d", minutes, seconds);
            totalGameTimerLabel.setText(timeText);

            if (timeLeft <= 30) {
                totalGameTimerLabel.setForeground(Color.RED);
            } else {
                totalGameTimerLabel.setForeground(new Color(255, 240, 230)); // لون فاتح دايماً
            }
        });
    }

    static class ReadyPlayersFrame extends JFrame {
        private JTextArea readyPlayersArea;
        private JLabel timerLabel;
        private JButton exitButton;

        public ReadyPlayersFrame(String playerName) {
            setTitle("شفرة");
            setSize(700, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            // Background Panel with template image
            BackgroundPanel bgPanel = new BackgroundPanel("/img/pg3.png");
            bgPanel.setLayout(null);
            setContentPane(bgPanel);

            // Ready Players Text Area
            readyPlayersArea = new JTextArea();
            readyPlayersArea.setFont(new Font("Al Nile", Font.PLAIN, 16));
            readyPlayersArea.setEditable(false);
            readyPlayersArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            readyPlayersArea.setOpaque(false);
            readyPlayersArea.setForeground(new Color(80, 60, 70));

            JScrollPane scrollPane = new JScrollPane(readyPlayersArea);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setBorder(null);
            scrollPane.setBounds(180, 175, 210, 300); // تعديل الموضع ليكون وسط الصندوق

            // Timer label - "الموقت"
            timerLabel = new JLabel("10", SwingConstants.CENTER);
            timerLabel.setFont(new Font("Al Nile", Font.BOLD, 20));
            timerLabel.setForeground(new Color(234, 224, 212));
            timerLabel.setBounds(280, 520, 140, 30);

            // Exit button
            exitButton = new JButton("خروج");
            exitButton.setFont(new Font("Al Nile", Font.BOLD, 14));
            exitButton.setBackground(new Color(91, 70, 78));
            exitButton.setForeground(Color.WHITE);
            exitButton.setFocusPainted(false);
            exitButton.setBorder(BorderFactory.createLineBorder(new Color(120, 100, 110), 2));
            exitButton.setBounds(38, 515, 90, 30);
            exitButton.addActionListener(e -> System.exit(0));

            bgPanel.add(scrollPane);
            bgPanel.add(timerLabel);
            bgPanel.add(exitButton);
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
                timerLabel.setText(String.valueOf(timeLeft));
            });
        }
    }

    static class GameStartFrame extends JFrame {
        private JLabel displayField;
        private JTextField textField;
        private JButton submitButton;
        private JButton exitButton;
        private PrintWriter out;
        private String playerName;
        private JPanel scoreboardPanel;
        private JLabel[] playerScoreLabels = new JLabel[5];
        private JLabel errorMessageLabel;

        public GameStartFrame(Socket socket, String imageName, String playerName, PrintWriter out) {
            this.playerName = playerName;
            this.out = out;

            setTitle("شفرة");
            setSize(700, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            // Background Panel with template image
            BackgroundPanel bgPanel = new BackgroundPanel("/img/pg4.png");
            bgPanel.setLayout(null);
            setContentPane(bgPanel);

            // Timer label - positioned at top center under "الموقت"
            totalGameTimerLabel = new JLabel("02:00", SwingConstants.CENTER);
            totalGameTimerLabel.setFont(new Font("Al Nile", Font.BOLD, 22));
            totalGameTimerLabel.setForeground(new Color(234, 224, 212));
            totalGameTimerLabel.setBounds(280, 87, 140, 30);

            // Image display - left side large box
            displayField = new JLabel("", JLabel.CENTER);
            displayField.setBounds(38, 125, 385, 380);
            updateImage(imageName);

            // Scoreboard panel - top right
            initScoreboard();
            scoreboardPanel.setBounds(474, 145, 190, 190);
            scoreboardPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            scoreboardPanel.setBackground(Color.WHITE);

            // Text field - middle right
            textField = new JTextField();
            textField.setFont(new Font("Al Nile", Font.PLAIN, 16));
            textField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            textField.setBackground(new Color(91, 70, 78));
            textField.setForeground(Color.WHITE);
            textField.setCaretColor(Color.WHITE);
            textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 100, 110), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            textField.setBounds(503, 365, 145, 35);

            // Submit button - "ارسال"
            submitButton = new JButton("ارسال");
            submitButton.setFont(new Font("Al Nile", Font.BOLD, 14));
            submitButton.setBackground(new Color(91, 70, 78));
            submitButton.setForeground(Color.WHITE);
            submitButton.setFocusPainted(false);
            submitButton.setBorder(BorderFactory.createLineBorder(new Color(120, 100, 110), 2));
            submitButton.setBounds(530, 433, 90, 30);

            // Exit button
            exitButton = new JButton("خروج");
            exitButton.setFont(new Font("Al Nile", Font.BOLD, 14));
            exitButton.setBackground(new Color(91, 70, 78));
            exitButton.setForeground(Color.WHITE);
            exitButton.setFocusPainted(false);
            exitButton.setBorder(BorderFactory.createLineBorder(new Color(120, 100, 110), 2));
            exitButton.setBounds(38, 515, 90, 30);

            // Error message label (hidden by default)
            errorMessageLabel = new JLabel("", SwingConstants.CENTER);
            errorMessageLabel.setFont(new Font("Al Nile", Font.BOLD, 16));
            errorMessageLabel.setForeground(Color.RED);
            errorMessageLabel.setVisible(false);
            errorMessageLabel.setBounds(200, 110, 300, 30);

            bgPanel.add(totalGameTimerLabel);
            bgPanel.add(displayField);
            bgPanel.add(scoreboardPanel);
            bgPanel.add(textField);
            bgPanel.add(submitButton);
            bgPanel.add(exitButton);
            bgPanel.add(errorMessageLabel);

            submitButton.addActionListener(e -> {
                String answer = textField.getText().trim();
                if (!answer.isEmpty()) {
                    out.println("answer:" + answer);
                    textField.setText("");
                }
            });

            exitButton.addActionListener(e -> System.exit(0));
            textField.addActionListener(e -> submitButton.doClick());
        }

        public void updateScoreboard(String scoresData) {
            SwingUtilities.invokeLater(() -> {
                for (JLabel label : playerScoreLabels) {
                    label.setHorizontalAlignment(SwingConstants.RIGHT);
                    label.setText("");
                }

                if (scoresData == null || scoresData.isEmpty() || scoresData.equals("SCORES:")) {
                    return;
                }

                String[] playerEntries = scoresData.split(",");
                for (int i = 0; i < Math.min(playerEntries.length, playerScoreLabels.length); i++) {
                    String entry = playerEntries[i].trim();
                    if (entry.isEmpty()) continue;
                    String[] parts = entry.split(":");
                    if (parts.length == 2 && !parts[0].isEmpty() && !parts[1].isEmpty()) {
                        String playerName = parts[0];
                        String score = parts[1];
                        playerScoreLabels[i].setText(playerName + ": " + score);
                    }
                }
            });
        }

        public void showErrorMessage(String message) {
            SwingUtilities.invokeLater(() -> {
                errorMessageLabel.setText(message);
                errorMessageLabel.setVisible(true);

                Timer timer = new Timer(2000, e -> {
                    errorMessageLabel.setVisible(false);
                });
                timer.setRepeats(false);
                timer.start();
            });
        }

        private void initScoreboard() {
            scoreboardPanel = new JPanel();
            scoreboardPanel.setLayout(new BoxLayout(scoreboardPanel, BoxLayout.Y_AXIS));
            scoreboardPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            scoreboardPanel.setOpaque(false);
            scoreboardPanel.setBackground(Color.WHITE);

            for (int i = 0; i < 5; i++) {
                playerScoreLabels[i] = new JLabel(" ");
                playerScoreLabels[i].setHorizontalAlignment(SwingConstants.RIGHT);
                playerScoreLabels[i].setFont(new Font("Al Nile", Font.BOLD, 14));
                playerScoreLabels[i].setForeground(new Color(80, 60, 70));
                scoreboardPanel.add(playerScoreLabels[i]);
                
                if (i < 4) {
                    scoreboardPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                }
            }
        }

        public void updateImage(String imageName) {
            SwingUtilities.invokeLater(() -> {
                String path = picMap.get(imageName);
                if (path != null) {
                    ImageIcon icon = new ImageIcon(getClass().getResource(path));
                    if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                        Image scaledImage = icon.getImage().getScaledInstance(370, 360, Image.SCALE_SMOOTH);
                        displayField.setIcon(new ImageIcon(scaledImage));
                    } else {
                        displayField.setText("الصورة غير متوفرة!");
                        displayField.setFont(new Font("Al Nile", Font.PLAIN, 16));
                        displayField.setForeground(new Color(80, 60, 70));
                    }
                } else {
                    displayField.setText("الصورة غير معروفة!");
                    displayField.setFont(new Font("Al Nile", Font.PLAIN, 16));
                    displayField.setForeground(new Color(80, 60, 70));
                }
            });
        }
    }

    static class WinnerFrame extends JFrame {
        private JButton exitButton;

        public WinnerFrame(Map<String, Integer> finalScores) {
            setTitle("شفرة - الفائزون");
            setSize(700, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            // Background Panel with template image
            BackgroundPanel bgPanel = new BackgroundPanel("/img/pg5.png");
            bgPanel.setLayout(null);
            setContentPane(bgPanel);

            // Exit button
            exitButton = new JButton("خروج");
            exitButton.setFont(new Font("Al Nile", Font.BOLD, 14));
            exitButton.setBackground(new Color(91, 70, 78));
            exitButton.setForeground(Color.WHITE);
            exitButton.setFocusPainted(false);
            exitButton.setBorder(BorderFactory.createLineBorder(new Color(120, 100, 110), 2));
            exitButton.setBounds(38, 515, 90, 30);
            exitButton.addActionListener(e -> System.exit(0));

            bgPanel.add(exitButton);

            determineWinners(finalScores, bgPanel);
        }

        private void determineWinners(Map<String, Integer> scores, BackgroundPanel bgPanel){

            if (scores.isEmpty()) {
                bgPanel.setImage("/img/pg6.png");

                JLabel noWinners = new JLabel("لا يوجد فائزون", SwingConstants.CENTER);
                noWinners.setFont(new Font("Al Nile", Font.BOLD, 20));
                noWinners.setForeground(new Color(80, 60, 70));
                noWinners.setBounds(200, 250, 300, 40);

                bgPanel.add(noWinners);
                bgPanel.revalidate();
                bgPanel.repaint();

                return;
            }

            // Sort players by score
            List<Map.Entry<String, Integer>> sortedPlayers = scores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

            // First place (tallest podium - center)
            if (sortedPlayers.size() > 0) {
                Map.Entry<String, Integer> first = sortedPlayers.get(0);
                JLabel firstLabel = new JLabel(first.getKey(), SwingConstants.CENTER);
                firstLabel.setFont(new Font("Al Nile", Font.BOLD, 18));
                firstLabel.setForeground(new Color(234, 224, 212));
                firstLabel.setBounds(280, 190, 140, 120);
                bgPanel.add(firstLabel);
            }

            // Second place (right podium - shorter)
            if (sortedPlayers.size() > 1) {
                Map.Entry<String, Integer> second = sortedPlayers.get(1);
                JLabel secondLabel = new JLabel(second.getKey(), SwingConstants.CENTER);
                secondLabel.setFont(new Font("Al Nile", Font.BOLD, 18));
                secondLabel.setForeground(new Color(234, 224, 212));
                secondLabel.setBounds(420, 277, 140, 95);
                bgPanel.add(secondLabel);
            }

            // Third place (left podium - shortest)
            if (sortedPlayers.size() > 2) {
                Map.Entry<String, Integer> third = sortedPlayers.get(2);
                JLabel thirdLabel = new JLabel(third.getKey(), SwingConstants.CENTER);
                thirdLabel.setFont(new Font("Al Nile", Font.BOLD, 16));
                thirdLabel.setForeground(new Color(234, 224, 212));
                thirdLabel.setBounds(140, 315, 140, 70);
                bgPanel.add(thirdLabel);
            }
        }
    }

    static class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        setImage(imagePath);
    }

    public void setImage(String imagePath) {
        try {
            backgroundImage = new ImageIcon(getClass().getResource(imagePath)).getImage();
        } catch (Exception e) {
            backgroundImage = null;
            System.out.println("Could not load background image: " + imagePath);
        }
        repaint(); 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(91, 70, 78));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}


        
}