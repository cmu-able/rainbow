package org.sa.rainbow.model.acme.znn.commands;

import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.core.type.IAcmeBooleanValue;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class SetCaptchaEnabledCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    public SetCaptchaEnabledCmd (String commandName, AcmeModelInstance model, String target, String enabled) {
        super (commandName, model, target, enabled);
    }

    @Override
    public IAcmeProperty getResult () throws IllegalStateException {
        return ((IAcmePropertyCommand )m_command).getProperty ();
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeComponent lb = getModelContext ().resolveInModel (getTarget (), IAcmeComponent.class);
        boolean enabeld = Boolean.valueOf (getParameters ()[0]);
        IAcmeProperty property = lb.getProperty ("captchaEnabled");
        IAcmeBooleanValue acmeVal = PropertyHelper.toAcmeVal (enabeld);
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        if (propertyValueChanging (property, acmeVal)) {
            m_command = lb.getCommandFactory ().propertyValueSetCommand (property,
                    acmeVal);
            cmds.add (m_command);
        }
        return cmds;
    }

}
