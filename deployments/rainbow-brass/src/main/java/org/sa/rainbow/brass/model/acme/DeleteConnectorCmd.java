package org.sa.rainbow.brass.model.acme;

import java.util.Arrays;
import java.util.List;

import org.acmestudio.acme.element.IAcmeConnector;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmeConnectorDeleteCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.AcmeModelOperation;

public class DeleteConnectorCmd extends AcmeModelOperation<IAcmeConnector> {

    private String                      m_connector;
    private IAcmeConnectorDeleteCommand m_deleteConnectorCmd;

    public DeleteConnectorCmd (String commandName, AcmeModelInstance model, String system, String connector) {
        super (commandName, model, system, connector);
        m_connector = connector;

    }

    @Override
    public IAcmeConnector getResult () throws IllegalStateException {
        return m_deleteConnectorCmd.getConnector ();
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeConnector comp = getModelContext ().resolveInModel (m_connector, IAcmeConnector.class);
        if (comp == null) throw new RainbowModelException (m_connector + " does not exist");
        m_deleteConnectorCmd = comp.getCommandFactory ().connectorDeleteCommand (comp);
        return Arrays.<IAcmeCommand<?>> asList (m_deleteConnectorCmd);
    }

    @Override
    protected boolean checkModelValidForCommand (IAcmeSystem model) {
        return true;
    }

}
