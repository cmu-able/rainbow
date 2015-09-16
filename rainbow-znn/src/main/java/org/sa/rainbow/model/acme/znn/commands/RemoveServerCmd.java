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

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.*;
import org.acmestudio.acme.model.IAcmeCommandFactory;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmeComponentDeleteCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

import java.util.LinkedList;
import java.util.List;

/**
 * Removes the a server from the model, and deletes any binary connectors that are connected to it
 * 
 * @author Bradley Schmerl: schmerl
 *
 */
public class RemoveServerCmd extends ZNNAcmeModelCommand<IAcmeComponent> {

    private IAcmeComponentDeleteCommand m_deleteServerCmd;

    public RemoveServerCmd (AcmeModelInstance model, String server) {
        super ("removeServer", model, server);
    }

    @Override
    public IAcmeComponent getResult () throws IllegalStateException {
        return m_deleteServerCmd.getComponent ();
    }

    /**
     * <pre>
     * {@code
     * set{roles} roles = server/ports/attachedRoles;
     * set{connector} attachedConnectors = select c in connectors | exists r in c.roles | contains (roles, r);
     * set{connector} binaryConnectors = select c in attachedConnectors | size (r.roles) <= 2;
     * set{connector} otherConnectors = select c in attachedConnectors | size (r.roles) > 2;
     * set{port} portsToRemove = binaryConnectors/roles/attachedPorts;
     * set{role} rolesToRemove = otherConnectors/r:roles[connected(r, server)]
     * foreach p in  portsToRemove {
     *   delete p;
     * }
     * foreach r in rolesToRemove {
     *   delete r;
     * }
     * foreach c in binaryConnectors {
     *   delete c;
     * }
     * delete server;
     * </pre>
     */
    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeComponent server = getModelContext ().resolveInModel (getTarget (), IAcmeComponent.class);
        IAcmeCommandFactory cf = getModel ().getCommandFactory ();
        if (server == null) throw new RainbowModelException ("Cannot delete unknown server " + getTarget ());
        IAcmeSystem system = ModelHelper.getAcmeSystem (server);
        List<IAcmeConnector> connectorsToRemove = new LinkedList<> ();
        List<IAcmeRole> rolesToRemove = new LinkedList<> ();
        List<IAcmePort> portsToRemove = new LinkedList<> ();
        for (IAcmePort port : server.getPorts ()) {
            for (IAcmeAttachment att : system.getAttachments (port)) {
                IAcmeConnector conn = (IAcmeConnector )att.getRole ().getParent ();
                if (conn.getRoles ().size () <= 2) {
                    connectorsToRemove.add (conn);
                    for (IAcmeRole role : conn.getRoles ()) {
                        if (role == att.getRole ()) {
                            continue;
                        }
                        for (IAcmeAttachment att2 : system.getAttachments (role)) {
                            portsToRemove.add (att2.getPort ());
                        }
                    }
                }
                else {
                    rolesToRemove.add (att.getRole ());
                }
            }
        }
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        for (IAcmeConnector conn : connectorsToRemove) {
            cmds.add (system.getCommandFactory ().connectorDeleteCommand (conn));
        }
        for (IAcmeRole role : rolesToRemove) {
            cmds.add (system.getCommandFactory ().roleDeleteCommand (role));
        }
        for (IAcmePort port : portsToRemove) {
            cmds.add (system.getCommandFactory ().portDeleteCommand (port));
        }
        m_deleteServerCmd = system.getCommandFactory ().componentDeleteCommand (server);
        cmds.add (m_deleteServerCmd);
        return cmds;
    }

}
