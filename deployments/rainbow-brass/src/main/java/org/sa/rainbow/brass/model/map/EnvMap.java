package org.sa.rainbow.brass.model.map;

import java.io.FileReader;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import com.google.common.base.Objects;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.sa.rainbow.brass.PropertiesConnector;
import org.sa.rainbow.brass.adaptation.PrismConnector;
import org.sa.rainbow.brass.model.map.Phase2MapPropertyKeys.ObsLevel;
import org.sa.rainbow.core.models.ModelReference;

/**
 * Created by camara on 12/20/2016.
 */

public class EnvMap {
	private final float SAME_LOCATION_RADIUS = 1.75f;

	public EnvMap(ModelReference model, Properties props) {
		if (props == null) {
			props = PropertiesConnector.DEFAULT;
		}
		m_model = model;
		m_last_insertion = new NodeInsertion();
		m_nodes = new HashMap<>();
		m_new_node_id = 0;
		m_arcs = new HashMap<>();
		m_arcs_lookup =new LinkedList<EnvMapArc>();
		// initWithSimpleMap(); // TODO: Substitute hardwired version of the map by one
		// parsed from file
		//loadFromFile(props.getProperty(PropertiesConnector.MAP_PROPKEY));
	}

	public EnvMap(ModelReference model) {
		m_model = model;
		m_last_insertion = new NodeInsertion();
		m_nodes = new HashMap<>();
		m_new_node_id = 0;
		m_arcs = new HashMap<>();
	}

	public ModelReference getModelReference() {
		return m_model;
	}

	public synchronized EnvMap copy() {
		EnvMap m = new EnvMap(m_model);
		m.m_nodes = new HashMap<String, EnvMapNode>(m_nodes);
		m.m_arcs = new HashMap<>(m_arcs);
		m.m_arcs_lookup = new LinkedList<EnvMapArc>(m_arcs_lookup);
		return m;
	}

	private Map<String, EnvMapNode> m_nodes;
	private Map<String, EnvMapArc> m_arcs = new HashMap<>();
	private LinkedList<EnvMapArc> m_arcs_lookup = new LinkedList<EnvMapArc>();
	// private LinkedList<EnvMapArc> m_arcs;
	private NodeInsertion m_last_insertion;
	private int m_new_node_id;

	private final ModelReference m_model;

	public synchronized void updateArcsLookup() {
		m_arcs_lookup = new LinkedList<EnvMapArc>(new LinkedHashSet<EnvMapArc>(m_arcs.values()));
	}

	public synchronized LinkedList<EnvMapArc> getArcs() {
		return m_arcs_lookup;
	}

	public synchronized Map<String, EnvMapNode> getNodes() {
		return m_nodes;
	}

	public synchronized EnvMapNode getNode(double x, double y) {
		for (EnvMapNode node : m_nodes.values()) {
			if (node.getX() >= x - SAME_LOCATION_RADIUS && node.getX() <= x + SAME_LOCATION_RADIUS
					&& node.getY() >= y - SAME_LOCATION_RADIUS && node.getY() <= y + SAME_LOCATION_RADIUS)
				return node;
		}

		return null;
	}

	public synchronized int getNodeCount() {
		return m_nodes.size();
	}

	public synchronized int getArcCount() {
		return m_arcs.size() / 2;
	}

	public synchronized int getUniqueArcCount() {
		return m_arcs.size();
	}

	public synchronized LinkedList<String> getNeighbors(String node) {
		LinkedList<String> res = new LinkedList<String>();
		for (int i = 0; i < getArcs().size(); i++) {
			EnvMapArc a = this.getArcs().get(i);
			if (a.getSource().equals(node)) {
				res.add(a.getTarget());
			}
		}
		return res;
	}
	
	public synchronized void AddNode(String label, double x, double y) {
		m_nodes.put(label, new EnvMapNode(label, x, y, m_new_node_id));
		m_new_node_id++;
	}

