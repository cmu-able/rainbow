package org.sa.rainbow.brass.model.map;

import java.io.*;


import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapArc;
import org.sa.rainbow.brass.model.map.EnvMapNode;
import org.sa.rainbow.brass.model.map.dijkstra.Dijkstra;
import org.sa.rainbow.brass.model.map.dijkstra.Edge;
import org.sa.rainbow.brass.model.map.dijkstra.Graph;
import org.sa.rainbow.brass.model.map.dijkstra.Vertex;
import org.sa.rainbow.brass.model.map.BatteryPredictor;


import java.util.*;
import java.io.BufferedWriter;
import java.text.*;

/**
 * @author jcamara
 *
 * Eventually, the MapTranslator might be moved into a more general
 * translator incorporating elements from the architecture model, etc.
 */

public class MapTranslator {
	
	public static final String MODEL_TYPE = "mdp";
	public static final String MODULE_POSTFIX_STR = "_module";	
	public static final String TURN_VARIABLE = "turn";
	public static final String MOVE_CMD_STR = "_to_";
	public static final String MIN_POSTFIX = "_min";
	public static final String MAX_POSTFIX = "_max";
	public static final String INIT_POSTFIX = "_init";
	public static final String UPDATE_POSTFIX = "_upd";
	public static final String ENVIRONMENT_TURN_STR = "ET";
	public static final String ROBOT_TURN_STR = "RT";

	// Environment Configuration constants

	public static final String ENVIRONMENT_PLAYER_NAME = "env";
	public static final String ENVIRONMENT_UPDATE_HOUSEKEEPING_STR = " ("+TURN_VARIABLE+"'="+ROBOT_TURN_STR+")";
	public static final String ENVIRONMENT_GUARD_STR = "& (turn="+ENVIRONMENT_TURN_STR+")";
	
	// Robot Configuration constants

	public static final String ROBOT_PLAYER_NAME = "bot";
	public static final String ROBOT_GUARD_STR = "& (turn="+ROBOT_TURN_STR+")";
	public static final String ROBOT_UPDATE_HOUSEKEEPING_STR = " & ("+TURN_VARIABLE+"'="+ENVIRONMENT_TURN_STR+")";
	public static final String ROBOT_LOCATION_VAR = "l";
	public static final String INITIAL_ROBOT_LOCATION_CONST = "INITIAL_LOCATION";
	public static final String TARGET_ROBOT_LOCATION_CONST = "TARGET_LOCATION";
	public static final String ROBOT_BATTERY_VAR = "b";
	public static final String ROBOT_BATTERY_RANGE_MIN = "0";
	public static final String ROBOT_BATTERY_RANGE_MAX = "32560";
	public static final String INITIAL_ROBOT_BATTERY_CONST = "INITIAL_BATTERY";
	public static final String ROBOT_BATTERY_DELTA = "10"; // Constant for the time being, this should be transition+context dependent
	public static final String BATTERY_GUARD_STR="& ("+ROBOT_BATTERY_VAR+">="+ROBOT_BATTERY_DELTA+")"; // Not used for the time being (battery depletion condition covered by STOP_GUARD_STR)
	public static final String BATTERY_UPDATE_STR = ROBOT_BATTERY_VAR+UPDATE_POSTFIX;
    
	public static final float ROBOT_FULL_SPEED_VALUE = 0.68f; // m/s
	public static final float ROBOT_HALF_SPEED_VALUE = 0.35f;
	public static final String ROBOT_FULL_SPEED_CONST = "FULL_SPEED"; // These are just symbolic constants for PRISM
	public static final String ROBOT_HALF_SPEED_CONST = "HALF_SPEED";
	public static final String ROBOT_SPEED_VAR = "s";

	// Goal and stop condition configuration constants
	
	public static final String GOAL_PRED="goal"; // Goal achieved predicate (currently target location reached)
	public static final String GOAL_PRED_DEF=GOAL_PRED+" = "+ROBOT_LOCATION_VAR+"="+TARGET_ROBOT_LOCATION_CONST+";";

