package org.sa.rainbow.brass.analyses.p2_cp3;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.acmestudio.acme.core.exception.AcmeException;
import org.acmestudio.acme.element.IAcmeDesignRule;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.environment.error.AcmeError;
import org.acmestudio.acme.rule.node.feedback.ExpressionEvaluationError;
import org.acmestudio.acme.type.verification.NodeScopeLookup;
import org.acmestudio.acme.type.verification.RuleTypeChecker;
import org.sa.rainbow.brass.model.instructions.IInstruction;
import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.KillNodesInstruction;
import org.sa.rainbow.brass.model.instructions.SetReconfiguringInstruction;
import org.sa.rainbow.brass.model.instructions.SetSensorInstruction;
import org.sa.rainbow.brass.model.instructions.StartNodesInstruction;
import org.sa.rainbow.brass.model.p2_cp3.acme.TurtlebotModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState.CP3ModelState;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RemoveModelProblemCmd;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.SetModelProblemCmd;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState.Sensors;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;

public class ConfigurationAnalyzer extends P2CP3Analyzer {

	private String m_lastPrintedLog;
	private boolean m_wasArchitectureOK = false;
	private boolean m_wasConfigurationOK = false;
	
	private static final Map<String,Set<String>>INST_TO_ARCH_MAP = new HashMap<>();
	static {
		INST_TO_ARCH_MAP.put("amcl", new HashSet<String>(Arrays.asList("amcl")));
		INST_TO_ARCH_MAP.put("mrpt", new HashSet<String>(Arrays.asList("mrpt")));
		INST_TO_ARCH_MAP.put("mapServer", new HashSet<String>(Arrays.asList("map_server")));
		INST_TO_ARCH_MAP.put("mapServerObs", new HashSet<String>(Arrays.asList("map_server_obs")));
		INST_TO_ARCH_MAP.put("aruco", new HashSet<String>(Arrays.asList("aruco_marker_publisher_back", "marker_pose_publisher","aruco_marker_publisher_front")));
		INST_TO_ARCH_MAP.put("laserscanNodelet", new HashSet<String>(Arrays.asList("laserScan_nodelet")));
	}

