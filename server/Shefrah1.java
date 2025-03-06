import java.io.*;
import java.net.*;
import java.util.*;

public class Shefrah1 {
    // قائمةاللاعبين المتصلين تتحمل الثريدز افضل من القائمة العادية
    private static List<ClientHandler> waitingRoom = Collections.synchronizedList(new ArrayList<>());
    // قائمةاللاعبين الجاهزين تتحمل الثريدز افضل من القائمة العادية
    private static List<String> waitingPlayers = Collections.synchronizedList(new ArrayList<>());
    // مؤقت العد التنازلي لبدء اللعبة
    private static int countdown = 60;
    //متغير للتأكد  إذا كان المؤقت يشتغل 
    private static boolean timerRunning = false;

    public static void main(String[] args) throws IOException {
        // السوكت
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("تم بدء السيرفر. في انتظار اتصال اللاعبين...");

        // قبول اتصالات اللاعبين بشكل مستمر
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("لاعب متصل.");

            ClientHandler clientHandler = new ClientHandler(clientSocket);
            waitingRoom.add(clientHandler);
            new Thread(clientHandler).start(); 

            // نتأكد إذا اللعبه تقدر تشتغل
            checkAndStartGame();
        }
    }

    // كلاس داخلي للتعامل مع الاتصال بكل لاعب
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String playerName;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        }

        @Override
        public void run() {
            try {
                // قراءة اسم اللاعب
                playerName = in.readLine();
                if (playerName == null || playerName.trim().isEmpty()) {
                    out.println("خطأ: اسم غير صالح");
                    socket.close();
                    return;
                }
                System.out.println("لاعب انضم: " + playerName);

                // إرسال قائمة اللاعبين المتصلين إلى جميع اللاعبين
                sendPlayersList();
                broadcastWaitingPlayers();

                // الاستماع للرسائل من اللاعب
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("play")) {
                        // إضافة اللاعب إلى قائمة الانتظار إذا ضغط على "جاهز"
                        synchronized (waitingPlayers) {
                            if (!waitingPlayers.contains(playerName)) {
                                waitingPlayers.add(playerName);
                            }
                        }
                        broadcastWaitingPlayers(); // تحديث قائمة الانتظار للاعبين
                        startCountdownIfNeeded(); // بدء العد التنازلي إذا لزم الأمر
                        checkAndStartGame(); // التحقق مما إذا كانت اللعبة يمكن أن تبدأ
                    }
                    sendPlayersList(); // تحديث قائمة اللاعبين المتصلين
                }
            } catch (IOException e) {
                System.out.println("لاعب " + playerName + " انقطع.");
            } finally {
                // إزالة اللاعب عند انقطاع الاتصال
                removePlayer();
            }
        }

        // إزالة اللاعب من قوائم السيرفر
        private void removePlayer() {
            waitingRoom.remove(this);
            waitingPlayers.remove(playerName);
            sendPlayersList();
            broadcastWaitingPlayers();
        }

        // إرسال رسالة إلى هذا اللاعب
        public void sendMessage(String message) {
            out.println(message);
        }
    }

    // إرسال قائمة جميع اللاعبين المتصلين إلى جميع العملاء
    private static void sendPlayersList() {
        String players = "Players:" + String.join(",", getPlayerNames());
        broadcastMessage(players);
    }

    // إرسال قائمة اللاعبين المنتظرين إلى جميع العملاء
    private static void broadcastWaitingPlayers() {
        String waiting = "WaitingPlayers:" + String.join(",", waitingPlayers);
        broadcastMessage(waiting);
    }

    // الحصول على أسماء جميع اللاعبين المتصلين
    private static List<String> getPlayerNames() {
        List<String> names = new ArrayList<>();
        for (ClientHandler client : waitingRoom) {
            names.add(client.playerName);
        }
        return names;
    }

    // بث رسالة إلى جميع اللاعبين المتصلين
    private static void broadcastMessage(String message) {
        synchronized (waitingRoom) {
            for (ClientHandler client : waitingRoom) {
                client.sendMessage(message);
            }
        }
    }

    // بدء العد التنازلي إذا كان هناك لاعبين جاهزين
    private static void startCountdownIfNeeded() {
        if (waitingPlayers.size() == 2 && !timerRunning) {
            timerRunning = true;
            new Thread(() -> {
                while (countdown > 0 && waitingPlayers.size() < 4) {
                    broadcastMessage("Timer:" + countdown); // بث قيمة المؤقت
                    try {
                        Thread.sleep(1000); // الانتظار لمدة ثانية
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    countdown--;
                }
                // بدء اللعبة إذا كان هناك على الأقل لاعبين جاهزين
                if (waitingPlayers.size() >= 2) {
                    System.out.println("اللعبة بدأت!");
                    broadcastMessage("GameStart");
                }
                resetGameState(); // إعادة تعيين حالة اللعبة
            }).start();
        }
    }

    // التحقق مما إذا كان هناك 4 لاعبين جاهزين وبدء اللعبة فورًا
    private static void checkAndStartGame() {
        if (waitingPlayers.size() >= 4) {
            countdown = 0; // إيقاف المؤقت
            timerRunning = false;
            System.out.println("اللعبة بدأت!");
            broadcastMessage("GameStart");
            resetGameState();
        }
    }

    // إعادة تعيين حالة اللعبة
    private static void resetGameState() {
        countdown = 60;
        timerRunning = false;
        waitingPlayers.clear();
    }
}