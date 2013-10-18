package org.sa.rainbow.model.acme.znn.commands;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.model.IAcmeCommandFactory;
import org.acmestudio.acme.model.command.IAcmeAttachmentCommand;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmeComponentCreateCommand;
import org.acmestudio.acme.model.command.IAcmeConnectorCreateCommand;
import org.acmestudio.acme.model.command.IAcmePortCreateCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class NewServerCmd extends ZNNAcmeModelCommand<IAcmeComponent> {

    private static final List<String>   SERVER_TYPE        = Arrays.asList ("ServerT");

    private static final List<String>   HTTP_CONN_T        = Arrays.asList ("ProxyConnT");

    private static final List<String>   PROXY_FORWARD_PORT = Arrays.asList ("ProxyForwardPortT");

    private static final List<String>   HTTP_PORT          = Arrays.asList ("HttpPortT");

    private String                      m_name;
    public String                       m_lb;

    private IAcmeComponentCreateCommand m_serverCommand;

    public NewServerCmd (String commandName, AcmeModelInstance model, String lb, String name) {
        super (commandName, model, lb, name);
        m_lb = lb;
        m_name = name;
    }


    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {

        IAcmeComponent lb = getModelContext ().resolveInModel (m_lb, IAcmeComponent.class);

        IAcmeCommandFactory cf = getModel ().getCommandFactory ();
        // Component <m_name> = new ServerT extended with {
        m_name = ModelHelper.getUniqueName (getModel (), m_name);
        m_serverCommand = cf.componentCreateCommand (getModel (), m_name,
                SERVER_TYPE, SERVER_TYPE);
        //   port http : HttpPortT = new HttpPortT;
        // }
        final IAcmePortCreateCommand httpCreateCommand = cf.portCreateCommand (m_serverCommand, "http", HTTP_PORT,
                HTTP_PORT);
        String lbName = ModelHelper.getUniqueName (getModel (), "proxyconn");
        // Connector proxyconn : ProxyConnT = new ProxyConnT;
        final IAcmeConnectorCreateCommand proxyConnCreateCmd = cf.connectorCreateCommand (getModel (), lbName,
                HTTP_CONN_T, HTTP_CONN_T);
        String fwd = ModelHelper.getUniqueName (lb, "fwd");
        // m_lb = m_lb extended with {
        //   port fwd : ProxyForwardPortT = new ProxyForwardPortT;
        // }
        final IAcmePortCreateCommand lbPortCmd = cf.portCreateCommand (lb, fwd, PROXY_FORWARD_PORT,
                PROXY_FORWARD_PORT);
        // attachment m_lb.fwd to proxyconn.req;
        final IAcmeAttachmentCommand attachLBEnd = cf.attachmentCreateCommand (getModel (),
                lb.getName () + "." + fwd,
                lbName + ".req");
        // attachment <m_name>.http to proxyconn.rec;
        final IAcmeAttachmentCommand attachServerEnd = cf.attachmentCreateCommand (getModel (), m_name + ".http",
                lbName + ".rec");
        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        cmds.add (m_serverCommand);
        cmds.add (httpCreateCommand);
        cmds.add (proxyConnCreateCmd);
        cmds.add (lbPortCmd);
        cmds.add (attachLBEnd);
        cmds.add (attachServerEnd);
        return cmds;
    }


    @Override
    public IAcmeComponent getResult () {
        return m_serverCommand.getComponent ();
    }

    @Override
    public String getCommandName () {
        return "newServer";
    }


}
