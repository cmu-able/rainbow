package org.sa.rainbow.brass.adaptation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Objects;

import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapNode;
import org.sa.rainbow.brass.model.map.MapTranslator;



/**
 * @author jcamara
 *
 */

public class PolicyToIG {
    public PrismPolicy m_prismPolicy;
    public EnvMap m_map;
    public float m_current_speed = MapTranslator.ROBOT_HALF_SPEED_VALUE;
    public String m_current_loc_mode = MapTranslator.ROBOT_LOC_MODE_HI_CONST;
    public boolean m_insert_additional_command = false;  // When translation of commands is not 1-to-1 from tactics, additional command insertion flag for translation
    public String m_command_insert = "";				 // (see build_cmd_tactic)
    public double m_location_x;
    public double m_location_y;
    public double m_theta;

    public static final long NO_DEADLINE=-1;
    public static final String SPECIAL_CMD_PREFIX = "scmd"; // Used to distinguish special command set deadline in translate method

    public PolicyToIG(PrismPolicy policy, EnvMap map) {
        m_prismPolicy = policy;
        m_map = map;
    }


    /**
     * Generates a movement instruction for the IG
     * @param cmdId int id for the instruction (IG Vertex)
     * @param tgt_x double x coordinate for target location
     * @param tgt_y double y coordinate for target location
     * @param speed double linear speed
     * @param theta orientation angle (in radians) that the robot should face after moving
     * @return
     */
    private String build_cmd_move(int cmdId, double tgt_x, double tgt_y, double speed, double theta) {
        NumberFormat f = new DecimalFormat("#0.0000");
        NumberFormat f2 = new DecimalFormat("#0.00");
        String cmd="";

        if (!Objects.equals(m_current_loc_mode, MapTranslator.ROBOT_LOC_MODE_LO_CONST)){
            cmd = "MoveAbsH(" + f2.format(tgt_x) + ", " + f2.format(tgt_y) + ", " + f2.format(speed) + ", " + f.format(theta) + ")";
        } else { // Dead reckoning
            synchronized(m_map){
                cmd = "Forward(" + f2.format (m_map.distanceBetweenCoords (m_location_x, m_location_y, tgt_x, tgt_y))
                + ", " + /*f2.format(MapTranslator.ROBOT_DR_SPEED_VALUE)*/speed + ")";
            }
        }

        return build_cmd(cmdId, cmd);
    }

    private String build_cmd_deadline (int cmdId, long deadline){
        String cmd = "Deadline(" + String.valueOf (deadline) + ")";
        return build_cmd(cmdId, cmd);
    }

    private String build_cmd_recalibrate (int cmdId, boolean recalibrate) {
        String cmd = "Recalibrate(" + (recalibrate ? 1 : 0) + ")";
        return build_cmd (cmdId, cmd);
    }

    /**
     * Generates a tactic instruction for the IG
     * @param cmdId int id for the instruction (IG Vertex)
     * @param name String literal containing the tactic's identifier
     * @return
     */
    private String build_cmd_tactic(int cmdId, String name) {
        NumberFormat f = new DecimalFormat("#0.00");
        NumberFormat f2 = new DecimalFormat("#0.0000");
        String cmd = "";

        if (Objects.equals(name, "t_recalibrate_light")){
            cmd = "Recalibrate(0)";
        }
        if (Objects.equals(name, "t_recalibrate")){
            cmd = "Recalibrate(1)";
        }
        if (Objects.equals(name, "t_set_loc_lo")){
            cmd = "SetLocalizationFidelity" + "("+MapTranslator.ROBOT_LOC_MODE_LO_VAL+")";
            m_current_loc_mode = MapTranslator.ROBOT_LOC_MODE_LO_CONST;
        }
        if (Objects.equals(name, "t_set_loc_med")){
            cmd = "SetLocalizationFidelity" + "("+MapTranslator.ROBOT_LOC_MODE_MED_VAL+")";
            if (Objects.equals(m_current_loc_mode, MapTranslator.ROBOT_LOC_MODE_LO_CONST)){
                cmd = "Locate("+f.format(m_location_x)+", "+f.format(m_location_y)+", "+f2.format(m_theta)+")";
                m_insert_additional_command = true;
                m_command_insert = "SetLocalizationFidelity" + "("+MapTranslator.ROBOT_LOC_MODE_MED_VAL+")";
            }
            m_current_loc_mode = MapTranslator.ROBOT_LOC_MODE_MED_CONST;
        }
        if (Objects.equals(name, "t_set_loc_hi")){
            cmd = "SetLocalizationFidelity" + "("+MapTranslator.ROBOT_LOC_MODE_HI_VAL+")";
            if (Objects.equals(m_current_loc_mode, MapTranslator.ROBOT_LOC_MODE_LO_CONST)){
                cmd = "Locate("+f.format(m_location_x)+", "+f.format(m_location_y)+", "+f2.format(m_theta)+")";
                m_insert_additional_command = true;
                m_command_insert = "SetLocalizationFidelity" + "("+MapTranslator.ROBOT_LOC_MODE_HI_VAL+")";
            }
            m_current_loc_mode = MapTranslator.ROBOT_LOC_MODE_HI_CONST;
        }
        if (Objects.equals(name, "t_recharge")){
            cmd = "Charge" + "("+f.format(MapTranslator.ROBOT_CHARGING_TIME)+")";
        }
        if (Objects.equals(name, "t_set_half_speed")){
            m_current_speed = MapTranslator.ROBOT_HALF_SPEED_VALUE;
            return ""; // Just set speed parameter, not explicit command in IG
        }
        if (Objects.equals(name, "t_set_full_speed")){
            m_current_speed = MapTranslator.ROBOT_FULL_SPEED_VALUE;
            return ""; // Just set speed parameter, not explicit command in IG
        }
        return build_cmd(cmdId, cmd);
    }


