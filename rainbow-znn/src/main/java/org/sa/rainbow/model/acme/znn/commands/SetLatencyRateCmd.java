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

public class SetLatencyRateCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    private String m_httpConn;
    private double m_latencyRate;

    public SetLatencyRateCmd (String commandName, AcmeModelInstance model, String conn, String latencyRate) {
        super (commandName, model, conn, latencyRate);
        m_httpConn = conn;
        m_latencyRate = Double.valueOf (latencyRate);
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeConnector httpConn = getModelContext ().resolveInModel (m_httpConn, IAcmeConnector.class);
        m_command = httpConn.getCommandFactory ().propertyValueSetCommand (httpConn.getProperty ("latencyRate"),
                PropertyHelper.toAcmeVal (m_latencyRate));
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        cmds.add (m_command);
        return cmds;
    }

    @Override
    public IAcmeProperty getResult () {
        return ((IAcmePropertyCommand )m_command).getProperty ();
    }


}