	public ConfigurationAnalyzer() {
		super("TurtlebotConfigurationAnalyzer");
		String period = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD);
		if (period != null) {
			setSleepTime(Long.parseLong(period));
		} else {
			setSleepTime(IRainbowRunnable.LONG_SLEEP_TIME);
		}
	}

	@Override
	protected void runAction() {
		CP3RobotState rs = getModels().getRobotStateModel().getModelInstance();
		MissionState ms = getModels().getMissionStateModel().getModelInstance();
		TurtlebotModelInstance tb = getModels().getTurtlebotModel();
		InstructionGraphModelInstance ig = getModels().getInstructionGraphModel();
		if (getModels().getRainbowStateModel().getModelInstance().waitForIG()) {
			m_wasArchitectureOK = true;
			m_wasConfigurationOK = true;
			return;
		}
		if (ig == null) return;
		
		EnumSet<Sensors> sensors = rs.getSensors();
		Collection<String> components = tb.getActiveComponents();
		StringBuffer log = new StringBuffer("Components: ");
		for (String c : components) {
			log.append(c);
			log.append(" ");
		}
		log.append("\nSensors: ");
		for (Sensors s : sensors) {
			log.append(s.name());
		}
		if (m_lastPrintedLog == null || !m_lastPrintedLog.equals(log.toString())) {
			m_lastPrintedLog = log.toString();
			log(m_lastPrintedLog);
		}

		if (ms.isMissionStarted()) {
			IInstruction currentInst = ig.getModelInstance().getCurrentInstruction();
			if (currentInst == null || currentInst instanceof SetSensorInstruction || currentInst instanceof StartNodesInstruction || currentInst instanceof KillNodesInstruction || currentInst instanceof SetReconfiguringInstruction) {
				// Currrently executing a command to change the configuration, so let's abort this
				return;
			}
			checkAcmeRules(tb);
			checkConfigurationConsistentWithIG(ig, rs, tb);
		}
	}

	private void checkConfigurationConsistentWithIG(InstructionGraphModelInstance ig, CP3RobotState rs, TurtlebotModelInstance tb) {
		Collection<? extends IInstruction> instructions = ig.getModelInstance().getInstructions();
		IInstruction currentInst = ig.getModelInstance().getCurrentInstruction();

		Iterator<? extends IInstruction> instIt = instructions.iterator();
		boolean reachedCurrentInstruction = false;
		EnumSet<Sensors> sensorsTurnedOn = EnumSet.noneOf(Sensors.class);
		EnumSet<Sensors> sensorsTurnedOff = EnumSet.noneOf(Sensors.class);
		Set<String> nodesTurnedOn = new HashSet<> ();
		Set<String> nodesTurnedOff = new HashSet<> ();
		while (instIt.hasNext() && !reachedCurrentInstruction) {
			IInstruction inst = instIt.next();
			if (inst.getInstructionLabel().equals(currentInst.getInstructionLabel())) {
				reachedCurrentInstruction = true;
				continue;
			}
			if (inst instanceof SetSensorInstruction) {
				SetSensorInstruction i = (SetSensorInstruction) inst;
				if (i.getEnablement()) {
					sensorsTurnedOn.add(i.getSensor());
					sensorsTurnedOff.remove(i.getSensor());
				}
			}
			else if (inst instanceof StartNodesInstruction) {
				StartNodesInstruction i = (StartNodesInstruction) inst;
				nodesTurnedOff.removeAll(INST_TO_ARCH_MAP.get(i.getNode()));
				nodesTurnedOn.addAll(INST_TO_ARCH_MAP.get(i.getNode()));
			}
			else if (inst instanceof KillNodesInstruction) {
				KillNodesInstruction i = (KillNodesInstruction) inst;
				nodesTurnedOff.addAll(INST_TO_ARCH_MAP.get(i.getNode()));
				nodesTurnedOn.removeAll(INST_TO_ARCH_MAP.get(i.getNode()));
			}
		}
		boolean configurationOK = true;
		EnumSet<Sensors> detectedSensors = rs.getSensors();
		Collection<String> activeComponents = tb.getActiveComponents();
		
		configurationOK = !Collections.disjoint(detectedSensors, EnumSet.of(Sensors.KINECT, Sensors.LIDAR, Sensors.CAMERA)) && detectedSensors.containsAll(sensorsTurnedOn) &&
				activeComponents.containsAll(nodesTurnedOn);
		if (configurationOK) {
			EnumSet<Sensors> clone = detectedSensors.clone();
			Set<String> clonea = new HashSet<> (activeComponents);
			clone.removeAll(sensorsTurnedOff);
			clonea.removeAll(nodesTurnedOff);
			// Are any turned off elements currently active
			configurationOK = clone.size() == detectedSensors.size() &&
					clonea.size () == activeComponents.size();
		}
		if (!configurationOK && m_wasConfigurationOK) {
			m_wasConfigurationOK = false;
			log("Configuration Analyzer reporting CONFIGURATION_ERROR");
			log("Sensors active: " + setToString(detectedSensors));
			log("Sensors in IG: " + setToString(sensorsTurnedOn) + " but not " + setToString(sensorsTurnedOff));
			log("Components active: " + setToString(activeComponents));
			log("Components in IG: " + setToString(nodesTurnedOn) + " but not " + setToString(nodesTurnedOff));
			SetModelProblemCmd cmd = getModels().getRainbowStateModel ().getCommandFactory ().setModelProblem(CP3ModelState.CONFIGURATION_ERROR);
			m_modelUSPort.updateModel(cmd);
		}
		else if (configurationOK && !m_wasConfigurationOK) {
			m_wasConfigurationOK = true;
			log("Configuration Analyzer removing CONFIGURATION_ERROR");
			RemoveModelProblemCmd cmd = getModels().getRainbowStateModel().getCommandFactory ().removeModelProblem(CP3ModelState.CONFIGURATION_ERROR);
			m_modelUSPort.updateModel(cmd);
		}
				
				
	}

	private String setToString(Collection<String> components) {
		StringBuffer sb = new StringBuffer("{");
		boolean first = true;
		for (String s : components) {
			if (!first) {first = false; sb.append(", ");}
			sb.append(s);
		}
		sb.append("}");
		return sb.toString();
	}

	private String setToString(EnumSet<Sensors> sensors) {
		StringBuffer sb = new StringBuffer("{");
		boolean first = true;
		for (Sensors s : sensors) {
			if (!first) {first = false; sb.append(", ");}
			sb.append(s.name());
		}
		sb.append("}");
		return sb.toString();
	}

	private void checkAcmeRules(TurtlebotModelInstance tb) {
		IAcmeSystem tbs = tb.getModelInstance();
		IAcmeDesignRule localization = tbs.getDesignRule("atLeastOneActiveLocalization");
		IAcmeDesignRule navigation = tbs.getDesignRule("atLeastOneActiveNavigation");
		IAcmeDesignRule instructionGraph = tbs.getDesignRule("atLeastOneActiveIG");
		IAcmeDesignRule mapServer = tbs.getDesignRule("atLeastOneActiveMapServer");

		IAcmeDesignRule[] rules = new IAcmeDesignRule[] { localization, navigation, instructionGraph, mapServer };

		Stack<AcmeError> errors = new Stack<>();
		boolean ok = true;
		Set<String> failingRules = new HashSet<>();
		for (IAcmeDesignRule r : rules) {
			try {
				boolean b = RuleTypeChecker.evaluateAsBoolean(tbs, r, r.getDesignRuleExpression(), errors,
						new NodeScopeLookup());
				if (!b) failingRules.add(r.getName());
				ok &= b;
			} catch (AcmeException e) {
				errors.push(new ExpressionEvaluationError(tbs, r, r.getDesignRuleExpression(), e.getMessage()));
			}
		}
		if (!ok && m_wasArchitectureOK) {
			m_wasArchitectureOK = false;
			log("ConfigurationAnalyzer reporting ARCHITECTURE_ERROR");
			log("The following rules failed: " + setToString(failingRules));
			SetModelProblemCmd cmd = getModels().getRainbowStateModel ().getCommandFactory ().setModelProblem(CP3ModelState.ARCHITECTURE_ERROR);
			m_modelUSPort.updateModel(cmd);
		}
		else if (ok && !m_wasArchitectureOK) {
			m_wasArchitectureOK = true;
			log("ConfigurationAnalyzer removing ARCHITECTURE_ERROR");
			RemoveModelProblemCmd cmd = getModels().getRainbowStateModel().getCommandFactory ().removeModelProblem(CP3ModelState.ARCHITECTURE_ERROR);
			m_modelUSPort.updateModel(cmd);
		}
	}

}
