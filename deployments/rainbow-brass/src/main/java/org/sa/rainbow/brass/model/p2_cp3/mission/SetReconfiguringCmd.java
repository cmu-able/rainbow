package org.sa.rainbow.brass.model.p2_cp3.mission;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class SetReconfiguringCmd extends AbstractRainbowModelOperation<Boolean, MissionState> {

	private boolean m_reconfiguring;
	private boolean m_old;

	public SetReconfiguringCmd(String commandName, MissionStateModelInstance model, String target, String reconfiguring) {
		super(commandName, model, target, reconfiguring);
		try {
			m_reconfiguring = Boolean.parseBoolean(reconfiguring);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public Boolean getResult() throws IllegalStateException {
		return m_reconfiguring;
	}

	@Override
	protected List<? extends IRainbowMessage> getGeneratedEvents(IRainbowMessageFactory messageFactory) {
		return generateEvents(messageFactory, getName());

	}

	@Override
	protected void subExecute() throws RainbowException {
		m_old = getModelContext().getModelInstance().isReconfiguring();
		getModelContext().getModelInstance().setReconfiguring(m_reconfiguring);
	}

	@Override
	protected void subRedo() throws RainbowException {
		subExecute();
	}

	@Override
	protected void subUndo() throws RainbowException {
		getModelContext().getModelInstance().setReconfiguring(m_old);
	}

	@Override
	protected boolean checkModelValidForCommand(MissionState model) {
		return true;
	}

}
