package org.sa.rainbow.model.acme.znn.commands;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.core.exception.AcmeException;
import org.acmestudio.acme.core.resource.RegionManager;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmePort;
import org.acmestudio.acme.environment.error.AcmeError;
import org.acmestudio.acme.model.IAcmeCommandFactory;
import org.acmestudio.acme.model.command.*;
import org.acmestudio.acme.model.util.core.UMStringValue;
import org.acmestudio.acme.rule.AcmeSet;
import org.acmestudio.acme.rule.node.IExpressionNode;
import org.acmestudio.acme.type.verification.NodeScopeLookup;
import org.acmestudio.acme.type.verification.RuleTypeChecker;
import org.acmestudio.standalone.resource.StandaloneLanguagePackHelper;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Created by schmerl on 2/4/2016.
 */
public class ConnectServerCmd extends ZNNAcmeModelCommand<IAcmeComponent> {

    private final String m_lb;
    private final String m_port;
    private final String m_host;
    private final String m_name;

    private static final String DISCONNECTED_QUERY = "/self/components:!ServerT[!isArchEnabled and " +
            "deploymentLocation==\"\"]";
    private static IExpressionNode s_DisconnectedQueryExpr;

    private IAcmeComponent m_server;

    public ConnectServerCmd (AcmeModelInstance model, String lb, String name, String host, String
            port) {
        super ("connectServer", model, lb, name, host, port);
        m_lb = lb;
        m_name = name;
        m_host = host;
        m_port = port;


    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {


        List<IAcmeCommand<?>> cmds = new LinkedList<> ();

        IAcmeComponent lb = getModelContext ().resolveInModel (m_lb, IAcmeComponent.class);

        IAcmeCommandFactory cf = getModel ().getCommandFactory ();

        // If "name" is an unconnected and disabled server in the model, use it.
        // If not, find a disconnected server and attach it
        IAcmeComponent srv = getModelContext ().getModelInstance ().getComponent (m_name);

        if (srv != null) {
            if (isConnected (srv)) {
                srv = null;
            }
        }
        if (srv == null) {
            // Find an unenabled server
            if (s_DisconnectedQueryExpr == null) {
                try {
                    s_DisconnectedQueryExpr = StandaloneLanguagePackHelper.defaultLanguageHelper ()
                            .designRuleExpressionFromString (DISCONNECTED_QUERY, new RegionManager ());
                } catch (Exception e) {
                    throw new RainbowModelException (String.format ("Could not construct the query expression '%s'",
                                                                    DISCONNECTED_QUERY));
                }
            }
            Stack<AcmeError> errorStack = new Stack<AcmeError> ();
            try {
                Object result = RuleTypeChecker.evaluateAsAny (getModel (), null, s_DisconnectedQueryExpr,
                                                               errorStack, new
                                                                       NodeScopeLookup ());
                if (errorStack.isEmpty () && result instanceof AcmeSet) {
                    AcmeSet set = ((AcmeSet) result);
                    if (!set.getValues ().isEmpty ()) {
                        final Object first = set.getValues ().iterator ().next ();
                        if (first instanceof IAcmeComponent)
                            srv = (IAcmeComponent) first;
                    }

                }
            } catch (AcmeException e) {
                throw new RainbowModelException (e);
            }
        }


        if (srv != null) {
            m_server = srv;
            final IAcmePortCreateCommand httpCreateCommand = cf.portCreateCommand (srv, "http", ZNNConstants
                                                                                           .HTTP_PORT,
                                                                                   ZNNConstants.HTTP_PORT);
            String lbName = ModelHelper.getUniqueName (getModel (), "proxyconn");
            // Connector proxyconn : ProxyConnT = new ProxyConnT;
            final IAcmeConnectorCreateCommand proxyConnCreateCmd = cf.connectorCreateCommand (getModel (), lbName,
                                                                                              ZNNConstants
                                                                                                      .HTTP_CONN_T,
                                                                                              ZNNConstants.HTTP_CONN_T);
            String fwd = ModelHelper.getUniqueName (lb, "fwd");
            // m_lb = m_lb extended with {
            //   port fwd : ProxyForwardPortT = new ProxyForwardPortT;
            // }
            final IAcmePortCreateCommand lbPortCmd = cf.portCreateCommand (lb, fwd, ZNNConstants.PROXY_FORWARD_PORT,
                                                                           ZNNConstants.PROXY_FORWARD_PORT);
            // attachment m_lb.fwd to proxyconn.req;
            final IAcmeAttachmentCommand attachLBEnd = cf.attachmentCreateCommand (getModel (),
                                                                                   lb.getName () + "." + fwd,
                                                                                   lbName + ".req");
            // attachment <m_name>.http to proxyconn.rec;
            final IAcmeAttachmentCommand attachServerEnd = cf.attachmentCreateCommand (getModel (), m_name + ".http",
                                                                                       lbName + ".rec");

            final IAcmePropertyCommand setLocation = cf.propertyCreateCommand (srv, "deploymentLocation",
                                                                               "String",
                                                                               new UMStringValue (m_host));
            final IAcmePropertyCreateCommand setPort = cf.propertyCreateCommand (srv, "httpPort", "String",
                                                                                 new UMStringValue (m_port));

            cmds.add (proxyConnCreateCmd);
            cmds.add (lbPortCmd);
            cmds.add (attachLBEnd);
            cmds.add (attachServerEnd);
            cmds.add (setLocation);
            cmds.add (setPort);

            return cmds;
        }
        throw new RainbowModelException ("There is no server that can be connected");
    }

    private boolean isConnected (IAcmeComponent srv) {
        final Set<? extends IAcmePort> ports = srv.getPorts ();
        if (ports.isEmpty ()) return false;
        for (IAcmePort port : ports) {
            if (!ModelHelper.getAcmeSystem (srv).getAttachments (port).isEmpty ())
                return true;
        }
        return false;
    }

    @Override
    public IAcmeComponent getResult () throws IllegalStateException {
        return m_server;
    }
}
