package org.sa.rainbow.brass.adaptation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import org.sa.rainbow.brass.confsynthesis.ConfigurationProvider;

import com.google.common.base.Objects;

/**
 * @author ashutosh
 *
 */
public class PrismPolicy {
    private static String m_policyFile;
    public ArrayList<String> m_plan = new ArrayList<String>();


    public PrismPolicy(String policyFile) {
        m_policyFile = policyFile;
    }


    /**
     * Generates a linear plan from a policy
     * @param initial_state
     * @param stateActionMap
     * @param startEndStateMap
     */
    private void extractPolicy(String initial_state, 
            Map<String, LinkedList<String>> stateActionMap, Map<String, LinkedList<String>> startEndStateMap) {

        String state = initial_state;
        String action = "";
        
        while (startEndStateMap.containsKey(state)) { // While current state is mapped to something
        	boolean foundState = false;
    		for (String e: startEndStateMap.get(state)){ // For each of the alternative states to which a source state can be mapped (probabilistic branches)
    			if (startEndStateMap.containsKey(e)){  // Lookahead
    				action = stateActionMap.get(state).get(0); 
    				state = e;
    				foundState=true;
    				}
        		}
    		if (!foundState) {
        	if ((startEndStateMap.get(state).size()==1) && (!startEndStateMap.containsKey(startEndStateMap.get(state).get(0)))){ // Special case for final state
        		action = stateActionMap.get(state).get(0);
        		state = startEndStateMap.get(state).get(0);
        	}
        	else if ((startEndStateMap.get(state).size() > 1 && !startEndStateMap.containsKey(startEndStateMap.get(state).get(0)) && !startEndStateMap.containsKey(startEndStateMap.get(state).get(1)) )) {
        		action = stateActionMap.get(state).get(0);
        		state = startEndStateMap.get(state).get(0);
        	}
    		}
        	
            if (action != "") {
                m_plan.add(action);
            }
        }
    }

    /**
     * Obtains the initial state in an adversary/strategy file
     * @return String id of the initial state
     */
    public static String findInitialState(){
        String initialState="";
        Map<String, LinkedList<String>> t = new HashMap<String, LinkedList<String>>();
        Scanner sc = null;
        try {
            sc = new Scanner(new File(m_policyFile));
        } catch (FileNotFoundException e){
            System.out.println("Error determining initial state in policy. File not found "+m_policyFile);
        }

        String l;
        if (sc.hasNextLine())
        {
            l=sc.nextLine(); // Eliminate header line in adversary file
        }
        while (sc.hasNextLine()){
            l = sc.nextLine();
            String[] chunks = l.split(" ");
            if (!t.containsKey(chunks[0])){
            	t.put(chunks[0], new LinkedList<String>());
            }
            t.get(chunks[0]).add(chunks[1]);
        }

        
        for (Map.Entry<String, LinkedList<String>> e : t.entrySet()){
            if (!isHit(t,e.getKey()) && t.get(e.getKey()).size()==1){
            	return e.getKey();	
            }
        }
        return initialState;
    }

    // Checks if a state id is mapped from something in the data structure
    public static boolean isHit( Map<String, LinkedList<String>> l, String s){
    	for (Map.Entry<String, LinkedList<String>> e : l.entrySet()){
    		if (e.getValue().contains(s))
    			return true;
    	}
    	return false;
    }
    
    /**
     * Reads an adversary/strategy file into a policy
     * Fixed!! (feb 25), initial state was incorrectly detected and method would return wrong policy
     * @return
     */
    public boolean readPolicy() {
        Map<String, LinkedList<String>> stateActionMap = new HashMap<String, LinkedList<String>>();
        Map<String, LinkedList<String>> startEndStateMap = new HashMap<String, LinkedList<String>>();

        // This will reference one line at a time
        String line = null;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = 
                    new FileReader(m_policyFile);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = 
                    new BufferedReader(fileReader);

            int line_no = 0;
            boolean firstActionFound = false;
            String startState="";
            String endState="";

            while((line = bufferedReader.readLine()) != null) {                
                ++line_no;
                if (line_no == 1)
                {
                    continue;                // Skip the first line
                }

                String[] elements = line.split(" ");
                startState=elements[0];
                endState = elements[1];
                String action = "";

                if (elements.length == 4) {
                    action = elements[3];
                }
                
                if (elements.length == 5) {
                	action = elements[4];
                }

                if (!stateActionMap.containsKey(startState)){
                	stateActionMap.put(startState, new LinkedList<String>());
                }
                stateActionMap.get(startState).add(action);
                
                if (!startEndStateMap.containsKey(startState)){
                	startEndStateMap.put(startState, new LinkedList<String>());
                }
                startEndStateMap.get(startState).add(endState);

                if (!firstActionFound && elements.length==4) {
                    firstActionFound = true;
                }
            }

            bufferedReader.close(); 

            this.extractPolicy (findInitialState (), stateActionMap, startEndStateMap);
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + m_policyFile + "'");                
        }
        catch(IOException ex) {
            System.out.println("Error reading file '"  + m_policyFile + "'");                  
        }
        return true;
    }

    /**
     * Returns the linear plan associated with the policy
     * @return List of strings encoding sequence of action labels, e.g., [action1, ..., actionN]
     */
    public ArrayList<String> getPlan() {
        return m_plan;
    }


    public ArrayList<String> getPlan(ConfigurationProvider cp, String fromConfiguration){
    	String confSetPrefix = "t_set_";
    	ArrayList<String> res = new ArrayList<String>();
    	
    	HashMap<String,List<String>> reconfs = cp.getLegalReconfigurationsFrom(fromConfiguration);
    	
    	for (int i=0; i<m_plan.size(); i++){
    		if (m_plan.get(i).startsWith(confSetPrefix)){
    			res.addAll(reconfs.get(m_plan.get(i).replace(confSetPrefix, "")));
    		} else
    			res.add(m_plan.get(i));
    	}
    	return res;
    }

    /**
     * Class test
     */
    public static void main (String[] args) throws Exception { // Class test
//        PrismConnector conn = new PrismConnector (null);
//        conn.invoke(8, 7);
//        PrismPolicy prismPolicy = new PrismPolicy("/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/botpolicy.adv");
        PrismPolicy prismPolicy = new PrismPolicy("src/test/resources/0.adv");
        prismPolicy.readPolicy();  
        System.out.println(prismPolicy.getPlan().toString());

    }  
}
