package org.sa.rainbow.brass.model.instructions;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Created by schmerl on 12/9/2016.
 */
public class SaveInstructionGraphCmd extends AbstractSaveModelCmd<InstructionGraph> {
    public SaveInstructionGraphCmd (IModelsManager mm, String location, FileOutputStream os, String source) {
        super ("saveInstructionGraph", mm, location, os, source);
    }

    @Override
    public Object getResult () throws IllegalStateException {
        return null;
    }

    @Override
    public ModelReference getModelReference () {
        return new ModelReference ("", "InstructionGraph");
    }

    @Override
    protected void subExecute () throws RainbowException {
        InstructionGraph model = getModelContext ().getModelInstance ();
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
    protected boolean checkModelValidForCommand (InstructionGraph instructionGraph) {
        return true;
    }
}
