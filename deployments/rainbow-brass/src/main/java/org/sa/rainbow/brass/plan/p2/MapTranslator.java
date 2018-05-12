package org.sa.rainbow.brass.plan.p2;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Collections;

import org.sa.rainbow.brass.model.map.dijkstra.Dijkstra;
import org.sa.rainbow.brass.model.map.dijkstra.Edge;
import org.sa.rainbow.brass.model.map.dijkstra.Graph;
import org.sa.rainbow.brass.model.map.dijkstra.Vertex;
import org.sa.rainbow.brass.model.mission.MissionState;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapArc;
import org.sa.rainbow.brass.model.map.EnvMapNode;
import org.sa.rainbow.brass.model.map.EnvMapPath;


import org.sa.rainbow.brass.confsynthesis.ConfigurationProvider;
import org.sa.rainbow.brass.confsynthesis.Configuration;
import org.sa.rainbow.brass.confsynthesis.ConfigurationSynthesizer;

import com.google.common.base.Objects;

/**
 * @author jcamara
 *
 * Eventually, the MapTranslator might be moved into a more general
 * translator incorporating elements from the architecture model, etc.
 */


public class MapTranslator {

    public static final String VERSION_STR = "V0.5a - April 2018";

    public static final String MODEL_TYPE = "mdp";
    public static final String MODULE_POSTFIX_STR = "_module";	
    public static final String TURN_VARIABLE = "turn";
    public static final String MOVE_CMD_STR = "_to_";
    public static final String MIN_POSTFIX = "_min";
    public static final String MAX_POSTFIX = "_max";
    public static final String INIT_POSTFIX = "_init";
    public static final String UPDATE_POSTFIX = "_upd";
    public static final String HEADING_CONST_PREFIX="H_";
    public static final String ROTATION_TIME_FORMULA_PREFIX="rot_time_";
    public static final String ROTATION_ENERGY_FORMULA_PREFIX="rot_energy_";
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
    public static long ROBOT_BATTERY_RANGE_MAX = 180000;
    public static final String ROBOT_BATTERY_RANGE_MAX_CONST = "MAX_BATTERY";
    public static final String INITIAL_ROBOT_BATTERY_CONST = "INITIAL_BATTERY";
    public static final String ROBOT_BATTERY_DELTA = "10"; // Constant for the time being, this should be transition+context dependent
    public static final String BATTERY_GUARD_STR="& ("+ROBOT_BATTERY_VAR+">="+ROBOT_BATTERY_DELTA+")"; // Not used for the time being (battery depletion condition covered by STOP_GUARD_STR)
    public static final String BATTERY_UPDATE_STR = ROBOT_BATTERY_VAR+UPDATE_POSTFIX;
    public static final float ROBOT_CHARGING_TIME = 15.0f;

    public static final float ROBOT_FULL_SPEED_VALUE = 0.68f; // m/s
    public static final float ROBOT_HALF_SPEED_VALUE = 0.35f;
    public static final float ROBOT_SAFE_SPEED_VALUE = 0.25f;
    public static final float ROBOT_DR_SPEED_VALUE = 0.25f; // Dead reckoning speed value .. this is implicit in ROBOT_LOC_MODE_LO
    public static final String ROBOT_FULL_SPEED_CONST = "FULL_SPEED"; // These are just symbolic constants for PRISM
    public static final String ROBOT_HALF_SPEED_CONST = "HALF_SPEED";
    public static final String ROBOT_DR_SPEED_CONST = "DR_SPEED";
    public static final String ROBOT_SPEED_VAR = "s";

    public static final float  ROBOT_ROTATIONAL_SPEED_VALUE = 1.5f;             // rad/s
    public static final String ROBOT_HEADING_VAR = "r";
    public static final String INITIAL_ROBOT_HEADING_CONST = "INITIAL_HEADING";

    public static final String ROBOT_CONF_VAR = "c";
    public static final String INITIAL_ROBOT_CONF_CONST = "INITIAL_CONFIGURATION";
    public static final String ROBOT_MAX_RECONF_CONST = "RECONF_MAX";
    public static final String ROBOT_RECONF_VAR = "cr";
    public static final String ROBOT_MAX_RECONF_VAL = "1"; // Only reconfiguring at start of plan, for the time being...
//    public static final String ROBOT_CONF_PREFIX = "sol_";


    public static final String ROBOT_COLLISION_VAR = "collided";

    
    public static final float  MAXIMUM_KINECT_OFF_DISTANCE_VAL = 6.0f; // Maximum driving distance with kinect off in m.



    // Goal and stop condition configuration constants

    public static final String GOAL_PRED="goal"; // Goal achieved predicate (currently target location reached)
    public static final String GOAL_PRED_DEF=GOAL_PRED+" = "+ROBOT_LOCATION_VAR+"="+TARGET_ROBOT_LOCATION_CONST+";";

    public static final String	STOP_PRED = "stop"; // Stop condition predicate (currently target location reached of insufficient battery to move to a nearby location)
    public static final String	STOP_PRED_DEF = STOP_PRED + " = "+GOAL_PRED+" | "+ROBOT_BATTERY_VAR+"<"+ROBOT_BATTERY_DELTA+";";
    public static final String	STOP_GUARD_STR = "& (!"+STOP_PRED+")";

