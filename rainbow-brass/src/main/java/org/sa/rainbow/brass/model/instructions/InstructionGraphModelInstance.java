package org.sa.rainbow.brass.model.instructions;

import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;

/**
 * Created by schmerl on 12/9/2016.
 */
public class InstructionGraphModelInstance implements IModelInstance<InstructionGraphProgress>{


    public static final String INSTRUCTION_GRAPH_TYPE = "InstructionGraphProgress";
    private InstructionGraphProgress       m_graph;
    private InstructionGraphCommandFactory m_commandFactory;
    private String                         m_source;

    public InstructionGraphModelInstance (InstructionGraphProgress graph, String source) {
        setModelInstance (graph);
        setOriginalSource (source);
    }
    @Override
    public InstructionGraphProgress getModelInstance () {
        return m_graph;
    }

    @Override
    public void setModelInstance (InstructionGraphProgress model) {
        m_graph = model;
    }

    @Override
    public IModelInstance<InstructionGraphProgress> copyModelInstance (String newName) throws RainbowCopyException {
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
    public InstructionGraphCommandFactory getCommandFactory () {
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
