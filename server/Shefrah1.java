
package com.mycompany.shefrah1;

import java.io.*;
import java.net.*;
import java.util.*;

public class Shefrah1 {
    
    private static ArrayList <NewClinet> clients = new ArrayList<>();  //NewClient = client thread class name 

    public static void main(String[] args) {
       
        ServerSocket serverSocket = new ServerSocket(3280);  //creating socket

        while (true){
            
            System.out.println("Waiting for client connection");
            
            Socket client = serverSocket.accept();          //accepting clients requists
             
            System.out.println("Connected to client");
            
            NewClinet clientThread = new NewClinet (client,clients);  // starting new thread and adding the client to the list
            
            clients.add(clientThread);          //adding client to the array to manage clients list
            
            new Thread (clientThread).start();   //

        }
       
    }
    
    
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
    }
}


