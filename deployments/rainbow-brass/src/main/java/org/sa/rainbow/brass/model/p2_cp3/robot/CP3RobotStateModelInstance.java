package org.sa.rainbow.brass.model.p2_cp3.robot;

import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.brass.model.robot.RobotStateCommandFactory;
import org.sa.rainbow.brass.model.robot.RobotStateModelInstance;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public class CP3RobotStateModelInstance extends RobotStateModelInstance {

	private CP3RobotStateCommandFactory m_commandFactory;

	public CP3RobotStateModelInstance(CP3RobotState r, String source) {
		super(r, source);
	}

	@Override
	public CP3RobotState getModelInstance() {
		return (CP3RobotState) super.getModelInstance();
	}
	
	@Override
	public ModelCommandFactory<RobotState> getCommandFactory() throws RainbowException {
		if (m_commandFactory == null)
			m_commandFactory = new CP3RobotStateCommandFactory(this);
		return m_commandFactory;
	}
}
