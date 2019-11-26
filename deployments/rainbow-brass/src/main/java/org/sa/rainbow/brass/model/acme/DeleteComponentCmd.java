package org.sa.rainbow.brass.model.acme;

import java.util.Arrays;
import java.util.List;

import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmeComponentDeleteCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.AcmeModelOperation;

public class DeleteComponentCmd extends AcmeModelOperation<IAcmeComponent> {

    private String                      m_component;
    private IAcmeComponentDeleteCommand m_deleteComponentCmd;

    public DeleteComponentCmd (String commandName, AcmeModelInstance model, String system, String component) {
        super (commandName, model, system, component);
        m_component = component;

    }

    @Override
    public IAcmeComponent getResult () throws IllegalStateException {
        return m_deleteComponentCmd.getComponent ();
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeComponent comp = getModelContext ().resolveInModel (m_component, IAcmeComponent.class);
        if (comp == null) throw new RainbowModelException (m_component + " does not exist");
        m_deleteComponentCmd = comp.getCommandFactory ().componentDeleteCommand (comp);
        return Arrays.<IAcmeCommand<?>> asList (m_deleteComponentCmd);
    }

    @Override
    protected boolean checkModelValidForCommand (IAcmeSystem model) {
        return true;
    }

}
