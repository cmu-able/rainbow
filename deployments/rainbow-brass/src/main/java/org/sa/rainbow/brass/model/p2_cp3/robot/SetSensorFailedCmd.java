package org.sa.rainbow.brass.model.p2_cp3.robot;

import java.util.List;

import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState.Sensors;
import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class SetSensorFailedCmd extends AbstractRainbowModelOperation<Boolean, RobotState> {

	private Sensors m_sensor;

	public SetSensorFailedCmd(String commandName, CP3RobotStateModelInstance model, String target, String param) {
		super(commandName, model, target, param);
		m_sensor = Sensors.valueOf(param);
	}

	@Override
	public Boolean getResult() throws IllegalStateException {
		return true;
	}

	@Override
	protected List<? extends IRainbowMessage> getGeneratedEvents(IRainbowMessageFactory messageFactory) {
		return generateEvents(messageFactory, getName());
	}

	@Override
	protected void subExecute() throws RainbowException {
		((CP3RobotState) getModelContext().getModelInstance()).setSensorFailed(m_sensor);
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
		return true;
	}

}
