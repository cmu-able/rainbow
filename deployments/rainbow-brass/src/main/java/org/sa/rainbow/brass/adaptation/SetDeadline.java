package org.sa.rainbow.brass.adaptation;

import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.mission.MissionCommandFactory;
import org.sa.rainbow.brass.model.mission.MissionStateModelInstance;
import org.sa.rainbow.brass.model.mission.SetDeadlineCmd;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.OperationResult;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.Result;

public class SetDeadline extends BrassPlan {

    private MissionStateModelInstance m_reference;
    private Long                      m_deadline;
    boolean                           m_outcome;
    private InstructionGraphModelInstance m_executorModel;

    public SetDeadline (MissionStateModelInstance m, InstructionGraphModelInstance executorModel, Long d) {
        m_reference = m;
        m_executorModel = executorModel;
        m_deadline = d;

    }

    @Override
    public Object evaluate (Object[] argsIn) {
        try {
			IAdaptationExecutor<BrassPlan> executor = (IAdaptationExecutor<BrassPlan>) Rainbow.instance ().getRainbowMaster ()
			        .adaptationExecutors().get (m_executorModel.getModelInstance ().getModelReference ().toString ());
			MissionCommandFactory cf = m_reference.getCommandFactory ();
			SetDeadlineCmd cmd = cf.setDeadlineCmd (m_deadline);
			System.out.println ("Setting deadline to " + m_deadline);
			OperationResult result = executor.getOperationPublishingPort ().publishOperation (cmd);
			m_outcome = result.result == Result.SUCCESS;
			System.out.println ("DOne");
		} catch (RainbowException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return m_outcome;
    }

    @Override
    public boolean getOutcome () {
        return m_outcome;
    }

}
