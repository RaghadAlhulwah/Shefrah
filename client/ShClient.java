import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ShClient extends JFrame {
    private JTextArea connectedPlayers; // منطقة نصية لعرض اللاعبين المتصلين
    private JButton playButton; // زر لتحديد أن اللاعب جاهز
    private Socket socket; // سوكيت للاتصال بالسيرفر
    private PrintWriter out; // لإرسال الرسائل إلى السيرفر
    private BufferedReader in; // لقراءة الرسائل من السيرفر
    private String playerName; // اسم اللاعب

    public ShClient(Socket clientSocket, String playerName) throws IOException {
        this.socket = clientSocket;
        this.playerName = playerName;

        // تهيئة قنوات الإدخال والإخراج
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // إعداد النافذة الرئيسية
        setTitle("شفرة");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // عنوان النافذة
        JLabel title = new JLabel("اللاعبون المنتظرون", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        // منطقة نصية لعرض اللاعبين المتصلين
        connectedPlayers = new JTextArea(20, 50);
        connectedPlayers.setEditable(false);

        // زر "جاهز"
        playButton = new JButton("جاهز");

        // إضافة المكونات إلى النافذة
        add(title, BorderLayout.NORTH);
        add(new JScrollPane(connectedPlayers), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(playButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // إرسال اسم اللاعب إلى السيرفر
        out.println(playerName);

        // إضافة مستمع لزر "جاهز"
        playButton.addActionListener(e -> {
            out.println("play"); // إعلام السيرفر أن اللاعب جاهز
            openReadyPlayersFrame(); // فتح نافذة "اللاعبون الجاهزون"
        });

        // بدء ثيل للاستماع لرسائل السيرفر
        new Thread(this::readServerMessages).start();
    }

    // الاستماع لرسائل السيرفر
    private void readServerMessages() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                System.out.println("السيرفر: " + serverMessage);
                if (serverMessage.startsWith("Players:")) {
                    // تحديث قائمة اللاعبين المتصلين
                    String playersList = serverMessage.substring(8);
                    updateConnectedPlayers(playersList.split(","));
                } else if (serverMessage.startsWith("WaitingPlayers:")) {
                    // تحديث قائمة اللاعبين المنتظرين
                    String waitingList = serverMessage.substring(15);
                    updateWaitingPlayers(waitingList.split(","));
                } else if (serverMessage.startsWith("Timer:")) {
                    // تحديث المؤقت
                    int timeLeft = Integer.parseInt(serverMessage.substring(6));
                    updateTimer(timeLeft);
                } else if (serverMessage.equals("GameStart")) {
                    // فتح نافذة "بدء اللعبة"
                    openGameStartFrame();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // تحديث قائمة اللاعبين المتصلين في الواجهة
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

    // تحديث قائمة اللاعبين المنتظرين في الواجهة
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

    // تحديث المؤقت في الواجهة
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

    // فتح نافذة "اللاعبون الجاهزون"
    private void openReadyPlayersFrame() {
        SwingUtilities.invokeLater(() -> {
            ReadyPlayersFrame readyPlayersFrame = new ReadyPlayersFrame(playerName);
            readyPlayersFrame.setVisible(true);
            this.setVisible(false); // إخفاء النافذة الحالية
        });
    }

    // فتح نافذة "بدء اللعبة"
    private void openGameStartFrame() {
        SwingUtilities.invokeLater(() -> {
            JFrame gameStartFrame = new JFrame("بدء اللعبة");
            gameStartFrame.setSize(400, 300);
            gameStartFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gameStartFrame.setLocationRelativeTo(null);

            JLabel message = new JLabel("اللعبة بدأت!", SwingConstants.CENTER);
            message.setFont(new Font("Arial", Font.BOLD, 24));

            gameStartFrame.add(message, BorderLayout.CENTER);
            gameStartFrame.setVisible(true);

            this.dispose(); // إغلاق النافذة الحالية
        });
    }

    // الدالة الرئيسية لبدء العميل
    public static void main(String[] args) {
        SwingUtilities.invokeLater(NameInputFrame::new);
    }
}

// نافذة لإدخال اسم اللاعب
class NameInputFrame extends JFrame {
    private JTextField nameField; // حقل نصي لإدخال اسم اللاعب
    private JButton okButton; // زر لتأكيد الاسم

    public NameInputFrame() {
        setTitle("ادخل اسمك");
        setSize(300, 150);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // نص الإرشاد
        JLabel prompt = new JLabel("أدخل اسمك:", SwingConstants.CENTER);
        nameField = new JTextField(15);

        // زر "موافق"
        okButton = new JButton("موافق");
        okButton.addActionListener(e -> submitName());

        // إضافة المكونات إلى النافذة
        JPanel inputPanel = new JPanel();
        inputPanel.add(nameField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);

        add(prompt, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // إرسال اسم اللاعب والاتصال بالسيرفر
    private void submitName() {
        String playerName = nameField.getText().trim();
        if (playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "يرجى إدخال اسم", "خطأ", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // الاتصال بالسيرفر
            Socket socket = new Socket("localhost", 1234);
            ShClient client = new ShClient(socket, playerName);
            client.setVisible(true);
            this.dispose(); // إغلاق نافذة إدخال الاسم
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "خطأ في الاتصال بالسيرفر", "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// نافذة لعرض اللاعبين الجاهزين والمؤقت
class ReadyPlayersFrame extends JFrame {
    private JTextArea readyPlayersArea; // منطقة نصية لعرض اللاعبين الجاهزين
    private JLabel timerLabel; // نص لعرض المؤقت

    public ReadyPlayersFrame(String playerName) {
        setTitle("اللاعبون الجاهزون - " + playerName);
        setSize(400, 300);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // منطقة نصية للاعبين الجاهزين
        readyPlayersArea = new JTextArea();
        readyPlayersArea.setEditable(false);
        readyPlayersArea.setText("اللاعبون الجاهزون:\n" + playerName);

        // نص المؤقت
        timerLabel = new JLabel("الوقت المتبقي: 60 ثانية", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));

        // إضافة المكونات إلى النافذة
        add(new JScrollPane(readyPlayersArea), BorderLayout.CENTER);
        add(timerLabel, BorderLayout.SOUTH);
    }

    // تحديث قائمة اللاعبين الجاهزين
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

    // تحديث المؤقت
    public void updateTimer(int timeLeft) {
        SwingUtilities.invokeLater(() -> {
            timerLabel.setText("الوقت المتبقي: " + timeLeft + " ثانية");
        });
    }
}