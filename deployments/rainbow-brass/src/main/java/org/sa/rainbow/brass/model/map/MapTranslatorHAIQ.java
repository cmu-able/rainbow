package org.sa.rainbow.brass.model.map;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Map;
import java.util.Properties;

import org.sa.rainbow.brass.PropertiesConnector;
import org.sa.rainbow.brass.confsynthesis.Configuration;
import org.sa.rainbow.brass.confsynthesis.ConfigurationProvider;
import org.sa.rainbow.brass.confsynthesis.ConfigurationSynthesizer;
import org.sa.rainbow.brass.confsynthesis.DetailedConfigurationBatteryModel;
import org.sa.rainbow.brass.confsynthesis.PropertiesConfigurationSynthesizer;

/**
 * @author javiercamaramoreno
 *
 */
public class MapTranslatorHAIQ {
	 
	   private static EnvMap m_map;
	   private static ConfigurationProvider m_cp;
	   public static DetailedConfigurationBatteryModel m_battery_model;

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

	    public static String getArcsCode(String startLocationId, String targetLocationId) {
	    	String res ="";
	    	 synchronized(m_map) {
	             for (EnvMapArc a : m_map.getArcs()){
	            	 String sigString = startLocationId.equals(a.getSource()) ? "ss" : "s";
	            	 sigString = targetLocationId.equals(a.getTarget()) ? "es" : sigString;
	                 res+= "lone sig "+ a.getSource() + a.getTarget() 
	                 + " extends "+sigString+"{}\n</\n"+getFormulaCode(a,FormulaType.HITRATE)
	                 + "\n"+getFormulaCode(a,FormulaType.ENERGY)
	                 + "\n"+getFormulaCode(a,FormulaType.TIME)+
	                 "\n/>\n\n";
	             }
	         }
	    	 return res;
	    }
	    
	    public static String getArcConstraintsCode() {
	    	String res= "fact {\n";
	    	 synchronized(m_map) {
	             for (EnvMapArc a : m_map.getArcs()){
	            	 res += "\tdisj["+a.getSource()+a.m_target+".c,s-(";
	            	 boolean first=true;
	            	 for (EnvMapArc b: m_map.getArcs()) {
	            		if (b.getSource().equals(a.getSource()) || 
	            			b.getSource().equals(a.getTarget()) ||
	            			b.getTarget().equals(a.getSource()) ||
	            			b.getTarget().equals(a.getTarget()) ) {
	            				res+=(first?"":"+")+b.getSource()+b.getTarget();
	            				first=false;
	            		}
	            	 }
	            	 res+=")]\n";
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
	    	String res = "\tformula "+(hr? "hitrate":(eg? "energy": "time"))+" = ";
	    	for (int i=0;i<(hr?cids.length:cidse.length); i++) {
	    		String formulaString = hr? ""+a.getHitRate(cids[i]) :( eg? getEnergy("sol_"+i,a) : getTime("sol_"+i,a) ) ;//getEnergy(cidse[i],a);
	    		res += "(" + (hr?cids[i].replace("-", "_"):cidse[i].replace("-","_")) + "?" + formulaString + (i<(hr?cids.length:cidse.length)-1?":":(hr?":0.0":":99999"));
	    	}
		    for (int i=0;i<(hr?cids.length:cidse.length); i++) res += ")";
	    	return res+=";\n";
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
