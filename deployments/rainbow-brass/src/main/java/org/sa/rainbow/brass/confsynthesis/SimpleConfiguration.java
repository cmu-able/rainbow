package org.sa.rainbow.brass.confsynthesis;

public class SimpleConfiguration implements Configuration {

	private String m_id;

	private Double m_cdr;
	private Double m_speed;

	private final String conf_prefix = "sol_";
	
	
	public SimpleConfiguration (String id, Double cdr, Double speed){
		m_id = id;
		m_cdr = cdr;
		m_speed = speed;
	}
	
	public void setId(String id){
		m_id = id;
	}
	
	public void setEnergyDischargeRate(Double cdr){
		m_cdr = cdr;
	}
	
	public void setSpeed(Double speed){
		m_speed = speed;
	}
	
	public String getId(){
		return conf_prefix+m_id;
	}
			
	public Double getEnergyDischargeRate(){
		return m_cdr;
	}
	
	public Double getSpeed(){
		return m_speed;
	}
	
}
