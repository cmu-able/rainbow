package org.sa.rainbow.brass.model.p2_cp3.robot;

import java.util.List;

import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
/**
 * This is a little tricky. Because of the way generics are set up, we can't
 * use CP3RobotState directly, and so therefore we need to use use RobotState.
 * This means we need to cast to CP3RobotState everywhere. The alternative is to
 * duplicate the RobotState commands rather than reuse through inheritence. C'est la vie.
 * 
 * @author schmerl
 *
 */
public class SetBumpedCmd extends AbstractRainbowModelOperation<Boolean, RobotState> {

	private boolean m_bumped;

	public SetBumpedCmd(String commandName, CP3RobotStateModelInstance model, String target, String bumped) {
		super(commandName, model, target, bumped);
		m_bumped = Boolean.getBoolean(bumped);
	}

	@Override
	public Boolean getResult() throws IllegalStateException {
		return ((CP3RobotState )getModelContext().getModelInstance()).bumpState();
	}

	@Override
	protected List<? extends IRainbowMessage> getGeneratedEvents(IRainbowMessageFactory messageFactory) {
		return generateEvents(messageFactory, getName());
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
