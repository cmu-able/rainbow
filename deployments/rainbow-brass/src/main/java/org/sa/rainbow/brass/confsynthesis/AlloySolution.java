package org.sa.rainbow.brass.confsynthesis;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

/**
 * @author jcamara
 *
 */
public class AlloySolution {
	
	public class AlloySolutionNode {

		public String m_id;
		
		AlloySolutionNode(String id){
			this.m_id=id;
		}
		
		public String getId() {
			return m_id;
		}

		public void setId(String m_id) {
			this.m_id = m_id;
		}
		
		@Override
		public String toString() {
			return "[AlloySolutionNode "+m_id+"]";
		}
	}
	
	public class AlloySolutionArc {
		public String m_id;
		public String m_source;
		public String m_target;
		public String m_relation;
		
		AlloySolutionArc(String id, String source, String target, String relation){
			this.m_id=id;
			this.m_source=source;
			this.m_target=target;
			this.m_relation=relation;
		}
		
		AlloySolutionArc(String id, AlloySolutionNode source, AlloySolutionNode target, String relation){
			this.m_id=id;
			this.m_source=source.getId();
			this.m_target=source.getId();
			this.m_relation=relation;
		}

		public String getId() {
			return m_id;
		}
		public void setId(String m_id) {
			this.m_id = m_id;
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
		public String getRelation() {
			return this.m_relation;
		}
		public void setRelation(String m_relation) {
			this.m_relation = m_relation;
		}
		
		@Override
		public String toString() {
			return "[AlloySolutionArc "+m_relation+"("+m_source+","+m_target+")]";
		}
	}
	
	private HashMap<String, AlloySolutionNode> m_nodes = new HashMap<String, AlloySolutionNode>();
	private HashMap<String, AlloySolutionArc> m_arcs = new HashMap<String, AlloySolutionArc>();
	private HashMap<String, LinkedList<String>> m_instances = new HashMap<String, LinkedList<String>>();
	
	
	public void addInstances (String type, LinkedList<String> l){
		m_instances.put(type, l);
	}
	
	public HashMap<String, LinkedList<String>> getAllInstances() {
		return m_instances;
	}
	
	public LinkedList<String> getInstances (String type){
		return m_instances.get(type);
	}
	
	/**
	 * @return the m_nodes
	 */
	public HashMap<String, AlloySolutionNode> getNodes() {
		return m_nodes;
	}

	/**
	 * @return the m_arcs
	 */
	public HashMap<String, AlloySolutionArc> getArcs() {
		return m_arcs;
	}

	public void addNode(String id){
		m_nodes.put(id, new AlloySolutionNode(id));
	}
	
	public void addNode(AlloySolutionNode node){
		m_nodes.put(node.getId(), node);
	}
	
	public AlloySolutionNode getNode(String id){
		return m_nodes.get(id);
	}
	
	public void addArc(String id, String source, String target, String relation){
		m_arcs.put(id, new AlloySolutionArc(id, source, target, relation));
	}	
	
	public void addArc(AlloySolutionArc arc){
		m_arcs.put(arc.getId(), arc);
	}
	
	public void loadFromString(String sol){
		String[] solbits = sol.split("\n");
		String[] nodes;
		String[] arcs;
		String[] chunks;
		
		for (int i=0; i<solbits.length; i++){
			chunks = solbits[i].split("=");
			
			if (chunks[0].startsWith("this/") && (!solbits[i].contains("<:"))){
				nodes = chunks[1].replaceAll("\\{|\\}", "").split(", ");
				LinkedList<String> linstances = new LinkedList<String>();
				for (int j=0; j < nodes.length; j++)
					linstances.add(nodes[j]);
				addInstances(chunks[0].replaceAll("this/", "").trim(), linstances);
			}
			
			if (Objects.equals(chunks[0], "univ")){
				nodes = chunks[1].replaceAll("\\{|\\}", "").split(", ");
				for (int j=0; j < nodes.length; j++)
					this.addNode(nodes[j]);
			}
			
			if (chunks[0].contains("<:")){
				arcs = chunks[1].replaceAll("\\{|\\}", "").split(", ");
				if (!Objects.equals(arcs[0], "")){
					for (int j=0; j < arcs.length; j++) {
						String relation = chunks[0].split("<:")[1];
						String[] arcbits = arcs[j].split("->");
						//System.out.println(arcs[j]);
						this.addArc(relation+"-"+arcbits[0]+"-"+arcbits[1], arcbits[0], arcbits[1],relation);
					}
				}
			}
		}
	
	}
	
	
	public HashMap<String, AlloySolutionNode> getRelated (AlloySolutionNode node, String relation){
		HashMap<String, AlloySolutionNode> res = new HashMap<String, AlloySolutionNode>();
		for (Map.Entry<String, AlloySolutionArc> e : m_arcs.entrySet()){
			if (Objects.equals(e.getValue().getSource(),node.getId())){
				if (Objects.equals(e.getValue().getRelation(), relation) | Objects.equals("", relation))
					res.put(e.getValue().m_id, getNode(e.getValue().getTarget()));
			}
		}
		return res;
	}
	
	@Override
	public String toString() {
	    String res ="Nodes:\n";
	    for (AlloySolutionNode node : m_nodes.values())
	    	res += String.valueOf(node);
	    res+="\nArcs:\n";
	    for (AlloySolutionArc arc : m_arcs.values())
	    	res += String.valueOf(arc);
	    return res;
	}
	

	
	
	public String toJSON(String conflabel){
		JSONObject obj = new JSONObject();
		obj.put("id", conflabel);
		
		JSONArray graph = new JSONArray();
		for (AlloySolutionNode node : m_nodes.values()){
	    	JSONObject nodeobj = new JSONObject();
	    	String nodeid = node.getId().replace("$", "");
	    	JSONArray nodeconns = new JSONArray();
	    	for (AlloySolutionArc arc : m_arcs.values()){
		    	String src = arc.getSource().replace("$", "");
		    	String tgt = arc.getTarget().replace("$", "");
		    	String label = arc.getRelation();
		    	
		    	if (Objects.equals(src, nodeid)){
		    		JSONObject auxobj = new JSONObject();
		    		auxobj.put(label, tgt);
		    		nodeconns.add(auxobj);
		    	}
		    }
	    	nodeobj.put(nodeid, nodeconns);
			graph.add(nodeobj);	    	
		}
		obj.put("graph", graph);
		
	    return(obj.toString());
	}
	
	
	
	public static void main(String[] args){
		
		AlloyConnector ac = new AlloyConnector();
		
		ac.generateSolutions("/Users/jcamara/Dropbox/Documents/Work/Projects/AlloyQ/GPA.als");
		String strSol = ac.getSolution(AlloyConnector.SOLUTION_STRING+"1");
		System.out.println(strSol);		
		AlloySolution sol = new AlloySolution();
		sol.loadFromString(strSol);
		System.out.println(String.valueOf(sol));
		
		for (Map.Entry<String, AlloySolutionNode> e : sol.getRelated(sol.getNode("Man$0"),"").entrySet()){
			System.out.println(String.valueOf(e.getKey())+" "+String.valueOf(e.getValue()));
		}
		
	}
}
