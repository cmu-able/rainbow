package org.sa.rainbow.brass.confsynthesis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.sa.rainbow.brass.adaptation.PrismConnectorAPI;
import org.sa.rainbow.brass.adaptation.PrismPolicy;
import org.sa.rainbow.brass.confsynthesis.AlloyConnector;
import org.sa.rainbow.brass.confsynthesis.AlloySolution;
import org.sa.rainbow.brass.confsynthesis.ConstantDefinition;
import org.sa.rainbow.brass.confsynthesis.PropertiesConfigurationSynthesizer;
import org.sa.rainbow.brass.confsynthesis.SpaceIterator;
import org.sa.rainbow.brass.confsynthesis.AlloySolution.AlloySolutionNode;

import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState.Sensors;

public class ConfigurationSynthesizer implements ConfigurationProvider {


	public HashMap<String, String> m_configurationPredicates = new HashMap<String, String>();
	public HashMap<String, String> m_configurations = new HashMap<String, String>();
	public HashMap<String, DetailedConfiguration> m_configuration_objects = new HashMap<String, DetailedConfiguration>();
	public LinkedList<String> m_allinstances = new LinkedList<String>();
	public HashMap<String, List<String>> m_reconfigurations = new HashMap<String,List<String>>();
	public static final HashMap<String, String> m_component_modes;
	static{
		m_component_modes = new HashMap<String, String>();
		m_component_modes.put("DISABLED", "0");
		m_component_modes.put("ENABLED", "1");
		m_component_modes.put("OFFLINE", "2");
	}
	
	private static final HashMap<String, String[]> m_component_actions;
	static{
		m_component_actions = new HashMap<String, String[]>();
		m_component_actions.put("enable", new String[] {"DISABLED","ENABLED"});
		m_component_actions.put("disable", new String[] {"ENABLED","DISABLED"});
		m_component_actions.put("crash", new String[] {"ENABLED","OFFLINE"});
	}
	
	private static final HashMap<String, String> m_configuration_dictionary;
	static{
		m_configuration_dictionary = new HashMap<String, String>();
		m_configuration_dictionary.put("sol_0", "amcl-lidar" );
		m_configuration_dictionary.put("sol_1", "aruco" );
		m_configuration_dictionary.put("sol_2", "mrpt-lidar" );
		m_configuration_dictionary.put("sol_3", "amcl-lidar" );
		m_configuration_dictionary.put("sol_4", "mrpt-kinect" );
		m_configuration_dictionary.put("sol_5", "amcl-kinect" );
		m_configuration_dictionary.put("sol_6", "aruco" );
		m_configuration_dictionary.put("sol_7", "aruco" );
		m_configuration_dictionary.put("sol_8", "aruco" );
		m_configuration_dictionary.put("sol_9", "mrpt-lidar" );
		m_configuration_dictionary.put("sol_10", "mrpt-kinect" );
		m_configuration_dictionary.put("sol_11", "amcl-kinect" );

	}
	
	private HashMap<String, ConstantDefinition> m_constant_definitions = new HashMap<String, ConstantDefinition>();

	public String m_res="result";
	public String m_myConstraintModel = PropertiesConfigurationSynthesizer.DEFAULT.getProperty(PropertiesConfigurationSynthesizer.CONSTRAINTMODEL_PROPKEY);
	public String m_myBaseModel = PropertiesConfigurationSynthesizer.DEFAULT.getProperty(PropertiesConfigurationSynthesizer.BASEMODEL_PROPKEY);
	public String m_myModel = PropertiesConfigurationSynthesizer.DEFAULT.getProperty(PropertiesConfigurationSynthesizer.TEMPMODEL_PROPKEY);
	public String m_myPolicy = PropertiesConfigurationSynthesizer.DEFAULT.getProperty(PropertiesConfigurationSynthesizer.POLICY_PROPKEY);
	public String m_myProps = PropertiesConfigurationSynthesizer.DEFAULT.getProperty(PropertiesConfigurationSynthesizer.PROPS_PROPKEY);;
	
	public static PrismConnectorAPI m_pc;
	
