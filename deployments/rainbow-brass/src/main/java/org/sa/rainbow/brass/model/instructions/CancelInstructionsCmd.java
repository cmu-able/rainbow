package org.sa.rainbow.brass.model.instructions;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class CancelInstructionsCmd extends AbstractRainbowModelOperation<Boolean, InstructionGraphProgress> {

	private LinkedList<IInstruction> m_oldInstructions;

	public CancelInstructionsCmd(String commandName, IModelInstance<InstructionGraphProgress> model,
			String target, String... parameters) {
		super(commandName, model, target, parameters);
	}

	@Override
	public Boolean getResult() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<? extends IRainbowMessage> getGeneratedEvents(IRainbowMessageFactory messageFactory) {
		return generateEvents(messageFactory, "cancelInstructions");
	}

	@Override
	protected void subExecute() throws RainbowException {
		Collection<? extends IInstruction> oldInst = getModelContext().getModelInstance().getInstructions();
		if (oldInst == null)
			m_oldInstructions = new LinkedList<IInstruction>();
		else 
			m_oldInstructions = new LinkedList<IInstruction>(oldInst);
		getModelContext().getModelInstance().setInstructions(Collections.<IInstruction>emptyList());
	}

	@Override
	protected void subRedo() throws RainbowException {
		subExecute();
	}

	@Override
	protected void subUndo() throws RainbowException {
        getModelContext ().getModelInstance ().setInstructions (m_oldInstructions);
	}

	@Override
	protected boolean checkModelValidForCommand(InstructionGraphProgress model) {
		return true;
	}

}
