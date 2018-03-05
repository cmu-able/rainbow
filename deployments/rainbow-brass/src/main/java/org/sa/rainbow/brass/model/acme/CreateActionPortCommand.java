package org.sa.rainbow.brass.model.acme;

import java.util.Arrays;
import java.util.List;

import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmePort;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmePortCreateCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class CreateActionPortCommand extends RosAcmeModelCommand<IAcmePort> {

    private String                 m_comp;
    private String                 m_portName;
    private String                 m_portType;
    private IAcmePortCreateCommand m_portCreateCommand;

    public CreateActionPortCommand (AcmeModelInstance model, String comp, String portName, String portType) {
        super ("createActionPort", model, comp, portName, portType);
        m_comp = comp;
        m_portName = portName;
        m_portType = portType;
    }

    @Override
    public IAcmePort getResult () throws IllegalStateException {
        return m_portCreateCommand.getPort ();
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeComponent comp = getModelContext ().resolveInModel (m_comp, IAcmeComponent.class);
        if (comp == null) throw new RainbowModelException ("Component '" + m_comp + "' not found.");
        m_portCreateCommand = comp.getCommandFactory ().portCreateCommand (comp, m_portName, Arrays.asList (m_portType),
                Arrays.asList (m_portType));
        return Arrays.<IAcmeCommand<?>> asList (m_portCreateCommand);
    }


}
