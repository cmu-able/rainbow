package org.sa.rainbow.brass.model.p2_cp3.robot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState.Sensors;
import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.brass.model.robot.RobotStateCommandFactory;
import org.sa.rainbow.brass.model.robot.RobotStateLoadCmd;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;

public class CP3RobotStateCommandFactory extends RobotStateCommandFactory {

	
	public static CP3RobotStateLoadCmd loadCommand(ModelsManager mm, String modelName, InputStream stream, String source) {
		return new CP3RobotStateLoadCmd(mm, modelName, stream, source);
	}
	
	public CP3RobotStateCommandFactory(CP3RobotStateModelInstance model) {
		super(CP3RobotStateModelInstance.class, model);
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
		m_commandMap.put("setBumped".toLowerCase(), SetBumpedCmd.class);
		m_commandMap.put("setSensor".toLowerCase(), SetSensorCmd.class);
		m_commandMap.put("setSensorFailed".toLowerCase(), SetSensorFailedCmd.class);
		m_commandMap.put("setLighting".toLowerCase(), SetLightingCmd.class);

	}

	public SetBumpedCmd setBumpedCmd(boolean bumped) {
		return new SetBumpedCmd((CP3RobotStateModelInstance )m_modelInstance, "", Boolean.toString(bumped));
	}
	
	public SetSensorCmd setSensorCmd(Sensors s, boolean on) {
		return new SetSensorCmd((CP3RobotStateModelInstance )m_modelInstance, "", s.name(), Boolean.toString(on));
	}
	
	public SetLightingCmd setLightingCmd(double lighting) {
		return new SetLightingCmd((CP3RobotStateModelInstance )m_modelInstance, "",  Double.toString(lighting));
	}
	
	public SetSensorFailedCmd sesnSensorFailedCmd(Sensors sensor) {
		return new SetSensorFailedCmd((CP3RobotStateModelInstance )m_modelInstance, sensor.name(), "");
	}
}
