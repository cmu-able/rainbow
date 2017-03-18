package org.sa.rainbow.brass.adaptation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


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
            Map<String, String> stateActionMap, Map<String, String> startEndStateMap) {

        String state = initial_state;
        String action = "";

        while (startEndStateMap.containsKey(state)) {
            action = stateActionMap.get(state);
            state = startEndStateMap.get(state);

            if (action != "") {
                m_plan.add(action);
            }
        }

//		for (int i = 0; i < m_plan.size(); i++) {
//			System.out.println(m_plan.get(i));
//		}
    }

    /**
     * Obtains the initial state in an adversary/strategy file
     * @return String id of the initial state
     */
    public static String findInitialState(){
        String initialState="";
        Map<String, String> t = new HashMap<String, String>();
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
            t.put(chunks[0], chunks[1]);
        }

        for (Map.Entry<String, String> e : t.entrySet()){
            if (!t.containsValue(e.getKey()))
                return e.getKey();	
        }
        return initialState;
    }


    /**
     * Reads an adversary/strategy file into a policy
     * Fixed!! (feb 25), initial state was incorrectly detected and method would return wrong policy
     * @return
     */
    public boolean readPolicy() {
        Map<String, String> stateActionMap = new HashMap<String, String>();
        Map<String, String> startEndStateMap = new HashMap<String, String>();

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

                stateActionMap.put(startState, action);
                startEndStateMap.put(startState, endState);

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



    /**
     * Class test
     */
    public static void main (String[] args) throws Exception { // Class test
        PrismConnector conn = new PrismConnector (null);
        conn.invoke(8, 7);
        PrismPolicy prismPolicy = new PrismPolicy("/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/botpolicy.adv");
        prismPolicy.readPolicy();  
        System.out.println(prismPolicy.getPlan().toString());

    }  
}
