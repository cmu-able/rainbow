package org.sa.rainbow.brass.model.mission;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class StallMissionCmd extends AbstractRainbowModelOperation<Boolean, MissionState> {

	public StallMissionCmd (MissionStateModelInstance model, String target) {
        super ("stallMission", model, target);
    }

	@Override
	public Boolean getResult() throws IllegalStateException {
		return getModelContext().getModelInstance().isErrorDetected();
	}

	@Override
	protected List<? extends IRainbowMessage> getGeneratedEvents(IRainbowMessageFactory messageFactory) {
		return generateEvents(messageFactory, "stallMission");
	}

	@Override
	protected void subExecute() throws RainbowException {
		getModelContext().getModelInstance().setErrorDetected(true);
	}

	@Override
	protected void subRedo() throws RainbowException {
		getModelContext().getModelInstance().setErrorDetected(true);
	}

	@Override
	protected void subUndo() throws RainbowException {
		// Assume that StallMissionCmd only sets m_errorDetected flag to true
		getModelContext().getModelInstance().setErrorDetected(false);
	}

	@Override
	protected boolean checkModelValidForCommand(MissionState missionState) {
		return missionState == getModelContext().getModelInstance();
	}

}
