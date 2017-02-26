package org.sa.rainbow.brass.adaptation;

import prism.Prism;
import prism.PrismException;
import prism.PrismFileLog;
import prism.Result;
import prism.UndefinedConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import java.util.Objects;
import java.util.HashMap;

import parser.*;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import parser.ast.Property;

public class PrismConnectorAPI {

	public static PrismFileLog m_log;
	public static Prism m_prism;
	public static ModulesFile m_modulesFile;
	public static PropertiesFile m_propertiesFile;
	public static Result m_result;
	public static UndefinedConstants m_undefinedMFConstants;
	public static Values m_definedMFConstants;
	public static Values m_definedPFConstants;
	public static UndefinedConstants m_undefinedConstants[];
	public static ArrayList<Property> m_propertiesToCheck;
	public static String m_constSwitch;
		
	
	public PrismConnectorAPI() {
		m_log = new PrismFileLog("stdout");
		m_prism = new Prism(m_log);
		m_propertiesToCheck = new ArrayList<Property>();
		m_prism.setGenStrat(true);
		try{
		m_prism.setExportAdv(Prism.EXPORT_ADV_MDP);
		} catch (PrismException e){
			System.out.println("Could not change strategy export mode to MDP");
		}
		//setRestrictStratToReach(true);
		m_constSwitch = "INITIAL_LOCATION=4,TARGET_LOCATION=0,INITIAL_BATTERY=5000,INITIAL_HEADING=1";
		
		try{
			System.out.println("Initializing PRISM");
			m_prism.initialise();
			System.out.println("Initialized");
			System.out.println("ENGINE: "+ String.valueOf(m_prism.getEngine()));
			m_prism.setEngine(Prism.EXPLICIT); 
		}catch (PrismException e) {
			System.out.println("Error: " + e.getMessage());
			System.exit(1);
		}
	}
	
	
	public static String modelCheckProperty(String property){
		String res="";
		
		return res;
	}
	
	public static String modelCheckFromFileS(String modelFileName, String propertiesFileName, String strategyFileName){
		return modelCheckFromFileS(modelFileName, propertiesFileName, strategyFileName, -1, m_constSwitch);
	}
		
	
	public static void loadModel(String modelFileName){
		try { // PRISM model parsing		
			m_modulesFile = m_prism.parseModelFile(new File(modelFileName));
			m_prism.loadPRISMModel(m_modulesFile);
			
		}
		catch (FileNotFoundException e) {
			System.out.println("Error FNE: " + e.getMessage());
			System.exit(1);
		}
		catch (PrismException e) {
			System.out.println("Error PE1: " + e.getMessage());
			System.exit(1);
		}
	}
	
	public static void loadProperties(String propertiesFileName){
		try { // PRISM property parsing						
			//m_propertiesFile = m_prism.parsePropertiesString(m_modulesFile, property);
			m_propertiesFile = m_prism.parsePropertiesFile(m_modulesFile, new File(propertiesFileName));
			
		
		}
		catch (FileNotFoundException e) {
			System.out.println("Error FNE: " + e.getMessage());
			System.exit(1);
		}
		catch (PrismException e) {
			System.out.println("Error PE1: " + e.getMessage());
			System.exit(1);
		}
	}
	
	public static String modelCheckFromFileS(String modelFileName, String propertiesFileName, String strategyFileName, int propertyToCheck, String constSwitch){
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
			m_propertiesToCheck.add(m_propertiesFile.getPropertyObject(propertyToCheck));
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
			System.out.println(e.getMessage());
		}
		
		try {
			m_definedMFConstants = m_undefinedMFConstants.getMFConstantValues();
			m_prism.setPRISMModelConstants(m_definedMFConstants);
		} catch (PrismException e) {
			System.out.println(e.getMessage());
		}
		
		try { // Model check
			
			if (m_propertiesFile != null) {
				m_definedPFConstants = m_undefinedConstants[0].getPFConstantValues();
				System.out.println(String.valueOf( m_undefinedConstants[0].getPFConstantValues()));
				m_propertiesFile.setSomeUndefinedConstants(m_definedPFConstants);	
			}
			
			m_result = m_prism.modelCheck(m_propertiesFile, m_propertiesToCheck.get(0));
			System.out.println(m_result.getResult());
			res = m_result.getResult().toString();
		} 
		catch (PrismException e) {
			System.out.println("Error PE2: " + e.getMessage());
			System.exit(1);
		}
		
		// Export strategy if generated
		if (m_result.getStrategy() != null) {
	//		System.out.println("*** Exporting Strategy "+strategyFileName);
			try {
				m_prism.exportStrategy(m_result.getStrategy(), Prism.StrategyExportType.ACTIONS, strategyFileName.equals("stdout") ? null : new File(strategyFileName+".act"));
				m_prism.exportStrategy(m_result.getStrategy(), Prism.StrategyExportType.INDUCED_MODEL, strategyFileName.equals("stdout") ? null : new File(strategyFileName+".ind"));
				mergeActionsInducedModelIntoAdversary(strategyFileName+".act", strategyFileName+".ind", strategyFileName+".adv");
			}
			// in case of error, report it and proceed
			catch (FileNotFoundException e) {
				System.out.println("Couldn't open file \"" + strategyFileName + "\" for output");
			} catch (PrismException e) {
				System.out.println(e.getMessage());
			}
		} else { System.out.println("*** No Strategy generated.");}
		
		//m_prism.closeDown();
		return res;
	}	
	
	/**
     * Exports a String to a text file
     * @param f String filename
     * @param text String text to be exported
     */
    public static void exportTextToFile(String f, String text){
        try {
            BufferedWriter out = new BufferedWriter (new FileWriter(f));
            out.write(text);
            out.close();
        }
        catch (IOException e){
            System.out.println("Error exporting text");
        }
    }
    
	public static void mergeActionsInducedModelIntoAdversary(String actionsFileName, String inducedModelFileName, String stratFileName){
		HashMap<String, String> actions = new HashMap<String, String>();
		Scanner sc=null;
		try { 
			sc = new Scanner(new File(actionsFileName));
		}
		catch (FileNotFoundException e){
			System.out.println("Error merging actions and induced model into policy. File not found "+actionsFileName);
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
			System.out.println("Error merging actions and induced model into policy. File not found "+inducedModelFileName);
		}
		
		String mergedStrat = "";
		while (sc.hasNextLine()) {
			  String transferLine = sc.nextLine();
			  String[] chunks = transferLine.split(" ");
			  String action = actions.get(chunks[0]);
			  if (!Objects.equals(action, "null") && chunks.length>2)
				  transferLine = transferLine +" "+action;
			  mergedStrat = mergedStrat+ transferLine + "\n";
		}	
		sc.close();		
		exportTextToFile(stratFileName, mergedStrat);
	}
	
	
	public static void main(String[] args){
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
