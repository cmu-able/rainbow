package org.sa.rainbow.brass.model.map;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Map;
import java.util.Properties;
import java.util.LinkedList;

import org.sa.rainbow.brass.PropertiesConnector;
import org.sa.rainbow.brass.confsynthesis.Configuration;
import org.sa.rainbow.brass.confsynthesis.ConfigurationProvider;
import org.sa.rainbow.brass.confsynthesis.ConfigurationSynthesizer;
import org.sa.rainbow.brass.confsynthesis.DetailedConfigurationBatteryModel;
import org.sa.rainbow.brass.confsynthesis.PropertiesConfigurationSynthesizer;
import org.sa.rainbow.brass.model.mission.MissionState;
import org.sa.rainbow.brass.plan.p2.BatteryPredictor;
import org.sa.rainbow.brass.plan.p2.MapTranslator;

/**
 * @author javiercamaramoreno
 *
 */
public class MapTranslatorHAIQ {
	 
	   private static EnvMap m_map;
	   private static ConfigurationProvider m_cp;
	   public static DetailedConfigurationBatteryModel m_battery_model;
	   public static Double ROBOT_BATTERY_RANGE_MAX = 180000.0;
	   public static Double AVG_ROT_ENERGY = 1750.0;
	   public static Double AVG_ROT_TIME = 1.0;
	   private static LinkedList<EnvMapArc> m_added = new LinkedList<EnvMapArc>();
   	

	    /**
	     * Sets the map to translate into a HAIQ specification
	     * @param map an EnvMap encoding the graph that captures the physical space
	     */
	    public static void setMap(EnvMap map){
	        m_map = map;
	    }
	   
		/**
	     * Sets the Configuration provider
	     */
	    public static void setConfigurationProvider(ConfigurationProvider cp){
	        m_cp = cp;
	    }
	    
	    
	    /**
	     * Generates HAIQ model structure
	     */
	    public static String getJHAIQTranslation(String startLocationId, String targetLocationId){
	        Properties props = PropertiesConnector.DEFAULT;
	    	String res="";
	        res += readFileLines(props.getProperty(PropertiesConnector.CP3_HAIQ_TEMPLATE_PROPKEY))+"\n";
	        res += getArcsCode(startLocationId, targetLocationId);
	        res += getArcConstraintsCode();
	    	return res+"\n";
	    }

	    public static boolean existsEquiv (LinkedList<EnvMapArc> l, String nid1, String nid2) {
	    	boolean res = false;
	    	for (int i=0; i<l.size(); i++) {
	    		EnvMapArc a = l.get(i);
	    		if (a.isArcBetween(nid2, nid1) || a.isArcBetween(nid1, nid2))
	    			return true;
	    	}
	    	return res;	    	
	    }
	    
	    public static boolean exists (EnvMapArc arc) {
	    	boolean res = false;
	    	for (int i=0; i<m_added.size(); i++) {
	    		EnvMapArc a = m_added.get(i);
	    		if (a.isArcBetween(arc.getSource(), arc.getTarget()))
	    			return true;
	    	}
	    	return res;	    	
	    }
	    
	    public static String getArcsCode(String startLocationId, String targetLocationId) {
	    	String res ="";
	    	m_added.clear();
	    	 synchronized(m_map) {
	             for (EnvMapArc a : m_map.getArcs()){
	            	 if (!existsEquiv(m_added, a.getSource(), a.getTarget())) {
		            	 String sigString = startLocationId.equals(a.getSource()) ? "ss" : "s";
		            	 sigString = targetLocationId.equals(a.getTarget()) ? "es" : sigString;
		                 res+= "lone sig "+ a.getSource() + a.getTarget() 
		                 + " extends "+sigString+"{}\n</\n"+getFormulaCode(a,FormulaType.HITRATE)
		                 + "\n"+getFormulaCode(a,FormulaType.ENERGY)
		                 + "\n"+getFormulaCode(a,FormulaType.TIME)+
	//	                 + "\n"+generateRotationTimeFormulaForArc(a)
	//	                 + "\n"+generateRotationEnergyFormulaForArc(a)+
		                 "\n/>\n\n";
		                 m_added.add(a);
	            	 }
	             }
	         }
	    	 return res;
	    }
	    
	    public static String getArcConstraintsCode() {
	    	String res= "fact {\n";
	    	 synchronized(m_map) {
	             for (EnvMapArc a : m_map.getArcs()){
	            	 if (exists(a)) {
		            	 res += "\tdisj["+a.getSource()+a.m_target+".c,s-(";
		            	 boolean first=true;
		            	 for (EnvMapArc b: m_map.getArcs()) {
		            		if (b.getSource().equals(a.getSource()) || 
		            			b.getSource().equals(a.getTarget()) ||
		            			b.getTarget().equals(a.getSource()) ||
		            			b.getTarget().equals(a.getTarget()) ) {
		            			if (exists(b)) {
		            				res+=(first?"":"+")+b.getSource()+b.getTarget();
		            				first=false;
		            			}
		            		}
		            	 }
		            	 res+=")]\n";
	            	 }
	             } 	 
	            	 
	         }
	    	return res+"}\n";
	    }
	    
	    private static String getDeltaEnergy(String configId, double distance){
	    	Configuration config = m_cp.getConfigurations().get(configId);
	    	return String.valueOf (Math.round (config.getSpeed()*distance*config.getEnergyDischargeRate()));
	    }

