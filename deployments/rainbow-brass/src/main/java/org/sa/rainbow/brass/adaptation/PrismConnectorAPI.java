package org.sa.rainbow.brass.adaptation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

import org.apache.log4j.Logger;

import parser.Values;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import parser.ast.Property;
import prism.Prism;
import prism.PrismException;
import prism.PrismFileLog;
import prism.Result;
import prism.UndefinedConstants;

/**
 * @author jcamara
 *
 */
public class PrismConnectorAPI {

    public PrismFileLog m_log;
    public  Prism m_prism;
    public  ModulesFile m_modulesFile;
    public  PropertiesFile m_propertiesFile;
    public  Result m_result;
    public  UndefinedConstants m_undefinedMFConstants;
    public  Values m_definedMFConstants;
    public  Values m_definedPFConstants;
    public  UndefinedConstants m_undefinedConstants[];
    public  ArrayList<Property> m_propertiesToCheck;
    public  String m_constSwitch;
    private Logger m_logger;

    public Logger getLogger() {
		return m_logger;
	}


	public void setLogger(Logger logger) {
		m_logger = logger;
	}

	public void logInfo(String msg) {
		if (m_logger == null) System.out.println(msg);
		else m_logger.info(msg);
	}
	
	public void logError(String msg) {
		if (m_logger == null) System.out.println(msg);
		else m_logger.error(msg);
	}
	
	
	protected static PrismConnectorAPI s_instance;
    
    public static PrismConnectorAPI instance () throws PrismException {
    	if (s_instance == null) {
    		s_instance = new PrismConnectorAPI();
    	}
    	return s_instance;
    }
    
    
    /**
     * Initializes PRISM instance and additional structures
     * 
     * @throws PrismException
     */
    protected PrismConnectorAPI () throws PrismException {
        m_log = new PrismFileLog("stdout");
        m_prism = new Prism(m_log);
        m_propertiesToCheck = new ArrayList<Property>();
        m_prism.setGenStrat(true);
        try{
            m_prism.setExportAdv(Prism.EXPORT_ADV_MDP);
        } catch (PrismException e){
            logError("Could not change strategy export mode to MDP");
            if (m_logger != null) m_logger.error(e);
        }

        m_constSwitch = "INITIAL_LOCATION=4,TARGET_LOCATION=0,INITIAL_BATTERY=5000,INITIAL_HEADING=1";

        try{
            logInfo("Initializing PRISM");
            m_prism.initialise();
            logInfo("Initialized");
            logInfo("ENGINE: "+ String.valueOf(m_prism.getEngine()));
            m_prism.setEngine(Prism.EXPLICIT); 
        }catch (PrismException e) {
        	logError("Error: " + e.getMessage());
            throw e;
        }
    }



    public synchronized String modelCheckFromFileS (String modelFileName,
            String propertiesFileName,
            String strategyFileName)
                    throws Exception {
        return modelCheckFromFileS(modelFileName, propertiesFileName, strategyFileName, -1, m_constSwitch);
    }

