package org.sa.rainbow.model.acme.znn.commands;

import edu.emory.mathcs.backport.java.util.Collections;
import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmeComponentCreateCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

import java.util.List;

/**
 * Created by schmerl on 2/4/2016.
 */
public class CreateDisconnectedServerCmd extends ZNNAcmeModelCommand<IAcmeComponent> {
    private String                      m_newName;
    private IAcmeComponentCreateCommand m_cmd;

    public CreateDisconnectedServerCmd (AcmeModelInstance model, String system, String newName) {
        super ("createDisconnectedServer", model, system, newName);
        m_newName = newName;
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        m_newName = ModelHelper.getUniqueName (getModel (), m_newName);
        m_cmd = getModel ().getCommandFactory ()
                .componentCreateCommand (getModel (), m_newName, ZNNConstants.SERVER_TYPE,
                                         ZNNConstants.SERVER_TYPE);
        return Collections.singletonList (m_cmd);
    }

    @Override
    public IAcmeComponent getResult () throws IllegalStateException {
        return m_cmd.getComponent ();
    }
}
