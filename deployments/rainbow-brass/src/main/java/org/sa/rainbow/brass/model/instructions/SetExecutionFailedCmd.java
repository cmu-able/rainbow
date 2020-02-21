package org.sa.rainbow.brass.model.instructions;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

import java.util.List;

/**
 * Created by schmerl on 12/9/2016.
 */
public class SetExecutionFailedCmd extends AbstractRainbowModelOperation<Boolean, InstructionGraphProgress> {
	private Boolean m_result;
    private boolean m_old;


    public SetExecutionFailedCmd (String commandName, InstructionGraphModelInstance modelInstance, String target, String instructionLabel) {
        super (commandName, modelInstance, target, instructionLabel);
    }

    @Override
    public Boolean getResult () throws IllegalStateException {
        return m_result;
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, getName());
    }

    @Override
    protected void subExecute () throws RainbowException {
        m_old = getModelContext ().getModelInstance ().getCurrentOK();
        Boolean b = Boolean.parseBoolean (getParameters ()[0]);
        getModelContext ().getModelInstance ().setCurrentOK (b);

    }

    @Override
    protected void subRedo () throws RainbowException {
        Boolean b = Boolean.parseBoolean (getParameters ()[0]);
        getModelContext ().getModelInstance ().setCurrentOK (b);
    }

    @Override
    protected void subUndo () throws RainbowException {
        getModelContext ().getModelInstance ().setCurrentOK (m_old);

    }

    @Override
    protected boolean checkModelValidForCommand (InstructionGraphProgress instructionGraph) {
        return true;
    }
}
