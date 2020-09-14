package org.sa.rainbow.brass.model.instructions;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;

import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Created by schmerl on 12/9/2016.
 */
public class SaveInstructionGraphCmd extends AbstractSaveModelCmd<InstructionGraphProgress> {
    public SaveInstructionGraphCmd (IModelsManager mm, String location, FileOutputStream os, String source) {
        super ("saveInstructionGraph", mm, location, os, source);
    }

    @Override
    public Object getResult () throws IllegalStateException {
        return null;
    }

    @Override
    public ModelReference getModelReference () {
        return new ModelReference ("", "InstructionGraphProgress");
    }

    @Override
    protected void subExecute () throws RainbowException {
        InstructionGraphProgress model = getModelContext ().getModelInstance ();
        try (PrintStream ps = new PrintStream (getStream ())) {
            ps.print (model.toString ());
        }
    }

    @Override
    protected void subRedo () throws RainbowException {

    }

    @Override
    protected void subUndo () throws RainbowException {

    }

    @Override
    protected boolean checkModelValidForCommand (InstructionGraphProgress instructionGraph) {
        return true;
    }
}
