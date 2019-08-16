package org.sa.rainbow.swim.adaptation;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.OperationResult;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.Result;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.swim.commands.DecDimmerCmd;
import org.sa.rainbow.model.acme.swim.commands.SwimCommandFactory;

public class DecDimmerPlan extends SwimExtendedPlan {

	private boolean            m_outcome;
	private IModelsManagerPort m_modelsManager;
	
	//private SwimModelUpdateOperatorsImpl 			   m_model;
	private AcmeModelInstance m_reference;

	public DecDimmerPlan (AcmeModelInstance m) {
      //m_instructionGraph = instructionGraph;
      m_reference = m;
      
	}
	
	@Override
	public Object evaluate(Object[] argsIn) {
		IAdaptationExecutor<SwimExtendedPlan> executor = 
				Rainbow.instance().getRainbowMaster().strategyExecutor
				(m_reference.getModelName() + ":" + m_reference.getModelType());
		SwimCommandFactory cf = (SwimCommandFactory) m_reference.getCommandFactory();
		DecDimmerCmd cmd = cf.decDimmerCmd(m_reference.getModelInstance().getComponent("LB0"));
		System.out.println ("Changing Instructions");
		if (executor == null) {
			System.out.println("executor null"); return false;
		}
		else {
		OperationResult result = executor.getOperationPublishingPort().publishOperation(cmd);
		m_outcome = result.result == Result.SUCCESS;
		System.out.println ("Done: " + m_outcome);
		return m_outcome; }
	}

	@Override
	public boolean getOutcome() {
		// TODO Auto-generated method stub
		return m_outcome;
	}

}

