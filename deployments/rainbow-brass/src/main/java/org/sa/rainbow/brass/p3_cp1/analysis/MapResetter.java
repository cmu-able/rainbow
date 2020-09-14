package org.sa.rainbow.brass.p3_cp1.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress.IGExecutionStateT;
import org.sa.rainbow.brass.model.map.EnvMapLoadCmd;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.util.Util;

public class MapResetter extends P2CP1Analyzer {

	private static final String NAME = "Map Resetter";
	private boolean m_isReset = false;
	private File m_modelPath;

	public MapResetter() {
		super(NAME);
		setSleepTime(3000);
	}

	@Override
	public void initialize(IRainbowReportingPort port) throws RainbowConnectionException {
		super.initialize(port);
		String path = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_MODEL_PATH_PREFIX + "5");
		m_modelPath = null;
		if (path != null) {
			m_modelPath = new File(path);
			if (!m_modelPath.isAbsolute()) {
				m_modelPath = Util.getRelativeToPath(Rainbow.instance().getTargetPath(), path);
			}

		}
	}

	@Override
	protected void runAction() {
		IGExecutionStateT state = getModels().getInstructionGraphModel().getModelInstance().getInstructionGraphState();
		if (state == IGExecutionStateT.FINISHED_SUCCESS && !m_isReset) {
			log("Resetting map because it is the end of a task");
			try {
				getModels().getEnvMapModel().getModelInstance().reload();
				log("Map reloaded");
			} catch (IllegalStateException e) {
				m_reportingPort.error(getComponentType(), "Failed to reload environment", e);
				e.printStackTrace();
			}
		} else if (state != IGExecutionStateT.FINISHED_SUCCESS && m_isReset) {
			m_isReset = false;
		}
	}

}
