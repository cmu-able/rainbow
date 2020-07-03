package org.sa.rainbow.brass.model.mission;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class SetRobotAccurateCmd extends AbstractRainbowModelOperation<Boolean, MissionState> {
	private boolean m_robotAccurate;

	public SetRobotAccurateCmd(String commandName, MissionStateModelInstance model, String target, String robotAccurate) {
		super(commandName, model, target, robotAccurate);
		m_robotAccurate = Boolean.parseBoolean(robotAccurate);
	}

	@Override
	public Boolean getResult() throws IllegalStateException {
		return getModelContext().getModelInstance().isRobotAccurate();
	}

	@Override
	protected boolean checkModelValidForCommand(MissionState missionState) {
		return missionState == getModelContext().getModelInstance();
	}

	@Override
	protected List<? extends IRainbowMessage> getGeneratedEvents(IRainbowMessageFactory messageFactory) {
		return generateEvents(messageFactory, getName());
	}

	@Override
	protected void subExecute() throws RainbowException {
		getModelContext().getModelInstance().setRobotAccurate(m_robotAccurate);
	}

	@Override
	protected void subRedo() throws RainbowException {
		getModelContext().getModelInstance().setRobotAccurate(m_robotAccurate);
	}

	@Override
	protected void subUndo() throws RainbowException {
		// Assume that SetRobotAccurateCmd only sets m_robotAccurate flag when the value changes
		getModelContext().getModelInstance().setRobotAccurate(!m_robotAccurate);
	}

}
