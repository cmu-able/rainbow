package org.sa.rainbow.brass.analyses.p2_cp3;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Stack;

import org.acmestudio.acme.core.exception.AcmeException;
import org.acmestudio.acme.element.IAcmeDesignRule;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.environment.error.AcmeError;
import org.acmestudio.acme.rule.node.feedback.ExpressionEvaluationError;
import org.acmestudio.acme.type.verification.NodeScopeLookup;
import org.acmestudio.acme.type.verification.RuleTypeChecker;
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
	private boolean m_wasOK;

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
		TurtlebotModelInstance tb = getModels().getTurtlebotModel();
		MissionState ms = getModels().getMissionStateModel().getModelInstance();

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
			IAcmeSystem tbs = tb.getModelInstance();
			IAcmeDesignRule localization = tbs.getDesignRule("atLeastOneActiveLocalization");
			IAcmeDesignRule navigation = tbs.getDesignRule("atLeastOneActiveNavigation");
			IAcmeDesignRule instructionGraph = tbs.getDesignRule("atLeastOneActiveIG");
			IAcmeDesignRule mapServer = tbs.getDesignRule("atLeastOneActiveMapServer");

			IAcmeDesignRule[] rules = new IAcmeDesignRule[] { localization, navigation, instructionGraph, mapServer };

			Stack<AcmeError> errors = new Stack<>();
			boolean ok = true;
			for (IAcmeDesignRule r : rules) {
				try {
					ok &= RuleTypeChecker.evaluateAsBoolean(tbs, r, r.getDesignRuleExpression(), errors,
							new NodeScopeLookup());
				} catch (AcmeException e) {
					errors.push(new ExpressionEvaluationError(tbs, r, r.getDesignRuleExpression(), e.getMessage()));
				}
			}
			if (!ok && m_wasOK) {
				m_wasOK = false;
				SetModelProblemCmd cmd = getModels().getRainbowStateModel ().getCommandFactory ().setModelProblem(CP3ModelState.ARCHITECTURE_ERROR);
				m_modelUSPort.updateModel(cmd);
			}
			else if (ok && !m_wasOK) {
				m_wasOK = true;
				RemoveModelProblemCmd cmd = getModels().getRainbowStateModel().getCommandFactory ().removeModelProblem(CP3ModelState.ARCHITECTURE_ERROR);
				m_modelUSPort.updateModel(cmd);
			}
		}
	}

}
