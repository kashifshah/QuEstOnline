/**
 *
 */
package shef.mt.tools;

import shef.mt.features.util.Sentence;
import java.io.*;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import ClientIrstLM.ClientIrstLM;

/**
 * Processes a file containing ngram probabilities and perplexities and sets the
 * corresponding values to the current sentence
 *
 * @author Catalina Hallett
 * @author Marco Turchi
 * 
 */
// Code modification for "on the fly" feature extraction. Modified by Marco Turchi Fondazione Bruno Kessler June 2013.

public class PPLProcessor extends ResourceProcessor {

    BufferedReader br;
    String[] valNames;
    String pplFile;
    ClientIrstLM clientLM;

    public PPLProcessor(ClientIrstLM cLM, String[] valNames) {

        /*try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(pplFile), "utf-8"));
            //                  System.out.println(br);
            this.valNames = valNames;
            this.pplFile = pplFile;
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    	
    	clientLM = cLM;
    	this.valNames = valNames;
        for (int i = 0; i < valNames.length; i++) {
            ResourceManager.registerResource(valNames[i]);
        }
    }

    public void processNextSentence(Sentence s) {
    	
    	 ArrayList<Double> scores = null;
        try {
 /*           String line = br.readLine();
            if (line == null) {
                System.out.println("line==null in " + new File(pplFile).getAbsolutePath() + " sent:" + s.getIndex() + " " + s.getText());
                return;
            }
            while (line.trim().isEmpty() || !line.endsWith("OOVs")) {
                line = br.readLine();
                //                            System.out.println(line);
            }
            line = br.readLine();
            //                            System.out.println(line);
            //ok, we found the line containing perplexities/log values
            String[] values = line.split(" ");
            //values we are interested in are at positions 3,5,7
//			System.out.println(line);
*/        
 
  		     
  				//out_sock.println(userInput);
  				//System.out.println("Received: " + in_sock.readLine());
    	   scores = clientLM.getPPL(s.getText());
    	  
    	  // System.out.println("Received: " +scores.get(0) +" "+scores.get(1));
  		     
  		    
        	
        	if(scores != null){
        		
        		
        		s.setValue(valNames[0], (scores.get(0).floatValue()));
        		s.setValue(valNames[1], (scores.get(1)).floatValue());
        		s.setValue(valNames[2], 0.0f);
        	}
        	
        	
   /*     	if (values[3].equals("undefined")) {
                s.setValue(valNames[0], 0.0f);
            } else {
                s.setValue(valNames[0], new Float(values[3]));
            }
            if (valNames.length > 1) {
                if (values[5].equals("undefined")) {
                    s.setValue(valNames[1], 0.0f);
                } else {
                    s.setValue(valNames[1], new Float(values[5]));
                }
                if (values[7].equals("undefined")) {
                    s.setValue(valNames[2], 0.0f);
                } else {
                    s.setValue(valNames[2], new Float(values[7]));
                }
            }*/
        } catch (Exception e) {
            //               System.out.println(pplFile+" "+s.getText());
            e.printStackTrace();
        } 
    }

    
    
    
    
    public void close() {
        try {
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
