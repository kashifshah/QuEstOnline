package ClientQuest;
/***********************************************************************
UpdatedQuEst - online version of the QuEst software
Copyright (C) 2013 Matecat (ICT-2011.4.2-287688).

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
***********************************************************************/

//package Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Random;


public class ClientLearn {

	/**
	 * @param args
	 */

	private int port=-1;
	private String host = "localhost";
	private BufferedReader stdIn;
	
	private boolean debug = false;
	
	

	/**
	 * Initialize the Quest client, and activates the connection to the server running a fake query to QuEst
	 *
	 * @param  h  host
	 * @param  p port
	 * @param  deb	enable or disable comment and error messages
	 *  
	 * @author Turchi Marco
	 */
	
	
         public static void main(String[] args) {
             ClientLearn quest = new ClientLearn("localhost", 7774 , false);
         }
        
        public ClientLearn(String h, int p, boolean deb) {
		 	debug = deb;

		    host = h;
		    port = p;
		    
		    stdIn = new BufferedReader(new InputStreamReader(System.in));
		    
		    //fake call to initialize the software

		    //String features = "1        2       3       4       5       6       7       8       9       10      11      12      13      14      15      16      17";
                    String features = "";
                    Random randomGenerator = new Random();
                    for (int idx = 1; idx <= 17; ++idx){
                        float randomInt = randomGenerator.nextFloat();
                                //nextInt(1);
                            features = features + "\t" + randomInt;
                        }
                    
                    double s = getPredictions(features);
                    //int indexOfOpenBracket = s.indexOf("[");
                    //int indexOfLastBracket = s.lastIndexOf("]");
                    //double d = Double.parseDouble(s.substring(indexOfOpenBracket+1, indexOfLastBracket));
                   //System.out.println(s.substring(indexOfOpenBracket+1, indexOfLastBracket));
                    
		
		    
	
	 }
	 
	 
	 
	 

	/**
	 * 
	 * Query the server to get the features for an input source/target sentence pair. Connection to the server is open for querying and then close.
	 *
	 * @param  source  source sentence used into the query
	 * @param  target target sentence used into the query
	 * 
	 * @return	features string of features in the form: feature_id:value feature_id:value
	 * @author Turchi Marco
	 */
	 public double  getPredictions(String features){
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
			 System.out.println("Features are  " + features);
		 }
		 
		 String scores ="";
	   // String userInput;

	    try {
	     // while ((userInput = stdIn.readLine()) != null) {
	    	  out_sock.println(features);
	    	  scores = (in_sock.readLine());
	    	  if(debug)
	    		  System.out.println("Received: " +features );
	    	 
	    	 
	    	 


	    } catch (IOException e) {
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
	    
            
            int indexOfOpenBracket = scores.indexOf("[");
            int indexOfLastBracket = scores.lastIndexOf("]");
            double d_scores = Double.parseDouble(scores.substring(indexOfOpenBracket+1, indexOfLastBracket));
	    return d_scores;
	 }
	 
	 


}
