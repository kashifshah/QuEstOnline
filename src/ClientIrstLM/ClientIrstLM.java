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

package ClientIrstLM;

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

public class ClientIrstLM {

	/**
	 * @param args
	 */

	private int port=-1;
	private String host = "localhost";
	private BufferedReader stdIn;
	
	private boolean debug = false;
	
	

	/**
	 * Initialize the IrstLM client, and activates the connection to the server running a fake query to the LM
	 *
	 * @param  h  host
	 * @param  p port
	 * @param  deb	enable or disable comment and error messages
	 *  
	 * @author Turchi Marco
	 */
	
	 public ClientIrstLM(String h, int p, boolean deb) {
		 	debug = deb;

		    host = h;
		    port = p;
		    
		    stdIn = new BufferedReader(new InputStreamReader(System.in));
		    
		    //fake call to initialize the software

		    getPPL("test");
                    //getPPL("Protests");
                    
		
		    
	
	 }
	 
	 

	/**
	 * 
	 * Query the server to get the probability and perplexity for an input sentence. Connection to the server is open for querying and then close.
	 *
	 * @param  userInput  sentence used into the query
	 * 
	 * @return	Array of double containing the perplexity at index 0 and the log probability at index 1
	 * @author Turchi Marco
	 */
	 public ArrayList<Double>  getPPL(String userInput){
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
		 }
		 
		 ArrayList<Double> scores = new ArrayList<Double>();
		   // String userInput;
		 	String ppl="";
		    try {
		     // while ((userInput = stdIn.readLine()) != null) {
		    	  out_sock.println("<s> "+userInput+" </s>");
		    	  String app = (in_sock.readLine());
                           //app = "test";
		    	  if(debug)
		    		  System.out.println("Received: " +app );
		    	  ppl = app.split("sent_PP=")[1].split(" ")[0];
		    	  if(debug)
		    		  System.out.println("Received: " +ppl );
		    	  
		    	  
		    	  String [] toks =app.split(" "); 

		    	  ppl = toks[2].replace("sent_PP=", "");
		    	  double nS = Double.parseDouble(toks[1].replace("sent_Nw=",""));
		    	  if(debug)
		    		  System.out.println("Received: " +ppl );
		    	  double f =Double.parseDouble(ppl);
		    	  //double logp = -(Math.log(f)/Math.log(2));
		    	  //double nS = ((userInput.split(" ")).length-1);
		    	  double logprob = -(nS*Math.log(f))/(Math.log(10));
		    	  scores.add(0, logprob);
		    	  scores.add(1,f);
		    	  if(debug)
		    		  System.out.println("nS: "+nS+" log:"+logprob);


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
		    
		    return scores;
	 }
	 
	 


}
