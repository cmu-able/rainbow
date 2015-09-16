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

public class RemoveClientCmd extends ZNNAcmeModelCommand<IAcmeComponent> {

    private IAcmeComponentDeleteCommand m_deleteClientCmd;

    public RemoveClientCmd (AcmeModelInstance model, String sys, String client) {
        super ("deleteClient", model, sys, client);
    }

    @Override
    public IAcmeComponent getResult () throws IllegalStateException {
        return m_deleteClientCmd.getComponent ();
    }

    @Override
    /**
     * <pre>
     * {@code
     *  set{role} roles = client/ports/attachedRoles;
     *  set{connector} attachedConnectors = select c in connectors | exists r in c.roles | contains (roles, r);
     *  set{connector} binaryConnectors = select c in attachedConnectors | size (r.roles) <= 2;
     *  set{connector} otherConnectors = select c in attachedConnectors | size (r.roles) > 2;
     *  set{port} portsToRemove = binaryConnectors/roles/attachedPorts;
     *  set{role} rolesToRemove = otherConnectors/r:roles[connected(r, server)]
     *  foreach p in portsToRemove {
     *     delete p;
     *  }
     *  foreach r in rolesToRemove {
     *    remove r;
     *  }
     *  foreach c in binaryConnectors {
     *    delete c;
     *  }
     *  delete client;
     * }
     * </pre>
     */
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeComponent client = getModelContext ().resolveInModel (getParameters ()[0], IAcmeComponent.class);
        IAcmeCommandFactory cf = getModel ().getCommandFactory ();
        if (client == null) throw new RainbowModelException ("Cannot delete unknown server " + getTarget ());
        IAcmeSystem system = ModelHelper.getAcmeSystem (client);
        List<IAcmeConnector> connectorsToRemove = new LinkedList<> ();
        List<IAcmeRole> rolesToRemove = new LinkedList<> ();
        List<IAcmePort> portsToRemove = new LinkedList<> ();
        for (IAcmePort port : client.getPorts ()) {
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
        m_deleteClientCmd = system.getCommandFactory ().componentDeleteCommand (client);
        cmds.add (m_deleteClientCmd);
        return cmds;
    }

}