    /**
     * Coats instructions with additional syntactic sugar
     * @param cmdId
     * @param commandLiteral
     * @return
     */
    private String build_cmd (int cmdId, String commandLiteral) {
        String cmd = "V(" + cmdId + ", do " + commandLiteral + " then " + ++cmdId + ")";
        return cmd;
    }



    /**
     * Builds the instruction graph
     * @param cmds ArrayList<String> plan actions
     * @return
     */    
    private String build_ig(ArrayList<String> cmds) {
        String ins_graph = "P(";
        int i = 0;
        for (i = 0; i < cmds.size (); i++) {
            if (i == 0) {
                ins_graph += cmds.get (i) + ",\n";
            }
            else {
                ins_graph += cmds.get (i) + "::\n";
            }
        }
        // add the end
        ins_graph += "V(" + (i+1) + ", end)::\nnil)";

        return ins_graph;
    }


    /**
     * Finds the orientation of the next movement in plan (to indicate orientation of robot at the end of current MoveAbsH movement)
     * @param plan
     * @param index
     * @return
     */
    public double findNextOrientation(ArrayList<String> plan, int index){
        double theta = 0;

        if (index+1>= plan.size())
            return theta;

        synchronized(m_map){
            String[] e = plan.get(index).split("_"); // Break current move action plan name into chunks
            Double src_x = m_map.getNodeX(e[2]);
            Double src_y = m_map.getNodeY(e[2]);

            for (int i = index+1; i < plan.size(); i++) {
                String action = plan.get(i);
                String[] e2 = action.split("_"); // Break action plan name into chunks
                if (!Objects.equals (e2[0], MapTranslator.TACTIC_PREFIX)
                        && !Objects.equals (e2[0], SPECIAL_CMD_PREFIX)) { // If action is *not* a tactic (i.e., move command)
                    Double tgt_x = m_map.getNodeX(e2[2]);
                    Double tgt_y = m_map.getNodeY(e2[2]);
                    theta =  MapTranslator.findArcOrientation(src_x, src_y, tgt_x, tgt_y);
                    return(theta);

                }
            }
        }
        return theta;
    }


    /**
     * Expands the list of actions in the plan to IG instructions
     * @return
     */

    public String translate(){
        return (translate (NO_DEADLINE, null));
    }

