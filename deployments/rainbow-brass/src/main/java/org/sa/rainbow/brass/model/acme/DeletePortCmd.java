package org.sa.rainbow.brass.model.acme;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmePort;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePortDeleteCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class DeletePortCmd extends RosAcmeModelCommand<IAcmePort> {

    private String                 m_component;
    private String                 m_port;
    private IAcmePortDeleteCommand m_deleteCommand;

    public DeletePortCmd (AcmeModelInstance model, String component, String port) {
        super ("deletePort", model, component, port);
        m_component = component;
        m_port = port;

    }

    @Override
    public IAcmePort getResult () throws IllegalStateException {
        return m_deleteCommand.getPort ();
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeComponent comp = getModelContext ().resolveInModel (m_component, IAcmeComponent.class);
        IAcmePort port = comp.getPort (m_port);
        if (port == null) return Collections.<IAcmeCommand<?>> emptyList ();
        m_deleteCommand = comp.getCommandFactory ().portDeleteCommand (port);
        return Arrays.<IAcmeCommand<?>> asList (m_deleteCommand);
    }

}
