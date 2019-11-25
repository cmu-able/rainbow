package org.sa.rainbow.brass.model.p2_cp3.rainbowState;

import java.util.EnumSet;

import org.sa.rainbow.brass.model.AbstractSimpleRainbowModelOperation;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState.CP3ModelState;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;

public class RemoveModelProblemCmd
		extends AbstractSimpleRainbowModelOperation<java.util.EnumSet<CP3ModelState>, RainbowState> {

	private CP3ModelState m_problem;

	public RemoveModelProblemCmd(String commandName, RainbowStateModelInstance model,
			String target, String problem) {
		super(commandName, "removeModelProblem", model, target, problem);
		m_problem = CP3ModelState.valueOf(problem);
	}

	@Override
	protected void subExecute() throws RainbowException {
		getModelContext().getModelInstance().removeModelProblem(m_problem);
		setResult(EnumSet.copyOf(getModelContext().getModelInstance().getProblems()));

	}

}
