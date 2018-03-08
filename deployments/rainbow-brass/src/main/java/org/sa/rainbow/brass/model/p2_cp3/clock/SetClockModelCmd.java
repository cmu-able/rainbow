package org.sa.rainbow.brass.model.p2_cp3.clock;

import java.text.MessageFormat;
import java.util.List;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public abstract class SetClockModelCmd<T extends ClockedModel> extends AbstractRainbowModelOperation<String, T> {

	private String m_clockReference;

	public SetClockModelCmd(IModelInstance<T> model, String target,
			String clockReference) {
		super("setClockModel", model, target, clockReference);
		m_clockReference = clockReference;
	}

	@Override
	public String getResult() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<? extends IRainbowMessage> getGeneratedEvents(IRainbowMessageFactory messageFactory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void subExecute() throws RainbowException {
		IModelInstance mr = Rainbow.instance().getRainbowMaster().modelsManager().getModelInstance(ModelReference.fromString(m_clockReference));
		if (mr instanceof ClockModelInstance) {
			Clock clock = ((ClockModelInstance )mr).getModelInstance();
			getModelContext().getModelInstance().setClock(clock);
		}
		else {
			throw new RainbowException(MessageFormat.format("Model {0} is not a Clock!", m_clockReference));
		}

	}

	@Override
	protected void subRedo() throws RainbowException {
	}

	@Override
	protected void subUndo() throws RainbowException {
	}

	@Override
	protected boolean checkModelValidForCommand(ClockedModel model) {
		return true;
	}

}
