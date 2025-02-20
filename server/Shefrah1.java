package com.mycompany.shefrah1;

import java.io.*;
import java.net.*;
import java.util.*;

public class Shefrah1 {
    
    private static ArrayList <String> waitingRoom = new ArrayList<>();  //array that store connected players (waiting list)
    private static ArrayList <String> playingRoom = new ArrayList<>();  //array that store players in the game (players list)
    
    private static HashMap<String, String> photoAnswers = new HashMap<>(); // Store photo names and correct answers    
    //private static Timer timer = new Timer(); // Timer for each question   
    //private static String currentPhoto = ""; // Current photo being displayed
    //private static int currentLevel = 1; // To keep track of the current level
    
    public static void main( String [] args) throws IOException {
       
        ServerSocket serverSocket = new ServerSocket(3280);  //creating socket
        while (true){
            
            System.out.println("Waiting for client connection");
            Socket client = serverSocket.accept();          //accepting clients requists
            System.out.println("Connected to client");
            ShClient clientThread = new ShClient (client,waitingRoom);  // starting new thread and adding the client to the list
            waitingRoom.add(clientThread);          //adding client to the array to manage clients list
            new Thread ((Runnable) clientThread).start();   //we dont know why runnable

            //if ((waitingRoom.size() == 2) || ()) {               //number will be chnged later  
                //sendPhotoForLevel(currentLevel); // Send the first photo for the first level
           // }  //end if // اضافة waiting list handler عشان اول ما يدخل الكلاينت الثاني يبدا تايمر واول مايخلص التايمر تبدا اللعبه
             
        } //end while
       
    }  //end main
    
    // Method to send a specific photo based on the level
    
    /*public static void sendPhotoForLevel (int level) {
        
        if (playingRoom.isEmpty())     //if there is no players in playing room don't send photo
            return; 
        
        // Select the photo based on the current level
        String levelPhoto = getPhotoForLevel(level);
        
        if (levelPhoto == null) {
            System.out.println("No photo found for level " + level);
            return;
        } //end if
        currentPhoto = levelPhoto;
        // Send the photo name to all connected clients
        for (ShClient client : playingRoom) {
            client.sendPhotoName(currentPhoto);
        } //end for
        // Start a timer for the 20-second limit
        startPhotoTimer(level);
        
    }  //end method sendPhotoForLevel
    
    private static String getPhotoForLevel(int level) {
  
        /* Here you can map each level to a specific photo.
           You can add more levels and their corresponding photos to this map/
        if (level == 1) {
            return "level1_photo.jpg";
        } else if (level == 2) {
            return "level2_photo.jpg";
        } else {
            return null; // No photo for the level
        }  //end if    //كانها  dns 
        
    }  //end method getPhotoForLevel
    
    private static void startPhotoTimer(int level) {
        
        // Schedule a task to execute after 20 seconds, when the time limit is up
        timer.schedule(new TimerTask() {
            @Override
        public void run() {
                System.out.println("Time's up for level " + level);
                // After the time is up, move to the next level
                currentLevel++;
                sendPhotoForLevel(currentLevel);
            }// فقط تايمر لكل لفل
        }, 20000); // 20000 milliseconds = 20 seconds
    }

    
    //شيء ما ندري وش سالفته :)
    Shefrah1(Socket socket) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    */
    //******************************** Thread ********************************//
    
    
private static class ClientHandler implements Runnable {

    private Socket server;
    private BufferedReader in;
    private PrintWriter out;
          
    public ClientHandler (Socket s) throws IOException {
  	     
            server = s;
            in = new BufferedReader ( new InputStreamReader ( server.getInputStream() ) ); 
            out = new PrintWriter( server.getOutputStream() , true ); 
            
    }
    	 
        @Override
    	 
    public void run(){

        String serverResponse;
            
        try {
        
            while(true){
            
                    serverResponse = in.readLine();    //handeling clients inputs
                
                    if (serverResponse == null)       //handeling the left client 
                            break;
                    
                    System.out.println("Server says: " + serverResponse);
            }
        } catch (IOException ex) {
            
            ex.printStackTrace();
                
        } finally {
         
            try { 
            
                in.close();
            } catch (IOException ex) {
                
                    ex.printStackTrace();
            }      
    }       
}
    
        public void sendPhotoName(String photoName) {// هذي الي توصل للكلاينت، الي قبل تجيب الاسم فقط
            out.println(photoName);  // Send the photo name to the client
        }
}}


