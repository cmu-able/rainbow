package org.sa.rainbow.brass.model.map;

import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.brass.model.map.EnvMapArc;
import org.sa.rainbow.brass.model.map.EnvMapNode;

import java.util.*;


/**
 * Created by camara on 12/20/2016.
 */

public class EnvMap {
	
	public EnvMap (ModelReference model) {
        m_model = model;
        m_last_insertion = new NodeInsertion();
		m_nodes = new HashMap<> ();
        m_arcs = new LinkedList<EnvMapArc> ();
        initWithSimpleMap(); // TODO: Substitute hardwired version of the map by one parsed from file
    }

    public ModelReference getModelReference () {
        return m_model;
    }
    
    public EnvMap copy(){
    	EnvMap m = new EnvMap (m_model);
    	m.m_nodes = new HashMap<String, EnvMapNode> (m_nodes);
    	m.m_arcs = new LinkedList<EnvMapArc> (m_arcs);
    	return m;
    }

    private Map<String, EnvMapNode> m_nodes;
    private LinkedList<EnvMapArc> m_arcs;
    private NodeInsertion m_last_insertion;
    
	private final ModelReference m_model;
	
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
	
	public float getNodeX(String n){
		return m_nodes.get(n).getX();
	}
	
	public float getNodeY(String n){
		return m_nodes.get(n).getY();
	}
	
	/**
	 * Eliminates all arcs in map between nodes with labels na and nb
	 * (independently of whether na is the source or the target node in the arc)
	 * @param na String node a label
	 * @param nb String node b label
	 */
	public void removeArcs(String na, String nb){
		ListIterator<EnvMapArc> iter = getArcs().listIterator();
		while(iter.hasNext()){
		    if(iter.next().includesNodes(na, nb)) {
		        iter.remove();
		    }
		}
	}

	public static class NodeInsertion{
		protected String m_n;
		protected String m_na;
		protected String m_nb;
		protected float m_x;
		protected float m_y;
		

		public NodeInsertion copy () {
			NodeInsertion ni = new NodeInsertion();
			ni.m_n = m_n;
			ni.m_na = m_na;
			ni.m_nb = m_nb;
			ni.m_x = m_x;
			ni.m_y = m_y;
			return ni;
		}
		
	}
	
	public NodeInsertion getNodeInsertionResult () {
		return m_last_insertion.copy();
	}
	
	/**
	 * Returns Euclidean distance between locations with labels na and nb
	 * @param na String node label a
	 * @param nb String node label b
	 * @return float distance
	 */
	public float distanceBetween(String na, String nb){
		EnvMapNode a = m_nodes.get(na);
		EnvMapNode b = m_nodes.get(nb);
		float xc = Math.abs(a.getX() - b.getX());
		float yc = Math.abs(a.getY() - b.getY());
		return (float)Math.sqrt(xc*xc+yc*yc);		
	}
	
	/**
	 * Inserts a new node in the map graph in between two nodes na and nb.
	 * The arcs between the original endpoints are split, and the new pair of arcs between
	 * the new node and nb are disabled (note that the order of na and nb in the invocation
	 * of the method matters!).
	 * @param n String label of the new node to insert
	 * @param na String label of the node that the robot is moving away from
	 * @param nb String label of the node that the robot is moving towards
	 * @param x float coordinates of the location of the new node in the map
	 * @param y
	 */
	public void insertNode (String n, String na, String nb, float x, float y) {
		AddNode (n, x, y);
		removeArcs(na, nb);
		addArc (na, n, distanceBetween(na,n), true);
		addArc (n, na, distanceBetween(na,n), true);
		addArc (nb, n, distanceBetween(nb,n), false);
		addArc (n, nb, distanceBetween(nb,n), false);
	}
	
	
	public void initWithSimpleMap(){
	 	AddNode("l1", 14.474f, 16f);
	 	AddNode("l2", 19.82f, 16f);
	 	AddNode("l3", 42.5f, 16f);
	 	AddNode("l4", 52.22f, 16f);
	 	AddNode("l5", 52.22f, 26.26f);
	 	AddNode("l6", 42.5f, 26.26f);
	 	AddNode("l7", 19.82f, 26.26f);
	 	AddNode("l8", 19.82f, 20.05f);
	 	AddNode("ls", 52.22f, 10.6f);
	 	
	 	addArc("l1", "l2", 5.436f, true);
	 	addArc("l2", "l1", 5.436f, true);
	 	addArc("l2", "l3", 22.572f, true);
	 	addArc("l3", "l2", 22.572f, true);
	 	addArc("l3", "l4", 9.72f, true);
	 	addArc("l4", "l3", 9.72f, true);
	 	addArc("l2", "l8", 4.05f, true);
	 	addArc("l8", "l2", 4.05f, true);
	 	addArc("l8", "l7", 6.21f, true);
	 	addArc("l7", "l8", 6.21f, true);
	 	addArc("l7", "l6", 22.572f, true);
	 	addArc("l6", "l7", 22.572f, true);
	 	addArc("l3", "l6", 10.26f, true);
	 	addArc("l6", "l3", 3f, true);
	 	addArc("l4", "l5", 10.26f, true);
	 	addArc("l5", "l4", 10.26f, true);
	 	addArc("l6", "l5", 9.72f, true);
	 	addArc("l5", "l6", 9.72f, true);
	 	addArc("l4", "ls", 5.4f, true);
	 	addArc("ls", "l4", 5.4f, true);
	}
	
}
