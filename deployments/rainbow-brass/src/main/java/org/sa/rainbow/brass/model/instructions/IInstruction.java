package org.sa.rainbow.brass.model.instructions;

public interface IInstruction {
	public String getInstructionLabel();
	public String getInstruction();
	public String getNextInstructionLabel();
	public IInstruction copy();
}
