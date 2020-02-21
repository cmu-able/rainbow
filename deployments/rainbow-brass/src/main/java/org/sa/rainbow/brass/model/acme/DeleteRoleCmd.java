package org.sa.rainbow.brass.model.acme;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.acmestudio.acme.element.IAcmeConnector;
import org.acmestudio.acme.element.IAcmeRole;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmeRoleDeleteCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class DeleteRoleCmd extends RosAcmeModelCommand<IAcmeRole> {

    private String                 m_connector;
    private String                 m_role;
    private IAcmeRoleDeleteCommand m_deleteCommand;

    public DeleteRoleCmd (String commandName, AcmeModelInstance model, String connector, String role) {
        super (commandName, model, connector, role);
        m_connector = connector;
        m_role = role;

    }

    @Override
    public IAcmeRole getResult () throws IllegalStateException {
        return m_deleteCommand.getRole ();
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeConnector comp = getModelContext ().resolveInModel (m_connector, IAcmeConnector.class);
        IAcmeRole role = comp.getRole (m_role);
        if (role == null) return Collections.<IAcmeCommand<?>> emptyList ();
        m_deleteCommand = comp.getCommandFactory ().roleDeleteCommand (role);
        return Arrays.<IAcmeCommand<?>> asList (m_deleteCommand);
    }

}
