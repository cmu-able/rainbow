package org.sa.rainbow.brass.adaptation;

import org.sa.rainbow.brass.model.mission.MissionCommandFactory;
import org.sa.rainbow.brass.model.mission.MissionStateModelInstance;
import org.sa.rainbow.brass.model.mission.SetDeadlineCmd;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.OperationResult;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.Result;

public class SetDeadline extends BrassPlan {

    private MissionStateModelInstance m_reference;
    private Long                      m_deadline;
    boolean                           m_outcome;

    public SetDeadline (MissionStateModelInstance m, Long d) {
        m_reference = m;
        m_deadline = d;
    }

    @Override
    public Object evaluate (Object[] argsIn) {
        IAdaptationExecutor<BrassPlan> executor = Rainbow.instance ().getRainbowMaster ()
                .strategyExecutor (m_reference.getModelInstance ().getModelReference ().toString ());
        MissionCommandFactory cf = m_reference.getCommandFactory ();
        SetDeadlineCmd cmd = cf.setDeadlineCmd (m_deadline);
        OperationResult result = executor.getOperationPublishingPort ().publishOperation (cmd);
        m_outcome = result.result == Result.SUCCESS;
        return m_outcome;
    }

    @Override
    public boolean getOutcome () {
        return m_outcome;
    }

}
