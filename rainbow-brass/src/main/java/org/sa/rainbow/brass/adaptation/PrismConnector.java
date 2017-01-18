package org.sa.rainbow.brass.adaptation;

import java.io.*;

/**
 * @author jcamara
 *
 */
public class PrismConnector {

	public static final String PRISM_BIN ="/Applications/prism-4.3.beta-osx64/bin/prism";
	public static final String PRISM_MODEL="/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/prismtmp.prism";
	public static final String PRISM_PROPERTIES="/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/mapbot.props";
	public static final String PRISM_PARAMETERS="INITIAL_LOCATION=0,TARGET_LOCATION=5,INITIAL_BATTERY=5000";
	public static final String PRISM_ADV_EXPORT="/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/botpolicy.adv";
	private static final boolean m_print_output = true;
			
	 public static void main(String[] args) throws Exception { 
         String line;
	     try { 
	    	 Process p = Runtime.getRuntime().exec(PRISM_BIN +" "+PRISM_MODEL+" "+ PRISM_PROPERTIES + " -prop 1 -ex -const "+PRISM_PARAMETERS+" -exportadv "+PRISM_ADV_EXPORT);
	    	 if (m_print_output){
		    	 BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    	 while ((line = input.readLine()) != null) {
		    		 System.out.println(line);
		         }
		         input.close();	    		 
	    	 }
	     }catch (IOException e) {  
	    	 e.printStackTrace();  
	     }  
	 }  
}

