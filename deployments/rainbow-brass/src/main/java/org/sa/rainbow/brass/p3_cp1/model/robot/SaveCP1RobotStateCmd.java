package org.sa.rainbow.brass.p3_cp1.model.robot;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Iterator;

import org.sa.rainbow.brass.model.p2_cp3.clock.ClockedModel.TimeStamped;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState.Sensors;
import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.brass.model.robot.SaveRobotStateCmd;
import org.sa.rainbow.core.models.IModelsManager;

public class SaveCP1RobotStateCmd extends SaveRobotStateCmd {

	public SaveCP1RobotStateCmd(IModelsManager mm, String resource, OutputStream os, String source) {
		super(mm, resource, os, source);
	}
	
	@Override
	protected void insertRobotState(PrintStream ps, RobotState model) {
		super.insertRobotState(ps, model);
		if (model instanceof CP1RobotState) {
			CP1RobotState cp3 = (CP1RobotState )model;
			ps.print("  configHistory: [\n");
			for (Iterator<TimeStamped<String>> iterator = cp3.m_configHistory.iterator(); iterator.hasNext();) {
				TimeStamped<String> entry = iterator.next();
				ps.print("{timestamp: " + entry.timestamp + ", config: " + entry.data + "}");
				if (iterator.hasNext()) ps.print(",");
			}
			
			ps.print("]");
		}
		
	}

}
