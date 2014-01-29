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

import org.apache.log4j.Logger;

import shef.mt.enes.FeatureExtractorSimple;



public class FeatureExtraction {

	private FeatureExtractorSimple fE;
	private boolean debug = false;

	public static final Logger log = Logger.getLogger(FeatureExtraction.class.getName());
	
	
	/**
	 * Initialize the feature extractor
	 *
	 * @param  configFile  configuration file for QuEst
	 * @param  WD the updated version of quest creates temporary a folder for each sentence pair, that is then deleted. This parameter
	 * 						sets the main folder where the temporary folder are created
	 * @param  deb	enable or disable comment and error messages
	 * @param  preProcText  enable and disable trucasing and tokanization of the sentences: true: already preprocessed text false: text to be preprocessed
	 *  
	 * @return      list of features into the standard output
	 * @author Turchi Marco
	 */
	public FeatureExtraction(String configFile, String wD, boolean deb, String preProcText) throws Exception  {
		// TODO Auto-generated constructor stub
		
		debug=deb;
		if (debug) {
			System.out.println("Features: "+preProcText);
		}
		//load all the resources...
		//String[] arg = {"-config", configFile, "-lang", "english spanish", "-mode","bb"};
		String[] arg = {"-config", configFile, "-mode","bb"};
		
		fE = new FeatureExtractorSimple(arg, wD, preProcText, debug);
		if (debug)
			log.info("Initialized Feature Extractor");
	}
	
	
	
	/**
	 * Extract the features
	 *
	 * @param  source  source sentece
	 * @param  target  target sentence
	 *  
	 * @return      string of features
	 * @author Turchi Marco
	 */
	public String getFeatures(String source, String target) throws Exception {
		String features = fE.run(source, target);
		if(debug){
			log.info("Features extracted: ");
			log.info(features);
		}
		
		return features;
	}

}
