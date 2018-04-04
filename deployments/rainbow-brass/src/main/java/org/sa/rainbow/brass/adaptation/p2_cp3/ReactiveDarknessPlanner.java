package org.sa.rainbow.brass.adaptation.p2_cp3;

import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;

import org.sa.rainbow.brass.adaptation.BrassPlan;
import org.sa.rainbow.brass.model.p2_cp3.ModelAccessor;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState.CP3ModelState;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowStateModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotStateModelInstance;
import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.brass.model.robot.RobotStateModelInstance;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.adaptation.IEvaluable;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowAdaptationEnqueuePort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

public class ReactiveDarknessPlanner extends AbstractRainbowRunnable implements IAdaptationManager<BrassPlan> {

	private IModelsManagerPort m_modelsManagerPort;
	private boolean m_adaptationEnabled;
	private ModelReference m_modelRef;
	private IRainbowAdaptationEnqueuePort<BrassPlan> m_adaptationNQPort;
	private long m_waitForEffect = 0;
	private RainbowStateModelInstance m_rainbowStateModel;
	private CP3RobotStateModelInstance m_robotStateModel;
	private boolean m_executingPlan;
	private ModelAccessor m_models;

	public ReactiveDarknessPlanner() {
		super("Hello Darkness my old friend");
		String per = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD);
        if (per != null) {
            setSleepTime (Long.parseLong (per));
        }
        else {
            setSleepTime (SLEEP_TIME);
        }
	}
	
	@Override
	public void initialize(IRainbowReportingPort port) throws RainbowConnectionException {
		super.initialize(port);
		initConnectors();
	}

	private void initConnectors() throws RainbowConnectionException {
		m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort();
		m_models = new ModelAccessor(m_modelsManagerPort);
	}

	@Override
	public void dispose() {
		m_adaptationEnabled = false;
	}

	@Override
	protected void log(String txt) {
        m_reportingPort.info (RainbowComponentT.ADAPTATION_MANAGER, txt);
	}
	


	@Override
	protected void runAction() {

		if (m_waitForEffect > 0) {
			if (new Date().before(new Date(m_waitForEffect)))
				// Still waiting for effect.
				return;
			else m_waitForEffect = 0;
		}
	
		EnumSet<CP3ModelState> problems = m_models.getRainbowStateModel ().getModelInstance().getProblems();
		if (!problems.isEmpty() && !m_executingPlan) {
			StringBuffer ps = new StringBuffer ("Problems: ");
			for (CP3ModelState p : problems) {
				ps.append(p.name());
				ps.append(" ");
			}
			log(ps.toString());
			if (problems.contains(CP3ModelState.TOO_DARK)) {
				log("Let there be light!");
				TurnOnHeadlamp toh = new TurnOnHeadlamp(m_models.getRobotStateModel(), true);
				m_executingPlan = true;
				AdaptationTree<BrassPlan> at = new AdaptationTree<>(toh);
				m_adaptationNQPort.offerAdaptation(at, new Object[0]);
			}
		}
	}

	@Override
	public RainbowComponentT getComponentType() {
		return RainbowComponentT.ADAPTATION_MANAGER;
	}

	@Override
	public void setModelToManage(ModelReference modelRef) {
		m_modelRef = modelRef;
		m_adaptationNQPort = RainbowPortFactory.createAdaptationEnqueuePort(modelRef);
	}


	@Override
	public void markStrategyExecuted(AdaptationTree<BrassPlan> plan) {
		m_executingPlan = false;
		Calendar cal = Calendar.getInstance();
		Date d = cal.getTime();
		cal.add (Calendar.SECOND, 10);
		m_waitForEffect = cal.getTimeInMillis();
	}

	@Override
	public void setEnabled(boolean enabled) {
		m_adaptationEnabled = enabled;
	}

	@Override
	public boolean isEnabled() {
		return m_adaptationEnabled;
	}

}