	public static final String	STOP_PRED = "stop"; // Stop condition predicate (currently target location reached of insufficient battery to move to a nearby location)
	public static final String	STOP_PRED_DEF = STOP_PRED + " = "+GOAL_PRED+" | "+ROBOT_BATTERY_VAR+"<"+ROBOT_BATTERY_DELTA+";";
	public static final String	STOP_GUARD_STR = "& (!"+STOP_PRED+")";
	
	private static EnvMap m_map;
	
	/**
	 * Sets the map to translate into a PRISM specification
	 * @param map an EnvMap encoding the graph that captures the physical space
	 */
	public static void setMap(EnvMap map){
		m_map = map;
	}
	
	/**
	 * Generates PRISM model game structure - Alternating Robot/Environment turns
	 * @return String a string with the general declarations for PRISM model turn structure
	 */
	public static String generateGameStructure(){
		 String buf=new String();
		 buf+=MODEL_TYPE+"\n\n";
		 buf+="const "+ENVIRONMENT_TURN_STR+"=0;\n";
		 buf+="const "+ROBOT_TURN_STR+"=1;\n";
		 buf+="\nglobal "+TURN_VARIABLE+":["+ENVIRONMENT_TURN_STR+".."+ROBOT_TURN_STR+"] init "+ENVIRONMENT_TURN_STR+";\n\n";		 
		 return buf+"\n";
	}
	 
	
	/**
	 * Generates a list of labels for movement commands between locations
	 * @return LinkedString<String> list of movement between location command strings 
	 * Unused for the time being
	 */
	public static LinkedList<String> generateMoveCommandStrs(){
		 LinkedList<String> res = new LinkedList<String> ();
		 LinkedList<EnvMapArc> arcs = m_map.getArcs();
		 for (int i=0; i<arcs.size(); i++){
			 res.add(arcs.get(i).getSource()+MOVE_CMD_STR+arcs.get(i).getTarget());
		 }
		 return res;
	}
	
	/**
	 * Generates labels for map locations
	 * @return String PRISM encoding for map location constants
	 */
	public static String generateLocationConstants(){
	    String buf="// Map location constants\n\n";
	    buf+="const "+INITIAL_ROBOT_LOCATION_CONST+";\n";
	    buf+="const "+TARGET_ROBOT_LOCATION_CONST+";\n\n";
	    buf+="formula "+GOAL_PRED_DEF+"\n\n";
	    buf+="formula "+STOP_PRED_DEF+"\n\n";
	    for (Map.Entry<String,EnvMapNode> entry : m_map.getNodes().entrySet() ){
	    	buf+="const "+entry.getKey()+"="+String.valueOf(entry.getValue().getId())+";\n";
	    }
	    return buf+"\n";
	}
	
	public static String generateEnvironmentModule(){
		String  buf="// Environment process\n\n";
		buf+="module "+ENVIRONMENT_PLAYER_NAME+MODULE_POSTFIX_STR+"\n";
		buf+="end:bool init false;\n\n";
		buf+="\t[] true "+ENVIRONMENT_GUARD_STR +" "+STOP_GUARD_STR+"-> "+ENVIRONMENT_UPDATE_HOUSEKEEPING_STR+";\n";
		buf+="\t[] "+STOP_PRED +"  & !end -> (end'=true);\n";
		buf+="endmodule\n\n";
		return buf;
	}
	        		
