package org.sa.rainbow.brass.analyses.p2_cp3;

import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowStateModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RemoveModelProblemCmd;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.SetModelProblemCmd;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState.CP3ModelState;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotStateModelInstance;
import org.sa.rainbow.brass.model.robot.RobotStateModelInstance;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;

public class DarknessAnalyzer extends P2CP3Analyzer implements IRainbowModelChangeCallback {

	private static final double ILLUMINATION_THRESHOLD = 10;
	private IModelChangeBusSubscriberPort m_modelChangePort;
	private IModelUSBusPort m_modelUSPort;
	private IModelsManagerPort m_modelsManagerPort;
	private CP3RobotState m_robotStateModel;
	private RainbowStateModelInstance m_rainbowStateModel;
	private boolean m_darkBefore = false;

	public DarknessAnalyzer() {
		super("Darkness");

	}





	@Override
	protected void runAction() {
		CP3RobotState rs = getModels().getRobotStateModel().getModelInstance();
		try {
			if (rs.getIllumination() < ILLUMINATION_THRESHOLD && getModels().getTurtlebotModel().getActiveComponents().contains("marker_pose_publisher") && !m_darkBefore) {
				SetModelProblemCmd cmd = getModels().getRainbowStateModel().getCommandFactory()
						.setModelProblem(CP3ModelState.TOO_DARK);
				m_darkBefore = true;
				m_modelUSPort.updateModel(cmd);
			} else if (m_darkBefore && (rs.getIllumination() >= ILLUMINATION_THRESHOLD || !getModels().getTurtlebotModel().getActiveComponents().contains("marker_pose_publisher"))) {
				RemoveModelProblemCmd cmd = getModels().getRainbowStateModel().getCommandFactory()
						.removeModelProblem(CP3ModelState.TOO_DARK);
				m_darkBefore = false;
				m_modelUSPort.updateModel(cmd);
			}
		} catch (IllegalStateException e) {

		}
	}

	@Override
	public RainbowComponentT getComponentType() {
		return RainbowComponentT.ANALYSIS;
	}

	@Override
	public void onEvent(ModelReference reference, IRainbowMessage message) {
		// TODO Auto-generated method stub

	}

}
