

package updatedQuEst;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.*;
import org.apache.xmlrpc.*;
import org.apache.xmlrpc.webserver.*;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import jsc.util.Rank;


import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import ClientQuest.*;


import java.net.*;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import com.memetix.mst.detect.Detect;
import shef.mt.util.PropertiesManager;

public class JavaServer {
    
    private static String srcLang ;
    private static String tgtLang ;
    private static int port = 0;
    private static PropertiesManager resourceManager;
    
    public void setLanguage(String src, String tgt){
        this.srcLang = src;
        this.tgtLang = tgt;
    }
    
    public String getsrcLanguage(){
        return srcLang;
    }
    
    public String gettgtLanguage(){
        return tgtLang;
    }
            

  public static final Logger log = Logger.getLogger(UpdatedQuEstMain.class.getName());  


public Object getAllPredictions(String sent) throws Exception {
    // Set your Windows Azure Marketplace client info - See http://msdn.microsoft.com/en-us/library/hh454950.aspx
    
    
    String bing_id = resourceManager.getString("bing.id");
    String bing_scrt = resourceManager.getString("bing.secret");
    Translate.setClientId(bing_id);
    Translate.setClientSecret(bing_scrt);

   System.out.println("Sent is = " + sent);
    
    StringTokenizer st = new StringTokenizer(sent, "\t");
    System.out.println("Number of tokens = " + st.countTokens());
    if (st.countTokens() != 2)
        return 0;
    
        String id = st.nextToken();
        System.out.println(id);
        String src = st.nextToken();
          
        
        // String src = new String(sent.getBytes("UTF-8"), "UTF-8");
        
      //  String src = new String(sent);
   
    
        Language detectedLanguage = Detect.execute(src);
        // Prints out the language code
        System.out.println("source language is = " + detectedLanguage);
        System.out.println("source text is = " + src);
         String tgt = Translate.execute(src, detectedLanguage, Language.ENGLISH);
        // String new_tgt = new String(tgt.getBytes("UTF-8"), "UTF-8");
        System.out.println("Source -> English : " + tgt);
        
        int qcp = Integer.parseInt(resourceManager.getString("quest.port"));
        int qlp = Integer.parseInt(resourceManager.getString("learn.port"));
        
        ClientQuEst client = new ClientQuEst("localhost", qcp, false);
        String m = client.getFeatures(src, tgt);
        //String m = client.getFeatures("ciao", "Hello");
        ClientLearn learn = new ClientLearn("localhost", qlp, false);
        double pred = learn.getPredictions(m);
        
        //String all_pred =  id + "\t" + src + "\t" + tgt + "\t" + pred;
         String all_pred =   src + "\t" + tgt + "\t" + pred;
    
    return all_pred;
    
        
   //     return  id + "\t" + text + "\t" + translatedText;
}


public Object getTranslation(String sent) throws Exception{
    // Set your Windows Azure Marketplace client info - See http://msdn.microsoft.com/en-us/library/hh454950.aspx
    System.out.println("translating");
    System.out.println("Sent is = " + sent);
    try{
    
     String bing_id = resourceManager.getString("bing.id");
    String bing_scrt = resourceManager.getString("bing.secret");
    Translate.setClientId(bing_id);
    Translate.setClientSecret(bing_scrt);    
        
    
    }catch(Throwable t){
        System.out.println(t.getCause());
    }
        
  // System.out.println("Sent is = " + sent);
    
    StringTokenizer st = new StringTokenizer(sent, "|||");
    System.out.println("Number of tokens = " + st.countTokens());
    boolean translated = false;
    if (st.countTokens() == 3){
        translated = true;
    }
    
    
        String id = st.nextToken();
        System.out.println(id);
        String src = st.nextToken();
        src = src.trim();
        String tgt = null;
     
      
        try{
        Language detectedLanguage = Detect.execute(src);
        String dls = detectedLanguage.toString();
        String dlt = Language.ENGLISH.toString();
        //detectedLanguage.g
        // Prints out the language code
       // System.out.println("source language is = " + detectedLanguage);
        System.out.println("source language is = " + dls);
        System.out.println("target language is = " + dlt);
        this.setLanguage(dls, dlt);
        
        System.out.println("source text is = " + src);
       
        if (translated){
             tgt = st.nextToken();
             tgt = tgt.trim();
        }
        else{
            tgt = Translate.execute(src, detectedLanguage, Language.ENGLISH);
        }
        
        }catch(Throwable t){
            System.out.println(t.getCause());
            System.out.println(t.getMessage());
        }
            System.out.println("Source -> English : " + tgt);
        
        
        
        //String all_pred =  id + "\t" + src + "\t" + tgt + "\t" + pred;
         String all_trans =   src + "\t" + tgt  ;
    
    return all_trans;
    
        
   //     return  id + "\t" + text + "\t" + translatedText;
}



public static int randInt(int min, int max) {

    // Usually this can be a field rather than a method variable
    Random rand = new Random();

    // nextInt is normally exclusive of the top value,
    // so add 1 to make it inclusive
    int randomNum = rand.nextInt((max - min) + 1) + min;

    return randomNum;
}


public static int nextIntInRangeButExclude1(int start, int end, int... excludes){
    int rangeLength = end - start + 1 - excludes.length;
    
    Random rand = new Random();
    int randomInt = rand.nextInt(rangeLength) + start;
    
    ArrayList<Integer> temp = new ArrayList<Integer>();
    for(int i = 0; i < excludes.length; i++) {
        temp.add(excludes[i]);
    }
    
    Collections.sort(temp);
    
    
    
    for(int i = 0; i < excludes.length; i++) {
        
        if(excludes[i] > randomInt) {
            return randomInt;
        }

        randomInt++;
    }

    return randomInt;
}

public static int nextIntInRangeButExclude(int start, int end, int... excludes){
    int rangeLength = end - start + 1 - excludes.length;
    
    Random rand = new Random();
    int randomInt = rand.nextInt(rangeLength) + start;
    
    ArrayList<Integer> temp = new ArrayList<Integer>();
    for(int i = 0; i < excludes.length; i++) {
        temp.add(excludes[i]);
    }
    
    Collections.sort(temp);
    
    
    
    for(int i = 0; i < excludes.length; i++) {
        
        if(temp.get(i) > randomInt) {
            return randomInt;
        }

        randomInt++;
    }

    return randomInt;
}

public static ArrayList Rank(ArrayList values)
{
    ArrayList sortedValues = new ArrayList(values);
    Collections.sort(sortedValues, Collections.reverseOrder());
 
    ArrayList ranks = new ArrayList();
 
    for (int i=0; i<values.size(); i++)
        ranks.add(sortedValues.indexOf(values.get(i)));
 
    return ranks;
}

public Object getFeatures(String sent){
    StringTokenizer st = new StringTokenizer(sent, "\t");
    System.out.println(sent);
    System.out.println(st.countTokens());
    
    if (st.countTokens() != 2)
        return 0;
    
    //String id = st.nextToken();
      //  System.out.println(id);
        String src = st.nextToken();
        System.out.println(src);
        String tgt = st.nextToken();
        System.out.println(tgt);
        
        String l_src = getsrcLanguage();
        
        ClientQuEst client = null;
        System.out.println("detected src lang is = " + l_src);
        if (l_src.equals("de")){
             client = new ClientQuEst("localhost", 7773, false);
        }else if (l_src.equals(Language.FRENCH)) {
            client = new ClientQuEst("localhost", 8883, false);
        }else{
            System.out.println("Language not supported");
            return 0;
        }
        
        String m = client.getFeatures(src, tgt);
        //ClientLearn learn = new ClientLearn("localhost", 7774, false);
        //double pred = learn.getPredictions(m);
        
        String all_feats =  src + "\t" + tgt + "\t" + m;
    
    return all_feats;
}

public Object getPredictions(String sent){
    StringTokenizer st = new StringTokenizer(sent, "\t");
    System.out.println(sent);
    System.out.println(st.countTokens());
    
    if (st.countTokens() != 2)
        return 0;
    
    //String id = st.nextToken();
      //  System.out.println(id);
        String src = st.nextToken();
        System.out.println(src);
        String tgt = st.nextToken();
        System.out.println(tgt);
        
        String l_src = getsrcLanguage();
        
        ClientQuEst client = null;
        ClientLearn learn = null;
        int qcp = Integer.parseInt(resourceManager.getString("quest.port"));
        int qlp = Integer.parseInt(resourceManager.getString("learn.port"));
        
        
        if (l_src.equals("de")){
             client = new ClientQuEst("localhost", 7773, false);
             learn = new ClientLearn("localhost", 7774, false);
        }else if (l_src.equals(Language.FRENCH)) {
            client = new ClientQuEst("localhost", 8883, false);
            learn = new ClientLearn("localhost", 8884, false);
        }else{
            System.out.println("Language not supported");
            return 0;
        }
        
        
        //ClientQuEst client = new ClientQuEst("localhost", 7773, false);
        String m = client.getFeatures(src, tgt);
        
        double pred = learn.getPredictions(m);
        
        String all_pred =  src + "\t" + tgt + "\t" + pred;
    
    return all_pred;
}
public Object callQuest_wth_line(String sent){
    StringTokenizer st = new StringTokenizer(sent, "\t");
    
    if (st.countTokens() != 5)
        return 0;
    
        String id = st.nextToken();
        System.out.println(id);
        String src = st.nextToken();
        System.out.println(src);
        String moses = st.nextToken();
        System.out.println(moses);
        String google = st.nextToken();
        System.out.println(google);
        String lucy = st.nextToken();
        System.out.println(lucy); 
        
         ClientQuEst client = new ClientQuEst("localhost", 7773, false);
   
    String m = client.getFeatures(src, moses);
    String g = client.getFeatures(src, google);
    String l = client.getFeatures(src, lucy);
    
    ClientLearn learn = new ClientLearn("localhost", 7774, false);
    double p_moses = learn.getPredictions(m);
    double p_google = learn.getPredictions(g);
    double p_lucy = learn.getPredictions(l);
    
   
    ArrayList<Double> systems = new ArrayList<Double>();
    systems.add(p_moses);
    systems.add(p_google);
    systems.add(p_lucy);
    ArrayList<Integer> ranked_systems = new ArrayList<Integer>();
    ranked_systems = Rank(systems);
    
    int r_moses = ranked_systems.get(0)+1; // add 1 to make range 1-3 
    int r_google = ranked_systems.get(1)+1;
    int r_lucy = ranked_systems.get(2)+1;
    
    if (r_moses == r_lucy && r_google == r_lucy && r_moses == r_google){
        //r_lucy = Collections.min(ranked_systems);
       // r_lucy = 1;  // defaut highest rank to lucy
        r_lucy = nextIntInRangeButExclude(1,3,4); // 4 is already excluded
        
  //  }
    
   // if(r_moses == r_google){
        //r_moses = randInt(1,3);
        r_google = nextIntInRangeButExclude(1,3,r_lucy);
       // ArrayList<Integer> temp = new ArrayList<Integer>();
       // temp.add(r_lucy);
       // temp.add(r_google);
       // Collections.sort(temp);
       // int temp0 = temp.get(0);
       // int temp1 = temp.get(1);
        r_moses = nextIntInRangeButExclude(1,3,r_lucy,r_google);
    } else if (r_moses == r_google)
            {
                r_google = nextIntInRangeButExclude(1,3,r_lucy);
                r_moses = nextIntInRangeButExclude(1,3,r_lucy,r_google);
            }
      else if (r_moses == r_lucy)
      {
          r_moses = nextIntInRangeButExclude(1,3,r_google);
          r_lucy = nextIntInRangeButExclude(1,3,r_google,r_moses);
      }else if (r_google == r_lucy)
      {
          r_lucy = nextIntInRangeButExclude(1,3,r_moses);
          r_google = nextIntInRangeButExclude(1,3,r_lucy,r_moses);
      }
    
    
    // r_moses = randInt(1,3);
    // r_google = randInt(1,3);
   //  r_lucy = randInt(1,3);
    
    String all = "id =" + id + "src = " +src+ "moses= " +m+
            "goolge =" +g+ "lucy =" +l;
          //  "<seg id=\"1\"><src>"+src+" </src><moses>"+m+" <moses>"
          //  +"<google>"+g+" <google></seg>";
    // return client.getFeatures(src, tgt);
    String all_pred =  id + "\t" + src + "\t" + p_moses + "\t" + moses + "\t" 
            + p_google + "\t" + google + "\t"
            + p_lucy + "\t" + lucy;
    
    String all_rank =  id + "\t" + src + "\t" + r_moses + "\t" + moses + "\t" 
            + r_google + "\t" + google + "\t"
            + r_lucy + "\t" + lucy;
    
    System.out.println(all_pred);
    System.out.println(all_rank);
    
    String rank_plus_pred = all_rank + "\n" + all_pred;
    
    return all_rank; 
   // return rank_plus_pred;
  
        
}

public Object getRank(String sent){
    StringTokenizer st = new StringTokenizer(sent, "\t");
    
    if (st.countTokens() < 4)
        return 0;
    
        String id = st.nextToken();
        System.out.println(id);
        String src = st.nextToken();
        System.out.println(src);
        
        //String Systems = "";
         ArrayList<String> systems_All = new ArrayList<String>();
        for (int i=2; i<st.countTokens(); i++){
            systems_All.add(st.nextToken());
        }
        
        
    ClientQuEst client = new ClientQuEst("localhost", 7773, false);
   ArrayList<String> systems_Features = new ArrayList<String>();
    for (int j=0; j<systems_All.size(); j++){
        systems_Features.add(client.getFeatures(src, systems_All.get(j)));
        
    }
    
    ArrayList<Double> systems_Predictions = new ArrayList<Double>();
    ClientLearn learn = new ClientLearn("localhost", 7774, false);
    for (int j=0; j<systems_All.size(); j++){
        systems_Predictions.add(learn.getPredictions(systems_Features.get(j)));
        
    }
    
    
    
    
    ArrayList<Integer> ranked_systems = new ArrayList<Integer>();
    ranked_systems = Rank(systems_Predictions);
    
    for (int j=0; j<systems_All.size(); j++){
        ranked_systems.set(j, ranked_systems.get(j)+1);
    }
    
    
    for (int j=0; j<systems_All.size(); j++){
        for (int k=j; k<systems_All.size(); k++){
            if (ranked_systems.get(j) == ranked_systems.get(k+1)){
                 ranked_systems.set(j, nextIntInRangeButExclude(1,systems_All.size()+1,ranked_systems.get(j)));
            }
        }
    }
     
    
    
    
    
    // r_moses = randInt(1,3);
    // r_google = randInt(1,3);
   //  r_lucy = randInt(1,3);
    
    String all_rank = "";
    for (int j=0; j<systems_All.size(); j++){
        all_rank += ranked_systems.get(j) + "\t";
    }
    
    
    System.out.println(all_rank);
    
    //String rank_plus_pred = all_rank + "\n" + all_pred;
    
    return all_rank; 
   // return rank_plus_pred;
  
        
}

public Object callQuest(String id, String src, String moses, String google, String lucy){
    
   // StringTokenizer st = new StringTokenizer(sent, "\t");
  //  while (st.hasMoreTokens()) {
    //    System.out.println(st.nextToken());
   // }

    System.out.println(id + src + moses + google + lucy);
    
    
   
    ClientQuEst client = new ClientQuEst("localhost", 9111, false);
   
    String m = client.getFeatures(src, moses);
    String g = client.getFeatures(src, google);
    String l = client.getFeatures(src, lucy);
    
    ClientLearn learn = new ClientLearn("localhost", 9222, false);
    double p_moses = learn.getPredictions(m);
    double p_google = learn.getPredictions(g);
    double p_lucy = learn.getPredictions(l);
    
  //  int r_moses = randInt(1,3);
  //  int r_google = randInt(1,3);
  //  int r_lucy = randInt(1,3);
    
    
    ArrayList<Double> systems = new ArrayList<Double>();
    systems.add(p_moses);
    systems.add(p_google);
    systems.add(p_lucy);
    ArrayList<Integer> ranked_systems = new ArrayList<Integer>();
    ranked_systems = Rank(systems);
    
    int r_moses = ranked_systems.get(0);
    int r_google = ranked_systems.get(1);
    int r_lucy = ranked_systems.get(2);
    
    String all = "id =" + id + "src = " +src+ "moses= " +m+
            "goolge =" +g+ "lucy =" +l;
          //  "<seg id=\"1\"><src>"+src+" </src><moses>"+m+" <moses>"
          //  +"<google>"+g+" <google></seg>";
    // return client.getFeatures(src, tgt);
    String all_pred =  id + "\t" + src + "\t" + p_moses + "\t" + moses + "\t" 
            + p_google + "\t" + google + "\t"
            + p_lucy + "\t" + lucy;
    
    String all_rank =  id + "\t" + src + "\t" + r_moses + "\t" + moses + "\t" 
            + r_google + "\t" + google + "\t"
            + r_lucy + "\t" + lucy;
    
    return all_rank;
}
	
	
 public Object Test(String sent){
     System.out.println("just testing");
     return 0;
 }
    

    public static void main (String [] args) {
        try {
            
            // Invoke me as <http://localhost:8080/RPC2>.
            System.out.println("Attempting to start XML-RPC Server...");
            
            String s_port = resourceManager.getString("server.port");
            port = Integer.parseInt(s_port);
            WebServer server = new WebServer(port);
           
            
            XmlRpcServer xmlRpcServer = server.getXmlRpcServer();
            
           
             PropertyHandlerMapping phm = new PropertyHandlerMapping();
             phm.addHandler("runQuest", JavaServer.class);
             xmlRpcServer.setHandlerMapping(phm);
        
          XmlRpcServerConfigImpl serverConfig =
              (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
          serverConfig.setEnabledForExtensions(true);
          serverConfig.setContentLengthOptional(false);
          
         

          server.start();
          
          
           System.out.println("Started successfully.");
     System.out.println("Accepting requests. (Halt program to stop.)");  
            
            
        } catch (Exception exception) {
            System.out.println("There is an exception");
            System.err.println("JavaServer: " + exception.toString());
        }
    }
}