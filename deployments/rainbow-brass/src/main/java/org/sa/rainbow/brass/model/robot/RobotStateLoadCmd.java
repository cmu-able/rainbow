package org.sa.rainbow.brass.model.robot;

import java.io.InputStream;

import org.sa.rainbow.brass.model.mission.MissionState;
import org.sa.rainbow.brass.model.mission.MissionStateModelInstance;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.AbstractLoadModelCmd;

public class RobotStateLoadCmd extends AbstractLoadModelCmd<RobotState> {

	private String m_modelName;
	protected InputStream m_stream;
	protected RobotStateModelInstance m_result;

	public RobotStateLoadCmd(IModelsManager mm, String resource, InputStream is, String source) {
		super("loadRobotState", mm, resource, is, source);
		m_modelName = resource;
		m_stream = is;
	}

	@Override
	public IModelInstance<RobotState> getResult() throws IllegalStateException {
		return m_result;
	}

	@Override
	public ModelReference getModelReference() {
		return new ModelReference(m_modelName, RobotStateModelInstance.ROBOT_STATE_TYPE);
	}

	@Override
	protected void subExecute() throws RainbowException {
		if (m_stream == null) {
			RobotState m = new RobotState(getModelReference());
			m_result = new RobotStateModelInstance(m, getOriginalSource());
			doPostExecute();
		}
	}

	@Override
	protected void subRedo() throws RainbowException {
		doPostExecute();
	}

	@Override
	protected void subUndo() throws RainbowException {
		doPostUndo();

	}

	@Override
	protected boolean checkModelValidForCommand(Object model) {
		return true;
	}

}
