package org.sa.rainbow.brass.missiongui;

import org.sa.rainbow.brass.missiongui.IBRASSOperations;
import org.sa.rainbow.brass.adaptation.PrismConnector;
import org.sa.rainbow.brass.model.map.MapTranslator;
import org.sa.rainbow.brass.missiongui.Renderer;

public class BRASSMissionGUITester extends Thread {

	private Renderer m_bop; // Use IBRASSOperations instead of renderer in production code
	private PrismConnector m_conn;
	private MapTranslator m_trans;
	
	private double m_robot_x = 40;
	private double m_robot_y = 69;
	private int m_frame=0;
	
	public BRASSMissionGUITester(Renderer bop){ // Use IBRASSOperations instead of renderer in production code
		super();
		m_bop = bop;
		m_conn = new PrismConnector (null);
		m_trans = new MapTranslator();
		System.out.println("Tester initialized");
	}
	

	public void update_robot_position(){ // Robot animation test code
        if (m_robot_y>0){ 
        	m_robot_y-=0.1;
    		}        
		m_robot_x+=0.0;
		m_bop.setRobotLocation(m_robot_x, m_robot_y);		
	}
	
	public void run(){

		while (true) {
			update_robot_position();
			
			if (m_frame==10){
				m_bop.setRobotObstructed(true);
				m_bop.reportFromDAS("Robot blocked!");
				m_trans.setMap(m_bop.m_map); // Using Renderer instead of IBRASSOperation to access m_map for testing purposes
				m_trans.exportMapTranslation("/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/prismtmp.prism");
				m_conn.invoke (8, 0);
				m_bop.newInstructionGraph(null);
			}
			
			if (m_frame==60){
				m_bop.setRobotObstructed(false);
				m_bop.reportFromDAS("Robot NOT blocked anymore!");
				m_bop.setExecutingInstruction("Instruction 1");
			}

			if (m_frame==65){
				m_bop.reportFromDAS("Notification 1 received from rainbow");
				m_bop.reportFromDAS("Notification 2 received from rainbow");
				m_bop.reportFromDAS("Notification 3 received from rainbow");
				m_bop.setExecutingInstruction("Instruction 2");

			}

			if (m_frame==70){
				m_bop.reportFromDAS("Inserted new node in map between l2 and l3");
				m_bop.insertMapNode("nn2", "l3", "l2", 30.0, 69.0);
				m_trans.exportMapTranslation("/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/prismtmp.prism");
		        m_conn.invoke (8, 0); 
		        m_bop.setInstructionFailed(true);
			}

			if (m_frame==80){
				m_bop.reportFromDAS("Notification 5");
				m_bop.setExecutingInstruction("Instruction 3");
			}

			if (m_frame==90){
				m_bop.reportFromDAS("Notification 6");
			}

			
			m_frame++;
			
			try {
				sleep(100);
			} catch (InterruptedException e){
				System.out.println("MissionGUITester: Interrupted thread.");
			}
		}
	}
}
