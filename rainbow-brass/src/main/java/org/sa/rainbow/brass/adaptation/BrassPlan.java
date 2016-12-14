package org.sa.rainbow.brass.adaptation;

import org.sa.rainbow.core.adaptation.IEvaluable;

/**
 * Created by schmerl on 12/13/2016.
 */
public class BrassPlan implements IEvaluable {

    // Evaluates (executes) the plan, returning a result
    @Override
    public Object evaluate (Object[] argsIn) {
        return null;
    }

    // This is deprecated
    @Override
    public long estimateAvgTimeCost () {
        return 0;
    }
}
