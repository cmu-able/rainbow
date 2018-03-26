package org.sa.rainbow.brass.adaptation.p2_cp3;

import org.sa.rainbow.brass.adaptation.BrassPlan;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState.Sensors;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotStateCommandFactory;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotStateModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.robot.SetSensorCmd;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.OperationResult;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort.Result;

public class TurnOnHeadlamp extends BrassPlan {

	private CP3RobotStateModelInstance m_robotStateModel;
	private boolean m_on;
	private boolean m_outcome;

	public TurnOnHeadlamp(CP3RobotStateModelInstance robotStateModel, boolean b) {
		m_robotStateModel = robotStateModel;
		m_on = b;
	}

	@Override
	public Object evaluate(Object[] argsIn) {
		IAdaptationExecutor<BrassPlan> executor = Rainbow.instance().
				getRainbowMaster().
				strategyExecutor(m_robotStateModel.getModelInstance().getModelReference().toString());
		CP3RobotStateCommandFactory cf = (CP3RobotStateCommandFactory )m_robotStateModel.getCommandFactory();
		SetSensorCmd tohl = cf.setSensorCmd(Sensors.HEADLAMP, m_on);
		OperationResult result = executor.getOperationPublishingPort().publishOperation(tohl);
		m_outcome = result.result == Result.SUCCESS;
		return m_outcome;
	}

	@Override
	public boolean getOutcome() {
		return m_outcome;
	}

}
