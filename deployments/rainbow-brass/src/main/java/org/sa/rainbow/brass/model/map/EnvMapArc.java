package org.sa.rainbow.brass.model.map;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.google.common.base.Objects;

import org.sa.rainbow.brass.confsynthesis.ConfigurationProvider;

public class EnvMapArc {
	public String m_source, m_target;


	private final Map<String, Object> m_properties = new HashMap<> ();

	public HashMap <String, Double> m_times = new HashMap<String, Double>();
    public HashMap <String, Double> m_hitrates = new HashMap<String, Double>(); // Times and hitrates. Keys are configuration identifiers
    public HashMap <String, Double> m_successrates = new HashMap<String, Double>(); // Probability of successfully traversing the edge, per configuration

	public EnvMapArc(String source, String target, double distance, boolean enabled) {
		super();
		this.m_source = source;
		this.m_target = target;
		m_properties.put(Phase1MapPropertyKeys.DISTANCE, distance);
		m_properties.put(Phase1MapPropertyKeys.ENABLEMENT, enabled);
	}
	
	public Map<String, Object> getAllProperties() {
		return Collections.unmodifiableMap(m_properties);
	}
	
	public void loadProperties(Map<String,Object> props) {
		m_properties.putAll(props);
	}

	public String getSource() {
		return m_source;
	}

	public void setSource(String m_source) {
		this.m_source = m_source;
	}

	public String getTarget() {
		return m_target;
	}

	public void setTarget(String m_target) {
		this.m_target = m_target;
	}

	public double getDistance() {
		return (Double)m_properties.get(Phase1MapPropertyKeys.DISTANCE);
	}

	public void setDistance(double distance) {
		m_properties.put(Phase1MapPropertyKeys.DISTANCE, (Double)distance);
	}

	public boolean isEnabled() {
		return (Boolean )m_properties.get(Phase1MapPropertyKeys.ENABLEMENT);
	}

	public void setEnabled(boolean enabled) {
		m_properties.put(Phase1MapPropertyKeys.ENABLEMENT, enabled);

	}

	public boolean includesNode(String node) {
		return (m_source.equals(node) || m_target.equals(node));
	}

	public boolean includesNodes(String nodea, String nodeb) {
		return (includesNode(nodea) && includesNode(nodeb));
	}

	public boolean isArcBetween(String nodea, String nodeb){
		return (Objects.equal(nodea, m_source) && Objects.equal(nodeb, m_target));
	}
	
	public void setProperty(String key, Object value) {
		m_properties.put(key, value);
	}

	public Object getProperty(String key) {
		return m_properties.get(key);
	}

	public void addTime(String cid, Double value){
	    	m_times.put(cid, value);
	    }
    public void addHitRate(String cid, Double value){
    	m_hitrates.put(cid, value);
    }    
    public Double getTime(String cid){
    	if (Objects.equal(m_times.get(cid.toString()), null))
    		return 0.0;
    	return (m_times.get(cid.toString()));
    }
    public Double getHitRate(String cid){
    	if (Objects.equal(m_hitrates.get(cid.toString()), null))
    		return 0.0;
    	return (m_hitrates.get(cid.toString()));
    }
    public Boolean includesHitRates(ConfigurationProvider cp){
    	for (Map.Entry<String, Double> e: m_hitrates.entrySet()){
    			if ((cp.containsConfiguration(e.getKey()) && (e.getValue()>0.0)))
    				return true;
    		}
    	return false;
    }

    public void addSuccessRate(String cid, Double value){
    	m_successrates.put(cid, value);
    }    

    public Double getSuccessRate(String cid){
    	if (Objects.equal(m_successrates.get(cid.toString()), null))		
    		return 0.0; // What should be the default value for this?
    	return (m_successrates.get(cid.toString()));
    }
 
    public Double getMaxSuccessRate(){
    	Double res=0.0;
    	for (Map.Entry<String, Double> e: m_successrates.entrySet()){
			if (e.getValue()>res)
				res = e.getValue();
		}
	return res;
    }
    
    public boolean existsSuccessRateAboveThreshold(Double t){
    	return (getMaxSuccessRate()>t);
    }
}