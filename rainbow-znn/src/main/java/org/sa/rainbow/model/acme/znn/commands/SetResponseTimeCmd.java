package org.sa.rainbow.model.acme.znn.commands;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class SetResponseTimeCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    private String m_client;
    private float          m_responseTime;

    public SetResponseTimeCmd (String commandName, AcmeModelInstance model, String client, String rt) {
        super (commandName, model, client, rt);
        m_client = client;
        m_responseTime = Float.valueOf (rt);
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeComponent client = getModelContext ().resolveInModel (m_client, IAcmeComponent.class);
        if (client == null)
            throw new RainbowModelException (MessageFormat.format (
                    "The client ''{0}'' could not be found in the model", getTarget ()));
        if (!client.declaresType ("ClientT"))
            throw new RainbowModelException (MessageFormat.format (
                    "The client ''{0}'' is not of the right type. It does not have a property ''experRespTime''",
                    getTarget ()));
        IAcmeProperty expRT = client.getProperty ("experRespTime");
        m_command = client.getCommandFactory ().propertyValueSetCommand (expRT,
                PropertyHelper.toAcmeVal (m_responseTime));
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        cmds.add (m_command);
        return cmds;
    }

    @Override
    public IAcmeProperty getResult () {
        return ((IAcmePropertyCommand )m_command).getProperty ();
    }


}