	public synchronized void AddNode(String label, double x, double y, boolean charging) {
		EnvMapNode mn = new EnvMapNode(label, x, y, m_new_node_id);
		mn.setProperty(Phase1MapPropertyKeys.CHARGING_STATION, charging);
		m_nodes.put(label, mn);
		m_new_node_id++;
	}

	public synchronized void addArc(String source, String target, double distance, boolean enabled) {
		EnvMapArc arc = new EnvMapArc(source, target, distance, enabled);
		m_arcs.put(source + target, arc);
		m_arcs.put(target + source, arc);
		updateArcsLookup();
	}
	
    public synchronized void addArc (EnvMapArc a) {
        if (!doesArcExist(a.getSource(), a.getTarget())){
     	   m_arcs.put(a.getSource()+a.getTarget(),a);
     	   updateArcsLookup();
        }
     }

    public boolean doesArcExist (String na, String nb) {
        ListIterator<EnvMapArc> iter = getArcs().listIterator();
        while(iter.hasNext()){
            if(iter.next().isArcBetween(na, nb)) {
                return true;
            }
        }
        return false;
    }
    
    public Double pathDistance (List<String> path){
    	Double d=0.0;
    	for (int i=0; i<path.size()-1;i++){
    		d += distanceBetween(path.get(i), path.get(i+1));
    	}
    	return d;
    }
    
    
	public synchronized double getNodeX(String n) {
		EnvMapNode envMapNode = m_nodes.get(n);
		if (envMapNode != null)
			return envMapNode.getX();
		return Double.NEGATIVE_INFINITY;
	}

	public synchronized double getNodeY(String n) {
		EnvMapNode envMapNode = m_nodes.get(n);
		if (envMapNode != null)
			return envMapNode.getY();
		return Double.NEGATIVE_INFINITY;
	}

	public synchronized int getNodeId(String n) {
		EnvMapNode envMapNode = m_nodes.get(n);
		if (envMapNode == null)
			return -1;
		return envMapNode.getId();
	}

	/**
	 * Eliminates all arcs in map between nodes with labels na and nb (independently
	 * of whether na is the source or the target node in the arc)
	 * 
	 * @param na
	 *            String node a label
	 * @param nb
	 *            String node b label
	 */
	public synchronized void removeArcs(String na, String nb) {
		ListIterator<EnvMapArc> iter = getArcs().listIterator();
		while (iter.hasNext()) {
			if (iter.next().includesNodes(na, nb)) {
				iter.remove();
			}
		}
	}

	public static class NodeInsertion {
		protected String m_n;
		protected String m_na;
		protected String m_nb;
		protected double m_x;
		protected double m_y;

		public NodeInsertion copy() {
			NodeInsertion ni = new NodeInsertion();
			ni.m_n = m_n;
			ni.m_na = m_na;
			ni.m_nb = m_nb;
			ni.m_x = m_x;
			ni.m_y = m_y;
			return ni;
		}

	}

	public NodeInsertion getNodeInsertionResult() {
		return m_last_insertion.copy();
	}

	/**
	 * Returns Euclidean distance between locations with labels na and nb
	 * 
	 * @param na
	 *            String node label a
	 * @param nb
	 *            String node label b
	 * @return float distance
	 */
	public synchronized double distanceBetween(String na, String nb) {
		EnvMapNode a = m_nodes.get(na);
		EnvMapNode b = m_nodes.get(nb);
		return distanceBetweenCoords(a.getX(), a.getY(), b.getX(), b.getY());
	}

	public double distanceBetweenCoords(double x1, double y1, double x2, double y2) {
		double xc = Math.abs(x1 - x2);
		double yc = Math.abs(y1 - y2);
		return (float) Math.sqrt(xc * xc + yc * yc);
	}

