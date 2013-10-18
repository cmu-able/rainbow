package org.sa.rainbow.model.acme.znn.commands;

import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class SetReqServiceRateCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    private String m_server;
    private Float  m_serviceRate;

    public SetReqServiceRateCmd (String commandName, AcmeModelInstance model, String target, String serviceRate) {
        super (commandName, model, target, serviceRate);
        m_server = target;
        m_serviceRate = Float.valueOf (serviceRate);

    }

    @Override
    public IAcmeProperty getResult () throws IllegalStateException {
        return ((IAcmePropertyCommand )m_command).getProperty ();
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeComponent server = getModelContext ().resolveInModel (m_server, IAcmeComponent.class);
        m_command = server.getCommandFactory ().propertyValueSetCommand (server.getProperty ("reqServiceRate"),
                PropertyHelper.toAcmeVal (m_serviceRate));
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        cmds.add (m_command);
        return cmds;
    }

}
