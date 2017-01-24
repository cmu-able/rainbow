package org.sa.rainbow.brass.adaptation;

import java.util.ArrayList;
import java.util.Properties;
import org.sa.rainbow.brass.model.map.EnvMap;

/**
 * @author ashutosh
 *
 */
public class PolicyToIG {
	public PrismPolicy m_prismPolicy;
	public EnvMap m_map;

	public PolicyToIG(PrismPolicy policy, EnvMap map) {
		// TODO Auto-generated constructor stub
		m_prismPolicy = policy;
		m_map = map;
	}
	
	private String build_cmd(int cmd_id, double dest_x, double dest_y, double speed) {
		//V(1, do MoveAbs(23.5, 0, 1) then 2)
		String cmd = "V(" + cmd_id + ", do MoveAbs(" + 
				dest_x + ", " + dest_y + 
				", " + speed + ") then " + ++cmd_id + ")";
		return cmd;
	}

	private String build_ig(ArrayList<String> cmds) {
		//P(V(1, do MoveAbs(23.5, 0, 1) then 2),
		//		  V(2, do MoveAbs(23.5, -10.5, 1) then 3)::
		//		  V(3, end)::
		//		  nil)
		String ins_graph = "P(";
		//System.out.println(cmds.size());

		for (int i = 0; i < cmds.size(); i++) {
			if (i == cmds.size() - 1) {
				ins_graph += cmds.get(i) + "::\nV(" + (i + 2) 
						+ ", end)::nil)";
			} else {
				ins_graph += cmds.get(i) + ",\n";
			}
		}

		return ins_graph;
	}
	
	public String translate() {
		ArrayList<String> plan = m_prismPolicy.getPlan();
		ArrayList<String> cmds = new ArrayList<String>();
		//double src_x = 0.0;
		//double src_y = 0.0;
		double dest_x = 1.0;
		double dest_y = 1.0;
		double speed = 1.0;
		int cmd_id = 1;

		for (int i = 0; i < plan.size(); i++) {
			String action = plan.get(i);
			//String source = "";
			//String destination = "";
			
			// Heuristic for now.
			String[] elements = action.split("_");

			//source  = elements[0];
			String destination = elements[2];
			String cmd = build_cmd(cmd_id, m_map.getNodeX(destination), m_map.getNodeY(destination), speed);
			cmds.add(cmd);
			++cmd_id;
			++dest_x;
			++dest_y;
			//System.out.println(action);
			//System.out.println(source);
			//System.out.println(destination);
			
			//addInfoToIG(action, source, destination);
		}
		
		String ins_graph = build_ig(cmds);
		System.out.println(ins_graph);
		
		return ins_graph;
	}
	
	public static void main (String[] args) throws Exception { // Class test
		  PrismPolicy prismPolicy = new PrismPolicy("/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/botpolicy.adv");
		  prismPolicy.readPolicy();  
		  EnvMap map = new EnvMap(null);
		  PolicyToIG translator = new PolicyToIG(prismPolicy, map);
		  translator.translate();
	    }  
}