	/**
	 * Inserts a new node in the map graph in between two nodes na and nb. The arcs
	 * between the original endpoints are split, and the new pair of arcs between
	 * the new node and nb are disabled (note that the order of na and nb in the
	 * invocation of the method matters!).
	 * 
	 * @param n
	 *            String label of the new node to insert
	 * @param na
	 *            String label of the node that the robot is moving away from
	 * @param nb
	 *            String label of the node that the robot is moving towards
	 * @param x
	 *            float coordinates of the location of the new node in the map
	 * @param y
	 */
	public synchronized void insertNode(String n, String na, String nb, double x, double y, boolean obstacle) {
		AddNode(n, x, y);
		addArc(na, n, distanceBetween(na, n), true);
		addArc(n, na, distanceBetween(na, n), true);
		if (obstacle) {
			removeArcs(na, nb);
		} else {
			addArc(nb, n, distanceBetween(nb, n), true);
			addArc(n, nb, distanceBetween(nb, n), true);
		}
		// Somehow, the planning things that n to nb is still valid
		// else {
		// addArc (nb, n, distanceBetween (nb, n), false);
		// addArc (n, nb, distanceBetween (nb, n), false);
		// }
		LinkedList<EnvMapArc> arcs = getArcs();
		for (EnvMapArc a : arcs) {
			System.out.println(a.m_source + " -> " + a.m_target + "(" + a.isEnabled() + ")");
		}
	}

	/**
	 * Reads map information from a JSON file into the EnvMap
	 * 
	 * @param mapFile
	 *            String filename of the JSON map file
	 */
	public synchronized void loadFromFile(String mapFile) {
		JSONObject loadJSONFromFile = loadJSONFromFile(mapFile);
		loadNodesFromJSON(loadJSONFromFile);
		loadArcsFromJSON(loadJSONFromFile);
		loadPropertiesFromFile(loadJSONFromFile);
	}

	private void loadPropertiesFromFile(JSONObject json) {
		// Do charging based on property

		JSONArray charge_stations = (JSONArray) json.get("charge_stations");
		if (charge_stations != null)
			charge_stations.forEach(new Consumer<Object>() {

				@Override
				public void accept(Object t) {
					EnvMapNode mapNode = m_nodes.get(t);
					mapNode.setProperty(Phase1MapPropertyKeys.CHARGING_STATION, true);
				}
			});

		JSONArray illuminance = (JSONArray) json.get("illuminance");
		if (illuminance != null)
			illuminance.forEach(new Consumer<Object>() {

				@Override
				public void accept(Object t) {
					JSONObject ill = (JSONObject) t;
					String src = (String) ill.get("src");
					String tgt = (String) ill.get("tgt");
					EnvMapArc arc = m_arcs.get(src + tgt);
					if (arc != null) {
						arc.setProperty(Phase2MapPropertyKeys.ILLUMINANCE,
								Double.parseDouble((String) ill.get("illuminance")));
					}
				}

			});

		JSONArray obstruction = (JSONArray) json.get("obstruction");
		if (obstruction != null)
			obstruction.forEach(new Consumer<Object>() {

				@Override
				public void accept(Object t) {
					JSONObject obs = (JSONObject) t;
					String src = (String) obs.get("src");
					String tgt = (String) obs.get("tgt");
					EnvMapArc arc = m_arcs.get(src + tgt);
					if (arc != null) {
						ObsLevel level = ObsLevel.NONE;
						if ("o1".equals(obs.get("label"))) {
							level = ObsLevel.HIGH;
						} else if ("o2".equals(obs.get("label")))
							level = ObsLevel.MEDIUM;
						else if ("o3".equals(obs.get("level")))
							level = ObsLevel.LOW;
						arc.setProperty(Phase2MapPropertyKeys.OBSTRUCTIONS, level);

					}
				}

			});
	}

	public synchronized void loadNodesFromFile(String mapFile) {
		JSONObject jsonObject = loadJSONFromFile(mapFile);
		loadNodesFromJSON(jsonObject);
	}

