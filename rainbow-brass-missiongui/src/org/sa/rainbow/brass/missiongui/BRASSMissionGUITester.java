package org.sa.rainbow.brass.missiongui;

import org.sa.rainbow.brass.missiongui.IBRASSOperations;

public class BRASSMissionGUITester extends Thread {

	private IBRASSOperations m_bop;
	
	private double m_robot_x = 40;
	private double m_robot_y = 69;
	private int m_frame=0;
	
	public BRASSMissionGUITester(IBRASSOperations bop){
		super();
		m_bop = bop;		
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
			}
			
			if (m_frame==60){
				m_bop.setRobotObstructed(false);
				m_bop.reportFromDAS("Robot NOT blocked anymore!");
			}

			if (m_frame==65){
				m_bop.reportFromDAS("Notification 1 received from rainbow");
				m_bop.reportFromDAS("Notification 2 received from rainbow");
				m_bop.reportFromDAS("Notification 3 received from rainbow");
			}

			if (m_frame==70){
				m_bop.reportFromDAS("Inserted new node in map between l2 and l3");
				m_bop.insertMapNode("nn2", "l2", "l3", 30.0, 69.0);
			}

			if (m_frame==72){
				m_bop.reportFromDAS("Notification 5");
			}

			if (m_frame==74){
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
