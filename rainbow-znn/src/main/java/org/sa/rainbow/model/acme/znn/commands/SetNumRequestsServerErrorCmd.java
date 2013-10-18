package org.sa.rainbow.model.acme.znn.commands;

import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.element.IAcmeConnector;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class SetNumRequestsServerErrorCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    private String m_server;
    private float          m_requests;

    public SetNumRequestsServerErrorCmd (String commandName, AcmeModelInstance model, String server,
            String requests) {
        super (commandName, model, server, requests);
        m_server = server;
        m_requests = Integer.valueOf (requests);
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeConnector httpConn = getModelContext ().resolveInModel (m_server, IAcmeConnector.class);
        m_command = httpConn.getCommandFactory ().propertyValueSetCommand (httpConn.getProperty ("numReqsServerError"),
                PropertyHelper.toAcmeVal (m_requests));
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        cmds.add (m_command);
        return cmds;
    }

    @Override
    public IAcmeProperty getResult () {
        return ((IAcmePropertyCommand )m_command).getProperty ();
    }


}
