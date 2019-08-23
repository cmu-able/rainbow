package org.sa.rainbow.brass.model.p2_cp3.rainbowState;

import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public class RainbowStateModelInstance implements IModelInstance<RainbowState> {

	
	
	public static final String TYPE = "RainbowState";
	private RainbowState m_rainbowState;
	private RainbowStateCommandFactory m_commandFactory;
	private String m_source;

	public RainbowStateModelInstance(RainbowState rs, String source) {
		setModelInstance(rs);
		setOriginalSource(source);
	}
	
	@Override
	public RainbowState getModelInstance() {
		return m_rainbowState;
	}

	@Override
	public void setModelInstance(RainbowState model) {
		m_rainbowState = model;
	}

	@Override
	public IModelInstance<RainbowState> copyModelInstance(String newName) throws RainbowCopyException {
		return new RainbowStateModelInstance(m_rainbowState.copy (), getOriginalSource());
	}

	@Override
	public String getModelType() {
		return TYPE;
	}

	@Override
	public String getModelName() {
		return getModelInstance().getModelReference().getModelName ();
	}

	@Override
	public RainbowStateCommandFactory getCommandFactory() {
		if (m_commandFactory == null) 
			m_commandFactory = new RainbowStateCommandFactory(this);
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
