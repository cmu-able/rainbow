package org.sa.rainbow.brass.model.p2_cp3.rainbowState;

import org.sa.rainbow.brass.model.AbstractSimpleRainbowModelOperation;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;

public class ClearModelProblemsCmd
		extends AbstractSimpleRainbowModelOperation<Boolean, RainbowState> {

	public ClearModelProblemsCmd(String commandName, RainbowStateModelInstance model,
			String target, String empty) {
		super(commandName, "clearModelProblems", model, target, empty);
	}

	@Override
	protected void subExecute() throws RainbowException {
		getModelContext().getModelInstance().clearModelProblems();
		setResult(getModelContext().getModelInstance().getProblems().isEmpty());
	}

}
