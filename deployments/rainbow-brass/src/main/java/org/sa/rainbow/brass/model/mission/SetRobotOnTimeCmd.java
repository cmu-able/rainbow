package org.sa.rainbow.brass.model.mission;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class SetRobotOnTimeCmd extends AbstractRainbowModelOperation<Boolean, MissionState> {
	private boolean m_robotOnTime;

	public SetRobotOnTimeCmd (String commandName, MissionStateModelInstance model, String target, String robotOnTime) {
        super (commandName, model, target, robotOnTime);
        m_robotOnTime = Boolean.parseBoolean(robotOnTime);
    }

	@Override
	public Boolean getResult() throws IllegalStateException {
		return getModelContext().getModelInstance().isRobotOnTime();
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
		getModelContext().getModelInstance().setRobotOnTime(m_robotOnTime);
	}

	@Override
	protected void subRedo() throws RainbowException {
		getModelContext().getModelInstance().setRobotOnTime(m_robotOnTime);
	}

	@Override
	protected void subUndo() throws RainbowException {
		// Assume that SetRobotOnTimeCmd only sets m_robotOnTime flag when the value changes
		getModelContext().getModelInstance().setRobotOnTime(!m_robotOnTime);
	}

}
