package org.sa.rainbow.brass.model.clock;

import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public class ClockModelInstance implements IModelInstance<Clock> {

	public static final String CLOCK_TYPE = "Clock";
	private Clock m_clock;
	private ClockCommandFactory m_commandFactory;
	private String m_source;

	public ClockModelInstance(Clock c, String source) {
		setModelInstance(c);
		setOriginalSource(source);
	}
	
	@Override
	public Clock getModelInstance() {
		return m_clock;
	}

	@Override
	public void setModelInstance(Clock model) {
		m_clock = model;
	}

	@Override
	public IModelInstance<Clock> copyModelInstance(String newName) throws RainbowCopyException {
		return new ClockModelInstance(m_clock.copy(), getOriginalSource());
	}

	@Override
	public String getModelType() {
		return CLOCK_TYPE;
	}

	@Override
	public String getModelName() {
		return getModelInstance().getModelReference().getModelName ();
	}

	@Override
	public ClockCommandFactory getCommandFactory() {
		if (m_commandFactory == null) 
			m_commandFactory = new ClockCommandFactory(this);
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
