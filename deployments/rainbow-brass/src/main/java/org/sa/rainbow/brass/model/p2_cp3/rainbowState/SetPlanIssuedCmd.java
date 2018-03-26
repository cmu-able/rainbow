package org.sa.rainbow.brass.model.p2_cp3.rainbowState;

import java.util.List;

import org.sa.rainbow.brass.model.AbstractSimpleRainbowModelOperation;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class SetPlanIssuedCmd extends AbstractSimpleRainbowModelOperation<Boolean, RainbowState> {


	private boolean m_issued;

	public SetPlanIssuedCmd(RainbowStateModelInstance model, String target,
			String issued) {
		super("setPlanIssued", "setPlanIssued", model, target, issued);
		m_issued = Boolean.parseBoolean(issued);
	}

	@Override
	protected void subExecute() throws RainbowException {
		getModelContext().getModelInstance().setPlanIssued(m_issued);
		setResult(getModelContext().getModelInstance().isPlanIssued());
	}

	

}