	/**
	 * @return String PRISM encoding for the robot process
	 */
	public static String generateRobotModule(){
        String buf="// Robot process\n\n";
        buf+="const "+INITIAL_ROBOT_BATTERY_CONST+";\n";
        buf+="const "+ROBOT_HALF_SPEED_CONST+"=0;\n";
        buf+="const "+ROBOT_FULL_SPEED_CONST+"=1;\n";
        buf+="\n"+generateBatteryUpdates();
        buf+="module "+ROBOT_PLAYER_NAME+MODULE_POSTFIX_STR+"\n";
        buf+=ROBOT_BATTERY_VAR+":["+ROBOT_BATTERY_RANGE_MIN+".."+ROBOT_BATTERY_RANGE_MAX+"] init "+INITIAL_ROBOT_BATTERY_CONST+";\n";
        buf+=ROBOT_LOCATION_VAR+":[0.."+m_map.getNodeCount()+"] init "+INITIAL_ROBOT_LOCATION_CONST+";\n";
        buf+=ROBOT_SPEED_VAR+":["+ROBOT_HALF_SPEED_CONST+".."+ROBOT_FULL_SPEED_CONST+"] init "+ROBOT_HALF_SPEED_CONST+";\n";
        buf+="robot_done:bool init false;\n";
        buf+="\t[] true "+ROBOT_GUARD_STR+" "+STOP_GUARD_STR+" & (robot_done) -> (robot_done'=false)"+ROBOT_UPDATE_HOUSEKEEPING_STR+";\n";
        buf+="\n"+generateMoveCommands();
        buf+="endmodule\n\n";
        return buf;
		
	}
	
	/**
	 * Generates battery update formulas employed in updates of movement commands in robot module
	 * @return String PRISM encoding for possible battery updates (corresponding to movements between map locations)
	 */
	public static String generateBatteryUpdates(){
        String buf="";
        BatteryPredictor bp = new BatteryPredictor();
        for (int i=0;i<m_map.getArcs().size();i++){
        	EnvMapArc a = m_map.getArcs().get(i);
            float t_distance = a.getDistance();
    	    String battery_delta_half_speed= String.valueOf(Math.round(bp.batteryConsumption(ROBOT_HALF_SPEED_CONST, t_distance/ROBOT_HALF_SPEED_VALUE)));
    	    String battery_delta_full_speed= String.valueOf(Math.round(bp.batteryConsumption(ROBOT_FULL_SPEED_CONST, t_distance/ROBOT_FULL_SPEED_VALUE)));
    	    buf+="formula "+BATTERY_UPDATE_STR+"_"+a.getSource()+"_"+a.getTarget()+"= "+ROBOT_SPEED_VAR+"="+ROBOT_HALF_SPEED_CONST+"? max(0,"+ROBOT_BATTERY_VAR+"-"+battery_delta_half_speed+") : max(0,"+ROBOT_BATTERY_VAR+"-"+battery_delta_full_speed+")" +";\n";    	        
        }
        return buf+"\n";
	}

	
	/**
	 * Generates PRISM encoding for movement commands between locations in the robot module
	 * @return String encoding between location command strings 
	 */
	public static String generateMoveCommands(){
        String buf="";
        for (int i=0;i<m_map.getArcs().size();i++){
        	EnvMapArc a = m_map.getArcs().get(i);
            buf+="\t ["+a.getSource()+MOVE_CMD_STR+a.getTarget()+"] ("+ROBOT_LOCATION_VAR+"="+a.getSource()+") "+STOP_GUARD_STR+" "+ROBOT_GUARD_STR+" & (!robot_done) -> ("+ROBOT_LOCATION_VAR+"'="+a.getTarget()+") "+" & ("+ROBOT_BATTERY_VAR+"'="+BATTERY_UPDATE_STR+"_"+a.getSource()+"_"+a.getTarget()+")"+" & (robot_done'=true);\n";
        }
        return buf+"\n";		
	}
	
