package org.sa.rainbow.model.acme.znn.commands;

import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.acmestudio.acme.model.util.core.UMFloatingPointValue;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

public class SetClientRequestRateCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    private String m_client;
    private Float  m_reqRate;

    public SetClientRequestRateCmd (AcmeModelInstance model, String client, String reqRate) {
        super ("setClientRequestRate", model, client, reqRate);
        m_client = client;
        m_reqRate = Float.valueOf (reqRate);
    }

    @Override
    public IAcmeProperty getResult () throws IllegalStateException {
        return ((IAcmePropertyCommand )m_command).getProperty ();
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeComponent client = getModelContext ().resolveInModel (m_client, IAcmeComponent.class);
        if (client == null)
            throw new RainbowModelException (MessageFormat.format ("Could not find client ''{0}'' in model", m_client));
        m_command = client.getCommandFactory ().propertyValueSetCommand (client.getProperty ("reqRate"),
                new UMFloatingPointValue (m_reqRate));
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        cmds.add (m_command);
        return cmds;
    }

}
