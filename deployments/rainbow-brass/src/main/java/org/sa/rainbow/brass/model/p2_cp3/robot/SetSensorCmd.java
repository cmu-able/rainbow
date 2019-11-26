package org.sa.rainbow.brass.model.p2_cp3.robot;

import java.util.EnumSet;
import java.util.List;

import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState.Sensors;
import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class SetSensorCmd extends AbstractRainbowModelOperation<EnumSet<Sensors>, RobotState> {

	private Boolean m_on;
	private Sensors m_sensor;

	public SetSensorCmd(String commandName, CP3RobotStateModelInstance model, String target, String sensor, String on) {
		super(commandName, model, target, sensor, on);
		m_sensor = Sensors.valueOf(sensor);
		m_on = Boolean.valueOf(on);
	}

	@Override
	public EnumSet<Sensors> getResult() throws IllegalStateException {
		return ((CP3RobotState) getModelContext().getModelInstance()).getSensors();
	}

	@Override
	protected List<? extends IRainbowMessage> getGeneratedEvents(IRainbowMessageFactory messageFactory) {
		return generateEvents(messageFactory, getName());
	}

	@Override
	protected void subExecute() throws RainbowException {
		((CP3RobotState) getModelContext().getModelInstance()).setSensor(m_sensor, m_on);
	}

	@Override
	protected void subRedo() throws RainbowException {
		subExecute();
	}

	@Override
	protected void subUndo() throws RainbowException {

	}

	@Override
	protected boolean checkModelValidForCommand(RobotState model) {
		return model == getModelContext().getModelInstance();
	}

}
