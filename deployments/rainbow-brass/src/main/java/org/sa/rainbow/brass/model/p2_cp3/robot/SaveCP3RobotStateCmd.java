package org.sa.rainbow.brass.model.p2_cp3.robot;

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

public class SaveCP3RobotStateCmd extends SaveRobotStateCmd {

	public SaveCP3RobotStateCmd(IModelsManager mm, String resource, OutputStream os, String source) {
		super(mm, resource, os, source);
	}
	
	@Override
	protected void insertRobotState(PrintStream ps, RobotState model) {
		super.insertRobotState(ps, model);
		if (model instanceof CP3RobotState) {
			CP3RobotState cp3 = (CP3RobotState )model;
			ps.print("  bumpHistory : [\n");
			for (Iterator<TimeStamped<Boolean>> iterator = cp3.m_bumpState.iterator(); iterator.hasNext();) {
				TimeStamped<Boolean> entry = iterator.next();
				ps.print("{timestamp: " + entry.timestamp + ", bumped : " + entry.data + "}");
				if (iterator.hasNext()) ps.print(", ");
			}
			ps.print("]");
			
			ps.print("  sensorHistory : [\n");
			for (Iterator<TimeStamped<EnumSet<Sensors>>> iterator = cp3.m_sensorHistory.iterator(); iterator.hasNext();) {
				TimeStamped<EnumSet<Sensors>> entry = iterator.next();
				ps.print("{timestamp: " + entry.timestamp + ", sensors : [" );
				EnumSet<Sensors> data = entry.data;
				for (Iterator iterator2 = data.iterator(); iterator2.hasNext();) {
					Sensors sensors = (Sensors) iterator2.next();
					ps.print(sensors.name());
					if (iterator2.hasNext()) ps.print(", ");
				}
				ps.print("]}");
				if (iterator.hasNext()) ps.print(", ");
			}
			ps.print("]");
		}
		
	}

}
