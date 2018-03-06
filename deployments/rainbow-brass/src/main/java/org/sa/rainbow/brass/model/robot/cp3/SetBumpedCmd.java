package org.sa.rainbow.brass.model.robot.cp3;

import java.util.List;

import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class SetBumpedCmd extends AbstractRainbowModelOperation<Boolean, RobotState> {

	private boolean m_bumped;

	public SetBumpedCmd(IModelInstance<RobotState> model, String target, String bumped) {
		super("SetBumpedCmd", model, target, bumped);
		m_bumped = Boolean.getBoolean(bumped);
	}

	@Override
	public Boolean getResult() throws IllegalStateException {
		return ((CP3RobotState )getModelContext().getModelInstance()).bumpState();
	}

	@Override
	protected List<? extends IRainbowMessage> getGeneratedEvents(IRainbowMessageFactory messageFactory) {
		return generateEvents(messageFactory, "setBumped");
	}

	@Override
	protected void subExecute() throws RainbowException {
		((CP3RobotState )getModelContext().getModelInstance()).setBumped(m_bumped);
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
