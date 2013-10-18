package org.sa.rainbow.model.acme.znn.commands;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.model.acme.AcmeModelCommand;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public abstract class ZNNAcmeModelCommand<T> extends AcmeModelCommand<T> {

    public ZNNAcmeModelCommand (String commandName, AcmeModelInstance model, String target,
            String... parameters) {
        super (commandName, model, target, parameters);
    }


    @Override
    protected boolean checkModelValidForCommand (IAcmeSystem model) {
        return model.declaresType ("ZNewsFam");
    }

}
