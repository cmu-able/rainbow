package org.sa.rainbow.brass.model.p2_cp3.rainbowState;

import java.io.InputStream;

import org.sa.rainbow.brass.model.p2_cp3.clock.ClockModelInstance;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.AbstractLoadModelCmd;

public class RainbowStateLoadCmd extends AbstractLoadModelCmd<RainbowState> {

	private String m_modelName;
	private InputStream m_stream;
	private RainbowStateModelInstance m_result;

	public RainbowStateLoadCmd(IModelsManager mm, String resource, InputStream is, String source) {
		super("loadRainbowState", mm, resource, is, source);
		m_modelName = resource;
		m_stream = is;
	}

	@Override
	public IModelInstance<RainbowState> getResult() throws IllegalStateException {
		return m_result;
	}

	@Override
	public ModelReference getModelReference() {
		return new ModelReference(m_modelName, RainbowStateModelInstance.TYPE);
	}

	@Override
	protected void subExecute() throws RainbowException {
		if (m_stream == null) {
			RainbowState rs = new RainbowState(getModelReference());
			m_result = new RainbowStateModelInstance(rs, getOriginalSource());
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
