package org.sa.rainbow.brass.model.instructions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

/**
 * Created by schmerl on 12/9/2016.
 */
public class SetInstructionsCmd extends AbstractRainbowModelOperation<List<IInstruction>, InstructionGraphProgress>{
    private final String m_instructionsStr;
    private List<IInstruction> m_result;
    private List<IInstruction> m_oldInstructions;

    public SetInstructionsCmd (InstructionGraphModelInstance modelInstance, String target, String instructionGraphCode) {
        super ("setInstructions", modelInstance, target, instructionGraphCode);
        m_instructionsStr = instructionGraphCode;

    }

    @Override
    public List<IInstruction> getResult () throws IllegalStateException {
        return m_result;
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, getName ());
    }

    @Override
    protected void subExecute () throws RainbowException {
        List<IInstruction> instructionList = InstructionGraphProgress.parseFromString (m_instructionsStr);
        Collection<? extends IInstruction> oldInst = getModelContext ().getModelInstance ()
                .getInstructions ();
        if (oldInst == null) {
            m_oldInstructions = new LinkedList<> ();
        }
        else {
            m_oldInstructions = new LinkedList<IInstruction> (oldInst);
        }
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
