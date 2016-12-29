package org.sa.rainbow.brass.effectors;

import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort;
import org.sa.rainbow.translator.effectors.EffectorManager;

/**
 * Created by schmerl on 12/27/2016.
 */
public class BRASSEffectorManager extends EffectorManager  {
    public BRASSEffectorManager () {
        super ("BRASS Effector Manager");
    }

    @Override
    public OperationResult publishOperation (IRainbowOperation cmd) {
        OperationResult badResult = new OperationResult ();
        badResult.result = Result.UNKNOWN;

        return badResult;

    }
}
