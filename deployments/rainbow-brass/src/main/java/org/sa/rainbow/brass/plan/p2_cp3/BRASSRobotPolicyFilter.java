package org.sa.rainbow.brass.plan.p2_cp3;

import java.util.ArrayList;
import java.util.Objects;

public class BRASSRobotPolicyFilter {

	ArrayList<String> m_plan;
		
	public BRASSRobotPolicyFilter (ArrayList<String> plan) {
		m_plan = plan;
	}
	
	public ArrayList<String> getPlan(String startLoc, String endLoc){
		ArrayList<String> reconf = new ArrayList<String>();
		ArrayList<String> task = new ArrayList<String>();
		ArrayList<String> taskn = new ArrayList<String>();
		
		for (int i=0;i<m_plan.size();i++) {
			if (m_plan.get(i).contains("_enable")||m_plan.get(i).contains("_disable"))
				reconf.add(m_plan.get(i).replaceFirst("_", "").trim());
			else
				task.add(m_plan.get(i));
		}
		
		ArrayList<String> path = getPath(startLoc, endLoc, task);
		
		for (int i=0;i<path.size()-1;i++) {
			taskn.add (path.get(i)+"_to_"+path.get(i+1));
		}
		
		reconf.addAll(taskn);
		return reconf;
	}
	
	public ArrayList<String> getPath(String start, String end, ArrayList<String> plan){
		String[][] l = new String[plan.size()][4];
		for (int i=0;i<plan.size();i++) {
			String[] segments = plan.get(i).replace("_moveTo_", "").replaceAll("_0", "").replace(" ", "").split("_");
			for (int j=0; j<segments.length; j++) {
				String[] locations = segments[j].replaceFirst("l", "").split("l");
				for (int k=0; k<2;k++) {
					l[i][j*2+k]="l"+locations[k];
					}
				}
			}
		return PathBuilder.getPath(l, start, end);
	}
	
	public String getNextLoc (String[] chunks, String currentLoc) {
		return Objects.equals(currentLoc, chunks[2]) ? chunks[4] : chunks[2];
	}
	
	public String reformatLabel(String label, String currentLoc) {
		String[] chunks = label.split("_");
		String nextLoc = getNextLoc (chunks, currentLoc);
		return currentLoc+"_to_"+nextLoc;
	}
	
}
