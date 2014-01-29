package shef.mt.enes;



import shef.mt.xmlwrap.MOSES_XMLWrapper;
import shef.mt.util.PropertiesManager;
import shef.mt.util.Logger;
import shef.mt.tools.FileModel;
import shef.mt.tools.FileModelOriginal;
import shef.mt.tools.NGramExec;
import shef.mt.tools.PPLProcessorOriginal;
import shef.mt.tools.ResourceManager;
import shef.mt.tools.LanguageModel;
import shef.mt.tools.POSProcessor;
import shef.mt.tools.MTOutputProcessor;
import shef.mt.tools.Tokenizer;
import shef.mt.tools.Giza;
import shef.mt.tools.TopicDistributionProcessor;
import shef.mt.tools.BParserProcessor;
import shef.mt.tools.NGramProcessor;
import shef.mt.tools.PPLProcessor;
import shef.mt.tools.PosTagger;
import shef.mt.tools.GlobalLexicon;
import shef.mt.tools.Triggers;
import shef.mt.tools.TriggersProcessor;
import shef.mt.features.util.Sentence;
import shef.mt.features.util.FeatureManager;
import org.apache.commons.cli.*;

import ClientIrstLM.ClientIrstLM;

import java.io.*;
import java.util.UUID;

import shef.mt.features.impl.Feature;

/**
 * FeatureExtractor extracts Glassbox and/or Blackbox features from a pair of
 * source-target input files and a set of additional resources specified as
 * input parameters Usage: FeatureExtractor -input <source><target> -lang
 * <source lang><target lang> -feat [list of features] -mode [gb|bb|all] -gb
 * [list of GB resources] -rebuild -log <br> The valid arguments are:<br> -help
 * : print project help information<br> -input <source file> <target file> <word
 * alignment file>: the input source and target files<br> -lang <source
 * language> <target language> : source and target language<br> -feat : the list
 * of features. By default, all features corresponding to the selected mode will
 * be included<br> -gb [list of files] input files required for computing the
 * glassbox features<br> The arguments sent to the gb option depend on the MT
 * system -mode <GB|BB|ALL><br> -rebuild : run all preprocessing tools<br> -log
 * : enable logging<br> -config <config file> : use the configuration file
 * <config file>
 *
 *
 * @author Catalina Hallett & Mariano Felice<br>
 * @author Marco Turchi
 * 
 */
// Code modification for 'on the fly' feature extraction. Modified by Marco Turchi Fondazione Bruno Kessler June 2013.

public class FeatureExtractorSimple{

    private static int mtSys;
    private static String workDir;
    private static String workFolder;
    private static String wordLattices;
	
    private static String gizaAlignFile;
    /**
     * path to the input folder
     */
    private static String input;
    /**
     * running mode: bb , gb or all
     */
    private String mod;
    /**
     * path to the output folder
     */
    private static String output;
    private static String sourceFile;
    private static String targetFile;
    private static String sourceLang;
    private static String targetLang;
    private static String features;
	private static String nbestInput;
	private static String onebestPhrases;
	private static String onebestLog;
	private static boolean preProcText;

    private static boolean forceRun = false;
    private static PropertiesManager resourceManager;
    private static FeatureManager featureManager;
    private static int ngramSize = 3;
	private static int IBM = 0;
	private static int MOSES = 1;
    private static String configPath;
	private static String gbXML;
	
	private BParserProcessor sourceParserProcessor;
    private	BParserProcessor targetParserProcessor;
    private TopicDistributionProcessor sourceTopicDistributionProcessor;
    private	TopicDistributionProcessor targetTopicDistributionProcessor;
    private String sourceTopicDistributionFile;
    private String targetTopicDistributionFile;
    private ClientIrstLM sourceLM;
    private ClientIrstLM targetLM;
    private ClientIrstLM posLM;
    
    private static boolean debug = false;
    


	
    /**
	 * set to 0 if the parameter sent to the -gb option is an xml file, 0 otherwise
	 */
	private int gbMode;
    
        /**
     * Initialises the FeatureExtractor from a set of parameters, for example
     * sent as command-line arguments
     *
	 * @param args
	 *            The list of arguments
     *
     */
    public FeatureExtractorSimple(String[] args) {
        workDir = System.getProperty("user.dir");
        new Logger("log.txt");
        parseArguments(args);

        input = workDir + File.separator + resourceManager.getString("input");
        output = workDir + File.separator + resourceManager.getString("output");
        
        System.out.println("input=" + input + "  output=" + output);

    }
    
    
    
