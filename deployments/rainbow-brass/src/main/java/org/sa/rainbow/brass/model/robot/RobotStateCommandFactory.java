package org.sa.rainbow.brass.model.robot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public class RobotStateCommandFactory extends ModelCommandFactory<RobotState> {
	private static final String SET_SPEED_CMD = "setSpeed";
	private static final String SET_BATTERY_CHARGE_CMD = "setBatteryCharge";
	private static final String SET_CLOCK_MODEL_CMD = "setClockModel";

	public RobotStateCommandFactory(
			RobotStateModelInstance model) throws RainbowException {
		super(RobotStateModelInstance.class, model);
	}
	
	protected RobotStateCommandFactory (Class<? extends IModelInstance<RobotState>> c, RobotStateModelInstance model) throws RainbowException {
		super (c, model);
	}

	@LoadOperation
	public static RobotStateLoadCmd loadCommand(ModelsManager mm, String modelName, InputStream stream, String source) {
		return new RobotStateLoadCmd(mm, modelName, stream, source);
	}

	@Override
	public AbstractSaveModelCmd<RobotState> saveCommand(String location) throws RainbowModelException {
		try (FileOutputStream os = new FileOutputStream(location)) {
			return new SaveRobotStateCmd(null, location, os, m_modelInstance.getOriginalSource());
		} catch (IOException e) {
			return null;
		}
	}
	
	@Operation(name=SET_CLOCK_MODEL_CMD)
	public SetClockModelCmd setClockModelCmd(String clockReference) {
		return new SetClockModelCmd(SET_CLOCK_MODEL_CMD, m_modelInstance, "", clockReference);
	}

	@Operation(name=SET_BATTERY_CHARGE_CMD)
	public SetBatteryChargeCmd setBatteryChargeCmd(double charge) {
        return new SetBatteryChargeCmd (SET_BATTERY_CHARGE_CMD, (RobotStateModelInstance) m_modelInstance, "", Double.toString (charge));
	}
	
	@Operation(name=SET_SPEED_CMD)
	public SetSpeedCmd setSpeedCmd(double speed) {
		return new SetSpeedCmd(SET_SPEED_CMD, (RobotStateModelInstance )m_modelInstance, "", Double.toString(speed));
	}
}
