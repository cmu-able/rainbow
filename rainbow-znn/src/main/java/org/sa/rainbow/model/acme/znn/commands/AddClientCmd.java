package org.sa.rainbow.model.acme.znn.commands;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.core.type.IAcmeStringValue;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.IAcmeCommandFactory;
import org.acmestudio.acme.model.command.IAcmeAttachmentCommand;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmeComponentCreateCommand;
import org.acmestudio.acme.model.command.IAcmeConnectorCreateCommand;
import org.acmestudio.acme.model.command.IAcmePortCreateCommand;
import org.acmestudio.acme.model.command.IAcmePropertyCreateCommand;
import org.acmestudio.acme.model.util.core.UMStringValue;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

/**
 * Creates a new client at the IP indicated, if it doesn't already exist
 * 
 * @author Bradley Schmerl: schmerl
 *
 */
public class AddClientCmd extends ZNNAcmeModelCommand<IAcmeComponent> {

    private static final List<String> HTTP_PORT     = Arrays.asList ("HttpPortT");
    private static final List<String> HTTP_CONN     = Arrays.asList ("HttpConnT");
    private static final List<String> HTTP_REQ_PORT = Arrays.asList ("HttpReqPortT");
    private static final List<String> CLIENT_COMP   = Arrays.asList ("ZNewsClientT");

    private IAcmeComponent              m_client;
    private String                      m_lb;
    private String                      m_clientIP;
    private IAcmeComponentCreateCommand m_clientCmd;

    /**
     * 
     * @param commandName
     *            The name for the command
     * @param model
     *            The model in which the new client may be created
     * @param lb
     *            The load balancer that the client will be attached to
     * @param clientIP
     *            The IP of the client to create
     */
    public AddClientCmd (String commandName, AcmeModelInstance model, String sys, String lb, String clientIP) {
        super (commandName, model, sys, lb, clientIP);
        m_lb = lb;
        m_clientIP = clientIP;
    }

    @Override
    public IAcmeComponent getResult () throws IllegalStateException {
        return m_clientCmd != null ? m_clientCmd.getComponent () : m_client;
    }

    /**
     * Executes a list of commands with the following operations
     * 
     * <pre>
     * {@code
     *   ClientT client = /self/components:!ClientT[deploymentLocation==clientIP]/next;
     *   if (client == null) {
     *     client = new ClientT();
     *     http = new HttpReqPortT() in client;
     *     httpConn = new HttpConnT ();
     *     http = new HttpPortT() in lb;
     *     client.deploymentLocation = clientIP;
     *     attach client.http to httpConn.req;
     *     attach lb.http to httpConn.rec;
     *   }
     * }
     * </pre>
     */

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        Set<? extends IAcmeComponent> components = getModel ().getComponents ();
        IAcmeComponent client = null;
        for (Iterator i = components.iterator (); i.hasNext () && client == null;) {
            IAcmeComponent comp = (IAcmeComponent )i.next ();
            if (comp.declaresType ("ClientT")) {
                IAcmeProperty prop = comp.getProperty ("deploymentLocation");
                if (prop != null && prop.getValue () instanceof IAcmeStringValue) {
                    IAcmeStringValue dl = (IAcmeStringValue )prop.getValue ();
                    if (dl.getValue ().equals (m_clientIP)) {
                        client = comp;
                    }

                }
            }
        }
        m_client = client;
        if (m_client == null) {
            IAcmeCommandFactory cf = getModel ().getCommandFactory ();
            String clientName = ModelHelper.getUniqueName (getModel (), "Client");
            m_clientCmd = cf.componentCreateCommand (getModel (), clientName, CLIENT_COMP, CLIENT_COMP);
            IAcmePortCreateCommand httpCmd = cf.portCreateCommand (m_clientCmd, "http", HTTP_PORT, HTTP_PORT);
            String connName = ModelHelper.getUniqueName (getModel (), "httpConn");
            IAcmeConnectorCreateCommand httpConnCmd = cf.connectorCreateCommand (getModel (), connName, HTTP_CONN,
                    HTTP_CONN);
            IAcmeComponent lb = getModelContext ().resolveInModel (m_lb, IAcmeComponent.class);
            if (lb == null) throw new RainbowModelException (
                    MessageFormat.format ("Could not find the load balancer ''{0}''.", m_lb));
            String httpPortName = ModelHelper.getUniqueName (lb, "http");
            IAcmePortCreateCommand lbPortCmd = cf.portCreateCommand (lb, httpPortName, HTTP_PORT, HTTP_PORT);
            IAcmePropertyCreateCommand dlCmd = cf.propertyCreateCommand (m_clientCmd, "deploymentLocation", "String",
                    new UMStringValue (m_clientIP));
            IAcmeAttachmentCommand att1Cmd = cf.attachmentCreateCommand (getModel (), clientName + ".http",
                    connName + ".req");
            IAcmeAttachmentCommand att2Cmd = cf.attachmentCreateCommand (getModel (), m_lb + "." + httpPortName,
                    connName + ".rec");
            List<IAcmeCommand<?>> cmds = new LinkedList<> ();
            cmds.add (m_clientCmd);
            cmds.add (httpCmd);
            cmds.add (httpConnCmd);
            cmds.add (lbPortCmd);
            cmds.add (dlCmd);
            cmds.add (att1Cmd);
            cmds.add (att2Cmd);

            return cmds;

        }
        else
            return Collections.<IAcmeCommand<?>> emptyList ();
    }

}
