package org.sa.rainbow.brass.confsynthesis;

import java.io.FileReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.LinkedList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.sa.rainbow.brass.confsynthesis.PropertiesSimpleConfigurationStore;
import org.sa.rainbow.core.ConfigHelper;

public class SimpleConfigurationStore implements ConfigurationProvider {

	public HashMap<String, SimpleConfiguration> m_configuration_objects = new HashMap<String, SimpleConfiguration>();
	private String m_source;
	private String m_conf_prefix = "sol_";
	
	
	public SimpleConfigurationStore() {
		this(PropertiesSimpleConfigurationStore.DEFAULT);
	}
	
	public SimpleConfigurationStore(Properties props) {
		m_source = ConfigHelper.convertToAbsolute(props.getProperty(PropertiesSimpleConfigurationStore.CONFIGURATIONS_SOURCE_PROPKEY));
	//	m_source = "/Users/jcamara/Dropbox/Documents/Work/projects/rainbow-alt/deployments/rainbow-brass/prismtmp/p2cp1/config_list.json";
	}

	public void populate(){
		System.out.println("Reading from"+m_source);
		loadFromFile(m_source);
	}
	
	public String translateId (String id){
		return id; // Just to comply with the standard ConfigurationProvider interface
	}
	
	public Boolean containsConfiguration(String id){
		return (m_configuration_objects.containsKey(id));
	}
	
    private synchronized void loadFromFile(String confFile){
        JSONParser parser = new JSONParser();
        Object obj=null;
                
        try{
            obj = parser.parse(new FileReader(confFile)); 
        } catch (Exception e) {
        	System.out.println(e.getMessage());
            System.out.println("Could not load Configuration File: "+confFile);
            e.printStackTrace();
        }

        JSONObject jsonObject = (JSONObject) obj;
        JSONArray nodes = (JSONArray) jsonObject.get("configurations");

        for (Object node : nodes) {
            JSONObject jsonNode = (JSONObject) node;
            String c_id = jsonNode.get("config_id").toString();
            Double c_cdr=0.0, c_speed=0.0;
            try{
            c_cdr = Double.parseDouble(String.valueOf(jsonNode.get("power_load")));
            c_speed = Double.parseDouble(String.valueOf(jsonNode.get("speed")));
            } catch (Exception e){
                System.out.println("Error parsing data from configuration "+c_id);
            }
            
            m_configuration_objects.put(m_conf_prefix+c_id, new SimpleConfiguration(c_id, c_cdr, c_speed));
            System.out.println("Added configuration " +m_configuration_objects.get(m_conf_prefix+c_id).getId() +" - Discharge rate: " + String.valueOf(m_configuration_objects.get(m_conf_prefix+c_id).getEnergyDischargeRate()) + " Speed: " + String.valueOf(m_configuration_objects.get(m_conf_prefix+c_id).getSpeed()));
        }
    }
	
    public Double getReconfigurationTime(String sourceConfiguration, String TargetConfiguration){
    	return 1.0;
    }
    
    public List<String> getReconfigurationPath (String sourceConfiguration, String targetConfiguration){
    	List<String> res = new LinkedList<String>();
    	res.add(targetConfiguration);
    	return res;
    }
    
	@SuppressWarnings("unchecked")
	public HashMap<String, Configuration> getConfigurations(){
		return (HashMap<String, Configuration>)(HashMap<String, ?>)m_configuration_objects;
	}
	
	public Configuration getConfiguration(String config) {
		if (!config.startsWith(m_conf_prefix)) {
			config = m_conf_prefix + config;
		}
		return m_configuration_objects.get(config);
	}
	
	public HashMap<String,List<String>> getLegalReconfigurationsFrom(String fromConfiguration){
		HashMap<String, List<String>> res = new HashMap<String, List<String>> ();
		for (String cid: m_configuration_objects.keySet()){
			LinkedList<String> reconfPath = new LinkedList<String>();
			reconfPath.add("t_set_"+cid);
			res.put(cid, reconfPath);			
		}
		return res;
	}
    
	public HashMap<String,Configuration> getLegalTargetConfigurations(){
		HashMap<String, Configuration> res = new HashMap<String, Configuration>();
		for (Map.Entry<String, SimpleConfiguration> e : m_configuration_objects.entrySet()){
			if (m_configuration_objects.containsKey(e.getKey())){
				res.put(e.getKey(), e.getValue());
			}
		}
		return res;
	}

    
	public static void main(String[] args) throws Exception{
		
		SimpleConfigurationStore cs = new SimpleConfigurationStore();
		cs.populate();
		System.out.println(cs.getLegalReconfigurationsFrom("sol_432"));
	}
	
}
