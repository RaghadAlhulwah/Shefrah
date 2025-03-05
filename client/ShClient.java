public class ShClient1 extends JFrame {
    
    private Socket socket;                     
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;
    private JTextArea connectedPlayers;
    private JButton playButton;  
    private JTextField nameField;
    private JButton okButton;
    private JTextArea readyPlayers;

    // ********** Constructor1 for the first frame (entering player name) ********** //
    
    public ShClient1() {
        
        setTitle("شفرة");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel lbl1EnterName = new JLabel("أدخل اسمك:", SwingConstants.CENTER);
        nameField = new JTextField(15);

        okButton = new JButton("موافق");
        okButton.addActionListener(e -> submitName());

        JPanel inputPanel = new JPanel();
        inputPanel.add(nameField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);

        add(lbl1EnterName, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
    }// end of constructor1
    
    // ***** This method takes the entered player name and swich the frame ***** //
    
    private void submitName() {
        
        String pName = nameField.getText().trim(); 
        
        // check if the field is empty return null 
        
        if (pName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "يرجى إدخال اسم صحيح!", "خطأ", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Creating a socket if the field is not empty 
        try {
            Socket s = new Socket("localhost", 3280);
            ShClient1 client = new ShClient1(s, pName); //calling the second frame constructor
            client.setVisible(true);
            this.dispose();                             // Close the name input window
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "خطأ في الاتصال بالخادم", "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ********** Constructor2 for the second frame (connected players list) ********** //
    
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

        JLabel title = new JLabel("اللاعبون المتصلون", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        connectedPlayers = new JTextArea(10, 30);
        connectedPlayers.setEditable(false);

        playButton = new JButton("انطلق");

        add(title, BorderLayout.NORTH);
        add(new JScrollPane(connectedPlayers), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(playButton);
        add(buttonPanel, BorderLayout.SOUTH);

        out.println(playerName);
        playButton.addActionListener(e -> sendPlayCommand());

        new Thread(this::readServerMessages).start();
    } //end of constructor2
    
    // ***** This method takes the entered player name and swich the frame ***** //

    private void sendPlayCommand() {
        out.println("play");
        playButton.setEnabled(false);
        
        // Switch to Frame 3 (Players Ready to Play)
        SwingUtilities.invokeLater(() -> {
            try {
                new ShClient1(socket, playerName, true).setVisible(true);
                this.dispose();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    
    // ***** This method takes the entered player name and swich the frame ***** //

    private void readServerMessages() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                System.out.println("Server: " + serverMessage);
                if (serverMessage.startsWith("Players:")) {
                    String playersList = serverMessage.substring(8);
                    updateConnectedPlayers(playersList.split(","));
                } else if (serverMessage.startsWith("ReadyPlayers:")) {
                    String readyList = serverMessage.substring(13);
                    updateReadyPlayers(readyList.split(","));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
     // ***** Updates the list of players who are ready to play ***** //
    public void updateReadyPlayers(String[] players) {
    SwingUtilities.invokeLater(() -> {
        if (readyPlayers != null) {
            readyPlayers.setText("");
            for (String player : players) {
                if (player != null && !player.isEmpty()) {
                    readyPlayers.append(player + "\n");
                }
            }
        }
    });
}

    
    // ***** This method takes the entered player name and swich the frame ***** //

    public void updateConnectedPlayers(String[] players) {
        connectedPlayers.setText("");
        for (String player : players) {
            if (player != null && !player.isEmpty()) {
                connectedPlayers.append(player + "\n");
            }
        }
    }
    
    // ********** Constructor3 for the third frame (Players ready to play) ********** //
    
    public ShClient1(Socket clientSocket, String playerName, boolean isReadyFrame) throws IOException {
        this.socket = clientSocket;
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

        add(title, BorderLayout.NORTH);
        add(new JScrollPane(readyPlayers), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(waitingMessage);
        add(buttonPanel, BorderLayout.SOUTH);

        // Notify server that this player is ready
        out.println("Ready: " + playerName);

        new Thread(this::readServerMessages).start();
    }

    
    // ***** This method takes the entered player name and swich the frame ***** //

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ShClient1().setVisible(true));
    }
    
    
        
} //end of class 

