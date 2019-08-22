package org.sa.rainbow.brass.p3_cp1.model.robot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sa.rainbow.brass.model.p2_cp3.robot.SaveCP3RobotStateCmd;
import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.brass.model.robot.RobotStateCommandFactory;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;

public class CP1RobotStateCommandFactory extends RobotStateCommandFactory {
	public CP1RobotStateCommandFactory(CP1RobotStateModelInstance model) {
		super(CP1RobotStateModelInstance.class, model);
	}

	public static CP1RobotStateLoadCmd loadCommand(ModelsManager mm, String modelName, InputStream stream, String source) {
		return new CP1RobotStateLoadCmd(mm, modelName, stream, source);
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
		m_commandMap.put("setConfig".toLowerCase(), SetConfigCmd.class);
	}
	
	public SetConfigCmd setConfigCmd(String configId) {
		return new SetConfigCmd((CP1RobotStateModelInstance )m_modelInstance, "", configId);
	}
}
