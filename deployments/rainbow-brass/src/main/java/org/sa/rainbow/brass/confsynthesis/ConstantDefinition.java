package org.sa.rainbow.brass.confsynthesis;

public class ConstantDefinition {

	String m_id, m_min, m_max, m_step;

	public ConstantDefinition(String id, String min, String max, String step){
		this.m_id=id; this.m_min=min; this.m_max=max; this.m_step=step;
	}	
	public String getId(){return m_id;}
	public String getMin(){return m_min;}
	public String getMax(){return m_max;}
	public String getStep(){return m_step;}

	@Override
	public String toString() {
		return "ConstantDefinition [m_id=" + m_id + ", m_min=" + m_min + ", m_max=" + m_max + ", m_step=" + m_step + "]";
	}
}