	public ConfigurationSynthesizer(){
		try{
			m_pc = new PrismConnectorAPI();
		} catch (Exception e){
			System.out.println("Configuration Synthesizer: Error initializing PRISM connector API.");
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public HashMap<String, Configuration> getConfigurations(){
		return (HashMap<String, Configuration>)(HashMap<String, ?>)m_configuration_objects;
	}
	
	/** 
	 *  Translates synthesized configuration identifiers into configuration identifiers in map
	 * 
	 */
	public String translateId (String id){
		if (Objects.equals(null, m_configuration_dictionary.get(id)))
			return "";
		return m_configuration_dictionary.get(id);
	}
	
	
	public Boolean containsConfiguration(String id){
		return (m_configuration_dictionary.containsKey(id) || m_configuration_dictionary.containsValue(id));
	}
	
	/**
	 * Unpacks a string of constant definitions into the dictionary of ConstantDefinitions for the solver
	 * @param s String in format "const1id=min1:max1:step1;const2id=val2; ..." 
	 */
	public void setConstantDefinitions(String s){
		String[] constantDefStrs = s.split(",");
		for (int i=0; i<constantDefStrs.length; i++){
			String defStr = constantDefStrs[i].trim();
			String def_id = defStr.split("=")[0].trim();
			System.out.println("RECONF HERE: "+defStr);
			String def_tail = defStr.split("=")[1].trim();
			String[] defParts = def_tail.split(":");
			if (defParts.length>1){ // If constant defined over a range min:max:step
				m_constant_definitions.put(def_id, new ConstantDefinition(def_id, defParts[0], defParts[1], defParts[2]));
			} else { // simple value
				m_constant_definitions.put(def_id, new ConstantDefinition(def_id, defParts[0], defParts[0], "0"));
			}
		}
		System.out.println(m_constant_definitions.toString());
	}
	
	
	/**
	 * Generates set of configurations and their corresponding translations into prism predicates
	 * @param modelFile Alloy model file
	 * @return number of solutions generated
	 */
	public int generateConfigurations(){
		AlloyConnector ac = new AlloyConnector();
		ac.generateSolutions(m_myConstraintModel);
		for (int i=0; i<ac.getSolutions().size();i++){
			String strSolId = AlloyConnector.SOLUTION_STRING+String.valueOf(i);
			String strSol = ac.getSolution(strSolId);
			m_configurations.put(strSolId, strSol);
			m_configuration_objects.put(strSolId, new DetailedConfiguration(strSolId, strSol));
		}
		return ac.getSolutions().size();
	}

	

	public void addConfigurationInstances(){
		for (Map.Entry<String, String> e: m_configurations.entrySet()){
			AlloySolution sol = new AlloySolution();
			sol.loadFromString(e.getValue());
			for (Map.Entry<String, LinkedList<String>> e2: sol.getAllInstances().entrySet()){
				for (int i=0; i<e2.getValue().size(); i++){
					//System.out.println("KEY:  "+e2.getValue().get(i));
					if (!m_allinstances.contains(e2.getValue().get(i).replace("$", "")) && !Objects.equals("", e2.getValue().get(i))){
						m_allinstances.add(e2.getValue().get(i).replace("$", ""));
					}
				}
			}
		}
	}
	
	
	public void generateBaseModel(){
		String mode_varname = "status";
		ArrayList<String> rewards = new ArrayList<String>();
		
		String code = "mdp\n";
		for (Map.Entry<String, String> e: m_component_modes.entrySet()){
			code += "const "+e.getKey()+"="+e.getValue()+";\n";
		}
		code+="\n";
		for (int i=0; i<m_allinstances.size();i++){
			String cid = m_allinstances.get(i); // Component id
			code += "const "+cid+"_INIT;\n";
			code += "module "+cid+"\n";
			code += "\t"+ cid + "_" + mode_varname +":[0.."+String.valueOf(m_component_modes.size())+"] init "+cid+"_INIT;\n";
			for (Map.Entry<String, String[]> e: m_component_actions.entrySet()){
				code += "\t["+cid+"_"+e.getKey()+"] (" + cid + "_" + mode_varname + "=" + e.getValue()[0] + ") -> ("+ cid + "_" + mode_varname + "'=" + e.getValue()[1] + ");\n";
				int reward_quantity=1;
				if (Objects.equals(e.getKey(), "crash"))
					reward_quantity=9999;
				rewards.add("\t["+cid+"_"+e.getKey()+"]  true : "+String.valueOf(reward_quantity)+";\n");
			}
			code += "endmodule\n\n";
		}
		
		code += "rewards \"steps\"\n";
		for (int i=0; i<rewards.size(); i++){
			code += rewards.get(i);
		}
		code += "endrewards\n\n"; // End code generation
//		System.out.println(code);	
		TextFileHandler fBaseModel = new TextFileHandler(m_myBaseModel); // Export to base model file
		fBaseModel.exportFile(code);		
	}
	
	public String configurationToPrismPred(String label) { // Exports to a prism predicate for reconfiguration
	    ArrayList<String> inst = new ArrayList<String>();
	    AlloySolution sol = new AlloySolution();
	    System.out.println(label);
	    sol.loadFromString(m_configurations.get(label));
		String res ="formula "+label+ " = true";
	    for (AlloySolutionNode node : sol.getNodes().values()){
	    	res += " & ("+node.getId().replace("$", "")+"_status=ENABLED)";
	    	inst.add(node.getId().replace("$", ""));
	    }
	    
	    for (int i=0; i<m_allinstances.size(); i++){
	    	if (!inst.contains(m_allinstances.get(i))){
	    		res += " & ("+m_allinstances.get(i)+"_status=DISABLED | "+m_allinstances.get(i)+"_status=OFFLINE)";
	    	}
	    }
	    res+=";\n";
	    return res;
	}
	
	public void generateConfigurationPreds(){
		for (Map.Entry<String, String> e: m_configurations.entrySet()){
			String prismConfString = configurationToPrismPred(e.getKey());
			m_configurationPredicates.put(e.getKey(), prismConfString);
			System.out.println(prismConfString);
		}
	}	
	
	public String generateTargetConfPredicate(){  // For PCTL formula generation
		String res = "(";
		for (int i=0; i<m_configurationPredicates.size();i++){
			if (i>0) res += " | ";
			res += AlloyConnector.SOLUTION_STRING+String.valueOf(i);
		}
		res +=")";
		return res;
	}
			
	
	public void generateReconfigurations(){
		m_reconfigurations.clear();
		String constantdefs = new String("");
		for (int i=0; i<m_allinstances.size();i++){
			if (i>0) constantdefs +=",";
			constantdefs += m_allinstances.get(i)+"_INIT=0:2:1";
		}
		setConstantDefinitions(constantdefs);
		SpaceIterator si= new SpaceIterator(m_constant_definitions);
			

		for (String s: si.getCombinations()){
			System.out.println("Init config: "+s);
			m_reconfigurations.put(s, generateReconfiguration(s));
		}

		int empty=0;
		for (String s: si.getCombinations()){
			System.out.println("Init config: "+s);
			System.out.println(m_reconfigurations.get(s));
			System.out.println("------------------------");
			if (m_reconfigurations.containsKey(s)){
				if (m_reconfigurations.get(s).size()==0){
					empty++;
				}
			} else {
				empty++;
			}
		}
		System.out.printf("%d Reconfiguration plans generated (%d empty)", m_reconfigurations.size(),empty);
		
	}
	
	public ArrayList<String> generateReconfiguration(String from){
		// Gets base model template, appends encoding of configuration formulas, target configuration predicate, and writes it to a temp model file
				TextFileHandler fBaseModel = new TextFileHandler(m_myBaseModel);
				ArrayList<String> baseCode = fBaseModel.readFileLines();
				
				// For a given configuration
				for (int i=0; i<m_configurationPredicates.size();i++){	
					String configKey = AlloyConnector.SOLUTION_STRING+String.valueOf(i);	
					baseCode.add(m_configurationPredicates.get(configKey));
					//System.out.println(cs.m_configurationPredicates.get(configKey));
				}
				//baseCode.add(cs.generateTargetConfPredicate());
				
				TextFileHandler fTempModel = new TextFileHandler(m_myModel);
				fTempModel.exportFile(baseCode);

				// Exports PCTL formula to compute reconfiguration plan to a temp property file
				TextFileHandler fTempProps = new TextFileHandler(m_myProps);
				fTempProps.exportFile("R{\"steps\"}min=? [ F "+generateTargetConfPredicate()+" ]");
				
			    PrismPolicy pp=null;
			    

			    try{
			    m_res = m_pc.modelCheckFromFileS(m_myModel, m_myProps, m_myPolicy, 0, from);
			    
			    } catch (Exception e){
			    	System.out.println("Configuration synthesizer: Error model checking reconfiguration from" + from);
			    }
			    pp = new PrismPolicy(m_myPolicy+".adv");
			    pp.readPolicy();  
			    String plan = pp.getPlan().toString();
			    System.out.println(plan);
			    System.out.println("Result is:" + m_res);
			    return pp.getPlan();
	}
	
	
	public ArrayList<String> generateReconfiguration(String from, String to){
		// Gets base model template, appends encoding of configuration formulas, target configuration predicate, and writes it to a temp model file
				TextFileHandler fBaseModel = new TextFileHandler(m_myBaseModel);
				ArrayList<String> baseCode = fBaseModel.readFileLines();
				
				// For a given configuration
				for (int i=0; i<m_configurationPredicates.size();i++){	
					String configKey = AlloyConnector.SOLUTION_STRING+String.valueOf(i);	
					baseCode.add(m_configurationPredicates.get(configKey));
					//System.out.println(cs.m_configurationPredicates.get(configKey));
				}
				//baseCode.add(cs.generateTargetConfPredicate());
				
				TextFileHandler fTempModel = new TextFileHandler(m_myModel);
				fTempModel.exportFile(baseCode);

				// Exports PCTL formula to compute reconfiguration plan to a temp property file
				TextFileHandler fTempProps = new TextFileHandler(m_myProps);
				fTempProps.exportFile("R{\"steps\"}min=? [ F "+to+" ]");
				
			    PrismPolicy pp=null;
			    

			    try{
			    m_res = m_pc.modelCheckFromFileS(m_myModel, m_myProps, m_myPolicy, 0, from);
			    
			    } catch (Exception e){
			    	System.out.println("Configuration synthesizer: Error model checking reconfiguration from" + from);
			    }
			    pp = new PrismPolicy(m_myPolicy+".adv");
			    pp.readPolicy();  
			    String plan = pp.getPlan().toString();
			    System.out.println(plan);
			    System.out.println("Result is:" + m_res);
			    return pp.getPlan();
	}
	
	
	public void generateReconfigurationsFrom(String from){
		m_reconfigurations.clear();
		for (String toConf : m_configuration_dictionary.keySet()){
			m_reconfigurations.put(toConf, generateReconfiguration(from, toConf));
		}
	}
	
	
	public HashMap<String,List<String>> getLegalReconfigurationsFrom(String fromConfiguration){
		generateReconfigurationsFrom(fromConfiguration);
		HashMap<String, List<String>> res = new HashMap<String, List<String>> ();
		for (Map.Entry<String,List<String>> e: m_reconfigurations.entrySet()){
			if (e.getValue().size()>0)
				res.put(e.getKey(), e.getValue());
		}
		return res;
	}
	
	public HashMap<String,Configuration> getLegalTargetConfigurations(){
		HashMap<String, Configuration> res = new HashMap<String, Configuration>();
		for (Map.Entry<String, List<String>> e : m_reconfigurations.entrySet()){
			if (m_configuration_objects.containsKey(e.getKey())){
				res.put(e.getKey(), m_configuration_objects.get(e.getKey()));
			}
		}
		return res;
	}

	public HashMap<String,Configuration> getLegalTargetConfigurationsFrom(String fromConfiguration){
		return getLegalTargetConfigurations();
	}
	
	public void populate(){
		String currentConfStr="markerLocalization0_INIT=0,markerRecognizer0_INIT=0,amcl0_INIT=1,laserscanNodelet0_INIT=1,mrpt0_INIT=2,camera0_INIT=1,lidar0_INIT=1,headlamp0_INIT=0,kinect0_INIT=2,fullSpeedSetting0_INIT=0,halfSpeedSetting0_INIT=1";
		generateConfigurations();
		addConfigurationInstances();
		generateBaseModel();
		generateConfigurationPreds();
		generateReconfigurationsFrom(currentConfStr);
	}
	
    public Double getReconfigurationTime(String sourceConfiguration, String targetConfiguration){
    	return 1.0;
    }
    
    public List<String> getReconfigurationPath(String sourceConfiguration, String targetConfiguration){
    	List<String> res = new LinkedList<String>();
    	res.add(targetConfiguration);
    	return res;
    }
    
	
	public static void main(String[] args) throws Exception{
		
		ConfigurationSynthesizer cs = new ConfigurationSynthesizer();
		cs.generateConfigurations();
//		cs.addConfigurationInstances();
//		cs.generateBaseModel();
//		cs.generateConfigurationPreds();
//		System.out.println("Global instance space: "+cs.m_allinstances.toString());
//		cs.generateReconfigurations();
		System.out.println(cs.getConfigurations().toString());
	}
	
}
