package org.sa.rainbow.brass.model.p2_cp1.robot;

import java.io.InputStream;

import org.sa.rainbow.brass.model.robot.RobotStateLoadCmd;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelsManager;

public class CP1RobotStateLoadCmd extends RobotStateLoadCmd {

	public CP1RobotStateLoadCmd(IModelsManager mm, String resource, InputStream is, String source) {
		super(mm, resource, is, source);
	}
	
	@Override
	protected void subExecute() throws RainbowException {
		if (m_stream == null) {
			CP1RobotState m = new CP1RobotState(getModelReference());
			m_result = new CP1RobotStateModelInstance(m, getOriginalSource());
			doPostExecute();
		}
	}

}
