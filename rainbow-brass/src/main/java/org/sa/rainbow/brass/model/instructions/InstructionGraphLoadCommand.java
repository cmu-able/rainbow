package org.sa.rainbow.brass.model.instructions;

import org.apache.commons.io.IOUtils;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.AbstractLoadModelCmd;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by schmerl on 12/9/2016.
 */
public class InstructionGraphLoadCommand extends AbstractLoadModelCmd<InstructionGraph> {


    private final String m_modelName;
    private final InputStream m_stream;
    private InstructionGraphModelInstance m_result;

    public InstructionGraphLoadCommand (String modelName, IModelsManager mm, InputStream is,
                                        String source) {
        super ("loadInstructionGraph", mm, modelName, is, source);
        m_modelName = modelName;
        m_stream = is;

    }

    @Override
    public InstructionGraphModelInstance getResult () throws IllegalStateException {
        return m_result;
    }

    @Override
    protected void subExecute () throws RainbowException {
        // Parse the instruction graph. This will be done very simply in the first instance through string manipulation
        try {
            String str = IOUtils.toString (m_stream);
            InstructionGraph g = InstructionGraph.parseFromString (getModelReference (), str);
            m_result = new InstructionGraphModelInstance (g, getOriginalSource ());
            doPostExecute ();
        } catch (IOException e) {
            throw new RainbowException (e);
        }
    }

    @Override
    protected void subRedo () throws RainbowException {
        doPostExecute ();
    }

    @Override
    protected void subUndo () throws RainbowException {
        doPostUndo ();
    }

    @Override
    protected boolean checkModelValidForCommand (Object o) {
        return true;
    }
}
