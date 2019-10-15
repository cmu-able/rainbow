package org.sa.rainbow.brass.p3_cp1.adaptation;

import org.sa.rainbow.brass.adaptation.BrassPlan;
import org.sa.rainbow.brass.p3_cp1.model.CP1ModelAccessor;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.OperationResult;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.Result;

public class CancelInstructionsTask extends BrassPlan {

	private CP1ModelAccessor m_models;
	private boolean m_outcome;

	public CancelInstructionsTask(CP1ModelAccessor models) {
		m_models = models;
	}
	
	@Override
	public Object evaluate(Object[] argsIn) {
		IAdaptationExecutor<BrassPlan> executor = (IAdaptationExecutor<BrassPlan>) Rainbow.instance().getRainbowMaster().adaptationExecutors().get(m_models.getRainbowStateModel().getModelInstance().getModelReference().toString());
		IRainbowOperation cancel = m_models.getInstructionGraphModel().getCommandFactory().cancelInstructionsCmd();
		OperationResult result = executor.getOperationPublishingPort().publishOperation(cancel);
		m_outcome = result.result == Result.SUCCESS;
		return m_outcome;
	}

	@Override
	public boolean getOutcome() {
		return m_outcome;
	}

}
