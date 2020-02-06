package org.sa.rainbow.swim.adaptation;

import org.sa.rainbow.core.adaptation.IEvaluable;

public abstract class SwimExtendedPlan implements IEvaluable {

	// Evaluates (executes) the plan, returning a result
	@Override
	public abstract Object evaluate(Object[] argsIn);

	// This is deprecated (?)
	@Override
	public long estimateAvgTimeCost() {
		// TODO Auto-generated method stub
		return 0;
	}

	public abstract boolean getOutcome();
}
