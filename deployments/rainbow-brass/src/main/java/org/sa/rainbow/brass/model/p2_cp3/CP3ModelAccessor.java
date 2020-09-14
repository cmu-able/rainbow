package org.sa.rainbow.brass.model.p2_cp3;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.brass.model.P2ModelAccessor;
import org.sa.rainbow.brass.model.p2_cp3.acme.TurtlebotModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotStateModelInstance;
import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.brass.model.robot.RobotStateModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;

public class CP3ModelAccessor extends P2ModelAccessor implements ICP3ModelAccessor {
	private CP3RobotStateModelInstance m_robotStateModel;
	private TurtlebotModelInstance m_turtlebotArchModel;
	public CP3ModelAccessor (IModelsManagerPort mmp) {
		super(mmp);
	}
	
	@Override
	public TurtlebotModelInstance getTurtlebotModel() {
		if (m_turtlebotArchModel == null) {
			m_turtlebotArchModel = (TurtlebotModelInstance) m_modelsManagerPort
					.<IAcmeSystem>getModelInstance(new ModelReference("Turtlebot", "Acme"));
		}
		return m_turtlebotArchModel;
	}

	@Override
	public CP3RobotStateModelInstance getRobotStateModel() {
		if (m_robotStateModel == null) {
			CP3RobotStateModelInstance modelInstance = (CP3RobotStateModelInstance) m_modelsManagerPort.<RobotState>getModelInstance(
					new ModelReference("Robot", RobotStateModelInstance.ROBOT_STATE_TYPE));
			m_robotStateModel = modelInstance;
		}
		return m_robotStateModel;
	}
}
