package org.sa.rainbow.brass.model.robot.cp3;

import java.io.FileOutputStream;
import java.io.IOException;

import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.brass.model.robot.RobotStateCommandFactory;
import org.sa.rainbow.brass.model.robot.cp3.CP3RobotState.Sensors;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;

public class CP3RobotStateCommandFactory extends RobotStateCommandFactory {

	public CP3RobotStateCommandFactory(CP3RobotStateModelInstance model) {
		super(model);
	}
	
	
	@Override
	public AbstractSaveModelCmd<RobotState> saveCommand(String location) throws RainbowModelException {
		try (FileOutputStream os = new FileOutputStream(location)) {
			return new SaveCP3RobotStateCmd(null, location, os, m_modelInstance.getOriginalSource());
		} catch (IOException e) {
			return null;
		}
	}
	@Override
	protected void fillInCommandMap() {
		super.fillInCommandMap();
		m_commandMap.put("setBumpedCmd".toLowerCase(), SetBumpedCmd.class);
		m_commandMap.put("setSensorCmd".toLowerCase(), SetSensorCmd.class);
	}

	public SetBumpedCmd setBumpedCmd(boolean bumped) {
		return new SetBumpedCmd(m_modelInstance, "", Boolean.toString(bumped));
	}
	
	public SetSensorCmd setSensorCmd(Sensors s, boolean on) {
		return new SetSensorCmd((CP3RobotStateModelInstance )m_modelInstance, "", s.name(), Boolean.toString(on));
	}
}
