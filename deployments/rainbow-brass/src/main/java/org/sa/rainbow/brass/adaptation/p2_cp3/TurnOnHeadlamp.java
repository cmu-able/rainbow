package org.sa.rainbow.brass.adaptation.p2_cp3;

import org.sa.rainbow.brass.adaptation.BrassPlan;
import org.sa.rainbow.brass.model.p2_cp3.ICP3ModelAccessor;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState.Sensors;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotStateCommandFactory;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotStateModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.robot.SetSensorCmd;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.OperationResult;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.Result;

public class TurnOnHeadlamp extends BrassPlan {

	private boolean m_on;
	private boolean m_outcome;
	private ICP3ModelAccessor m_models;

	public TurnOnHeadlamp(ICP3ModelAccessor models, boolean b) {
		m_models = models;
		m_on = b;
	}

	@Override
	public Object evaluate(Object[] argsIn) {
		try {
			IAdaptationExecutor<BrassPlan> executor = (IAdaptationExecutor<BrassPlan>) Rainbow.instance().
					getRainbowMaster().
					adaptationExecutors().get(m_models.getRainbowStateModel().getModelInstance().getModelReference().toString());
			CP3RobotStateCommandFactory cf = (CP3RobotStateCommandFactory) m_models.getRobotStateModel().getCommandFactory();
			SetSensorCmd tohl = cf.setSensorCmd(Sensors.HEADLAMP, m_on);
			OperationResult result = executor.getOperationPublishingPort().publishOperation(tohl);
			m_outcome = result.result == Result.SUCCESS;
		} catch (RainbowException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return m_outcome;
	}

	@Override
	public boolean getOutcome() {
		return m_outcome;
	}

}
