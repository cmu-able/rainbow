package org.sa.rainbow.brass.model.robot;

import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public class RobotStateModelInstance implements IModelInstance<RobotState> {
	
	public static final String ROBOT_STATE_TYPE = "RobotState";
	private RobotState m_robotState;
	private RobotStateCommandFactory m_commandFactory;
	private String m_source;

	public RobotStateModelInstance(RobotState r, String source) {
		setModelInstance(r);
		setOriginalSource(source);
	}

	@Override
	public RobotState getModelInstance() {
		return m_robotState;
	}

	@Override
	public void setModelInstance(RobotState model) {
		m_robotState = model;
	}

	@Override
	public IModelInstance<RobotState> copyModelInstance(String newName) throws RainbowCopyException {
		return new RobotStateModelInstance(getModelInstance().copy(), getOriginalSource());
	}

	@Override
	public String getModelType() {
		return ROBOT_STATE_TYPE;
	}

	@Override
	public String getModelName() {
		return getModelInstance().getModelReference().getModelName();
	}

	@Override
	public ModelCommandFactory<RobotState> getCommandFactory() {
		if (m_commandFactory == null)
			m_commandFactory = new RobotStateCommandFactory(this);
		return m_commandFactory;
	}

	@Override
	public void setOriginalSource(String source) {
		m_source = source;
	}

	@Override
	public String getOriginalSource() {
		return m_source;
	}

	@Override
	public void dispose() throws RainbowException {

	}

}