	/**
	 * Initialization of the feature extractor. 
	 * This method initializes:
	 * 1) Giza
	 * 2) Global Lexicon
	 * 3) Berkeley Parser (never tested)
	 * 4) topic modelling
	 * 5) source, target and POS lms (creating a connection to the servers)
	 * 6) process and load the ngrams
	 * 7) load the Giza model
	 *
	 * @param  args  configuration arguments
	 * @param  wF the updated version of quest creates temporary a folder for each sentence pair, that is then deleted. This parameter
	 * 						sets the main folder where the temporary folder are created
	 * @param  deb	enable or disable comment and error messages
	 * @param  PreProcText  enable and disable truecasing and tokenization of the sentences: true: already preprocessed text false: text to be preprocessed
	 *  
	 * @return      list of features into the standard output
	 * @author Turchi Marco
	 */
    public FeatureExtractorSimple(String[] args, String wF, String PreProcT, boolean deb) throws Exception  {
    	
    	debug = deb;
        workFolder = wF;
       
        preProcText = Boolean.parseBoolean(PreProcT);
        new Logger("log.txt");
        
     
        parseArguments(args);
        
        if (debug) {
        	System.out.println("PREPROCESSING FLAG: "+preProcText);
        }
        //Initialize Giza
        
        // Load the reference corpus for feature 1036
        // NOTE the actual version of the software does not filter out the words extracted from the reference corpus that do not appear in
        // the source corpus. It keeps everything to make the software usable in the sentence-pair extraction  version
        if(debug)
        	System.out.println("Building FileModel from: "+resourceManager.getString(sourceLang + ".corpus"));
        FileModel fm = new FileModel(
        	      resourceManager.getString(sourceLang + ".corpus"), debug);
        
     
        
        //Initialize Global Lexicon
        boolean gl = false; 
        String temp0 = resourceManager.getString("GL");
        if (temp0.equals("1")) {
            gl = true ;
        }
        
        if (gl) {
         loadGlobalLexicon();
        }

        
        //Initialize Berkeley Parser
        boolean bp = false; 
        String temp = resourceManager.getString("BP");
        if (temp.equals("1")) {
            bp = true ;
        }

        
        if (bp) {
	        sourceParserProcessor = new BParserProcessor();
	        targetParserProcessor = new BParserProcessor();
  
        }
        
        
        //Initialize Topic Modelling
        boolean tm = false; 
        String temp1 = resourceManager.getString("TM");
        if (temp1.equals("1")) {
            tm = true ;
        }
    
      if (tm) {
        sourceTopicDistributionFile = resourceManager.getString(sourceLang + ".topic.distribution");
        targetTopicDistributionFile = resourceManager.getString(targetLang + ".topic.distribution");
        sourceTopicDistributionProcessor = new TopicDistributionProcessor(sourceTopicDistributionFile, "sourceTopicDistribution");
        targetTopicDistributionProcessor = new TopicDistributionProcessor(targetTopicDistributionFile, "targetTopicDistribution");
        
      }
      
      //Initialize lms
      //Source lm

      sourceLM = new ClientIrstLM(resourceManager.getString(sourceLang + ".lm.url"), Integer.parseInt(resourceManager.getString(sourceLang + ".lm.port")), debug); 
      
      //Target lm 
      
       targetLM = new ClientIrstLM(resourceManager.getString(targetLang + ".lm.url"), Integer.parseInt(resourceManager.getString(targetLang + ".lm.port")), debug); 

      //Pos lm 
      
      posLM = new ClientIrstLM(resourceManager.getString(targetLang + ".poslm.url"), Integer.parseInt(resourceManager.getString(targetLang + ".poslm.port")), debug); //resourceManager.getString(sourceLang + ".lm")

      
      processNGrams();
      
      loadGiza();

      
      
    

    }
    
    

   
    /**
     * Parses the command line arguments and sets the respective fields
     * accordingly. This function sets the input source and target files, the
     * source and target language, the running mode (gb or bb), the additional
     * files required by the GB feature extractor, the rebuild and log options
     *
     * @param args The command line arguments
     */
    public void parseArguments(String[] args) {

        Option help = OptionBuilder.withArgName("help").hasArg()
                .withDescription("print project help information")
                .isRequired(false).create("help");

       // Option input = OptionBuilder.withArgName("input").hasArgs(3)
       //         .isRequired(true).create("input");

        Option lang = OptionBuilder.withArgName("lang").hasArgs(2)
                .isRequired(false).create("lang");

        Option feat = OptionBuilder.withArgName("feat").hasArgs(1)
                .isRequired(false).create("feat");

		Option gb = OptionBuilder.withArgName("gb")
				.withDescription("GlassBox input files").hasOptionalArgs(2)
				.hasArgs(3).create("gb");

        Option mode = OptionBuilder
                .withArgName("mode")
                .withDescription("blackbox features, glassbox features or both")
                .hasArgs(1).isRequired(true).create("mode");
        

        Option config = OptionBuilder
                .withArgName("config")
                .withDescription("cofiguration file")
                .hasArgs(1).isRequired(false).create("config");
        


        Option rebuild = new Option("rebuild", "run all preprocessing tools");
        rebuild.setRequired(false);


		
        CommandLineParser parser = new PosixParser();
        Options options = new Options();

        options.addOption(help);
       //options.addOption(input);
        options.addOption(mode);
        options.addOption(lang);
        options.addOption(feat);
		options.addOption(gb);
        options.addOption(rebuild);
        options.addOption(config);
        
        


        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("config")) {
                resourceManager = new PropertiesManager(line.getOptionValue("config"));
            } else {
                resourceManager = new PropertiesManager();
            }
            

