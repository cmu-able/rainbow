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
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

import java.util.LinkedList;
import java.util.List;

public class SetLoadCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    private String m_server;
    private float          m_load;

    public SetLoadCmd (AcmeModelInstance model, String server, String load) {
        super ("setLoad", model, server, load);
        m_server = server;
        m_load = Float.valueOf (load);
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeComponent server = getModelContext ().resolveInModel (m_server, IAcmeComponent.class);
        m_command = server.getCommandFactory ().propertyValueSetCommand (server.getProperty ("load"),
                PropertyHelper.toAcmeVal (m_load));
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        cmds.add (m_command);
        return cmds;
    }

    @Override
    public IAcmeProperty getResult () {
        return ((IAcmePropertyCommand )m_command).getProperty ();
    }


}
