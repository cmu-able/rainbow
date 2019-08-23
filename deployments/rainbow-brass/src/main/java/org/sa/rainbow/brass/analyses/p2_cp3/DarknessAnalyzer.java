package org.sa.rainbow.brass.analyses.p2_cp3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.sa.rainbow.brass.confsynthesis.ConfigurationSynthesizer;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapArc;
import org.sa.rainbow.brass.model.map.EnvMapNode;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState.LocationRecording;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState.CP3ModelState;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RemoveModelProblemCmd;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.SetModelProblemCmd;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState;
import org.sa.rainbow.core.IRainbowEnvironment;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;

public class DarknessAnalyzer extends P2CP3Analyzer implements IRainbowModelChangeCallback {

	private static final double ILLUMINATION_THRESHOLD = 80;
	private boolean m_darkBefore = false;

	public DarknessAnalyzer() {
		super("Darkness");

	}
	
	public DarknessAnalyzer(IRainbowEnvironment env) {
		super("Darkness",env);
	}

	protected static class LightingData {
		boolean m_darkProcessed = false;
		boolean m_badLightProcessed = false;
		double m_badLightSuccessRate = 0.0;
		double m_litSuccessRate = 0.0;
	}

	Map<EnvMapArc, Map<String, LightingData>> m_lightingData = new HashMap<>();

	@Override
	protected void runAction() {
		CP3RobotState rs = getModels().getRobotStateModel().getModelInstance();
		try {
			double ill = rs.getIllumination();
			if (ill < ILLUMINATION_THRESHOLD * 2) { // Adjust success rate for light sensitive configs
				// TODO: Turn this into commands rather than direct manipulation of the model
				LocationRecording currentPose = getModels().getMissionStateModel().getModelInstance().getCurrentPose();
				EnvMap envMap = getModels().getEnvMapModel().getModelInstance();
				EnvMapNode node = envMap.getNode(currentPose.getX(), currentPose.getY(), 0.5);
				if (node != null) {
					String label = node.getLabel();
					LinkedList<String> neighbors = envMap.getNeighbors(label);
					for (String t : neighbors) {
						EnvMapArc arc = envMap.getArc(label, t);
						if (arc != null) {
							processDarkData(ill, arc);
						}
						arc = envMap.getArc(t, label);
						if (arc != null) {
							processDarkData(ill, arc);
						}
					}
				} else {
					EnvMapArc arc = envMap.getArc(currentPose.getX(), currentPose.getY());
					if (arc != null) {
						processDarkData(ill, arc);
					}
					arc = envMap.getArc(arc.m_target, arc.m_source);
					if (arc != null) {
						processDarkData(ill, arc);
					}
				}
			}
			if (ill < ILLUMINATION_THRESHOLD
					&& getModels().getTurtlebotModel().getActiveComponents().contains("marker_pose_publisher")
					&& !m_darkBefore) {
				log("DarknessAnalyzer reporting TOO_DARK");
				SetModelProblemCmd cmd = getModels().getRainbowStateModel().getCommandFactory()
						.setModelProblem(CP3ModelState.TOO_DARK);
				m_darkBefore = true;
				m_modelUSPort.updateModel(cmd);
			} else if (m_darkBefore && (ill >= ILLUMINATION_THRESHOLD
					|| !getModels().getTurtlebotModel().getActiveComponents().contains("marker_pose_publisher"))) {
				log("DarknessAnalyzer removing TOO_DARK");
				RemoveModelProblemCmd cmd = getModels().getRainbowStateModel().getCommandFactory()
						.removeModelProblem(CP3ModelState.TOO_DARK);
				m_darkBefore = false;
				m_modelUSPort.updateModel(cmd);
			}

		} catch (IllegalStateException e) {

		}
	}

	void processDarkData(double ill, EnvMapArc arc) {
		Map<String, LightingData> ldm = m_lightingData.get(arc);
		if (ldm == null) {
			ldm = new HashMap<String, LightingData>();
			m_lightingData.put(arc, ldm);
		}
		double factor = ill < ILLUMINATION_THRESHOLD ? 0.5 : 0.75;
		for (String c : ConfigurationSynthesizer.getLightSensitiveConfigs()) {
			LightingData ld = ldm.get(c);
			if (ld == null) {
				ld = new LightingData();
				ldm.put(c, ld);
			}
			Double osr = arc.getSuccessRate(c);

			if ((factor == 0.5 && !ld.m_badLightProcessed) || (factor == 0.75 && !ld.m_badLightProcessed)) {
				ld.m_litSuccessRate = osr;
				ld.m_badLightProcessed = true;
				ld.m_darkProcessed = factor == 0.5;
				arc.addSuccessRate(c, osr * factor);
			}

			else if (factor == 0.5 && !ld.m_darkProcessed) {
				ld.m_badLightSuccessRate = osr;
				ld.m_darkProcessed = true;
				arc.addSuccessRate(c, osr * factor);
			}
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