           // System.out.println(args);
            /*if (line.hasOption("input")) {
                // print the value of block-size
                String[] files = line.getOptionValues("input");
                sourceFile = files[0];
                targetFile = files[1];
            }*/

           
            
            if (line.hasOption("lang")) {
                String[] langs = (line.getOptionValues("lang")[0]).split(" ");
                //System.out.println(langs[0]);
               // System.out.println(langs[1]);
                sourceLang = langs[0];
                targetLang = langs[1];
                
            } else {
                sourceLang = resourceManager.getString("sourceLang.default");
                targetLang = resourceManager.getString("targetLang.default");
                if(debug)
                	System.out.println("SourceLang: "+sourceLang +" TargetLang: "+targetLang);
            }
            

            
			if (line.hasOption("gb")) {
				String[] gbOpt = line.getOptionValues("gb");
				if(debug){
					for (String s : gbOpt)
						System.out.println(s);
				}
				if (gbOpt.length > 1) {
					mtSys = MOSES;
					nbestInput = gbOpt[0];
					onebestPhrases = gbOpt[1];
					onebestLog = gbOpt[2];
					gbMode = 1;
				} else 
				{
					File f = new File(gbOpt[0]);
					if (f.isDirectory()){
						mtSys = IBM;
						wordLattices = gbOpt[0];
						gbMode = 1;
					}
					else {
						gbMode = 0;
						gbXML = gbOpt[0];
					}

				}
				
					

			}

            if (line.hasOption("mode")) {
                String[] modeOpt = line.getOptionValues("mode");
                setMod(modeOpt[0].trim());
                if(debug)
                	System.out.println(getMod());
                configPath = resourceManager.getString("featureConfig." + getMod());
                if(debug)
                	System.out.println("feature config:" + configPath);
                featureManager = new FeatureManager(configPath);
            }

            if (line.hasOption("feat")) {
                // print the value of block-size
                features = line.getOptionValue("feat");
                featureManager.setFeatureList(features);
            } else {
                featureManager.setFeatureList("all");
            }

