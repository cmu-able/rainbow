package org.sa.rainbow.model.acme.znn.commands;

import java.text.MessageFormat;
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

/**
 * This commands sets the model property indicating whether captcha is enabled.
 * 
 * @author Bradley Schmerl: schmerl
 *
 */
public class SetCaptchaEnabledCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    /**
     * 
     * @param commandName
     * @param model
     * @param target
     *            The load balancer to set the property on
     * @param enabled
     *            "true" if captcha is enabled, "false" otherwise
     */
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
        if (lb == null)
            throw new RainbowModelException (MessageFormat.format (
                    "The load balancer ''{0}'' could not be found in the model", getTarget ()));
        if (!lb.declaresType ("CaptchaRedirectT"))
            throw new RainbowModelException (MessageFormat.format (
                    "The server ''{0}'' is not of the right type. It does not have a property ''captchaEnabled''",
                    getTarget ()));

        List<IAcmeCommand<?>> cmds;
        try {
            boolean enabled = Boolean.valueOf (getParameters ()[0]);
            IAcmeProperty property = lb.getProperty ("captchaEnabled");
            IAcmeBooleanValue acmeVal = PropertyHelper.toAcmeVal (enabled);
            cmds = new LinkedList<> ();
            if (propertyValueChanging (property, acmeVal)) {
                m_command = lb.getCommandFactory ().propertyValueSetCommand (property, acmeVal);
                cmds.add (m_command);
            }
        }
        catch (Exception e) {
            throw new RainbowModelException (MessageFormat.format (
                    "The parameter ''{0}'' cannot be parsed as a boolean", getParameters ()[0]));
        }
        return cmds;
    }

}
