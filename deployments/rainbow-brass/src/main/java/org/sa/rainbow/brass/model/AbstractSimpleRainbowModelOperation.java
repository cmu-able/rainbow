package org.sa.rainbow.brass.model;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public abstract class AbstractSimpleRainbowModelOperation<R, M> extends AbstractRainbowModelOperation<R, M> {

	
	private String m_event;
	private R m_result;

	public AbstractSimpleRainbowModelOperation(String commandName, String eventName, IModelInstance<? extends M> model, String target,
			String... parameters) {
		super(commandName, model, target, parameters);
		m_event = eventName;
	}

	protected void setResult(R r) {
		m_result = r;
	}
	
	@Override
	public R getResult() throws IllegalStateException {
		return m_result;
	}

	@Override
	protected List<? extends IRainbowMessage> getGeneratedEvents(IRainbowMessageFactory messageFactory) {
		return generateEvents(messageFactory, m_event);

	}

	

	@Override
	protected void subRedo() throws RainbowException {

	}

	@Override
	protected void subUndo() throws RainbowException {

	}

	@Override
	protected boolean checkModelValidForCommand(M model) {
		return true;
	}





}
