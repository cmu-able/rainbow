package org.sa.rainbow.model.acme.znn.commands;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;

public class SetResponseTimeCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    private String m_client;
    private float          m_responseTime;

    public SetResponseTimeCmd (String commandName, IAcmeSystem system, String client, String rt) {
        super (commandName, system, client, rt);
        m_client = client;
        m_responseTime = Float.valueOf (rt);
    }

    @Override
    protected void doConstructCommand () throws RainbowModelException {
        IAcmeComponent client = resolveInModel (m_client, IAcmeComponent.class);
        m_command = client.getCommandFactory ().propertyValueSetCommand (client.getProperty ("experRespTime"),
                PropertyHelper.toAcmeVal (m_responseTime));
    }

    @Override
    protected IAcmeProperty getResult () {
        return ((IAcmePropertyCommand )m_command).getProperty ();
    }


}