	    private static String getTime(String configId, EnvMapArc a){
	    	Configuration config = m_cp.getConfigurations().get(configId);
	    	return String.valueOf (Math.round (config.getSpeed()*a.getDistance()));
	    }
	    
	    private static String getEnergy(String configId, EnvMapArc a) {
	    	return getDeltaEnergy (configId,a.getDistance());
	    }
	    
	    private enum FormulaType {HITRATE, ENERGY, TIME};
	    public static String getFormulaCode (EnvMapArc a, FormulaType t) {
	    	boolean hr = t.equals(FormulaType.HITRATE);
	    	boolean eg = t.equals(FormulaType.ENERGY);
	    	String[] cids = {"amcl-kinect", "amcl-lidar", "mrpt-kinect", "mrpt-lidar", "aruco"};
	    	String[] cidse= {"amcl-kinect-35", "aruco-camera-headlamp-24", "amcl-kinect-68",
	    			"mrpt-lidar-35", "aruco-camera-35", "aruco-camera-headlamp-35", "amcl-lidar-35",
	    			"mrpt-kinect-35", "aruco-camera-24", "aruco-camera-headlamp-68", "mrpt-lidar-68",
	    			"amcl-lidar-68", "aruco-camera-68", "mrpt-kinect-68", "mrpt-kinect-24",
	    			"mrpt-lidar-24", "amcl-lidar-24", "amcl-kinect-24"};
	    	String res = "\tformula "+(hr? "hitrate":(eg? "energy": "time"))+" = "+(hr?"" : (eg? AVG_ROT_ENERGY : AVG_ROT_TIME)+"+");
	    	for (int i=0;i<(hr?cids.length:cidse.length); i++) {
	    		String formulaString = hr? ""+a.getHitRate(cids[i]) :( eg? getEnergy("sol_"+i,a) : getTime("sol_"+i,a) ) ;//getEnergy(cidse[i],a);
	    		res += "(" + (hr?cids[i].replace("-", "_"):cidse[i].replace("-","_")) + "?" + formulaString + (i<(hr?cids.length:cidse.length)-1?":":(hr?":0.0":":99999"));
	    	}
		    for (int i=0;i<(hr?cids.length:cidse.length); i++) res += ")";
	    	return res+=";\n";
	    }
	    
	    
	    /**
	     * Generates the PRISM encoding (as a formula) for all possible rotation times 
	     * (for every heading in MissionState.Heading), given a map arc a
	     * @param a Map arc
	     * @return PRISM encoding for rotation times in arc a
	     */
	    public static String generateRotationTimeFormulaForArc(EnvMapArc a){
	        NumberFormat f = new DecimalFormat ("#0.0000");
	        String buf="formula "+MapTranslator.ROTATION_TIME_FORMULA_PREFIX+a.getSource()+MapTranslator.MOVE_CMD_STR+a.getTarget()+" = ";
	        for (MissionState.Heading h : MissionState.Heading.values()) {
	            buf += MapTranslator.ROBOT_HEADING_VAR + "=" + MapTranslator.HEADING_CONST_PREFIX + h.name() + " ? " + f.format (getRotationTime( MissionState.Heading.convertToRadians(h),a)) + " : ";
	        }
	        buf+=" 0;\n";
	        return buf;
	    }

	    public static String generateRotationEnergyFormulaForArc(EnvMapArc a){
	        NumberFormat f = new DecimalFormat ("#0");
	        String buf="formula "+MapTranslator.ROTATION_ENERGY_FORMULA_PREFIX+a.getSource()+MapTranslator.MOVE_CMD_STR+a.getTarget()+" = ";
	        for (MissionState.Heading h : MissionState.Heading.values()) {
	          buf += MapTranslator.ROBOT_HEADING_VAR + "=" + MapTranslator.HEADING_CONST_PREFIX + h.name() + " ? " + f.format (BatteryPredictor.batteryConsumption(MapTranslator.ROBOT_HALF_SPEED_CONST, true, getRotationTime( MissionState.Heading.convertToRadians(h),a))) + " : ";
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
	        return (min_theta/MapTranslator.ROBOT_ROTATIONAL_SPEED_VALUE); 
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
	     * Imports a text file into a String Array
	     * @param f String filename
	     */

		public static String readFileLines(String f){		
		    String res = ""; 
			try (BufferedReader br = new BufferedReader(new FileReader(f)))
		     {
		         String sCurrentLine;
		         while ((sCurrentLine = br.readLine()) != null) {
		             res += sCurrentLine+"\n";
		         }	
		     } catch (IOException e) {
		         System.out.println("There was a problem reading from file "+f);
		         System.exit(-1);
		     } 
		     return res;
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
	            System.out.println("Error exporting PRISM map translation:\n" + e);
	        }
	    }

	    
	    public static void main(String[] args) throws Exception {
	    	 
	    	 ConfigurationSynthesizer cs = new ConfigurationSynthesizer();
			 m_battery_model = new DetailedConfigurationBatteryModel(PropertiesConfigurationSynthesizer.DEFAULT);
			 cs.generateConfigurations();
			 cs.populate();
	    	 EnvMap dummyMap = new EnvMap (null, null); 
	         setMap(dummyMap);
	         setConfigurationProvider(cs);
	         Properties props = PropertiesConnector.DEFAULT;
	         exportTranslation(props.getProperty(PropertiesConnector.CP3_HAIQ_MODEL_PROPKEY), getJHAIQTranslation("l39","l55"));

	    }
}
