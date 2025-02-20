package shefrah1;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;



public class Shclient extends JFrame{

    private JTextArea ConnectedPlayers;
    private JButton PlayButton;
    
    
    public Shclient(){
        setTitle(" شفرة");
        setSize(600,400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);//اذا قفل ما يوديه لفريم اخر"توقع"
        
        setLayout(new BorderLayout());
        
        JLabel Title= new JLabel(" اللاعبون المتصلون ", SwingConstants.CENTER);
        Title.setFont(new Font("Arial", Font.BOLD,24));
        
        ConnectedPlayers = new JTextArea(20,50);
        ConnectedPlayers.setEditable(false);
        
        PlayButton = new JButton(" انطلق");
        
        add(Title,BorderLayout.NORTH);
        add(ConnectedPlayers, BorderLayout.CENTER);
        
         JPanel buttonPanel = new JPanel();
        buttonPanel.add(PlayButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    // 
    
    
    public void updateConnectedPlayers(String[] players) {
        ConnectedPlayers.setText("");
        for (String player : players) {
            if (player != null && !player.isEmpty()) {
                ConnectedPlayers.append(player + "\n");
            }
        }
    }

    public void addPlayButtonListener(ActionListener listener) {
        PlayButton.addActionListener(listener);
    }

      public static void main(String[] args) {
        // تشغيل واجهة التسجيل
        SwingUtilities.invokeLater(() -> {
            new Shclient().setVisible(true);
        });
    }



} 




/*package com.mycompany.shefrah1;

import java.io.*;
import java.net.*;
import java.util.*;

public class ShClient {
    private static final String Server_IP = "localhost";
    private static final int Server_port = 9090;
 
        public static void main(String[] args) throws IOException {
            
          try(Socket socket = new Socket (Server_IP,Server_port)) {
              Shefrah1 servcon=new Shefrah1(socket);
              BufferedReader keyboard=new BufferedReader (new InputStreamReader(System.in));
              PrintWriter out=new PrintWriter(socket.getOutputStream(),true);
              new Thread ((Runnable) servcon).start(); 
              try{
                  while(true){
                      System.out.println("> ");
                      String command=keyboard.readLine();                     
                      if(command.equals("quit")) break;
                      out.println(command); 
                  } // end of while loop
              } catch (Exception e){
                  e.printStackTrace();
              }
          }
              System.exit(0);
        }

    ShClient(Socket client, ArrayList<ShClient> clients) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    void sendPhotoName(String currentPhoto) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}*/