	/**
	 * @return String PRISM encoding for time rewards associated with an EnvMap
	 */
	public static String generateTimeReward(){
        String buf="rewards \"time\"\n";
        NumberFormat f = new DecimalFormat("#0.0000");
        for (int i=0;i<m_map.getArcs().size();i++){
        	EnvMapArc a = m_map.getArcs().get(i);
            float t_distance = a.getDistance(); //  float(self.get_transition_attribute_value(t,"distance"))
            String t_time_half_speed=f.format(t_distance/ROBOT_HALF_SPEED_VALUE);
            String t_time_full_speed=f.format(t_distance/ROBOT_FULL_SPEED_VALUE);
            buf+="\t["+a.getSource()+MOVE_CMD_STR+a.getTarget()+"] true :"+ROBOT_SPEED_VAR+"="+ROBOT_HALF_SPEED_CONST+"? "+t_time_half_speed+" : "+t_time_full_speed+";\n";
        }
        buf+="endrewards\n\n";
        return buf;
	}
	
	
	/**
	 * Returns shortest distance between two nodes computed using Dijkstra's Algorithm
	 * @param node1 String label of source node (associated with an EnvMapNode in the map to translate)
	 * @param node2 String label of target node (associated with an EnvMapNode in the map to translate)
	 * @return float shortest distance between node1 and node2
	 */
	public static float shortestPathDistance(String node1, String node2){
		
		 Graph graph = new Graph();
	        Vertex[] vertices = new Vertex[m_map.getNodeCount()];
	        
	        Map<String, String> node_indexes = new HashMap<> ();

	        int i=0;
	        for (Map.Entry<String,EnvMapNode> entry : m_map.getNodes().entrySet() ){
	        	vertices[i] = new Vertex(entry.getKey());
	        	graph.addVertex(vertices[i], true);
	        	node_indexes.put(entry.getKey(), String.valueOf(i));
		    	i++;
	        }
	        
	        Edge[] edges = new Edge[m_map.getArcCount()];
	        
	        
	        for (i=0;i<m_map.getArcs().size();i++){
	        	EnvMapArc a = m_map.getArcs().get(i);
	        	Vertex source = vertices[Integer.parseInt(node_indexes.get(a.getSource()))];
	        	Vertex target = vertices[Integer.parseInt(node_indexes.get(a.getTarget()))];
	        	edges[i] = new Edge(source, target, a.getDistance());
	        }
	        
	        for(Edge e: edges){
	            graph.addEdge(e.getOne(), e.getTwo(), e.getWeight());
	        }
	        
	        Dijkstra dijkstra = new Dijkstra(graph, node1);
	        return (dijkstra.getDistanceTo(node2));
	}
	
	
	/**
	 * @return String PRISM encoding for distance rewards between nodes in the map
	 */
	public static String generateDistanceReward(){
		String buf="rewards \"distance\"\n";
		
		for (Map.Entry<String,EnvMapNode> entry : m_map.getNodes().entrySet() ){
		    String v = entry.getKey();
		    
		    buf+="\t"+STOP_PRED+" & "+TARGET_ROBOT_LOCATION_CONST+"="+v+" : ";

		    for (Map.Entry<String,EnvMapNode> entry2 : m_map.getNodes().entrySet() ){
			    String v2 = entry2.getKey();
                if (!v2.equals(v)){
                	buf+= ROBOT_LOCATION_VAR+"="+v2+" ? "+String.valueOf(shortestPathDistance(v,v2))+" : ";
                }
		    }
		    buf+=" 0;\n";

		}
	    buf+="endrewards\n\n";
	    return buf;		
	}

	
	/**
	 * Generates the PRISM specification for an adaptation scenario, based on a given EnvMap
	 * @return String PRISM encoding for adaptation scenario
	 */
	public static String getMapTranslation(){
		 String buf="// Generated with BRASS Robot Map PRISM Renderer\n\n";
		 buf+=generateGameStructure()+"\n";
		 buf+=generateLocationConstants()+"\n";
		 buf+=generateEnvironmentModule()+"\n";
		 buf+=generateRobotModule()+"\n";
		 buf+=generateTimeReward()+"\n";
		 buf+=generateDistanceReward()+"\n";
		 buf+="// --- End of generated code ---\n";
		 return buf;
	}
	
	/**
	 * Generates and exports the PRISM specification for an adaptation scenario to a text file
	 * @param f String filename to export PRISM specification
	 */
	public static void exportMapTranslation(String f){
		try {
			BufferedWriter out = new BufferedWriter (new FileWriter(f));
			out.write(getMapTranslation());
			out.close();
		}
		catch (IOException e){
			System.out.println("Error exporting PRISM map translation");
		}
	}
	
	public MapTranslator(){
		
	}
	
	public static void main(String[] args) {
		EnvMap dummyMap = new EnvMap(null);		 	
		setMap(dummyMap);
		System.out.println(getMapTranslation()); // Class test
		System.out.println();
	}
}
