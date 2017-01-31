package org.sa.rainbow.brass.adaptation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.io.*;
import org.sa.rainbow.brass.adaptation.PrismConnector;


/**
 * @author ashutosh
 *
 */
public class PrismPolicy {
	private String m_policyFile;
	public ArrayList<String> m_plan = new ArrayList<String>();
	
	

	public PrismPolicy(String policyFile) {
		m_policyFile = policyFile;
	}
	
	// This method extracts a linear plan from a policy.
	private void extractPolicy(String initial_state, 
			Map<String, String> stateActionMap, Map<String, String> startEndStateMap) {
		
		String state = initial_state;
		String action = "";
		//System.out.println(stateActionMap.size());
		//System.out.println(startEndStateMap.size());

		while (startEndStateMap.containsKey(state)) {
			action = stateActionMap.get(state);
			state = startEndStateMap.get(state);
			//System.out.println(state);
			//System.out.println(action);
			
			if (action != "") {
				m_plan.add(action);
			}
		}
		
		//System.out.println(m_plan.size());
		for (int i = 0; i < m_plan.size(); i++) {
			System.out.println(m_plan.get(i));
		}
	}
	
	// This method parses the policy file.
	public boolean readPolicy() {
		Map<String, String> stateActionMap = new HashMap<String, String>();
		Map<String, String> startEndStateMap = new HashMap<String, String>();
		
        // This will reference one line at a time
        String line = null;
        String initial_state = "";

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = 
                new FileReader(this.m_policyFile);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);

            int line_no = 0;
            boolean firstActionFound = false;
            String formerEndState = "";
            String startState="";
            String endState="";

            while((line = bufferedReader.readLine()) != null) {
                //System.out.println(line);
                
                ++line_no;

                // Skip the first line
                if (line_no == 1) continue;
                
                String[] elements = line.split(" ");

                startState=elements[0];
                formerEndState=endState;
                endState = elements[1];

                //String probability = elements[2];
                String action = "";

                if (elements.length == 4) {
                	action = elements[3];
                }

                stateActionMap.put(startState, action);
                startEndStateMap.put(startState, endState);
                
              if (!firstActionFound && elements.length==4) {
                	firstActionFound = true;
                	initial_state = formerEndState;
                }
            }

            bufferedReader.close(); 
            
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '"
                + m_policyFile + "'");                
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + m_policyFile + "'");                  
        }
        
        this.extractPolicy(initial_state, stateActionMap, startEndStateMap);

		return true;
	}
	
	public ArrayList<String> getPlan() {
		return m_plan;
	}
	
	  public static void main (String[] args) throws Exception { // Class test
		  PrismConnector conn = new PrismConnector (null);
		  conn.invoke(8, 7);
		  PrismPolicy prismPolicy = new PrismPolicy("/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/botpolicy.adv");
		  prismPolicy.readPolicy();  
		  System.out.println(prismPolicy.getPlan().toString());
	    }  
}
