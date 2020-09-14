package org.sa.rainbow.brass.model.robot;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class SetSpeedCmd extends AbstractRainbowModelOperation<Double, RobotState> {

	private double m_speed;

	public SetSpeedCmd(String commandName, IModelInstance<RobotState> model, String target, String speed) {
		super(commandName, model, target, speed);
		m_speed = Double.parseDouble(speed);
	}

	@Override
	public Double getResult() throws IllegalStateException {
        return getModelContext ().getModelInstance ().getSpeed ();

	}

	@Override
	protected List<? extends IRainbowMessage> getGeneratedEvents(IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, getName());

	}

	@Override
	protected void subExecute() throws RainbowException {
        getModelContext ().getModelInstance ().setSpeed (m_speed);

	}

	@Override
	protected void subRedo() throws RainbowException {
        subExecute ();

	}

	@Override
	protected void subUndo() throws RainbowException {

	}

	@Override
	protected boolean checkModelValidForCommand(RobotState model) {
        return model == getModelContext ().getModelInstance ();

	}

}
