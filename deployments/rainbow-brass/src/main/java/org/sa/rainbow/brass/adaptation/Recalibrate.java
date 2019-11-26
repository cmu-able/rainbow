package org.sa.rainbow.brass.adaptation;

import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.mission.MissionCommandFactory;
import org.sa.rainbow.brass.model.mission.MissionStateModelInstance;
import org.sa.rainbow.brass.model.mission.RecalibrateCmd;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.OperationResult;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.Result;

public class Recalibrate extends BrassPlan {
    private MissionStateModelInstance m_reference;
    boolean                           m_outcome;
    private InstructionGraphModelInstance m_executorModel;

    public Recalibrate (MissionStateModelInstance m, InstructionGraphModelInstance executorModel) {
        m_reference = m;
        m_executorModel = executorModel;
    }

    @Override
    public Object evaluate (Object[] argsIn) {
        try {
			IAdaptationExecutor<BrassPlan> executor = (IAdaptationExecutor<BrassPlan>) Rainbow.instance ().getRainbowMaster ()
			        .adaptationExecutors().get (m_executorModel.getModelInstance ().getModelReference ().toString ());
			MissionCommandFactory cf = m_reference.getCommandFactory ();
			RecalibrateCmd cmd = cf.recalibrate (false);
			OperationResult result = executor.getOperationPublishingPort ().publishOperation (cmd);
			m_outcome = result.result == Result.SUCCESS;
			return m_outcome;
		} catch (RainbowException e) {
			e.printStackTrace();
			return m_outcome;
		}
    }

    @Override
    public boolean getOutcome () {
        return m_outcome;
    }

}
