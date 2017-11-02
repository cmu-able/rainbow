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

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

public class SetResponseTimeCmd extends ZNNAcmeModelCommand<IAcmeProperty> {

    private String m_client;
    private float          m_responseTime;

    public SetResponseTimeCmd (AcmeModelInstance model, String client, String rt) {
        super ("setResponseTime", model, client, rt);
        m_client = client;
        m_responseTime = Float.valueOf (rt);
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeComponent client = getModelContext ().resolveInModel (m_client, IAcmeComponent.class);
        if (client == null)
            throw new RainbowModelException (MessageFormat.format (
                    "The client ''{0}'' could not be found in the model", getTarget ()));
        if (!client.declaresType ("ClientT"))
            throw new RainbowModelException (MessageFormat.format (
                    "The client ''{0}'' is not of the right type. It does not have a property ''experRespTime''",
                    getTarget ()));
        IAcmeProperty expRT = client.getProperty ("experRespTime");
        m_command = client.getCommandFactory ().propertyValueSetCommand (expRT,
                PropertyHelper.toAcmeVal (m_responseTime));
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        cmds.add (m_command);
        return cmds;
    }

    @Override
    public IAcmeProperty getResult () {
        return ((IAcmePropertyCommand )m_command).getProperty ();
    }


}