    public static final double MAX_DISTANCE = 999.0; // Distance assigned to disabled edges (for distance reward computation)
    public static final double DEFAULT_TIME_TACTIC_TIME=1; // Tactics are not instantaneous;
    public static final String TACTIC_PREFIX="t";

    // Properties' indices
    public static final int TIME_PROPERTY = 0;
    public static final int ACCURACY_PROPERTY = 1;

    private static EnvMap m_map;
    private static ConfigurationProvider m_cp;

    

    /**
     * Sets the Configuration provider
     */
    public static void setConfigurationProvider(ConfigurationProvider cp){
        m_cp = cp;
    }
    
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
        synchronized (m_map) {
            LinkedList<String> res = new LinkedList<String> ();
            LinkedList<EnvMapArc> arcs = m_map.getArcs();
            for (int i=0; i<arcs.size(); i++){
                res.add(arcs.get(i).getSource()+MOVE_CMD_STR+arcs.get(i).getTarget());
            }
            return res;
        }
    }

    /**
     * Generates labels for robot orientation/heading
     * @return String PRISM encoding for robot orientation constants
     */
    public static String generateHeadingConstants(){
        String buf="// Robot heading/orientation constants\n\n";
        int c=0;
        for (MissionState.Heading h : MissionState.Heading.values()) {
            buf+="const "+HEADING_CONST_PREFIX+h.name()+"="+String.valueOf(c)+";\n";
            c++;
        }
        return buf+"\n";
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
    
    /**
     * Generates labels for robot configurations
     * @return String PRISM encoding for configuration constants
     */
    public static String generateConfigurationConstants(){
        String buf="// Configuration constants\n\n";
        buf+="const "+INITIAL_ROBOT_CONF_CONST+";\n";
        int cid=0;
        for (Map.Entry<String,Configuration> entry : m_cp.getConfigurations().entrySet() ){
            buf+="const "+entry.getKey()+"="+String.valueOf(cid)+";\n";
            cid+=1;
        }
        return buf+"\n";
    }

    /**
     * @return String PRISM encoding for the environment process
     */
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
     * @param boolean inhibitTactics if true, it does not generate any tactic commands (just movement commands)
     */
    public static String generateRobotModule(boolean inhibitTactics){
        String buf="// Robot process\n\n";
        buf+="const "+ROBOT_BATTERY_RANGE_MAX_CONST+"="+ROBOT_BATTERY_RANGE_MAX+";\n";
        buf+="const "+INITIAL_ROBOT_BATTERY_CONST+";\n";
        buf+="const "+INITIAL_ROBOT_HEADING_CONST+";\n";
        buf+="const "+ROBOT_MAX_RECONF_CONST+"="+ROBOT_MAX_RECONF_VAL+";\n";   
      
        buf+="\n"+generateBatteryUpdates();
        buf+="module "+ROBOT_PLAYER_NAME+MODULE_POSTFIX_STR+"\n";
        buf+=ROBOT_BATTERY_VAR+":["+ROBOT_BATTERY_RANGE_MIN+".."+ROBOT_BATTERY_RANGE_MAX_CONST+"] init "+INITIAL_ROBOT_BATTERY_CONST+";\n";
        buf+=ROBOT_LOCATION_VAR+":[0.."+m_map.getNodeCount()+"] init "+INITIAL_ROBOT_LOCATION_CONST+";\n";
        buf+=ROBOT_CONF_VAR+":[-1.."+m_cp.getConfigurations().size()+"] init "+INITIAL_ROBOT_CONF_CONST+";\n";
        buf+=ROBOT_HEADING_VAR+":[0.."+String.valueOf(MissionState.Heading.values().length)+"] init "+INITIAL_ROBOT_HEADING_CONST+";\n";
        buf+=ROBOT_RECONF_VAR+":[0.."+ROBOT_MAX_RECONF_VAL+"] init 0;\n";
        buf+=ROBOT_COLLISION_VAR+": bool init false;\n";

        buf+="robot_done:bool init false;\n";
        buf+="\t[] true "+ROBOT_GUARD_STR+" "+STOP_GUARD_STR+" & (robot_done) -> (robot_done'=false)"+ROBOT_UPDATE_HOUSEKEEPING_STR+";\n";
        if (!inhibitTactics) {
            buf+="\n"+generateTacticCommands();
        }
        buf+="\n"+generateMoveCommands();
        buf+="endmodule\n\n";
        return buf;

    }

    /**
     * Generates battery update formulas employed in updates of movement commands in robot module
     * @return String PRISM encoding for possible battery updates (corresponding to movements between map locations)
     */

    private static String getDeltaEnergy(Configuration config, double distance){
        return String.valueOf (Math.round (config.getSpeed()*distance*config.getEnergyDischargeRate()));
    }

    public static String generateBatteryUpdates(){
        synchronized (m_map) {
            String buf="";
//          buf+="formula b_upd_charge = min("+ROBOT_BATTERY_VAR+"+"+String.valueOf(Math.round (bp.batteryCharge(ROBOT_CHARGING_TIME)))+", "+ROBOT_BATTERY_RANGE_MAX_CONST+");\n\n";
            buf+="formula b_upd_charge = "+ROBOT_BATTERY_RANGE_MAX_CONST+";\n\n";
            for (int i=0;i<m_map.getArcs().size();i++){
                EnvMapArc a = m_map.getArcs().get(i);
                if (!a.isEnabled ()) {
                    continue;
                }
                double t_distance = a.getDistance ();

                HashMap<String,String> battery_deltas = new HashMap<String, String>();
                for (Map.Entry<String, Configuration> c: m_cp.getConfigurations().entrySet()){
                	battery_deltas.put(c.getKey(),getDeltaEnergy(c.getValue(), t_distance));
                }

                String rote = ROTATION_ENERGY_FORMULA_PREFIX+a.getSource()+MOVE_CMD_STR+a.getTarget();

                String formulaBaseName = BATTERY_UPDATE_STR+"_"+a.getSource()+"_"+a.getTarget();
                buf+="formula " + formulaBaseName + "= ";
                int counter=0;
                for (Map.Entry<String, String> d: battery_deltas.entrySet()){
                	if (counter>0) buf += " : ";
                	buf += ROBOT_CONF_VAR + "=" + d.getKey() + "? max(0,"+ROBOT_BATTERY_VAR+"-("+d.getValue()+"+"+rote+"))";
                	counter++;
                }
                buf += ": 0;\n";
            }
            return buf+"\n";
        }
    }


    /**
     * Generates PRISM encoding for movement commands between locations in the robot module
     * @return String encoding between location command strings 
     */
    public static String generateMoveCommands(){
        synchronized (m_map) {
            String buf="";
            String confStr="";
            for (int i=0;i<m_map.getArcs().size();i++){
                EnvMapArc a = m_map.getArcs().get(i);
                String hitRateGuard = "";
                String hitRateComplementGuard = "";
                if (a.isEnabled()){
                	if (!a.includesHitRates(m_cp)){ // If no risk of collision exists, generate only a simple deterministic command for the transition
                		buf+="\t ["+a.getSource()+MOVE_CMD_STR+a.getTarget()+"] ("+ROBOT_LOCATION_VAR+"="+a.getSource()+") "+STOP_GUARD_STR+" "+ROBOT_GUARD_STR+" & (!robot_done) -> ("+ROBOT_LOCATION_VAR+"'="+a.getTarget()+") "+" & ("+ROBOT_BATTERY_VAR+"'="+BATTERY_UPDATE_STR+"_"+a.getSource()+"_"+a.getTarget()+")"+ " & ("+ROBOT_HEADING_VAR+"'="+HEADING_CONST_PREFIX + findArcHeading(a).name() + ") & (robot_done'=true);\n";                	
                		//System.out.println("\t ["+a.getSource()+MOVE_CMD_STR+a.getTarget()+"] does not include hitrates");
                	} else { // If collision risk exist, generate alternative probabilistic branches
                	    
                   		for (Map.Entry<String, Configuration> c: m_cp.getConfigurations().entrySet()){
                   			confStr=m_cp.translateId(c.getKey());
                   			if (a.getHitRate(confStr)>0.0){
                   				String confGuard = "("+ROBOT_CONF_VAR+"="+c.getValue().getId()+")";
			                    buf+="\t ["+a.getSource()+MOVE_CMD_STR+a.getTarget()+"] ("+ROBOT_LOCATION_VAR+"="+a.getSource()+") "+STOP_GUARD_STR+" "+ROBOT_GUARD_STR+" & "+confGuard+" & (!robot_done) -> ";                	
			                    
			                    String mainUpdateStr = "("+ROBOT_LOCATION_VAR+"'="+a.getTarget()+") "+" & ("+ROBOT_BATTERY_VAR+"'="+BATTERY_UPDATE_STR+"_"+a.getSource()+"_"+a.getTarget()+")"+ " & ("+ROBOT_HEADING_VAR+"'="+HEADING_CONST_PREFIX + findArcHeading(a).name() + ") & (robot_done'=true)";
			                    
			                    if (a.getHitRate(confStr)>0.0){
			                    	hitRateComplementGuard = String.valueOf(1.0-a.getHitRate(confStr)) + ": ";
			                    	hitRateGuard = String.valueOf(a.getHitRate(confStr)) + ": ";
			                    	buf+= " "+ hitRateGuard + mainUpdateStr + " & ("+ROBOT_COLLISION_VAR+"'=true)";                	
			                        buf+= " + "+ hitRateComplementGuard + mainUpdateStr + " & ("+ROBOT_COLLISION_VAR+"'=false);\n";                	
			                    } else {
			                    	buf+= " "+ mainUpdateStr+";\n";        
			                    }
                   			}
                   		}
                    	
                   	}
                }
            }
            return buf+"\n";		
        }
    }

    /**
     * Returns PRISM encoding for robot module tactics
     * @return
     */
    public static String generateTacticCommands(){
        String buf="";
        buf += generateReconfTacticCommands();
        buf += generateChargingTacticCommands();
        return buf;
    }

    /**
     * Returns PRISM encoding for robot module tactics (Sensing)
     * @return
     */
    public static String generateReconfTacticCommands(){
          String reconfGuard = "& ("+ROBOT_RECONF_VAR+"<"+ROBOT_MAX_RECONF_CONST+") ";
          String reconfUpdate = "& ("+ROBOT_RECONF_VAR+"'="+ROBOT_RECONF_VAR+"+1) ";
    	  String buf="";
    	  for (Map.Entry<String, Configuration> c: m_cp.getLegalTargetConfigurations().entrySet()){
//    		  buf+= "\t [t_set_"+c.getValue().getId()+"] ("+ROBOT_CONF_VAR+"!="+ROBOT_CONF_PREFIX+c.getValue().getId()+") "+reconfGuard +STOP_GUARD_STR+" "+ROBOT_GUARD_STR+" & (!robot_done) ->  ("+ROBOT_CONF_VAR+"'="+ROBOT_CONF_PREFIX+c.getValue().getId()+")"+ reconfUpdate +" & (robot_done'=true);\n";                	
    		  buf+= "\t [t_set_"+c.getValue().getId()+"] ("+ROBOT_CONF_VAR+"!="+c.getValue().getId()+") "+reconfGuard +STOP_GUARD_STR+" "+ROBOT_GUARD_STR+" & (!robot_done) ->  ("+ROBOT_CONF_VAR+"'="+c.getValue().getId()+")"+ reconfUpdate +" & (robot_done'=true);\n";                	

    	  }
          return buf+"\n";
    }

    /**
     * Returns PRISM encoding for robot module tactics (Charging)
     * @return
     */
    public static String generateChargingTacticCommands(){
        String guard_can_charge=" & (false";
        synchronized(m_map){
            for (Map.Entry<String, EnvMapNode> e: m_map.getNodes().entrySet()){
                if (e.getValue().isChargingStation()){
                    guard_can_charge +="|"+ROBOT_LOCATION_VAR+"="+e.getValue().getId();

                }	
            }
//            guard_can_charge+=") & ("+ROBOT_BATTERY_VAR+"<1500*"+String.valueOf(BatteryPredictor.m_battery_scaling_factor)+")";	//TODO: refine this constraint
            guard_can_charge+=") & ("+ROBOT_BATTERY_VAR+"<"+ROBOT_BATTERY_RANGE_MAX+")";	//TODO: refine this constraint
        }

        String buf="\t // Recharge tactics\n";
        buf+="\t [t_recharge] true "+ guard_can_charge +STOP_GUARD_STR+" "+ROBOT_GUARD_STR+" & (!robot_done) ->  ("+ROBOT_BATTERY_VAR+"'=b_upd_charge"+")"+" & (robot_done'=true);\n";                	
        return buf+"\n";
    }

    /**
     * @return String PRISM encoding for time rewards associated with an EnvMap
     * @param inhibitTactics boolean if true, it does not generate rewards associated with tactics
     */
    
    private static String getDeltaTime(Configuration config, double distance){
        return String.valueOf (Math.round (config.getSpeed()*distance));
    }
    
    public static String generateTimeReward(boolean inhibitTactics){
        synchronized (m_map) {
            String buf="rewards \"time\"\n";
            NumberFormat f = new DecimalFormat("#0.0000");

            
            // Robot recharging tactics
            if (!inhibitTactics){
 //               buf+="\t[t_set_half_speed] true : "+String.valueOf(DEFAULT_TIME_TACTIC_TIME)+";\n";
                buf+="\t[t_recharge] true : ("+ROBOT_BATTERY_RANGE_MAX+" - "+ROBOT_BATTERY_VAR+")/"+String.valueOf(BatteryPredictor.getChargingTimeRatio())+";\n";
            }
            
            // Robot movement tactics
            for (int i=0;i<m_map.getArcs().size();i++){
                EnvMapArc a = m_map.getArcs().get(i);
                if (a.isEnabled()) {
                    double t_distance = a.getDistance (); //  float(self.get_transition_attribute_value(t,"distance"))
                    HashMap<String,String> time_deltas = new HashMap<String, String>();
                    for (Map.Entry<String, Configuration> c: m_cp.getConfigurations().entrySet()){
                    	String confStr=m_cp.translateId(c.getKey());
                    	if (a.getTime(confStr)>0.0) // If we have timing information for the configuration in that arc
                    		time_deltas.put(c.getKey(), String.valueOf(a.getTime(confStr)));
                    	else // Otherwise, we just use the speed estimation formula
                    		time_deltas.put(c.getKey(),getDeltaTime(c.getValue(), t_distance));
                    }
                    
                    //String t_time_half_speed=f.format(SpeedPredictor.moveForwardTimeSimple(t_distance, ROBOT_HALF_SPEED_CONST));
                    //String t_time_full_speed=f.format(SpeedPredictor.moveForwardTimeSimple(t_distance, ROBOT_FULL_SPEED_CONST));
                    //String t_time_dr_speed=f.format(SpeedPredictor.moveForwardTimeSimple(t_distance, ROBOT_DR_SPEED_CONST));

                    
              // 
                     String action_name = a.getSource()+MOVE_CMD_STR+a.getTarget();
                //    String drs = ROBOT_VAR +" = "+ROBOT_LO_CONST+" ? "+ t_time_dr_speed + " + " + ROTATION_TIME_FORMULA_PREFIX+action_name + " : " ;
                //    buf+="\t["+action_name+"] true :" + drs + ROBOT_SPEED_VAR+"="+ROBOT_HALF_SPEED_CONST+"? "+t_time_half_speed+" + "+ROTATION_TIME_FORMULA_PREFIX+action_name+" : "+t_time_full_speed+" + "+ROTATION_TIME_FORMULA_PREFIX + action_name+";\n";
                     buf+="\t["+action_name+"] true :";
                     int counter=0;
                     for (Map.Entry<String, String> d: time_deltas.entrySet()){
                     	if (counter>0) buf += " : ";
                     	buf += ROBOT_CONF_VAR + "=" + d.getKey() + " ? "+d.getValue();
                     	counter++;
                     }
                     buf += ": 99999;\n";   
                
                }
            }
            
            // Robot reconfiguration tactics
            for (Map.Entry<String, Configuration> c: m_cp.getLegalTargetConfigurations().entrySet()){
      		  buf+= "\t [t_set_"+c.getValue().getId()+"]  true :";
      		  int counter=0;
      		  for (Map.Entry<String, Configuration> cs: m_cp.getLegalTargetConfigurations().entrySet()){
      			  if (counter>0) buf += " : ";
      			  buf+= ROBOT_CONF_VAR + "=" + cs.getKey() + " ? "+  m_cp.getReconfigurationTime(cs.getValue().getId(),c.getValue().getId());  
      			  counter++;
      		  }
      		  buf += ": 0;\n";  
      	  }
            
            buf+="endrewards\n\n";
            return buf;
        }
    }


    /**
     * @return PRISM encoding for all rotation formulas (for all arcs in map)
     */
    public static String generateRotationTimeFormulas(){
        String buf="// Rotation time formulas for map arcs\n";
        synchronized (m_map) {
            for (int i=0;i<m_map.getArcs().size();i++){
                EnvMapArc a = m_map.getArcs().get(i);
                if (a.isEnabled()) {
                    buf = buf+generateRotationTimeFormulaForArc(a);
                }
            }
        }
        return buf +"\n";
    }

    public static String generateRotationEnergyFormulas(){
        String buf="// Rotation time formulas for map arcs\n";
        synchronized (m_map) {
            for (int i=0;i<m_map.getArcs().size();i++){
                EnvMapArc a = m_map.getArcs().get(i);
                if (a.isEnabled()) {
                    buf = buf+generateRotationEnergyFormulaForArc(a);
                }
            }
        }
        return buf +"\n";
    }

    /**
     * Generates the PRISM encoding (as a formula) for all possible rotation times 
     * (for every heading in MissionState.Heading), given a map arc a
     * @param a Map arc
     * @return PRISM encoding for rotation times in arc a
     */
    public static String generateRotationTimeFormulaForArc(EnvMapArc a){
        NumberFormat f = new DecimalFormat ("#0.0000");
        String buf="formula "+ROTATION_TIME_FORMULA_PREFIX+a.getSource()+MOVE_CMD_STR+a.getTarget()+" = ";
        for (MissionState.Heading h : MissionState.Heading.values()) {
            buf += ROBOT_HEADING_VAR + "=" + HEADING_CONST_PREFIX + h.name() + " ? " + f.format (getRotationTime( MissionState.Heading.convertToRadians(h),a)) + " : ";
        }
        buf+=" 0;\n";
        return buf;
    }

    public static String generateRotationEnergyFormulaForArc(EnvMapArc a){
        NumberFormat f = new DecimalFormat ("#0");
        String buf="formula "+ROTATION_ENERGY_FORMULA_PREFIX+a.getSource()+MOVE_CMD_STR+a.getTarget()+" = ";
        for (MissionState.Heading h : MissionState.Heading.values()) {
//            buf += ROBOT_HEADING_VAR + "=" + HEADING_CONST_PREFIX + h.name() + " ? " + f.format (BatteryPredictor.batteryConsumption(ROBOT_HALF_SPEED_CONST, true, ROBOT_MED_KINECT, ROBOT_HI_CPU_VAL, getRotationTime( MissionState.Heading.convertToRadians(h),a))) + " : ";
          buf += ROBOT_HEADING_VAR + "=" + HEADING_CONST_PREFIX + h.name() + " ? " + f.format (BatteryPredictor.batteryConsumption(ROBOT_HALF_SPEED_CONST, true, getRotationTime( MissionState.Heading.convertToRadians(h),a))) + " : ";
        }
        buf+=" 0;\n";
        return buf;
    }


    /**
     * Returns the rotation time in seconds from a given robot orientation to the right angle before it
     * starts moving towards a given location (target of arc a)
     * @param robotOrientation orientation of the robot (angle in Radians)
     * @param a Map arc 
     * @return Rotation time in seconds
     */
    public static double getRotationTime(double robotOrientation, EnvMapArc a){    	
        double theta = Math.abs(robotOrientation-findArcOrientation(a));
        double min_theta = (theta > Math.PI) ? 2*Math.PI - theta : theta;
        return (min_theta/ROBOT_ROTATIONAL_SPEED_VALUE); 
    }


    /**
     * Determines the angle (in Radians) between two endpoints of an arc, taking the source node of the arc
     * as the reference of coordinates in the plane. Used to determine the part of the time reward structure 
     * associated with in-node robot rotations.
     * @param a Map arc
     * @return angle in radians between a.m_source and a.m_target
     */
    public static double findArcOrientation(EnvMapArc a){
        synchronized (m_map) {
            double nodeX = m_map.getNodeX(a.getSource());
            double nodeY = m_map.getNodeY(a.getSource());
            double nodeX2 = m_map.getNodeX(a.getTarget());
            double nodeY2 = m_map.getNodeY(a.getTarget());
            if (nodeX == Double.NEGATIVE_INFINITY || nodeX2 == Double.NEGATIVE_INFINITY) return 0;
            return findArcOrientation( nodeX, nodeY, nodeX2, nodeY2);
        }
    }


    public static double findArcOrientation(double src_x, double src_y, double tgt_x, double tgt_y){
        return Math.atan2( tgt_y - src_y, tgt_x - src_x);
    }


    /**
     * Determines the heading between two endpoints of an arc (snaps result of findArcOrientation to one of the predetermined headings)
     * @param a Map arc
     * @return heading enum arc heading
     */
    public static MissionState.Heading findArcHeading (EnvMapArc a) {
        return MissionState.Heading.convertFromRadians(findArcOrientation(a));
    }


    public static Stack<String> connectionPath = null; // Aux data structures for finding all paths between arbitrary locations
    public static List<Stack> connectionPaths = null;


    /**
     * Generates all non-cyclic paths between two locations in map
     * @param node1
     * @param node2
     */
    public static List<Stack> goFindAllPaths(String node1, String node2){
        connectionPath = new Stack<String>();
        connectionPath.push(node1);
        connectionPaths = new ArrayList<>();

        findAllPaths (node1, node2);
        for (int i=0; i<connectionPaths.size();i++) {
            connectionPaths.get(i).add(node2);
//          System.out.println(connectionPaths.get(i).toString());
        }
        return connectionPaths;
    }

    public static synchronized void findAllPaths(String src, String tgt) {
        for (String nextNode : m_map.getNeighbors(src)){
            //System.out.println("Generating paths "+src.toString());

            if (nextNode.equals(tgt)){
                Stack temp = new Stack<String>();
                for (String node1 : connectionPath) {
                    temp.add(node1);
                }
                connectionPaths.add(temp);
            } else if (!connectionPath.contains(nextNode)) {
                connectionPath.push(nextNode);
                findAllPaths(nextNode, tgt);
                connectionPath.pop();
            }
        }

    }

    /**
     * Translates a path into a PRISM module constraining the movements of the robot to that path
     * @param path List of strings with the locations of the path from source to target (e.g., ["ls", ..., "l1"])
     * @return String PRISM encoding of the path constraint module
     */
    public static String generatePathConstraintModule(List path){
        String buf="\n"+"module path_constraint\n";
        LinkedList<String> allowed = new LinkedList<String>();
        for (int i=0; i< path.size()-1; i++){
            allowed.add(path.get(i)+ MOVE_CMD_STR + path.get(i+1));
        }
        buf+= "// Allowed arcs: "+ String.valueOf(allowed) + "\n";
        synchronized(m_map) {
            for (EnvMapArc a : m_map.getArcs()){
                String str_arc= a.getSource() + MOVE_CMD_STR + a.getTarget();
                if (!allowed.contains(str_arc)) {
                    buf += "\t[" + str_arc + "] false -> true; \n";
                }
            }
        }
        buf += "endmodule\n";
        return buf;
    }

    /**
     * Translates a plan into a PRISM module constraining the action of the robots to that plan00
     * @param plan List of strings (e.g., ["l1_to_l2", ..., "t_recharge", ..., "l4_to_l5"])
     * @return String PRISM encoding of the plan constraint module
     */
    public static String generatePlanConstraintModule(List<String> plan){
        LinkedList<String> allTactics = new LinkedList<String>(Arrays.asList("t_set_loc_lo", "t_set_loc_med", "t_set_loc_hi", "t_set_half_speed", "t_set_full_speed", "t_recharge"));

        String buf="\n"+"module plan_constraint\n";
        buf+= "pc_s : [0.."+String.valueOf(plan.size())+"] init 0;\n";
        for (int i=0; i< plan.size(); i++){
            buf += "\t["+plan.get(i)+"] (pc_s="+String.valueOf(i)+") -> (pc_s'="+String.valueOf(i+1)+"); \n";
        }

        LinkedList<String> allowed = new LinkedList<String>();
        for (int i=0; i< plan.size(); i++){
            String action = plan.get(i);
            String[] e = action.split("_");
            if (!Objects.equal(MapTranslator.TACTIC_PREFIX, e[0])) {
                allowed.add(plan.get(i));
            } 
        }
        buf+="\t // Disallowed tactics\n";
        for (int i=0; i< allTactics.size(); i++){
            String action = allTactics.get(i);
            if (!plan.contains(action)) {
                buf += "\t[" + action + "] false -> true; \n";
            }
        }

        buf+= "\t // Allowed arcs: "+ String.valueOf(allowed) + "\n";
        buf+="\t // Disallowed arcs\n";
        synchronized(m_map) {
            for (EnvMapArc a : m_map.getArcs()){
                String str_arc= a.getSource() + MOVE_CMD_STR + a.getTarget();
                if (!allowed.contains(str_arc)) {
                    buf += "\t[" + str_arc + "] false -> true; \n";
                }
            }
        }

        buf += "endmodule\n";
        return buf;
    }


    /**
     * Returns shortest distance between two nodes computed using Dijkstra's Algorithm
     * @param node1 String label of source node (associated with an EnvMapNode in the map to translate)
     * @param node2 String label of target node (associated with an EnvMapNode in the map to translate)
     * @return float shortest distance between node1 and node2
     */
    public static double shortestPathDistance (String node1, String node2) {
        synchronized (m_map) {
            Graph graph = new Graph();
            Vertex[] vertices = new Vertex[m_map.getNodeCount()];

            Map<String, Integer> node_indexes = new HashMap<> ();

            int i=0;
            for (Map.Entry<String,EnvMapNode> entry : m_map.getNodes().entrySet() ){
                vertices[i] = new Vertex(entry.getKey());
                graph.addVertex(vertices[i], true);
                node_indexes.put (entry.getKey (), i);
                i++;
            }

            Edge[] edges = new Edge[m_map.getUniqueArcCount()];

            for (i=0;i<m_map.getArcs().size();i++){
                EnvMapArc a = m_map.getArcs().get(i);
                Vertex source = vertices[(node_indexes.get (a.getSource ()))];
                Vertex target = vertices[(node_indexes.get (a.getTarget ()))];
//                System.out.println("ARC: "+a.getSource()+" "+a.getTarget());
                if (a.isEnabled()){
                    edges[i] = new Edge(source, target, a.getDistance());
                } else {
                    edges[i] = new Edge(source, target, MAX_DISTANCE); // If edge is disabled, assign max possible distance                   	
                }
            }

            for(Edge e: edges){
                if (e.getWeight() < MAX_DISTANCE){
                    graph.addEdge(e.getOne(), e.getTwo(), e.getWeight());
                }
            }

            Dijkstra dijkstra = new Dijkstra(graph, node1);
            return (dijkstra.getDistanceTo(node2));
        }
    }


    /**
     * @return String PRISM encoding for distance rewards between nodes in the map
     */
    public static String generateDistanceReward(){
        synchronized (m_map) {
            NumberFormat f = new DecimalFormat ("#0.0000");
            String buf="rewards \"distance\"\n";

            for (Map.Entry<String,EnvMapNode> entry : m_map.getNodes().entrySet() ){
                String v = entry.getKey();

                buf+="\t"+STOP_PRED+" & "+TARGET_ROBOT_LOCATION_CONST+"="+v+" : ";

                for (Map.Entry<String,EnvMapNode> entry2 : m_map.getNodes().entrySet() ){
                    String v2 = entry2.getKey();
                    if (!v2.equals(v)){
                        buf += ROBOT_LOCATION_VAR + "=" + v2 + " ? " + f.format (shortestPathDistance (v, v2)) + " : ";
                    }
                }
                buf+=" 0;\n";

            }
            buf+="endrewards\n\n";
            return buf;		
        }
    }


    /**
     * @return String PRISM encoding for energy rewards at the end of the execution
     */
    public static String generateEnergyReward(){
        synchronized (m_map) {
            String buf="rewards \"energy\"\n";
            buf+="\t"+STOP_PRED+" : "+ROBOT_BATTERY_VAR+";\n";
            buf+="endrewards\n\n";
            return buf;		
        }
    }
    
    
    /**
     * Generates the PRISM specification for an adaptation scenario, based on a given EnvMap
     * @param inhibitTactics boolean if true, it generates a specification only with move actions, disabling the rest of actions and tactics
     * @return String PRISM encoding for adaptation scenario
     */

    public static String getMapTranslation(){
        return getMapTranslation(false);
    }


    public static String getMapTranslation(boolean inhibitTactics){
        String buf="// Generated by BRASS MARS Robot Map PRISM Translator "+VERSION_STR+".\n\n";
        buf+=generateGameStructure()+"\n";
        buf+=generateHeadingConstants()+"\n";
        buf+=generateConfigurationConstants()+"\n";
        buf+=generateLocationConstants()+"\n";
        buf+=generateEnvironmentModule()+"\n";
        buf+=generateRobotModule(inhibitTactics)+"\n";
        buf+=generateTimeReward(inhibitTactics)+"\n";
        buf+=generateRotationTimeFormulas()+"\n";
        buf+=generateRotationEnergyFormulas()+"\n";
//        buf+=generateDistanceReward()+"\n";    // Used in Phase 1 only
        buf+=generateEnergyReward()+"\n";
        buf+="// --- End of generated code ---\n";
        return buf;
    }

    /**
     * Generates the PRISM specification for an adaptation scenario, constrained to a specific path of robot movements
     * @param path List of strings containing the sequence of locations in the path, e.g., ["l1", ..., "l8"]
     * @return String PRISM encoding for constrained adaptation scenario
     */

    public static String getConstrainedToPathMapTranslation(List<String> path){
        return getConstrainedToPathMapTranslation(path, false);
    }

    public static String getConstrainedToPathMapTranslation(List<String> path, boolean inhibitTactics){
        return getMapTranslation(inhibitTactics) +"\n\n"+ generatePathConstraintModule(path);
    }

    /**
     * Generates the PRISM specification for an adaptation scenario, constrained to a specific plan
     * @param path List of strings containing the sequence of actions for the plan (e.g., ["l1_to_l2", ..., "t_recharge", ..., "l4_to_l5"])
     * @return String PRISM encoding for constrained adaptation scenario
     */
    public static String getConstrainedToPlanMapTranslation(List<String> plan){
        return getMapTranslation() +"\n\n"+ generatePlanConstraintModule(plan);
    }

    /**
     * Generates and exports the PRISM specification for an adaptation scenario to a text file
     * @param f String filename to export PRISM specification (constrained to a path)
     * @param path List of strings containing the sequence of locations in the path, e.g., ["l1", ..., "l8"]
     */


    public static void exportMapTranslation(String f, List<String> path) {
        exportMapTranslation (f, path, false);
    }

    public static void exportMapTranslation(String f, List<String> path, boolean inhibitTactics) {
        exportTranslation(f, getConstrainedToPathMapTranslation(path, inhibitTactics));
    }

    /**
     * Generates and exports the PRISM specification for an adaptation scenario to a text file
     * @param f String filename to export PRISM specification (constrained to a plan)
     * @param plan String list with action sequence (e.g., ["l1_to_l2", ..., "t_recharge", ..., "l4_to_l5"])
     */
    public static void exportConstrainedToPlanMapTranslation(String f, List<String> plan) {
        exportTranslation(f, getConstrainedToPlanMapTranslation(plan));
    }

    /**
     * Generates and exports the PRISM specification for an adaptation scenario to a text file
     * @param f String filename to export PRISM specification
     */
    public static void exportMapTranslation(String f){
        exportTranslation(f, getMapTranslation());
    }

    public static void exportMapTranslation(String f, boolean inhibitTactics){
        exportTranslation(f, getMapTranslation(inhibitTactics));
    }


    /**
     * Exports a piece of code to a text file
     * @param f String filename
     * @param code String code to be exported
     */
    public static void exportTranslation(String f, String code){
        try {
            BufferedWriter out = new BufferedWriter (new FileWriter(f));
            out.write(code);
            out.close();
        }
        catch (IOException e){
            System.out.println("Error exporting PRISM map translation");
        }
    }


    /**
     * Generates PRISM encoding variants constrained by all non-cyclic paths between two locations
     * @param f_base String base for PRISM models filenames (e.g., target folder)
     * @param source String label of source location
     * @param target String label of target location
     * @return
     */
    public static Map<List, String> exportConstrainedTranslationsBetween(String f_base, String source, String target) {
        return exportConstrainedTranslationsBetween(f_base, source, target, false);   
    }

    public static Map<List, String> exportSingleTranslationBetween(String f_base, String source, String target, boolean inhibitTactics) {
    	 List<Stack> paths = goFindAllPaths(source, target);
         Map<List, String> specifications = new HashMap<List, String>();
         int c=0;
         String filename = f_base + "/" + String.valueOf (c);
         exportMapTranslation (filename, inhibitTactics);
         System.out.println("Exported map translation "+String.valueOf(c));
         specifications.put(paths.get(0), filename);
         return specifications;
    }

    
    
    public static Map<List, String> exportConstrainedTranslationsBetween(String f_base, String source, String target, boolean inhibitTactics) {
        List<Stack> paths = goFindAllPaths(source, target);
        Map<List, String> specifications = new HashMap<List, String>();
        int c=0;
        for ( List path : paths )  {
            String filename = f_base + "/" + String.valueOf (c);
            exportMapTranslation (filename, path, inhibitTactics);
            System.out.println("Exported map translation "+String.valueOf(c));
            specifications.put(path, filename);
            c++;
        }
        return specifications;
    }

    
    public static Map<List, String> exportConstrainedTranslationsBetweenCutOff(String f_base, String source, String target, boolean inhibitTactics) {
        List<Stack> paths = goFindAllPaths(source, target);
        int cutoff=5;
        ArrayList<EnvMapPath> map_paths = new ArrayList<EnvMapPath>();
        for (int i=0; i<paths.size();i++){
        	map_paths.add(new EnvMapPath(paths.get(i), m_map));
        }
        
        Collections.sort(map_paths);
        
        Map<List, String> specifications = new HashMap<List, String>();
        int c=0;
        for ( EnvMapPath path : map_paths )  {
            String filename = f_base + "/" + String.valueOf (c);
            exportMapTranslation (filename, path.getPath(), inhibitTactics);
            System.out.println("Exported map translation "+String.valueOf(c));
            specifications.put(path.getPath(), filename);
            System.out.println("Candidate Path distance : "+String.valueOf(path.getDistance())+ " "+String.valueOf(path.getPath()));
            c++;
            if (c==cutoff){
            	break;
            }
        }
        return specifications;
    }
    
    
    /**
     * Class test
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        
    	
    	ConfigurationSynthesizer cs = new ConfigurationSynthesizer();
    	cs.populate();
    	EnvMap dummyMap = new EnvMap (null, null);
        //dummyMap.insertNode("newnode", "l1", "l2", 17.0, 69.0);
        setMap(dummyMap);
        setConfigurationProvider(cs);
        System.out.println(getMapTranslation()); // Class test
        //System.out.println();
        exportMapTranslation("/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/prismtmp-simple.prism", false);
        // String export_path="/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/";

        // Map<List, String> specifications = exportConstrainedTranslationsBetween (export_path, "ls", "l1");
        // System.out.println(String.valueOf(specifications));

    }
}
