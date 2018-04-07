package org.sa.rainbow.brass.model.p2_cp1.robot;

import java.util.List;

import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class SetConfigCmd extends AbstractRainbowModelOperation<String, RobotState> {

	public SetConfigCmd(CP1RobotStateModelInstance model, String target,
			String config) {
		super("setConfig", model, target, config);
	}

	@Override
	public String getResult() throws IllegalStateException {
		return ((CP1RobotState )getModelContext()).getConfigId();
	}

	@Override
	protected List<? extends IRainbowMessage> getGeneratedEvents(IRainbowMessageFactory messageFactory) {
		return generateEvents(messageFactory, "setConfig");
	}

	@Override
	protected void subExecute() throws RainbowException {
		((CP1RobotState )getModelContext().getModelInstance()).setConfigId(getParameters()[0]);
	}

	@Override
	protected void subRedo() throws RainbowException {
		subExecute();
	}

	@Override
	protected void subUndo() throws RainbowException {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean checkModelValidForCommand(RobotState model) {
		return model == getModelContext().getModelInstance();
	}

}
