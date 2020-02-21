package org.sa.rainbow.brass.model.instructions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

/**
 * Created by schmerl on 12/9/2016.
 */
public class InstructionGraphCommandFactory extends ModelCommandFactory<InstructionGraphProgress> {

    private static final String CANCEL_INSTRUCTIONS_CMD = "cancelInstructions";
	private static final String SET_EXECUTION_FAILED_CMD = "setExecutionFailed";
	private static final String SET_EXECUTING_INSTRUCTION_CMD = "setExecutingInstruction";
	private static final String SET_INSTRUCTIONS_CMD = "setInstructions";

	@LoadOperation
	public static InstructionGraphLoadCommand loadCommand (ModelsManager mm, String modelName, InputStream stream,
            String source) {
        return new InstructionGraphLoadCommand (modelName, mm, stream, source);
    }


    public InstructionGraphCommandFactory (InstructionGraphModelInstance model) throws RainbowException {
        super (InstructionGraphModelInstance.class, model);
    }

    @Override
    public SaveInstructionGraphCmd saveCommand (String location) throws RainbowModelException {
        try (FileOutputStream os = new FileOutputStream (location)) {
            return new SaveInstructionGraphCmd (null, location, os, m_modelInstance.getOriginalSource ());
        }
        catch (IOException e) {
            return null;
        }
    }

    @Operation(name=SET_INSTRUCTIONS_CMD)
    public SetInstructionsCmd setInstructionsCmd (String instructionGraphCode) {
        return new SetInstructionsCmd (SET_INSTRUCTIONS_CMD,(InstructionGraphModelInstance )m_modelInstance, "", instructionGraphCode);
    }
    
    @Operation(name=CANCEL_INSTRUCTIONS_CMD)
    public CancelInstructionsCmd cancelInstructionsCmd() {
    	return new CancelInstructionsCmd(CANCEL_INSTRUCTIONS_CMD, (InstructionGraphModelInstance )m_modelInstance, "");
    }

    @Operation(name=SET_EXECUTING_INSTRUCTION_CMD)
    public SetExecutingInstructionCmd setExecutingInstructionCmd (String instructionLabel, String state) {
        return new SetExecutingInstructionCmd (SET_EXECUTING_INSTRUCTION_CMD, (InstructionGraphModelInstance )m_modelInstance, "", instructionLabel,
                state);
    }

    @Operation(name=SET_EXECUTION_FAILED_CMD)
    public SetExecutionFailedCmd setExecutionFailedCmd (String instructionLabel) {
        return new SetExecutionFailedCmd (SET_EXECUTION_FAILED_CMD,(InstructionGraphModelInstance )m_modelInstance, "", instructionLabel);
    }
}
