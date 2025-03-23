import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.List;

public class ShClient extends JFrame {
    private JTextArea connectedPlayers; 
    private JButton playButton; 
    private Socket socket; 
    private PrintWriter out; 
    private BufferedReader in; 
    private String playerName;
    private static final List<String> picName = Arrays.asList(
       "pic1", "pic2", "pic3", "pic4", "pic5", 
        "pic6", "pic7", "pic8", "pic9",
        "pic10", "pic11", "pic12", "pic13", "pic14", "pic15"
    );
    
     private static final List<String> picPath = Arrays.asList(
           "/shefrah2/imgSh.png","/shefrah2/imgSh2.png","/shefrah2/imgSh3.png",
             "/shefrah2/imgSh4.png", "/shefrah2/imgSh5.png","/shefrah2/imgSh6.png",
             "/shefrah2/imgSh7.png","/shefrah2/imgSh8.png", "/shefrah2/imgSh9.png",
             "/shefrah2/imgSh10.png","/shefrah2/imgSh11.png","/shefrah2/imgSh12.png",
             "/shefrah2/imgSh13.png","/shefrah2/imgSh14.png","/shefrah2/imgSh15.png"
    );
     
    public static void main(String[] args) {
        SwingUtilities.invokeLater(NameInputFrame::new);
    }

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
    SwingUtilities.invokeLater(() -> new GameStartFrame(socket));
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
                ShClient client = new ShClient(socket, playerName);
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
    public class GameStartFrame extends JFrame {
    private JLabel displayField;
    private JTextField textField;
    private JButton submitButton;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int score = 0;
    private int round = 0;
    private final int MAX_ROUNDS = 15;

    public GameStartFrame(Socket socket) {
        this.socket = socket;
        setTitle("بدء اللعبة");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        displayField = new JLabel();
        imagePanel.add(displayField);

        textField = new JTextField(20);
        submitButton = new JButton("إرسال");

        inputPanel.add(textField);
        inputPanel.add(submitButton);

        add(imagePanel, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        setupConnection();

        submitButton.addActionListener(this::sendAnswer);
        setVisible(true);
    }

    private void setupConnection() {
    try {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        new Thread(this::listenForServerMessages).start();
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "فشل الاتصال بالخادم!", "خطأ", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

private void listenForServerMessages() {
    try {
        String message;
        while ((message = in.readLine()) != null) {
            String finalMessage = message;
            SwingUtilities.invokeLater(() -> {
                if (finalMessage.startsWith("NextRound:")) {
                    String imageName = finalMessage.substring(10); // Extract image name
                    updateImage(imageName); // Update the image
                } else if (finalMessage.equals("Correct")) {
                    score++;
                    round++;
                    if (round > MAX_ROUNDS) {
                        JOptionPane.showMessageDialog(this, "اللعبة انتهت! نقاطك: " + score);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "إجابة صحيحة! الانتقال إلى السؤال التالي.");
                        textField.setText("");
                    }
                } else if (finalMessage.equals("Incorrect")) {
                    JOptionPane.showMessageDialog(this, "إجابة خاطئة! حاول مرة أخرى.");
                }
            });
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

private void updateImage(String imageName) {
    SwingUtilities.invokeLater(() -> {
        try {
            int index = picName.indexOf(imageName); // Find the index of the image name
            if (index == -1) {
                displayField.setText("الصورة غير موجودة!");
                return;
            }
            String path = picPath.get(index); // Get the corresponding image path
            ImageIcon originalImage = new ImageIcon(getClass().getResource(path)); // Load the image
            if (originalImage.getIconWidth() == -1) {
                displayField.setText("الصورة غير موجودة!");
            } else {
                Image scaledImage = originalImage.getImage().getScaledInstance(500, 500, Image.SCALE_SMOOTH);
                displayField.setIcon(new ImageIcon(scaledImage)); // Display the image
            }
        } catch (Exception e) {
            displayField.setText("خطأ في تحميل الصورة!");
        }
    });
}

/*private void sendAnswer(ActionEvent e) {
    String answer = textField.getText().trim();
    if (!answer.isEmpty()) {
        out.println("answer:" + answer); // Send the answer to the server
    }
}*/

private void sendAnswer(ActionEvent e) {
    String answer = textField.getText().trim();
    if (!answer.isEmpty()) {
        out.println("answer:" + answer); // Send the answer to the server
        System.out.println("Sent answer to server: " + answer); // Debug statement
    } else {
        System.out.println("Answer is empty!"); // Debug statement
    }
}
    }
}
