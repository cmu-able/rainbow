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

import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

/**
 * A class representing the Acme command to remove a server from SWIM
 * 
 * @author gmoreno
 */
public class RemoveServerCmd extends SwimAcmeModelCommand<IAcmeProperty> {

	/**
	 *  Target is the server add
	 *  
	 *  Note that since in SWIM we just remove a server (not a particular server)
	 *  the target must be the server currently enabled with the highest index.
	 *  
	 *  This command does not mark the server as not enabled. That happens
	 *  through the gauge when is reported by the probe.
	 *  
	 *  The target is the load balancer, and the server is the server to be removed
	 */
    public RemoveServerCmd (String commandName, AcmeModelInstance model, String target, String server) {
        super (commandName, model, target, server);
    }

    /**
     * Nothing is changed in the model, so the command is empty
     * 
     * @return the list of commands
     */
    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
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
