/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.sa.rainbow.model.acme.znn.commands;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.element.property.IAcmePropertyValue;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * This command sets the blackholed property of the load balancer with the ips of the clients that are blackholed (or
 * blacklisted).
 * 
 * @author Bradley Schmerl: schmerl
 *
 */
public class SetBlackholedCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    /**
     *  @param model
     * @param target
     *            the load balancer
     * @param ipSet
     */
    public SetBlackholedCmd (AcmeModelInstance model, String target, String ipSet) {
        super ("setBlackholed", model, target, ipSet);
    }

    @Override
    public IAcmeProperty getResult () throws IllegalStateException {
        return ((IAcmePropertyCommand )m_command).getProperty ();
    }


    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        // Resolve and check the load balancer
        IAcmeComponent lb = getModelContext ().resolveInModel (getTarget (), IAcmeComponent.class);
        if (lb == null)
            throw new RainbowModelException (MessageFormat.format (
                    "The load balancer ''{0}'' could not be found in the model", getTarget ()));
        if (!lb.declaresType ("BlackholerT"))
            throw new RainbowModelException (MessageFormat.format (
                    "The server ''{0}'' is not of the right type. It does not have a property ''blackholed''",
                    getTarget ()));

        // Form the IP set
        String[] split = getParameters ()[0].split (",");
        HashSet<String> set = new HashSet<> ();
        if (!getParameters ()[0].isEmpty ()) {
            set.addAll (Arrays.asList (split));
        }
        IAcmeProperty property = lb.getProperty ("blackholed");
        if (property == null)
            throw new RainbowModelException (
                    MessageFormat
                    .format (
                            "The load balancer ''{0}'' does not have a property called ''blackholed''. This should not happen.",
                            lb.getQualifiedName ()));
        IAcmePropertyValue acmeVal = PropertyHelper.toAcmeVal (set);
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        if (propertyValueChanging (property, acmeVal)) {
            m_command = lb.getCommandFactory ().propertyValueSetCommand (property, acmeVal);
            cmds.add (m_command);
        }
        return cmds;
    }

}
