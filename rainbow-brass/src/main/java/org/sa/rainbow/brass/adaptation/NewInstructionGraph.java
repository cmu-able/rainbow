package org.sa.rainbow.brass.adaptation;

import org.sa.rainbow.brass.model.instructions.InstructionGraphCommandFactory;
import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.SetInstructionsCmd;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.OperationResult;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.Result;
import org.sa.rainbow.core.ports.IModelsManagerPort;

/**
 * An class that just publishes an operation to set the instruction graph.
 * 
 * @author Bradley Schmerl: schmerl
 *
 */
public class NewInstructionGraph extends BrassPlan {

    private String m_instructionGraph;
    private InstructionGraphModelInstance m_reference;

    private IModelsManagerPort m_modelsManager;
    private boolean            m_outcome;

    public NewInstructionGraph (InstructionGraphModelInstance m, String instructionGraph) {
        m_instructionGraph = instructionGraph;
        m_reference = m;

    }

    @Override
    public Object evaluate (Object[] argsIn) {
        IAdaptationExecutor<BrassPlan> executor = Rainbow.instance ().getRainbowMaster ()
                .strategyExecutor (m_reference.getModelInstance ().getModelReference ().toString ());
        InstructionGraphCommandFactory cf = m_reference.getCommandFactory ();
        SetInstructionsCmd cmd = cf.setInstructionsCmd (m_instructionGraph);
        OperationResult result = executor.getOperationPublishingPort ().publishOperation (cmd);
        m_outcome = result.result == Result.SUCCESS;
        return m_outcome;
    }

    @Override
    public boolean getOutcome () {
        return m_outcome;
    }

}
