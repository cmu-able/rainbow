package org.sa.rainbow.brass.model.acme;

import java.util.Arrays;
import java.util.List;

import org.acmestudio.acme.element.IAcmeConnector;
import org.acmestudio.acme.element.IAcmeRole;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmeRoleCreateCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class CreateTopicRoleCommand extends RosAcmeModelCommand<IAcmeRole> {

    private String                 m_comp;
    private String                 m_roleName;
    private String                 m_roleType;
    private IAcmeRoleCreateCommand m_roleCreateCommand;

    public CreateTopicRoleCommand (String commandName, AcmeModelInstance model, String comp, String roleName, String roleType) {
        super (commandName, model, comp, roleName, roleType);
        m_comp = comp;
        m_roleName = roleName;
        m_roleType = roleType;
    }

    @Override
    public IAcmeRole getResult () throws IllegalStateException {
        return m_roleCreateCommand.getRole ();
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeConnector conn = getModelContext ().resolveInModel (m_comp, IAcmeConnector.class);
        if (conn == null) throw new RainbowModelException ("Connector '" + m_comp + "' not found.");
        m_roleCreateCommand = conn.getCommandFactory ().roleCreateCommand (conn, m_roleName, Arrays.asList (m_roleType),
                Arrays.asList (m_roleType));
        return Arrays.<IAcmeCommand<?>> asList (m_roleCreateCommand);
    }


}
