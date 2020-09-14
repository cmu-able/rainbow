package org.sa.rainbow.brass.model.p2_cp3.robot;

import java.io.InputStream;

import org.sa.rainbow.brass.model.robot.RobotStateLoadCmd;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelsManager;

public class CP3RobotStateLoadCmd extends RobotStateLoadCmd {

	public CP3RobotStateLoadCmd(IModelsManager mm, String resource, InputStream is, String source) {
		super(mm, resource, is, source);
	}
	
	@Override
	protected void subExecute() throws RainbowException {
		if (m_stream == null) {
			CP3RobotState m = new CP3RobotState(getModelReference());
			m_result = new CP3RobotStateModelInstance(m, getOriginalSource());
			doPostExecute();
		}
	}

}
