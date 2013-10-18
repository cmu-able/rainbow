package org.sa.rainbow.model.acme.znn.commands;

import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.element.property.IAcmePropertyValue;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class SetByteServiceRateCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    private String m_server;
    private Float  m_serviceRate;

    public SetByteServiceRateCmd (String commandName, AcmeModelInstance model, String target, String serviceRate) {
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
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        IAcmeProperty property = server.getProperty ("byteServiceRate");
        IAcmePropertyValue newVal = PropertyHelper.toAcmeVal (m_serviceRate);
        if (propertyValueChanging (property, newVal)) {
            m_command = server.getCommandFactory ().propertyValueSetCommand (property,
                    newVal);
            cmds.add (m_command);
        }
        return cmds;
    }

}
