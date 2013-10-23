package org.sa.rainbow.model.acme.znn.commands;

import java.util.Collections;
import java.util.List;

import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class ForceReauthenticationCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    public ForceReauthenticationCmd (String commandName, AcmeModelInstance model, String target) {
        super (commandName, model, target, new String[0]);
    }

    @Override
    public IAcmeProperty getResult () throws IllegalStateException {
        return null;
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        return Collections.<IAcmeCommand<?>> emptyList ();
    }

}
