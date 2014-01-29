package updatedQuEst;

import java.util.*;
import org.apache.xmlrpc.*;
import org.apache.xmlrpc.client.*;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.net.URL;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class JavaClient {
 public static void main (String [] args) {
  try {
      
      XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
     // config.setServerURL(new URL("http://localhost:9999/RPC2"));
      config.setServerURL(new URL("http://143.167.8.76:35722"));
      //config.setServerURL(new URL("http://www.quest.dcs.shef.ac.uk:35722/RPC2"));
      config.setEnabledForExtensions(true);
      config.setConnectionTimeout(60 * 1000);
      config.setReplyTimeout(60 * 1000);
      
     //XmlRpcClient server = new XmlRpcClient("http://localhost/RPC2");
     XmlRpcClient server = new XmlRpcClient();
    server.setConfig(config);
    
     Vector params = new Vector(); 
  
    
     
     BufferedReader br = null;
 
		try {
 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader("input_quest.txt"));
 
			while ((sCurrentLine = br.readLine()) != null) {
				System.out.println(sCurrentLine);
                                params.addElement(sCurrentLine);
                                Object result2 = server.execute("runQuest.callQuest_wth_line", params);
                                params.removeAllElements();
                                System.out.println("Features extracted");
                                System.out.println(result2 + "\n");
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		

  }
   } catch (Exception exception) {
     System.err.println("JavaClient: " + exception);
   }
  }
}