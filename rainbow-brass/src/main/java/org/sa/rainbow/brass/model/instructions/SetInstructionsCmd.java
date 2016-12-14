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
public class SetInstructionsCmd extends AbstractRainbowModelOperation<List<InstructionGraph.Instruction>, InstructionGraph>{
    private final String                       m_instructionsStr;
    private List<InstructionGraph.Instruction> m_result;
    private List<InstructionGraph.Instruction> m_oldInstructions;

    public SetInstructionsCmd (InstructionGraphModelInstance modelInstance, String instructionGraphCode) {
        super ("setInstructionsCmd", modelInstance, "", instructionGraphCode);
        m_instructionsStr = instructionGraphCode;

    }

    @Override
    public List<InstructionGraph.Instruction> getResult () throws IllegalStateException {
        return m_result;
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, getName ());
    }

    @Override
    protected void subExecute () throws RainbowException {
        List<InstructionGraph.Instruction> instructionList = InstructionGraph.parseFromString (m_instructionsStr);
        m_oldInstructions = new LinkedList<InstructionGraph.Instruction> (getModelContext ().getModelInstance ()
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
    protected boolean checkModelValidForCommand (InstructionGraph instructionGraph) {
        return true;
    }
}
