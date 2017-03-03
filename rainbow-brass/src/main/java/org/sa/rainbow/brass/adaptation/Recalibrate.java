package org.sa.rainbow.brass.adaptation;

import org.sa.rainbow.brass.model.mission.MissionCommandFactory;
import org.sa.rainbow.brass.model.mission.MissionStateModelInstance;
import org.sa.rainbow.brass.model.mission.RecalibrateCmd;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.OperationResult;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.Result;

public class Recalibrate extends BrassPlan {
    private MissionStateModelInstance m_reference;
    boolean                           m_outcome;

    public Recalibrate (MissionStateModelInstance m) {
        m_reference = m;
    }

    @Override
    public Object evaluate (Object[] argsIn) {
        IAdaptationExecutor<BrassPlan> executor = Rainbow.instance ().getRainbowMaster ()
                .strategyExecutor (m_reference.getModelInstance ().getModelReference ().toString ());
        MissionCommandFactory cf = m_reference.getCommandFactory ();
        RecalibrateCmd cmd = cf.recalibrate (false);
        OperationResult result = executor.getOperationPublishingPort ().publishOperation (cmd);
        m_outcome = result.result == Result.SUCCESS;
        return m_outcome;
    }

    @Override
    public boolean getOutcome () {
        return m_outcome;
    }

}