	private void loadNodesFromJSON(JSONObject jsonObject) {
		JSONArray nodes = (JSONArray) jsonObject.get("map");

		for (Object node : nodes) {
			JSONObject jsonNode = (JSONObject) node;
			String id = (String) jsonNode.get("node-id");
			JSONObject src_coords = (JSONObject) jsonNode.get("coords");
			if (src_coords == null) { // Try backward compatible "coord"
				src_coords = (JSONObject) jsonNode.get("coord");
			}
			double src_x = 0, src_y = 0;
			try {
				src_x = Double.parseDouble(String.valueOf(src_coords.get("x")));
				src_y = Double.parseDouble(String.valueOf(src_coords.get("y")));
			} catch (Exception e) {
				System.out.println("Error parsing coordinates in location " + id);
			}

			AddNode(id, src_x, src_y, id.indexOf("c") == 0 ? true : false); // The last parameter flags that this is a
																			// charging station
			// if the location's id starts with "c"... maybe to be changed later
			System.out.println("Added node " + id + " - X: " + String.valueOf(src_x) + " Y: " + String.valueOf(src_y)
					+ (id.indexOf("c") == 0 ? " (Charging Station)" : ""));
		}
	}

	private JSONObject loadJSONFromFile(String mapFile) {
		JSONParser parser = new JSONParser();
		NumberFormat format = NumberFormat.getInstance();
		mapFile = PrismConnector.convertToAbsolute(mapFile);
		Object obj = null;
		try {
			obj = parser.parse(new FileReader(mapFile));
		} catch (Exception e) {
			System.out.println("Could not load Map File");
		}

		JSONObject jsonObject = (JSONObject) obj;
		return jsonObject;
	}

	public synchronized void loadArcsFromFile(String mapFile) {
		JSONObject jsonObject = loadJSONFromFile(mapFile);
		loadArcsFromJSON(jsonObject);
	}

	private void loadArcsFromJSON(JSONObject jsonObject) {
		JSONArray nodes = (JSONArray) jsonObject.get("map");

		for (Object node : nodes) {
			JSONObject jsonNode = (JSONObject) node;
			String id = (String) jsonNode.get("node-id");
			JSONObject src_coords = (JSONObject) jsonNode.get("coords");
			if (src_coords == null)
				src_coords = (JSONObject) jsonNode.get("coord"); // try backwards compatible
			double src_x = 0, src_y = 0;
			try {
				src_x = Double.parseDouble(String.valueOf(src_coords.get("x")));
				src_y = Double.parseDouble(String.valueOf(src_coords.get("y")));
			} catch (Exception e) {
				System.out.println("Error parsing coordinates in location " + id);
			}

			JSONArray hitrates = (JSONArray) jsonObject.get("hitrate");
            JSONArray ttimes = (JSONArray) jsonObject.get("time");
			
			JSONArray neighbors = (JSONArray) jsonNode.get("connected-to");
			for (Object neighbor : neighbors) {
				String ns = String.valueOf(neighbor);
				double distance = distanceBetweenCoords(getNodeX(id), getNodeY(id), getNodeX(ns), getNodeY(ns));
				//addArc(id, ns, distance, true);
				//System.out.println("Added arc [" + id + "," + ns + "] (distance=" + distance + ")");
                EnvMapArc newarc = new EnvMapArc (id, ns, distance, true);
                // Add hitrates here
                
                if (!Objects.equal(null, hitrates)){
                	HashMap <String, Double> hitrateDictionary = retrieveHitrates(hitrates, newarc);
                
                	for (Map.Entry<String, Double> e: hitrateDictionary.entrySet()){
                		newarc.addHitRate(e.getKey(), e.getValue());
                		//System.out.println("Added HitRate: "+e.getKey()+" "+e.getValue());
                	}
                }
                
                if (!Objects.equal(null, ttimes)){
                	HashMap <String, Double> timeDictionary = retrieveTimes(ttimes, newarc);
                	for (Map.Entry<String, Double> e: timeDictionary.entrySet()){
                		newarc.addTime(e.getKey(), e.getValue());
                		//System.out.println("Added Time: "+e.getKey()+" "+e.getValue());
                	}
                }
                
                addArc (newarc);
                //addArc(id, ns, distance, true);
                System.out.println("Added arc ["+id+","+ns+"] (distance="+ distance +")" );
					
			}

		}
	}

