package org.sa.rainbow.evaluator.acme;

import org.sa.rainbow.model.acme.AcmeModelInstance;

public interface IArchEvaluator {

    /**
     * 
     * @return The model instance that is being used by the evaluator. Use getAcmeModel to get the IAcmeModel
     */
    public abstract AcmeModelInstance getModel ();

    /**
     * Requests that adaptation needs to be performed by the evaluator. 
     */
    public abstract void requestAdaptation();

}