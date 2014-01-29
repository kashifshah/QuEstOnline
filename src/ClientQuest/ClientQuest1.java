package ClientQuest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author kashif
 */
public class ClientQuest1 {
    
    private int port=-1;
	private String host = "localhost";
	private BufferedReader stdIn;
	
	private boolean debug = false;
	
        
        public ClientQuest1(String h, int p, boolean deb) {
		 	debug = deb;

		    host = h;
                     host = "localhost";
		    port = p;
                    port = 9111;
		    
		    stdIn = new BufferedReader(new InputStreamReader(System.in));
		    
		    //fake call to initialize the software

		    runQuest("test");
                    //getPPL("Protests");
                    
	
	 }
        
        
         public void runQuest(String userInput){
		 int delay = 1000000;
			Socket mySocket = null;
			PrintWriter out_sock = null;
			BufferedReader in_sock = null;
                        
                        
                         if(debug)
			    	System.out.println("** Trying to connect with " + host + " on port " + port);

			    try {
			    	
			    	SocketAddress me = new InetSocketAddress(host,port);
			        mySocket = new Socket();
			        mySocket.connect(me, delay);
			        out_sock = new PrintWriter(mySocket.getOutputStream(), true);
			        in_sock  = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
			    } catch (SocketTimeoutException e) {
					// TODO Auto-generated catch block
				      System.err.println("Timeout elapsed! Couldn't connect to: " + host);
				      System.exit(1);
				}catch (UnknownHostException e) {
			      System.err.println("Don't know about host: " + host);
			      System.exit(1);
			    } catch (IOException e) {
			      System.err.println("Couldn't get I/O for the connection to: " + host);
			      System.exit(1);
			    }
			    
			    
			    
			    if(debug)
			    	System.out.println("** COnnection established with " + host);
                            
                            if(debug){
			 System.out.println("host "+host +" port "+port);
			 System.out.println("LM SENTENCE "+userInput);
                        
                         
                         String ppl="";
		    try {
                        // Process myProcess = new ProcessBuilder("Hello", "arg").start();
		      //while ((userInput = stdIn.readLine()) != null) {
		    	  //out_sock.println(myProcess);
                         // out_sock.println("<s> "+userInput+" </s>");
                          out_sock.println("<seg id=\"1\"><src>source sentence </src><trg>target sentence </trg></seg>");
		    	  String app = (in_sock.readLine());
                          if(debug)
		    		  System.out.println("Received: " +app );
		    	//  ppl = app.split("sent_PP=")[1].split(" ")[0];
		    	  if(debug)
		    		  System.out.println("Received: " +ppl );
                         // } 
                    }  
                            catch (IOException e) {
		     
                              System.err.println("connection closed");
		    }
                    out_sock.close();
		    try {
				in_sock.close();
			   // stdIn.close();
			    mySocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
                         
		 }
                            
                            
                            
                          
         }
         public static void main(String[] args) {
             ClientQuest1 quest = new ClientQuest1("localhost", 9111, true);
         }
	 
    
}