    public String translate (long deadline, Boolean recalibrate) {
        ArrayList<String> plan = m_prismPolicy.getPlan();
        ArrayList<String> cmds = new ArrayList<String>();
        String cmd="";

        if (deadline!=NO_DEADLINE) {
            plan.add(0,SPECIAL_CMD_PREFIX+"_setdeadline_"+String.valueOf(deadline));
        }
        if (recalibrate != null) {
            plan.add (0, SPECIAL_CMD_PREFIX + "_recalibrate_" + String.valueOf (recalibrate));
        }

//  For testing purposes
//        plan.add("t_set_loc_lo");
//        plan.add("l1_to_l2");
//        plan.add("t_set_loc_hi");
//        plan.add("l2_to_c1");
//
//        System.out.println(String.valueOf(plan));

        int cmd_id = 1;

        for (int i = 0; i < plan.size(); i++) {
            String action = plan.get(i);

            String[] elements = action.split("_"); // Break action plan name into chunks
            if (Objects.equals(elements[0], SPECIAL_CMD_PREFIX)){
                if (elements[1].equals ("setdeadline")) {
                    cmd = build_cmd_deadline (cmd_id, Long.parseLong (elements[2]));
                }
                else if (elements[1].equals ("recalibrate")) {
                    cmd = build_cmd_recalibrate(cmd_id, Boolean.parseBoolean (elements[2]));
                }
            } else if (Objects.equals(elements[0], MapTranslator.TACTIC_PREFIX)) { // If action is a tactic
                cmd = build_cmd_tactic (cmd_id, action );
            } else { // Other actions (robot movement for the time being)
                synchronized (m_map){	
                    String destination = elements[2];
                    String origin = elements[0];
                    // cmd = build_cmd_move(cmd_id, m_map.getNodeX(destination), m_map.getNodeY(destination), m_current_speed);
                    cmd = build_cmd_move (cmd_id, m_map.getNodeX(destination), m_map.getNodeY(destination), m_current_speed, findNextOrientation(plan,i));
                    m_theta = MapTranslator.findArcOrientation(m_map.getNodeX(origin), m_map.getNodeY(origin), m_map.getNodeX(destination), m_map.getNodeY(destination));
                    m_location_x =  m_map.getNodeX(destination);
                    m_location_y =  m_map.getNodeY(destination); 
                }
            }
            if (!Objects.equals(cmd, "")) {
                cmds.add(cmd);
                ++cmd_id;		
                if (m_insert_additional_command){
                    cmds.add(build_cmd(cmd_id, m_command_insert));
                    ++cmd_id;
                    m_insert_additional_command = false;
                    m_command_insert = "";
                }
            }
        }
        String ins_graph = build_ig(cmds);
        //System.out.println(ins_graph);
        return ins_graph;
    }


    /**
     * Generates a JSON description of a waypoint list
     * @param p
     * @param r
     * @return
     */
    public static String generateJSONWayPointList(PrismPolicy p, String r) {
        String out="{\"path\": [";
        for (int i=0; i<p.getPlan().size(); i++){
            String[] e = p.getPlan().get(i).split("_");
            if (i==0){
                out = out + "\""+e[0]+"\"";
            }
            out = out + ",\""+e[2]+"\"";
        }
        out = out +"], \"time\": "+r;
        out = out +"}";		
        return out;
    }


    /**
     * Exports IG translation to a file
     * @param f String filename
     * @param s String code for the IG
     */
    public static void exportIGTranslation(String f, String s){
        try {
            BufferedWriter out = new BufferedWriter (new FileWriter(f));
            out.write(s);
            out.close();
        }
        catch (IOException e){
            System.out.println("Error exporting Instruction Graph translation");
        }
    }



    /**
     * Class test
     * @param args
     * @throws Exception
     */
    public static void main (String[] args) throws Exception { // Class test
        EnvMap map = new EnvMap (null, null);
        PrismConnector conn = new PrismConnector (null);
        String out_dir_ig="/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/igs/";
        String out_dir_wp="/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/paths/";
        for (EnvMapNode node_src : map.getNodes().values()) {
            for (EnvMapNode node_tgt : map.getNodes().values()) {
                if (node_src.getId()!=node_tgt.getId()){
                    System.out.println("Src:"+String.valueOf(node_src.getId())+" Tgt:"+String.valueOf(node_tgt.getId()));
                    conn.invoke (node_src.getId(),node_tgt.getId()); 
                    String prismResult = conn.getResult();
                    Long ttc = new Double (Double.parseDouble(prismResult))
                            .longValue();
                    PrismPolicy prismPolicy = new PrismPolicy("/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/botpolicy.adv");
                    prismPolicy.readPolicy();
                    //System.out.println(prismPolicy.getPlan().toString());
                    PolicyToIG translator = new PolicyToIG(prismPolicy, map);
                    // System.out.println(translator.translate());
                    exportIGTranslation(out_dir_ig+node_src.getLabel()+"_to_"+node_tgt.getLabel()+".ig",translator.translate());
                    String w =generateJSONWayPointList(prismPolicy, String.valueOf(ttc));
                    System.out.println(w);
                    exportIGTranslation(out_dir_wp+node_src.getLabel()+"_to_"+node_tgt.getLabel()+".json",w);  

                }
            }
        }
    }  
}
