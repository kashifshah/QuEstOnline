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


package updatedQuEst;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;


import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;


public class UpdatedQuEstMain {

	public static final Logger log = Logger.getLogger(UpdatedQuEstMain.class.getName());

	/**
	 * Interface to the feature extractor. It waits for structured text in the standard input:
	 * <seg id="1"><src>source sentence </src><trg>target sentence </trg></seg>
	 * <seg id="1"><src>It also rose in Mexico, where the economy has recovered after suffering a big drop in output last year. </src><trg>También aumentó en México, donde la economía se ha recuperado después de sufrir una caída en la producción del año pasado. </trg></seg> 
	 * 
	 * The structured text is then parsed and sent to the feature extractor. Features are sent back into the stadard output.
	 *
	 * @param  configFileFE  configuration file for QuEst
	 * @param  outputFolder the updated version of quest creates temporary a folder for each sentence pair, that is then deleted. This parameter
	 * 						sets the main folder where the temporary folder are created
	 * @param  debug	enable or disable comment and error messages
	 * @param  preProcessedText  enable and disable truecasing and tokenization of the sentences: true: already preprocessed text false: text to be preprocessed
	 *  
	 * @return      list of features into the standard output
	 * @author Turchi Marco
	 */
	public static void main(String[] args) {		

		String configFileFE="";
		String outputFolder="";
		boolean debug = false;
		String preProcessedText = "";
		
		if(args.length ==4){

			configFileFE = args[0];
			outputFolder = args[1];
			debug = Boolean.parseBoolean(args[2]);
			preProcessedText = args[3];
			
		}else {
			System.err.println("Wrong number of input parameters");
			log.error("Wrong number of input parameters");
			System.exit(1);
		}
		
		
		
        //Initialize the feature extractor
        FeatureExtraction fE=null;
        try {
			fE = new FeatureExtraction(configFileFE, outputFolder, debug, preProcessedText);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.err.println("Error Setting the Feature Extractor");
			log.error("Error Setting the Feature Extractor");
			System.exit(1);
		}
		
      //Initialize the channels to the standard input and output
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));
		
		//System.out.println("###End Init");
		String input ="";
		try {
			input = stdin.readLine();
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
		
		// wait for structured text in the standard input
		while((input != null)){

			if(debug)
				System.out.println(input);
			try {
				 double score=0;
			  

			       if((input != null) && (input.startsWith("<seg id="))){
			    	   long start = System.currentTimeMillis();
			      
			    	   if (debug) {
			    		   System.out.println(input);
			    		   log.info("input string: "+input);
			    	   }
				       try {    
				    	  //parse the input text
				    	   String id =null;
				    	   String source = null;
				    	   String target = null;
				    	   try{
					    	   id = (input.split("id=\"")[1]).split("\"><src>")[0];
					    	   
					    	   source = (input.split("<src>")[1]).split("</src>")[0];
					    	   
					    	   target = (input.split("<trg>")[1]).split("</trg>")[0];
					       }
					    	 catch (Exception e) {
					    	   //e.printStackTrace();
					    	   log.error("Input string has a wrong format");
					    	   id="";
					    	   source ="";
					    	   target = "";
					    	 }
				    	  
				    	   
				    	   if (debug) {
				    		   System.out.println("id: "+id);
					    	   System.out.println("source: "+source);
					    	   System.out.println("target: "+target);
				    		   log.info("id: "+id);
				    		   log.info("source: "+source);
				    		   log.info("target: "+target);
				    	   }
				    	   
				    	   //get features
				    	   if((source.trim().length() !=0)&& (target.trim().length() != 0) ){
				    	   
					    	   String features = fE.getFeatures(source, target);
					    	  // System.out.println("Features: " +features);
					       
					    	   if(features != null){
					    		   if (debug) {
					    			   System.out.println("Features:" +features);
					    		   }
					    		   //System.out.println("Features:" +features);
					    		  
					    		   //out.write("<seg id=\""+id+"\">"+features+"</seg>\n");
                                                           out.write(features+"\n");
					    		   out.flush();
					    	   }
				    	   }else{
					    	   log.info("Errof found during the feature extraction process. ");
					    	 
				    	   }
				    	  
				    	 }
				    	 catch (Exception e) {
				    	   //e.printStackTrace();
				    	   log.error("Exception: "+e.getMessage());
				    	   log.info("Errof found during the Feature extracion process.");
				    	 }
			
				       long elapsed = System.currentTimeMillis() - start;
				       if(debug){
				    	   	log.info("features computed in " + elapsed / 1000F + " sec");
				    	   	System.out.println("features computed in " + elapsed / 1000F + " sec");

				       }
			       }
			   
			       input = stdin.readLine();
			 }
			  
			 catch (java.io.IOException e) { 
				 System.out.println(e);
				 log.error("IOEception: "+e.getMessage());
				 break;
			 } catch(Exception e){
				 System.out.println(e);
				 log.error("Eception: "+e.getMessage());
				 break;
			 }
	
			
		}
		

	}
	
	
	

}
