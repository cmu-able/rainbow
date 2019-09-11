package org.sa.rainbow.brass.model.instructions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

/**
 * Created by schmerl on 12/9/2016.
 */
public class InstructionGraphCommandFactory extends ModelCommandFactory<InstructionGraphProgress> {

    public static InstructionGraphLoadCommand loadCommand (ModelsManager mm, String modelName, InputStream stream,
            String source) {
        return new InstructionGraphLoadCommand (modelName, mm, stream, source);
    }


    public InstructionGraphCommandFactory (InstructionGraphModelInstance model) {
        super (InstructionGraphModelInstance.class, model);
    }

    @Override
    protected void fillInCommandMap () {
        m_commandMap.put ("setInstructions".toLowerCase (), SetInstructionsCmd.class);
        m_commandMap.put ("setExecutingInstruction".toLowerCase (), SetExecutingInstructionCmd.class);
        m_commandMap.put ("setExecutionFailed".toLowerCase (), SetExecutionFailedCmd.class);
        m_commandMap.put("cancelInstructions".toLowerCase(), CancelInstructionsCmd.class);

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

    public SetInstructionsCmd setInstructionsCmd (String instructionGraphCode) {
        return new SetInstructionsCmd ((InstructionGraphModelInstance )m_modelInstance, "", instructionGraphCode);
    }
    
    public CancelInstructionsCmd cancelInstructionsCmd() {
    	return new CancelInstructionsCmd((InstructionGraphModelInstance )m_modelInstance, "");
    }

    public SetExecutingInstructionCmd setExecutingInstructionCmd (String instructionLabel, String state) {
        return new SetExecutingInstructionCmd ((InstructionGraphModelInstance )m_modelInstance, "", instructionLabel,
                state);
    }

    public SetExecutionFailedCmd setExecutionFailedCmd (String instructionLabel) {
        return new SetExecutionFailedCmd ((InstructionGraphModelInstance )m_modelInstance, "", instructionLabel);
    }
}
