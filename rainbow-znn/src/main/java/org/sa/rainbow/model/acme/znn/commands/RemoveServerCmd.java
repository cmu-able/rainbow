package org.sa.rainbow.model.acme.znn.commands;

import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeAttachment;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeConnector;
import org.acmestudio.acme.element.IAcmePort;
import org.acmestudio.acme.element.IAcmeRole;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.model.IAcmeCommandFactory;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmeComponentDeleteCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

/**
 * Removes the a server from the model, and deletes any binary connectors that are connected to it
 * 
 * @author Bradley Schmerl: schmerl
 *
 */
public class RemoveServerCmd extends ZNNAcmeModelCommand<IAcmeComponent> {

    private IAcmeComponentDeleteCommand m_deleteServerCmd;

    public RemoveServerCmd (String commandName, AcmeModelInstance model, String server) {
        super (commandName, model, server, new String[0]);
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
        List<IAcmeConnector> connectorsToRemove = new LinkedList<IAcmeConnector> ();
        List<IAcmeRole> rolesToRemove = new LinkedList<IAcmeRole> ();
        List<IAcmePort> portsToRemove = new LinkedList<IAcmePort> ();
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
