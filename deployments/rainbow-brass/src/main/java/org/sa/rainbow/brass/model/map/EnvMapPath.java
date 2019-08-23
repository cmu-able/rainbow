package org.sa.rainbow.brass.model.map;

import org.sa.rainbow.brass.model.map.*;

import com.google.common.base.Objects;

import java.util.List;

public class EnvMapPath implements Comparable<EnvMapPath> {

	private List<String> m_path;
	private Double m_distance;
	private EnvMap m_map;
	
	
	public EnvMapPath(List<String> path, EnvMap map){
		m_path = path;
		m_map = map;
		m_distance = m_map.pathDistance(m_path);
	}
	
	public List<String> getPath(){
		return m_path;
	}
	
	public Double getDistance(){
		return m_distance;
	}
	
	public String toString(){
		return "PATH:"+String.valueOf(m_path)+" DISTANCE: " + String.valueOf(m_distance);
	}
	
	@Override
    public int compareTo(EnvMapPath cto) {
        Double comparedist=0.0;
        
        if (!Objects.equal(cto, null)){
        	comparedist=((EnvMapPath)cto).getDistance();
        	return Double.compare(m_distance, comparedist);       
        } else return -1;
        
    }
	
}
