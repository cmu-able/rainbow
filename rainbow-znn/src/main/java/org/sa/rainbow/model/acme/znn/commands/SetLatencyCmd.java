package org.sa.rainbow.model.acme.znn.commands;

import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.core.type.IAcmeFloatValue;
import org.acmestudio.acme.element.IAcmeConnector;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class SetLatencyCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    private String m_httpConn;
    private double m_latency;

    public SetLatencyCmd (String commandName, AcmeModelInstance model, String conn, String latency) {
        super (commandName, model, conn, latency);
        m_httpConn = conn;
        m_latency = Double.valueOf (latency);
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeConnector httpConn = getModelContext ().resolveInModel (m_httpConn, IAcmeConnector.class);
        IAcmeProperty property = httpConn.getProperty ("latency");
        IAcmeFloatValue acmeVal = PropertyHelper.toAcmeVal (m_latency);
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        if (propertyValueChanging (property, acmeVal)) {
            m_command = httpConn.getCommandFactory ().propertyValueSetCommand (property,
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
