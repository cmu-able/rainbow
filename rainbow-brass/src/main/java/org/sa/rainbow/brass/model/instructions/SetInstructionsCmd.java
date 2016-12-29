package org.sa.rainbow.brass.model.instructions;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by schmerl on 12/9/2016.
 */
public class SetInstructionsCmd extends AbstractRainbowModelOperation<List<InstructionGraphProgress.Instruction>, InstructionGraphProgress>{
    private final String                               m_instructionsStr;
    private List<InstructionGraphProgress.Instruction> m_result;
    private List<InstructionGraphProgress.Instruction> m_oldInstructions;

    public SetInstructionsCmd (InstructionGraphModelInstance modelInstance, String target, String instructionGraphCode) {
        super ("setInstructionsCmd", modelInstance, target, instructionGraphCode);
        m_instructionsStr = instructionGraphCode;

    }

    @Override
    public List<InstructionGraphProgress.Instruction> getResult () throws IllegalStateException {
        return m_result;
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, getName ());
    }

    @Override
    protected void subExecute () throws RainbowException {
        List<InstructionGraphProgress.Instruction> instructionList = InstructionGraphProgress.parseFromString (m_instructionsStr);
        m_oldInstructions = new LinkedList<InstructionGraphProgress.Instruction> (getModelContext ().getModelInstance ()
                .getInstructions ());
        getModelContext ().getModelInstance ().setInstructions (instructionList);
    }

    @Override
    protected void subRedo () throws RainbowException {
        subExecute ();
    }

    @Override
    protected void subUndo () throws RainbowException {
        getModelContext ().getModelInstance ().setInstructions (m_oldInstructions);
    }

    @Override
    protected boolean checkModelValidForCommand (InstructionGraphProgress instructionGraph) {
        return true;
    }
}