    /**
     * Loads PRISM model from file
     * @param modelFileName
     */
    public void loadModel (String modelFileName) throws Exception {
        try { // PRISM model parsing	
            modelFileName =    modelFileName.replaceAll ("\\\"", "");
            m_modulesFile = m_prism.parseModelFile(new File(modelFileName));
            m_prism.loadPRISMModel(m_modulesFile);

        }
        catch (FileNotFoundException e) {
            logError ("Error FNE: " + e.getMessage () + ", " + modelFileName);
            throw e;
        }
        catch (PrismException e) {
            logError("Error PE1: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Loads PRISM properties from file
     * @param propertiesFileName
     */
    public void loadProperties (String propertiesFileName) throws Exception {
        try { // PRISM property parsing						
            m_propertiesFile = m_prism.parsePropertiesFile(m_modulesFile, new File(propertiesFileName));				
        }
        catch (FileNotFoundException e) {
            logError ("Error FNE: " + e.getMessage () + ", " + propertiesFileName);
            throw e;
        }
        catch (PrismException e) {
            logError("Error PE1: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Model checks properties on a PRISM model specification
     * @param modelFileName  String filename of PRISM model
     * @param propertiesFileName String filename of PRISM properties
     * @param strategyFileName String output filename for strategy export (if applicable)
     * @param propertyToCheck int index of property to check in the properties file (-1 for all properties)
     * @param constSwitch String encoding all undefined constant (parameter)  values (comma-separated, e.g., CONST1=VAL1,..,CONSTN=VALN)
     * @return
     */
    public synchronized String modelCheckFromFileS (String modelFileName,
            String propertiesFileName,
            String strategyFileName,
            int propertyToCheck,
            String constSwitch) throws Exception {
        int numPropertiesToCheck=0;
        int i;
        m_constSwitch = constSwitch;
        m_propertiesToCheck.clear();

        String res="";

        loadModel(modelFileName);
        loadProperties(propertiesFileName);	

        // no properties to check
        if (m_propertiesFile == null) {
            numPropertiesToCheck = 0;
        }
        // unless specified, verify all properties
        else if (propertyToCheck == -1) {
            numPropertiesToCheck = m_propertiesFile.getNumProperties();
            for (i = 0; i < numPropertiesToCheck; i++) {
                m_propertiesToCheck.add(m_propertiesFile.getPropertyObject(i));
            }
        } else {
        	logInfo("Checking property " + propertyToCheck);
            m_propertiesToCheck.add(m_propertiesFile.getPropertyObject(propertyToCheck));
            logInfo("Done");
            numPropertiesToCheck=1;
        }

        // process info about undefined constants
        // first, see which constants are undefined
        // (one set of info for model, and one set of info for each property)
        m_undefinedMFConstants = new UndefinedConstants(m_modulesFile, null);
        m_undefinedConstants = new UndefinedConstants[numPropertiesToCheck];
        for (i = 0; i < numPropertiesToCheck; i++) {
            m_undefinedConstants[i] = new UndefinedConstants(m_modulesFile, m_propertiesFile, m_propertiesToCheck.get(i));
        }

        try {
            // then set up value using const switch definitions
            m_undefinedMFConstants.defineUsingConstSwitch(m_constSwitch);
            for (i = 0; i < numPropertiesToCheck; i++) {
                m_undefinedConstants[i].defineUsingConstSwitch(m_constSwitch);
            }	
        }
        catch (PrismException e) {
            logError(e.getMessage());
            if (m_logger != null) m_logger.error(e);
        }

        try {
            m_definedMFConstants = m_undefinedMFConstants.getMFConstantValues();
            m_prism.setPRISMModelConstants(m_definedMFConstants);
        } catch (PrismException e) {
            logError(e.getMessage());
        }

        try { // Model check	
            if (m_propertiesFile != null) {
                m_definedPFConstants = m_undefinedConstants[0].getPFConstantValues();
                logInfo(String.valueOf( m_undefinedConstants[0].getPFConstantValues()));
                m_propertiesFile.setSomeUndefinedConstants(m_definedPFConstants);	
            }		
            
            for (i=0;i<numPropertiesToCheck;i++){
            	m_result = m_prism.modelCheck(m_propertiesFile, m_propertiesToCheck.get(i));
            	//System.out.println(m_result.getResult());
                if (i>0)
                	res += ",";
            	res += m_result.getResult().toString();
            }
        } 
        catch (PrismException e) {
            logError("Error PE2: " + e.getMessage());
            throw e;
        }

        // Export strategy if generated
        if (m_result.getStrategy() != null) {
            try {
                m_prism.exportStrategy(m_result.getStrategy(), Prism.StrategyExportType.ACTIONS, strategyFileName.equals("stdout") ? null : new File(strategyFileName+".act"));
                m_prism.exportStrategy(m_result.getStrategy(), Prism.StrategyExportType.INDUCED_MODEL, strategyFileName.equals("stdout") ? null : new File(strategyFileName+".ind"));
                mergeActionsInducedModelIntoAdversary(strategyFileName+".act", strategyFileName+".ind", strategyFileName+".adv");
            }
            // in case of error, report it and proceed
            catch (FileNotFoundException e) {
                logError("Could not open file \"" + strategyFileName + "\" for output");
            } catch (PrismException e) {
                logError(e.getMessage());
            }
        } 		
        else {
            exportTextToFile (strategyFileName + ".adv", "");
        }
        //m_prism.closeDown();
        return res;
    }

    /**
     * Exports a String to a text file
     * @param f String filename
     * @param text String text to be exported
     */
    public void exportTextToFile(String f, String text){
        try {
            BufferedWriter out = new BufferedWriter (new FileWriter(f));
            out.write(text);
            out.close();
        }
        catch (IOException e){
            logError("Error exporting text");
        }
    }

    /**
     * Merges action names and induced model strategy exports from method modelCheckFromFileS into a single 
     * adversary file including transitions and action names
     * @param actionsFileName
     * @param inducedModelFileName
     * @param stratFileName String output filename to export strategy
     */
    public void mergeActionsInducedModelIntoAdversary(String actionsFileName, String inducedModelFileName, String stratFileName){
        HashMap<String, String> actions = new HashMap<String, String>();
        Scanner sc=null;
        try { 
            sc = new Scanner(new File(actionsFileName));
        }
        catch (FileNotFoundException e){
            logError("Error merging actions and induced model into policy. File not found "+actionsFileName);
        }

        while (sc.hasNextLine()) {
            String[] pairs = sc.nextLine().split(":");
            actions.put(pairs[0], pairs[1]);
        }	
        sc.close();

        try { 
            sc = new Scanner(new File(inducedModelFileName));
        }
        catch (FileNotFoundException e){
            logError("Error merging actions and induced model into policy. File not found "+inducedModelFileName);
        }

        String mergedStrat = "";
        while (sc.hasNextLine()) {
            String transferLine = sc.nextLine();
            String[] chunks = transferLine.split(" ");
            String action = actions.get(chunks[0]);
            if (!Objects.equals(action, "null") && chunks.length>2) {
                transferLine = transferLine +" "+action;
            }
            mergedStrat = mergedStrat+ transferLine + "\n";
        }	
        sc.close();		
        exportTextToFile(stratFileName, mergedStrat);
    }


    /**
     * Class test
     * @param args
     */
    public static void main (String[] args) throws Exception {
        String res="result";
        String myModel = "/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/0.prism";
        String myPolicy = "/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/0";
        String myProps = "/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/mapbot.props";
        PrismConnectorAPI pc= new PrismConnectorAPI();
//		res = pc.modelCheckFromFileS(myModel,"R{\"time\"}min=? [ F goal ]", myPolicy);
        res = pc.modelCheckFromFileS(myModel,myProps, myPolicy);
        System.out.println("Result is:" + res);
    }

}