    public HashMap<String, Double> retrieveHitrates(JSONArray hitrates, EnvMapArc a){
    	HashMap<String, Double> res = new HashMap<String, Double>();
    	for (Object hitrate: hitrates){
    		JSONObject jsonHitrate = (JSONObject) hitrate;
         	
         	String srcnode = (String) jsonHitrate.get("from");
     		String tgtnode = (String) jsonHitrate.get("to");
     		
     		if (Objects.equal(srcnode, a.getSource()) && Objects.equal(tgtnode, a.getTarget())){
     			
     			for (Object k: jsonHitrate.keySet()){
             		if (!Objects.equal(k.toString(), "from") && !Objects.equal(k.toString(), "to") && !Objects.equal(k.toString(), "prob")){
                 		//System.out.println(k.toString());
             			Double hr = ((Number)((JSONObject)jsonHitrate.get(k.toString())).get("hitrate")).doubleValue();
             			//System.out.println(hr.toString());
             			res.put(k.toString(), hr);
     				}
             	}    	
   	    	 return res;
     		}
    	 }
    	return res;
    }
    
    
    public HashMap<String, Double> retrieveTimes(JSONArray times, EnvMapArc a){
    	HashMap<String, Double> res = new HashMap<String, Double>();
    	for (Object ttime: times){
    		JSONObject jsonTime = (JSONObject) ttime;
         	
         	String srcnode = (String) jsonTime.get("from");
     		String tgtnode = (String) jsonTime.get("to");
     		
     		if (Objects.equal(srcnode, a.getSource()) && Objects.equal(tgtnode, a.getTarget())){
     			
     			for (Object k: jsonTime.keySet()){
             		if (!Objects.equal(k.toString(), "from") && !Objects.equal(k.toString(), "to") && !Objects.equal(k.toString(), "stdev") && !Objects.equal(k.toString(), "mean")){
                 		//System.out.println(k.toString());
             			Double hr = ((Number)((JSONObject)jsonTime.get(k.toString())).get("mean")).doubleValue();
             			//System.out.println(hr.toString());
             			res.put(k.toString(), hr);
     				}
             	}    	
   	    	 return res;
     		}
    	 }
    	return res;
    }
    
	
	public synchronized void initWithSimpleMap() {
		AddNode("l1", 14.474, 69);
		AddNode("l2", 19.82, 69);
		AddNode("l3", 42.5, 69);
		AddNode("l4", 52.22, 69);
		AddNode("l5", 52.22, 58.74);
		AddNode("l6", 42.5, 58.74);
		AddNode("l7", 19.82, 58.74);
		AddNode("l8", 19.82, 64.95);
		AddNode("ls", 52.22, 74.4);

		addArc("l1", "l2", 5.436, true);
		addArc("l2", "l1", 5.436, true);
		addArc("l2", "l3", 22.572, true);
		addArc("l3", "l2", 22.572, true);
		addArc("l3", "l4", 9.72, true);
		addArc("l4", "l3", 9.72, true);
		addArc("l2", "l8", 4.05, true);
		addArc("l8", "l2", 4.05, true);
		addArc("l8", "l7", 6.21, true);
		addArc("l7", "l8", 6.21, true);
		addArc("l7", "l6", 22.572, true);
		addArc("l6", "l7", 22.572, true);
		addArc("l3", "l6", 10.26, true);
		addArc("l6", "l3", 3, true);
		addArc("l4", "l5", 10.26, true);
		addArc("l5", "l4", 10.26, true);
		addArc("l6", "l5", 9.72, true);
		addArc("l5", "l6", 9.72, true);
		addArc("l4", "ls", 5.4, true);
		addArc("ls", "l4", 5.4, true);
	}

	/**
	 * Class test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		EnvMap dummyMap = new EnvMap(null, null);
		dummyMap.loadFromFile(PropertiesConnector.MAP_PROPKEY);
	}


}
