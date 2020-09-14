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
package org.sa.rainbow.model.acme.swim.commands;

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
 * A class representing the Acme command to add a server to SWIM
 * 
 * @author gmoreno
 */
public class AddServerCmd extends SwimAcmeModelCommand<IAcmeProperty> {

    private static final String PROPERTY = "expectedActivationTime";
    
    private String m_server;


	/**
	 *  Target is the server add
	 *  
	 *  Note that since in SWIM we just add a server (not add a particular server)
	 *  the target must be the server not currently enabled with the lowest index.
	 *  
	 *  This command does not mark the server as enabled. It only sets its expected
	 *  activation time. Enablement happens through the gauge when is reported by the
	 *  probe.
	 *  
	 *  The target is the load balancer, and the server is the server to be added
	 */
    public AddServerCmd (AcmeModelInstance model, String target, String server) {
        super ("addServer", model, target, server);
        m_server = server;
    }

    /**
     * Constructs the list of commands for setting the expectedActivationTime property
     * 
     * @return the list of commands
     * @throws RainbowModelException
     *             when the server cannot be found, is of the incorrect type, or the argument is malformed.
     */
    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        IAcmeComponent server = getModelContext ().resolveInModel (m_server, IAcmeComponent.class);
        if (server == null)
            throw new RainbowModelException (MessageFormat.format (
                    "The server ''{0}'' could not be found in the model", m_server));
        IAcmeProperty property = server.getProperty (PROPERTY);
        if (property == null) {
			throw new RainbowModelException(MessageFormat.format(
					"The server ''{0}'' is not of the right type. It does not have a property ''{1}''",
					m_server, PROPERTY));
        }
        
        IAcmeProperty prop = getModelContext().getModelInstance().getProperty("ADD_SERVER_LATENCY_SEC");
    	int addServerLatency = ((Integer) PropertyHelper.toJavaVal(prop.getValue())).intValue();
    	int expectedActivationTime = (int)(System.currentTimeMillis() / 1000) + addServerLatency;
        
        try {
            IAcmePropertyValue acmeVal = PropertyHelper.toAcmeVal (Integer.valueOf(expectedActivationTime));
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
