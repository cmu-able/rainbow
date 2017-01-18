package org.sa.rainbow.brass.adaptation;

import org.sa.rainbow.core.adaptation.IEvaluable;

/**
 * Created by schmerl on 12/13/2016.
 */
public abstract class BrassPlan implements IEvaluable {

    // Evaluates (executes) the plan, returning a result
    @Override
    public abstract Object evaluate (Object[] argsIn);

    // This is deprecated
    @Override
    public long estimateAvgTimeCost () {
        return 0;
    }
}
