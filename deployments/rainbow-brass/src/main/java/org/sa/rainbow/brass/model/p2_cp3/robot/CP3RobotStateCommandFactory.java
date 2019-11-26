package org.sa.rainbow.brass.model.p2_cp3.robot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState.Sensors;
import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.brass.model.robot.RobotStateCommandFactory;
import org.sa.rainbow.brass.model.robot.RobotStateLoadCmd;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;

public class CP3RobotStateCommandFactory extends RobotStateCommandFactory {

	
	private static final String SET_LIGHTING_CMD = "setLighting";
	private static final String SET_SENSOR_FAILED_CMD = "setSensorFailed";
	private static final String SET_SENSOR_CMD = "setSensor";
	private static final String SET_BUMPED_CMD = "setBumped";

	@LoadOperation
	public static CP3RobotStateLoadCmd loadCommand(ModelsManager mm, String modelName, InputStream stream, String source) {
		return new CP3RobotStateLoadCmd(mm, modelName, stream, source);
	}
	
	public CP3RobotStateCommandFactory(CP3RobotStateModelInstance model) throws RainbowException {
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

	@Operation(name=SET_BUMPED_CMD)
	public SetBumpedCmd setBumpedCmd(boolean bumped) {
		return new SetBumpedCmd(SET_BUMPED_CMD, (CP3RobotStateModelInstance )m_modelInstance, "", Boolean.toString(bumped));
	}
	
	@Operation(name=SET_SENSOR_CMD)
	public SetSensorCmd setSensorCmd(Sensors s, boolean on) {
		return new SetSensorCmd(SET_SENSOR_CMD, (CP3RobotStateModelInstance )m_modelInstance, "", s.name(), Boolean.toString(on));
	}
	
	@Operation(name=SET_LIGHTING_CMD)
	public SetLightingCmd setLightingCmd(double lighting) {
		return new SetLightingCmd(SET_LIGHTING_CMD, (CP3RobotStateModelInstance )m_modelInstance, "",  Double.toString(lighting));
	}
	
	@Operation(name=SET_SENSOR_FAILED_CMD)
	public SetSensorFailedCmd sesnSensorFailedCmd(Sensors sensor) {
		return new SetSensorFailedCmd(SET_SENSOR_FAILED_CMD, (CP3RobotStateModelInstance )m_modelInstance, sensor.name(), "");
	}
}
