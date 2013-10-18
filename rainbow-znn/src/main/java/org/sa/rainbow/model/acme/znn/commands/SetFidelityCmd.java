package org.sa.rainbow.model.acme.znn.commands;

import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.core.type.IAcmeFloatValue;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class SetFidelityCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    private String m_server;
    private float  m_fidelity;

    public SetFidelityCmd (String commandName, AcmeModelInstance model, String server, String fidelity) {
        super (commandName, model, server, fidelity);
        m_server = server;
        m_fidelity = Integer.valueOf (fidelity);
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeComponent server = getModelContext ().resolveInModel (m_server, IAcmeComponent.class);
        IAcmeProperty property = server.getProperty ("fidelity");
        IAcmeFloatValue acmeVal = PropertyHelper.toAcmeVal (m_fidelity);
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        if (propertyValueChanging (property, acmeVal)) {
            m_command = server.getCommandFactory ().propertyValueSetCommand (property,
                    acmeVal);
            cmds.add (m_command);
        }
        return cmds;
    }

    @Override
    public IAcmeProperty getResult () {
        return ((IAcmePropertyCommand )m_command).getProperty ();
    }


}