            if (line.hasOption("rebuild")) {
                forceRun = true;
            }


        } catch (ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
        }
    }

    public void runPOSTagger() {
        // required by BB features 65-69, 75-80
        String sourceOutput = runPOS(sourceFile, sourceLang, "source");
        String targetOutput = runPOS(targetFile, targetLang, "target");

    }

	
    /**
     * runs the part of speech tagger
     * @param file input file
     * @param lang language
     * @param type source or target
     * @return path to the output file of the POS tagger
     */
    public String runPOS(String file, String lang, String type) {
        String posName = resourceManager.getString(lang + ".postagger");
        String langResPath = input + File.separator + lang;
       // System.out.println("Lang Res 1: "+langResPath);
        File f = new File(file);
        String absoluteSourceFilePath = f.getAbsolutePath();
        String fileName = f.getName();
        String relativeFilePath = langResPath + File.separator + fileName
                + ".pos";
        String absoluteOutputFilePath = (new File(relativeFilePath))
                .getAbsolutePath();
        String posSourceTaggerPath = resourceManager.getString(lang
                    + ".postagger.exePath");
        //System.out.println("PosSourceTaggerPath: "+posSourceTaggerPath);
        String outPath = "";
        try {
            Class c = Class.forName(posName);
            PosTagger tagger = (PosTagger) c.newInstance();
            tagger.setParameters(type, posName, posSourceTaggerPath,
                    absoluteSourceFilePath, absoluteOutputFilePath);
            PosTagger.ForceRun(forceRun);
            outPath = tagger.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // returns the path of the output file; this is for convenience only so
        // we do't have to calculate it again
        return outPath;

    }

    private static void loadGiza() {
    	if(debug)
    		System.out.println("SourceLang: "+sourceLang +" TargetLang: "+targetLang);
    	
        String gizaPath = resourceManager.getString("pair." + sourceLang
                + targetLang + ".giza.path");
        if(debug)
        	System.out.println("GIZA PATH: "+gizaPath);
        Giza giza = new Giza(gizaPath);
    }
    
    private static void loadGlobalLexicon() {
        final String glmodelpath = resourceManager.getString("pair." + sourceLang
                + targetLang + ".glmodel.path");
        final Double minweight = Double.valueOf(
                resourceManager.getString("pair." + sourceLang
                    + targetLang + ".glmodel.minweight"));
        GlobalLexicon globalLexicon = new GlobalLexicon(glmodelpath, minweight);
    }

    /*
     * Computes the perplexity and log probability for the source file Required
     * by features 8-13
     */
    private static void runNGramPPL() {
        // required by BB features 8-13
        NGramExec nge = new NGramExec(
                resourceManager.getString("tools.ngram.path"));
        System.out.println("runNgramPPL");
        File f = new File(sourceFile);
        String sourceOutput = input
                + File.separator + sourceLang + File.separator + f.getName()
                + ".ppl";
        f = new File(targetFile);
        String targetOutput = input
                + File.separator + targetLang + File.separator + f.getName()
                + ".ppl";
        nge.runNGramPerplex(sourceFile, sourceOutput,
                resourceManager.getString(sourceLang + ".lm"));
        System.out.println(resourceManager.getString(targetLang + ".lm"));
        nge.runNGramPerplex(targetFile, targetOutput,
                resourceManager.getString(targetLang + ".lm"));
    }

    /**
     * Computes the perplexity and log probability for the POS tagged target
     * file<br> Required by BB features 68-69<br> This function could be merged
     * with
     *
     * @seerunNGramPPL() but I separated them to make the code more readable
     *
     * @param posFile file tagged with parts-of-speech
     */
    private String runNGramPPLPos(String posFile) {
        NGramExec nge = new NGramExec(
                resourceManager.getString("tools.ngram.path"), forceRun);

        File f = new File(posFile);
        String posTargetOutput = input
                + File.separator + targetLang + File.separator + f.getName()
                + resourceManager.getString("tools.ngram.output.ext");
        nge.runNGramPerplex(posFile, posTargetOutput,
                resourceManager.getString(targetLang + ".poslm"));
        return posTargetOutput;
    }

    /**
     * Performs some basic processing of the input source and target files For
     * English, this consists of converting the input to lower case and
     * tokenizing For Arabic, this consists of transliteration and tokenization.
     * Please note that the current tools used for tokenizing Arabic also
     * perform POS tagging and morphological analysis Although we could separate
     * the tokenization process from the more in-depth text analysis performed
     * by these tools, for efficiency reasons this is not desirable The input
     * files are also copied to the /input folder. This is necessary because the
     * MADA analyser produces its output in the same folder as the input file,
     * which may cause problems if the right access rights are not available for
     * that particular folder
     * 
     */
    private static void preprocessing(boolean preProcessed)  {
        String sourceInputFolder = input + File.separator + sourceLang;
        String targetInputFolder = input + File.separator + targetLang;
        File origSourceFile = new File(sourceFile);
        File inputSourceFile = new File(sourceInputFolder + File.separator + origSourceFile.getName());
        if(debug){
	        System.out.println("source input:" + sourceFile);
	        System.out.println("target input:" + targetFile);
        }
        File origTargetFile = new File(targetFile);
        File inputTargetFile = new File(targetInputFolder + File.separator + origTargetFile.getName());
        try {
        	if(debug)
        		System.out.println("copying input to " + inputSourceFile.getPath());
            copyFile(origSourceFile, inputSourceFile);
            if(debug)
            	System.out.println("copying input to " + inputTargetFile.getPath());
            copyFile(origTargetFile, inputTargetFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!preProcessed){
	        //run tokenizer for source (English)
        	if(debug)
        		System.out.println("running tokenizer");
	       
	       String src_abbr = ""; 
	        if (sourceLang.equals ("english"))
	                src_abbr = "en";
	            else if (sourceLang.equals ("spanish"))
	                src_abbr = "es";
	            else if (sourceLang.equals ("french"))
	                src_abbr = "fr";
	            else if (sourceLang.equals ("german"))
	                src_abbr = "de"; 
	            else if (sourceLang.equals ("italian"))
	                    src_abbr = "it"; 
	            else 
	            	if(debug)
	            		System.out.println("Don't recognise the source language");
	        
	        
	        String tgt_abbr = ""; 
	        if (targetLang.equals ("english"))
	                tgt_abbr = "en";
	            else if (targetLang.equals ("spanish"))
	                tgt_abbr = "es";
	            else if (targetLang.equals ("french"))
	                tgt_abbr = "fr";
	            else if (targetLang.equals ("german"))
	                tgt_abbr = "de"; 
	            else if (sourceLang.equals ("italian"))
	                src_abbr = "it"; 
	            else 
	            	if(debug)
	            		System.out.println("Don't recognise the target language");
	        
	                
	        String truecasePath = "";
	        truecasePath = resourceManager.getString(sourceLang + ".truecase") + "|" + resourceManager.getString(sourceLang + ".truecase.model");
	        Tokenizer enTok = new Tokenizer(inputSourceFile.getPath(), inputSourceFile.getPath() + ".tok", truecasePath, resourceManager.getString(sourceLang + ".tokenizer"), src_abbr, forceRun);
	        
	        
	        // Tokenizer enTok = new Tokenizer(inputSourceFile.getPath(), inputSourceFile.getPath() + ".tok", resourceManager.getString("english.lowercase"), resourceManager.getString("english.tokenizer"), "en", forceRun);
	        enTok.run();
	        sourceFile = enTok.getTok();
	        if(debug)
	        	System.out.println(sourceFile);
	
	        //run tokenizer for target (Spanish)
	        if(debug)
	        	System.out.println("running tokenizer");
	//        Tokenizer esTok = new Tokenizer(inputTargetFile.getPath(), inputTargetFile.getPath() + ".tok", resourceManager.getString("spanish.lowercase"), resourceManager.getString("spanish.tokenizer"), "es", forceRun);
	       
	         truecasePath = resourceManager.getString(targetLang + ".truecase") + "|" + resourceManager.getString(targetLang + ".truecase.model");
	         Tokenizer esTok = new Tokenizer(inputTargetFile.getPath(),inputTargetFile.getPath() + ".tok", truecasePath, resourceManager.getString(targetLang + ".tokenizer"), tgt_abbr, forceRun);
	        
	        esTok.run();
	        sourceFile = enTok.getTok();
	        if(debug)
	        	System.out.println("Tokenized File: "+targetFile);
        } else{
        	File inputSourceTokFile = new File(sourceInputFolder + File.separator + origSourceFile.getName()+".tok");
        	File inputTargetTokFile = new File(targetInputFolder + File.separator + origTargetFile.getName()+".tok");
        	
        	
            try {
            	if(debug)
            		System.out.println("copying input to " + inputSourceTokFile.getPath());
				copyFile(origSourceFile, inputSourceTokFile);
				if(debug)
					System.out.println("copying input to " + inputTargetTokFile.getPath());
		        copyFile(origTargetFile, inputTargetTokFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
           
        	
        	sourceFile = inputSourceTokFile.toString();
        	targetFile = inputTargetTokFile.toString();
        	
        }

        // Normalize files to avoid strange characters in UTF-8 that may break the PoS tagger
        //normalize_utf8();

    }

    private static LanguageModel processNGrams() {
        // required by BB features 30-44
        NGramProcessor ngp = new NGramProcessor(
                resourceManager.getString(sourceLang + ".ngram"));
        if(debug){
        	System.out.println("Processing here");
        	System.out.println("Processing file: "+resourceManager.getString(sourceLang + ".ngram"));
        }
        return ngp.run();
    }

    /**
     * constructs the folders required by the application. These are, typically:
     * <br> <ul><li>/input and subfolders <ul> <li>/input/<i>sourceLang</i>,
     * /input/<i>targetLang</i> (for storing the results of processing the input
     * files with various tools, such as pos tagger, transliterator,
     * morphological analyser),<br> <li>/input/systems/<i>systemName</i> (for
     * storing system specific resources - for example, the compiled and
     * processed word lattices in the case of the IBM system </ul> <li> /output
     * (for storing the resulting feature files), </ul>
     */
    public void constructFolders() {

        File f = new File(input);
        if (!f.exists()) {
            f.mkdir();
            if(debug)
            	System.out.println("folder created " + f.getPath());
        }


        f = new File(input + File.separator + sourceLang);
        if (!f.exists()) {
            f.mkdir();
            if(debug)
            	System.out.println("folder created " + f.getPath());
        }
        
        f = new File(input + File.separator + targetLang);
        if (!f.exists()) {
            f.mkdir();
            if(debug)
            	System.out.println("folder created " + f.getPath());
        }
        f = new File(input + File.separator + targetLang + File.separator
                + "temp");
        if (!f.exists()) {
            f.mkdir();
            if(debug)
            	System.out.println("folder created " + f.getPath());
        }

        String output = resourceManager.getString("output");
        f = new File(output);
        if (!f.exists()) {
            f.mkdir();
            if(debug)
            	System.out.println("folder created " + f.getPath());
        }
    }

    /**
     * Runs the Feature Extractor<br> <ul> <li>constructs the required folders
     * <li>runs the pre-processing tools <li>runs the BB features, GB features
     * or both according to the command line parameters </ul>
     */
	public  String initialiseGBResources() {
		// transform the m output to xml
		String xmlOut = resourceManager.getString("input") + File.separator
				+ "systems" + File.separator;
		File f = new File(sourceFile);
		if (mtSys == MOSES) {
			xmlOut += "moses_" + f.getName() + ".xml";
			if(debug)
				System.out.println(xmlOut);
			MOSES_XMLWrapper cmuwrap = new MOSES_XMLWrapper(nbestInput, xmlOut,
					onebestPhrases, onebestLog);
			cmuwrap.run();
   
			// now send the xml output from cmuwrap to be processed
		} 
                
		return xmlOut;
	}

	
 

    
    
    /**
     * runs the BB features from a sentence pair
     */
    public String runBBSlim() {
    	
    	String extractedFeatures="";
        File f = new File(sourceFile);
        String sourceFileName = f.getName();
        f = new File(targetFile);
        String targetFileName = f.getName();
 
        String pplSourcePath = input
                + File.separator + sourceLang + File.separator + sourceFileName
                + resourceManager.getString("tools.ngram.output.ext");
        //String pplTargetPath = resourceManager.getString("input")
        String pplTargetPath = input
                + File.separator + targetLang + File.separator + targetFileName
                + resourceManager.getString("tools.ngram.output.ext");


        //String pplPOSTargetPath = resourceManager.getString("input")
        //        + File.separator + targetLang + File.separator + targetFileName + PosTagger.getXPOS()
       //         + resourceManager.getString("tools.ngram.output.ext");
        
        long start3 = System.currentTimeMillis();
   	
   
        //Time Consuming
 /*       runNGramPPL();

        PPLProcessorOriginal pplProcSource = new PPLProcessorOriginal(pplSourcePath,
                new String[]{"logprob", "ppl", "ppl1"});
        PPLProcessorOriginal pplProcTarget = new PPLProcessorOriginal(pplTargetPath,
                new String[]{"logprob", "ppl", "ppl1"});*/
        
        
        
        // new implementation of the perplexity extractor
        PPLProcessor pplProcSource = new PPLProcessor(sourceLM,
                new String[]{"logprob", "ppl", "ppl1"});
        PPLProcessor pplProcTarget = new PPLProcessor(targetLM,
                new String[]{"logprob", "ppl", "ppl1"});
        
        long elapsed3 = System.currentTimeMillis() - start3;
        if(debug)
        	System.out.println("Time spent to Run Perplexity " + elapsed3 / 1000F + " sec");
        
        
        
      //This is time consuming
       //  FileModel fm = new FileModel(sourceFile,
      //          resourceManager.getString(sourceLang + ".corpus"));
        
       //   FileModel fm = new FileModel(sourceFile,
       //         resourceManager.getString("source" + ".corpus"));
        
        
        
        String sourcePosOutput = runPOS(sourceFile, sourceLang, "source");
        String targetPosOutput = runPOS(targetFile, targetLang, "target");
     
        
        /*//Run the creation of the ppl file on the POS sentence
        String targetPPLPos = runNGramPPLPos(targetPosOutput + PosTagger.getXPOS());
        System.out.println("---------TARGET PPLPOS: " + targetPPLPos); */
       
      /* PPLProcessorOriginal pplPosTarget = new PPLProcessorOriginal(targetPPLPos,
               new String[]{"poslogprob", "posppl", "posppl1"});*/

        PPLProcessor pplPosTarget = new PPLProcessor(posLM,
                new String[]{"poslogprob", "posppl", "posppl1"});
        
        //This is time consuming
        //loadGiza();
        
       // processNGrams();
        
        /*       boolean gl = false; 
       String temp0 = resourceManager.getString("GL");
        if (temp0.equals("1")) {
            gl = true ;
        }
        
        if (gl) {
         loadGlobalLexicon();
        }*/
        
        try {
            BufferedReader brSource = new BufferedReader(new FileReader(
                    sourceFile));
            BufferedReader brTarget = new BufferedReader(new FileReader(
                    targetFile));
           // BufferedWriter output = new BufferedWriter(new FileWriter(out));
            BufferedReader posSource = null;
            BufferedReader posTarget = null;
            boolean posSourceExists = ResourceManager
                    .isRegistered("sourcePosTagger");
            boolean posTargetExists = ResourceManager
                    .isRegistered("targetPosTagger");
            POSProcessor posSourceProc = null;
            POSProcessor posTargetProc = null;

            if(debug)
            	ResourceManager.printResources();
            Sentence sourceSent;
            Sentence targetSent;
            int sentCount = 0;

            String lineSource = brSource.readLine();
            String lineTarget = brTarget.readLine();
            

            if((lineSource != null) && (lineTarget != null)) {
                //lineSource = lineSource.trim().substring(lineSource.indexOf(" ")).replace("+", "");
                sourceSent = new Sentence(lineSource, sentCount);
                targetSent = new Sentence(lineTarget, sentCount);

           
                sourceSent.computeNGrams(3);
                targetSent.computeNGrams(3);
                pplProcSource.processNextSentence(sourceSent);
                pplProcTarget.processNextSentence(targetSent);
                
                
                //read sentence in file target.txt.tok.pos.XPOS
                //Get only POS sentence form the target PPLPos file
                String targetPosSent  =  readPosSentence(targetPosOutput + PosTagger.getXPOS());
                
                
                pplPosTarget.processNextSentence(new Sentence(targetPosSent,0) );
             

                ++sentCount;
                extractedFeatures = featureManager.runFeatures(sourceSent, targetSent);
             
            }


            brSource.close();
            brTarget.close();
          
            Logger.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return extractedFeatures;
    }

    
    private String readPosSentence(String PosFile){
    	if(debug)
    		System.out.println("XPOS FILE NAME: "+PosFile);
    	
    	BufferedReader br=null;
    	
    	try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(PosFile), "utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	String line="";
    	  try {
    		  if (br != null)
    			   line = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
        try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	
    	
    	return line;
    }
    
    
    private static void copyFile(File sourceFile, File destFile)
            throws IOException {
        if (sourceFile.equals(destFile)) {
        	if(debug)
        		System.out.println("source=dest");
            return;
        }
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        java.nio.channels.FileChannel source = null;
        java.nio.channels.FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    /**
     * returns the working mode: bb, gb or all
     *
     * @return the working mode
     */
    public String getMod() {
        return mod;
    }

    /**
     * sets the working mode
     *
     * @param mod the working mode. Valid values are bb, gb and all
     */
    public void setMod(String mod) {
        this.mod = mod;
    }


	/**
	 * runs the GB features
	 */
	public  void runGB() {
		MTOutputProcessor mtop = null;

		if (gbMode == 1)
			gbXML = initialiseGBResources();

		String nbestSentPath = resourceManager.getString("input")
				+ File.separator + targetLang + File.separator + "temp";
		String ngramExecPath = resourceManager.getString("tools.ngram.path");

		mtop = new MTOutputProcessor(gbXML, nbestSentPath, ngramExecPath,
				ngramSize);
//		MorphAnalysisProcessor map = new MorphAnalysisProcessor(madaFile);

		File f = new File(sourceFile);
		String sourceFileName = f.getName();
		f = new File(targetFile);
		String targetFileName = f.getName();

		String outputFileName = sourceFileName + "_to_" + targetFileName
				+ ".out";

		String out = resourceManager.getString("output") + File.separator + getMod()
				+ outputFileName;
		if(debug)
			System.out.println("Output will be: " + out);

		String lineTarget;

		try {
			BufferedReader brSource = new BufferedReader(new FileReader(
					sourceFile));
			BufferedReader brTarget = new BufferedReader(new FileReader(
					targetFile));
			BufferedWriter output = new BufferedWriter(new FileWriter(out));

			ResourceManager.printResources();

			Sentence targetSent;
			Sentence sourceSent;
			int sentCount = 0;

			String lineSource;

			while (((lineSource = brSource.readLine()) != null)
					&& ((lineTarget = brTarget.readLine()) != null)) {

				lineSource = lineSource.trim().substring(lineSource.indexOf(" "));
				sourceSent = new Sentence(lineSource,
						sentCount);
				targetSent = new Sentence(lineTarget, sentCount);

                //map.processNextSentence(sourceSent);
				mtop.processNextSentence(sourceSent);

				++sentCount;
				output.write(featureManager.runFeatures(sourceSent, targetSent));
				output.write("\r\n");

}
			brSource.close();
			brTarget.close();
			output.close();
			featureManager.printFeatureIndeces();
			Logger.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
        }

	
	
	/**
	 * Extract the enabled features for the source and target sentences
	 *
	 * @param  source source sentence
	 * @param  target target sentence
	 *  
	 * @return      list of features 
	 * @author Turchi Marco
	 */
	
	
 public String run(String source, String target) throws Exception  {
	 
	
	 
	 input =workFolder+"/"+UUID.randomUUID().toString();
	 
	File f = new File(input);
     if (!f.exists()) {
         f.mkdir();
         if(debug)
        	 System.out.println("folder created " + f.getPath());
     }
	 
     long start0 = System.currentTimeMillis();
     
     // for simplicity in the integration of the new 'on the fly' feature extractor with the original code (in particular for the computation of the POS), the source and target sentences are stored in two files
     // in a temporary folder. In this folder are stored all the files needed to extract the features. 
     
	 //build temporary source file
	 try{
		  // Create file 
		  sourceFile = input +"/source.txt";
		  FileWriter fstream = new FileWriter(sourceFile);
		  BufferedWriter out = new BufferedWriter(fstream);
		  out.write(source);
		  //Close the output stream
		  out.close();
	 }catch (Exception e){//Catch exception if any
		  System.err.println("Error: " + e.getMessage());
		  System.exit(1);
	 }
	 
	 //build temporary target file
	 try{
		  // Create file 
		  targetFile= input +"/target.txt";
		  FileWriter fstream = new FileWriter(targetFile);
		  BufferedWriter out = new BufferedWriter(fstream);
		  out.write(target);
		  //Close the output stream
		  out.close();
	 }catch (Exception e){//Catch exception if any
		  System.err.println("Error: " + e.getMessage());
		  System.exit(1);
	 }
	 
	 long elapsed0 = System.currentTimeMillis() - start0;
	 if(debug)
		 System.out.println("Time spent to write the files " + elapsed0 / 1000F + " sec");
	 
	 long start = System.currentTimeMillis();
	 constructFolders();
	 long elapsed = System.currentTimeMillis() - start;
	 if(debug)
		 System.out.println("Time spent to Build the folders " + elapsed / 1000F + " sec");
	 
	 long start2 = System.currentTimeMillis();
	 
	
	
	 
	 preprocessing(preProcText);
	 long elapsed2 = System.currentTimeMillis() - start2;
	 if(debug)
		 System.out.println("Time spent to PreProcessing " + elapsed2 / 1000F + " sec");
	 
	 long start3 = System.currentTimeMillis();
	 String features = runBBSlim();
	 long elapsed3 = System.currentTimeMillis() - start3;
	 if(debug)
		 System.out.println("Time spent to extract the features " + elapsed3 / 1000F + " sec");
	 
	 long start4 = System.currentTimeMillis();
	 //remove temporary folder
	 deleteFolder(new File(input));
	 long elapsed4 = System.currentTimeMillis() - start4;
	 if(debug)
		 System.out.println("Time spent to delete the folder " + elapsed4 / 1000F + " sec");

	 
	 //System.out.println("f: "+features);
	 return features;
 }
 
 
 private static void deleteFolder(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}

}


