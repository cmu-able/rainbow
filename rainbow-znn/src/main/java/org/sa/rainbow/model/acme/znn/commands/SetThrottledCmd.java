package org.sa.rainbow.model.acme.znn.commands;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
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

public class SetThrottledCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    public SetThrottledCmd (String commandName, AcmeModelInstance model, String target, String parameters) {
        super (commandName, model, target, parameters);
    }

    @Override
    public IAcmeProperty getResult () throws IllegalStateException {
        return ((IAcmePropertyCommand )m_command).getProperty ();
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeComponent server = getModelContext ().resolveInModel (getTarget (), IAcmeComponent.class);
        if (server == null)
            throw new RainbowModelException (MessageFormat.format (
                    "The server ''{0}'' could not be found in the model", getTarget ()));
        if (!server.declaresType ("ThrottlerT"))
            throw new RainbowModelException (MessageFormat.format (
                    "The server ''{0}'' is not of the right type. It does not have a property ''throttled''",
                    getTarget ()));
        String[] ips = getParameters ()[0].split (",");
        HashSet<String> ipSet = new HashSet<> ();
        if (!getParameters ()[0].isEmpty ()) {
            ipSet.addAll (Arrays.asList (ips));
        }
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        IAcmeProperty property = server.getProperty ("throttled");
        IAcmePropertyValue acmeVal = PropertyHelper.toAcmeVal (ipSet);
        if (propertyValueChanging (property, acmeVal)) {
            m_command = server.getCommandFactory ().propertyValueSetCommand (property, acmeVal);
            cmds.add (m_command);
        }
        return cmds;
    }

}
