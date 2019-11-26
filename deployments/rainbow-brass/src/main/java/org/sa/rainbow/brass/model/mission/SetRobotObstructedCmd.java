package org.sa.rainbow.brass.model.mission;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class SetRobotObstructedCmd extends AbstractRainbowModelOperation<Boolean, MissionState> {
	private boolean m_robotObstructed;

	public SetRobotObstructedCmd (String commandName, MissionStateModelInstance model, String target, String robotObstructed) {
        super (commandName, model, target, robotObstructed);
        m_robotObstructed = Boolean.parseBoolean(robotObstructed);
    }

	@Override
	public Boolean getResult() throws IllegalStateException {
		return getModelContext().getModelInstance().isRobotObstructed();
	}

	@Override
	protected List<? extends IRainbowMessage> getGeneratedEvents(IRainbowMessageFactory messageFactory) {
		return generateEvents(messageFactory, getName());
	}

	@Override
	protected void subExecute() throws RainbowException {
		getModelContext().getModelInstance().setRobotObstructed(m_robotObstructed);
	}

	@Override
	protected void subRedo() throws RainbowException {
		getModelContext().getModelInstance().setRobotObstructed(m_robotObstructed);
	}

	@Override
	protected void subUndo() throws RainbowException {
		// Assume that SetRobotObstructedCmd only sets m_robotObstructed flag when the value changes
		getModelContext().getModelInstance().setRobotObstructed(!m_robotObstructed);
	}

	@Override
	protected boolean checkModelValidForCommand(MissionState missionState) {
		return missionState == getModelContext().getModelInstance();
	}

}
