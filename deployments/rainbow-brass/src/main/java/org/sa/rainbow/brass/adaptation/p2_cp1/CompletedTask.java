package org.sa.rainbow.brass.adaptation.p2_cp1;

import org.sa.rainbow.brass.adaptation.BrassPlan;
import org.sa.rainbow.brass.model.p2_cp1.CP1ModelAccessor;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState.CP3ModelState;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.OperationResult;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.Result;

public class CompletedTask extends BrassPlan {

	private CP1ModelAccessor m_models;
	private Boolean m_successOrOtherwise;
	private boolean m_outcome;

	public CompletedTask(CP1ModelAccessor models, boolean success) {
		m_models = models;
		m_successOrOtherwise = success;
		
	}
	
	@Override
	public Object evaluate(Object[] argsIn) {
		IAdaptationExecutor<BrassPlan> executor = Rainbow.instance().getRainbowMaster().strategyExecutor(m_models.getRainbowStateModel().getModelInstance().getModelReference().toString());
		IRainbowOperation op = null;
		if (m_successOrOtherwise) {
			op = m_models.getRainbowStateModel().getCommandFactory().removeModelProblem(CP3ModelState.INSTRUCTION_GRAPH_FAILED);
		}
		else {
			op = m_models.getRainbowStateModel().getCommandFactory().setModelProblem(CP3ModelState.INSTRUCTION_GRAPH_FAILED);
		}
		OperationResult result = executor.getOperationPublishingPort().publishOperation(op);
		m_outcome = result.result == Result.SUCCESS;
		return m_outcome;
	}


	@Override
	public boolean getOutcome() {
		return m_outcome;
	}

}
