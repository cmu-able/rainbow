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

public class EnableServerCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    public EnableServerCmd (String commandName, AcmeModelInstance model, String target, String enable) {
        super (commandName, model, target, enable);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        IAcmeComponent server = getModelContext ().resolveInModel (getTarget (), IAcmeComponent.class);
        IAcmeProperty property = server.getProperty ("isArchEnabled");
        IAcmePropertyValue acmeVal = PropertyHelper.toAcmeVal (Boolean.valueOf (getParameters ()[0]));
        if (propertyValueChanging (property, acmeVal)) {
            m_command = property.getCommandFactory ().propertyValueSetCommand (property,
                    acmeVal);
            cmds.add (m_command);
        }
        return cmds;

    }


    @Override
    public IAcmeProperty getResult () throws IllegalStateException {
        return ((IAcmePropertyCommand )m_command).getProperty ();
    }

}
