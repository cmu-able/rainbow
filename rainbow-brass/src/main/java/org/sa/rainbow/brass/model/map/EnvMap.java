package org.sa.rainbow.brass.model.map;

import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.brass.model.map.EnvMapArc;
import org.sa.rainbow.brass.model.map.EnvMapNode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




/**
 * Created by camara on 12/20/2016.
 */

public class EnvMap {
	
//	public static EnvMap parseFromString (ModelReference ref, String mStr) {
//        EnvMap m = new EnvMap (ref);
//        // TODO complete code here
//        return m;
//    }
	
//	public EnvMap (ModelReference model) {
	public EnvMap () {
//        m_model = model;
		m_nodes = new HashMap<> ();
        m_arcs = new LinkedList<EnvMapArc> ();
    }

//    public ModelReference getModelReference () {
//        return m_model;
//    }

    private Map<String, EnvMapNode> m_nodes;
    //private LinkedList<EnvMapNode> m_nodes;
    private LinkedList<EnvMapArc> m_arcs;
    
//	private final ModelReference           m_model;
	
    public LinkedList<EnvMapArc> getArcs(){
    	return m_arcs;
    }
    
    public Map<String, EnvMapNode> getNodes(){
    	return m_nodes;
    }
    
    public int getNodeCount(){
    	return m_nodes.size();
    }
    
    public int getArcCount(){
    	return m_arcs.size();
    }
    
	public void AddNode (String label, float x, float y){
		m_nodes.put(label, new EnvMapNode(label, x, y));
	}

	public void addArc(String source, String target, float distance, boolean enabled ){
		m_arcs.add(new EnvMapArc(source, target, distance, enabled));
	}


}
