package org.sa.rainbow.brass.model.p2_cp3;

import org.sa.rainbow.brass.model.IP2ModelAccessor;
import org.sa.rainbow.brass.model.p2_cp3.acme.TurtlebotModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotStateModelInstance;

public interface ICP3ModelAccessor extends IP2ModelAccessor {

	CP3RobotStateModelInstance getRobotStateModel();

	TurtlebotModelInstance getTurtlebotModel();

}
