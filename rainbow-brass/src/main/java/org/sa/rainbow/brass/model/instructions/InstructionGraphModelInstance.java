package org.sa.rainbow.brass.model.instructions;

import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

/**
 * Created by schmerl on 12/9/2016.
 */
public class InstructionGraphModelInstance implements IModelInstance<InstructionGraph>{


    public static final String INSTRUCTION_GRAPH_TYPE = "InstructionGraph";
    private InstructionGraph m_graph;
    private InstructionGraphCommandFactory m_commandFactory;
    private String m_source;

    public InstructionGraphModelInstance (InstructionGraph graph, String source) {
        setModelInstance (graph);
        setOriginalSource (source);
    }
    @Override
    public InstructionGraph getModelInstance () {
        return m_graph;
    }

    @Override
    public void setModelInstance (InstructionGraph model) {
        m_graph = model;
    }

    @Override
    public IModelInstance<InstructionGraph> copyModelInstance (String newName) throws RainbowCopyException {
        return new InstructionGraphModelInstance (getModelInstance ().copy (), getOriginalSource ());
    }

    @Override
    public String getModelType () {
        return INSTRUCTION_GRAPH_TYPE;
    }

    @Override
    public String getModelName () {
        return getModelInstance ().getModelReference ().getModelName ();
    }

    @Override
    public ModelCommandFactory<InstructionGraph> getCommandFactory () {
        if (m_commandFactory == null) {
            m_commandFactory = new InstructionGraphCommandFactory (this);
        }
        return m_commandFactory;
    }

    @Override
    public void setOriginalSource (String source) {
        m_source = source;
    }

    @Override
    public String getOriginalSource () {
        return m_source;
    }

    @Override
    public void dispose () throws RainbowException {
        m_graph = null;
        m_source = null;
    }
}
