package org.sa.rainbow.brass.model.robot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public class RobotStateCommandFactory extends ModelCommandFactory<RobotState> {
	public RobotStateCommandFactory(
			RobotStateModelInstance model) {
		super(RobotStateModelInstance.class, model);
	}
	
	protected RobotStateCommandFactory (Class<? extends IModelInstance<RobotState>> c, RobotStateModelInstance model) {
		super (c, model);
	}

	public static RobotStateLoadCmd loadCommand(ModelsManager mm, String modelName, InputStream stream, String source) {
		return new RobotStateLoadCmd(mm, modelName, stream, source);
	}

	@Override
	protected void fillInCommandMap() {
		m_commandMap.put("setClockModel".toLowerCase(), SetClockModelCmd.class);
		m_commandMap.put("setBatteryCharge".toLowerCase(), SetBatteryChargeCmd.class);
		m_commandMap.put("setSpeed".toLowerCase(), SetSpeedCmd.class);

	}

	@Override
	public AbstractSaveModelCmd<RobotState> saveCommand(String location) throws RainbowModelException {
		try (FileOutputStream os = new FileOutputStream(location)) {
			return new SaveRobotStateCmd(null, location, os, m_modelInstance.getOriginalSource());
		} catch (IOException e) {
			return null;
		}
	}
	
	public SetClockModelCmd setClockModelCmd(String clockReference) {
		return new SetClockModelCmd(m_modelInstance, "", clockReference);
	}

	public SetBatteryChargeCmd setBatteryChargeCmd(double charge) {
        return new SetBatteryChargeCmd ((RobotStateModelInstance) m_modelInstance, "", Double.toString (charge));
	}
	
	public SetSpeedCmd setSpeedCmd(double speed) {
		return new SetSpeedCmd((RobotStateModelInstance )m_modelInstance, "", Double.toString(speed));
	}
}
