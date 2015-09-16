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
import java.util.LinkedList;
import java.util.List;

/**
 * A class representing the Acme command to enable a server in Znn. Currently, this is modeled by setting the property
 * "isArchEnabled" on the model.
 * 
 * @author Bradley Schmerl: schmerl
 *
 */
public class EnableServerCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    // Target is the server to enable, enable is whether to set it as enabled or not "true" or "false"
    public EnableServerCmd (AcmeModelInstance model, String target, String enable) {
        super ("enableServer", model, target, enable);
    }

    /**
     * Constructs the list of commands for enabling the server by setting the isArchEnabled property
     * 
     * @return the list of commands
     * @throws RainbowModelException
     *             when the server cannot be found, is of the incorrect type, or the argument is malformed.
     */
    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        IAcmeComponent server = getModelContext ().resolveInModel (getTarget (), IAcmeComponent.class);
        if (server == null)
            throw new RainbowModelException (MessageFormat.format (
                    "The server ''{0}'' could not be found in the model", getTarget ()));
        if (!server.declaresType ("ArchElementT"))
            throw new RainbowModelException (MessageFormat.format (
                    "The server ''{0}'' is not of the right type. It does not have a property ''isArchEnabled''",
                    getTarget ()));
        IAcmeProperty property = server.getProperty ("isArchEnabled");
        try {
            IAcmePropertyValue acmeVal = PropertyHelper.toAcmeVal (Boolean.valueOf (getParameters ()[0]));
            if (propertyValueChanging (property, acmeVal)) {
                m_command = property.getCommandFactory ().propertyValueSetCommand (property,
                        acmeVal);
                cmds.add (m_command);
            }
        }
        catch (IllegalArgumentException e) {
            throw new RainbowModelException (e.getMessage (), e);
        }
        return cmds;

    }


    /**
     * 
     * @return the property after the command has been executed
     * @throws IllegalStateException
     */
    @Override
    public IAcmeProperty getResult () throws IllegalStateException {
        return ((IAcmePropertyCommand )m_command).getProperty ();
    }

}
