package org.sa.rainbow.brass.model.mission;

import java.util.List;

import org.sa.rainbow.brass.model.instructions.SetLocalizationFidelityInstruction.LocalizationFidelity;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class SetRobotLocalizationFidelityCmd extends AbstractRainbowModelOperation<LocalizationFidelity, MissionState> {
	private LocalizationFidelity m_fidelity;

	public SetRobotLocalizationFidelityCmd(MissionStateModelInstance model, String target, String fidelity) {
		super("setRobotLocalizationFidelity", model, target, fidelity);
		m_fidelity = LocalizationFidelity.valueOf(fidelity);
	}

	@Override
	public LocalizationFidelity getResult() throws IllegalStateException {
		return getModelContext().getModelInstance().getLocalizationFidelity();
	}

	@Override
	protected boolean checkModelValidForCommand(MissionState missionState) {
		return  missionState == getModelContext().getModelInstance();
	}

	@Override
	protected List<? extends IRainbowMessage> getGeneratedEvents(IRainbowMessageFactory messageFactory) {
		return generateEvents(messageFactory, "setRobotLocalizationFidelity");
	}

	@Override
	protected void subExecute() throws RainbowException {
		getModelContext().getModelInstance().setLocalizationFidelity(m_fidelity);
	}

	@Override
	protected void subRedo() throws RainbowException {
		getModelContext().getModelInstance().setLocalizationFidelity(m_fidelity);
	}

	@Override
	protected void subUndo() throws RainbowException {
		getModelContext ().getModelInstance ().m_localizationFidelityHistory.pop ();
	}

}
