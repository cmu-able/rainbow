package org.sa.rainbow.brass.analyses.p2_cp3;

import java.util.Collection;
import java.util.HashSet;

import org.sa.rainbow.brass.model.p2_cp3.clock.Clock;
import org.sa.rainbow.brass.model.p2_cp3.clock.ClockModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.clock.ClockedModel;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

/** This analyzer finds all models in the models manager that are clocked, and adds the
 * reference to the clock model. This needs to be run in the same VM as the Rainbow Master
 * because it cheats somewhat in setting up the clock (i.e., it doesn't use operations)
 * 
 * @author schmerl
 *
 */
public class UpdateClockModel extends AbstractRainbowRunnable implements IRainbowAnalysis {

	private IModelsManagerPort m_modelsManagerPort;
	private ClockModelInstance m_clockModel;
	private Collection<String> m_clockedModels = new HashSet<> ();

	public UpdateClockModel() {
		super("Add Clock to Models");
		String period = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD);
		if (period != null) {
			setSleepTime(Long.parseLong(period));
		} else {
			setSleepTime(IRainbowRunnable.LONG_SLEEP_TIME);
		}
	}

	@Override
	public void initialize(IRainbowReportingPort port) throws RainbowConnectionException {
		super.initialize(port);
		m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort();

	}
	@Override
	public void dispose() {
	}

	@Override
	public void setProperty(String key, String value) {
	}

	@Override
	public String getProperty(String key) {
		return null;
	}

	@Override
	protected void log(String txt) {
	}

	@Override
	protected void runAction() {
		if (m_clockModel == null) {
			m_clockModel = (ClockModelInstance )m_modelsManagerPort.<Clock>getModelInstance(new ModelReference("Clock","Clock"));
		}
		if (m_clockModel != null) {
			Collection<? extends String> types = Rainbow.instance().getRainbowMaster().modelsManager().getRegisteredModelTypes();
			for (String t : types) {
				Collection<? extends IModelInstance<?>> modelInstances = Rainbow.instance().getRainbowMaster().modelsManager().getModelsOfType(t);
				for (IModelInstance<?> mi : modelInstances) {
					if (mi.getModelInstance() instanceof ClockedModel && !m_clockedModels.contains(mi.getModelName() + ":" + mi.getModelType())) {
						log("Adding clock to " + mi.getModelName());
						((ClockedModel )mi.getModelInstance()).setClock(m_clockModel.getModelInstance());
						m_clockedModels.add (mi.getModelInstance() + ":" + mi.getModelType());
					}
				}
				
			}
		}
	}

	@Override
	public RainbowComponentT getComponentType() {
		return RainbowComponentT.ANALYSIS;
	}

}
