package org.sa.rainbow.brass.model.acme;

import java.util.Arrays;
import java.util.List;

import org.acmestudio.acme.element.IAcmeConnector;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmeConnectorCreateCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class CreateActionConnectorCommand extends RosAcmeModelCommand<IAcmeConnector> {

    private String                      m_connName;
    private IAcmeConnectorCreateCommand m_connectorCreateCommand;

    public CreateActionConnectorCommand (AcmeModelInstance model, String target, String connName) {
        super ("createActionConnector", model, target, connName);
        m_connName = connName;
    }

    @Override
    public IAcmeConnector getResult () throws IllegalStateException {
        return m_connectorCreateCommand.getConnector ();
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        m_connectorCreateCommand = getModel ().getCommandFactory ().connectorCreateCommand (getModel (), m_connName,
                Arrays.asList ("ActionConnT"), Arrays.asList ("ActionConnT"));
        return Arrays.<IAcmeCommand<?>> asList (m_connectorCreateCommand);
    }

}
