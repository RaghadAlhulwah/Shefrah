
package com.mycompany.shefrah1;

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
}
