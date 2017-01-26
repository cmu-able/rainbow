package org.sa.rainbow.brass.model.instructions;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

/**
 * Created by schmerl on 12/9/2016.
 */
public class SetExecutingInstructionCmd extends AbstractRainbowModelOperation<String, InstructionGraphProgress> {
    private String m_instructionLabel;
    private String m_oldInstructionLabel;

    public SetExecutingInstructionCmd (InstructionGraphModelInstance modelInstance, String target, String instructionLabel) {
        super ("setExecutingInstruction", modelInstance, "", instructionLabel);
        m_instructionLabel = instructionLabel;
    }

    @Override
    public String getResult () throws IllegalStateException {
        return m_instructionLabel;
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, "setExecutingInstruction");
    }

    @Override
    protected void subExecute () throws RainbowException {
        m_oldInstructionLabel = getModelContext ().getModelInstance ().getExecutingInstruction ();
        getModelContext ().getModelInstance ().setExecutingInstruction (m_instructionLabel);
    }

    @Override
    protected void subRedo () throws RainbowException {
        getModelContext ().getModelInstance ().setExecutingInstruction (m_instructionLabel);
    }

    @Override
    protected void subUndo () throws RainbowException {
        getModelContext ().getModelInstance ().setExecutingInstruction (m_oldInstructionLabel);

    }

    @Override
    protected boolean checkModelValidForCommand (InstructionGraphProgress instructionGraph) {
        return true;
    }
}
