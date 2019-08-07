package org.sa.rainbow.evaluator.utility;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.core.IRainbowMaster;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.UtilityPreferenceDescription;
import org.sa.rainbow.core.models.UtilityPreferenceDescription.UtilityAttributes;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.utility.UtilityModelInstance;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.core.Tactic;
import org.sa.rainbow.stitch.visitor.Stitch;
import org.sa.rainbow.util.IRainbowConfigurationChecker;
import org.sa.rainbow.util.RainbowConfigurationChecker.Problem;
import org.sa.rainbow.util.RainbowConfigurationChecker.ProblemT;

public class UtilityConfigurationChecker implements IRainbowConfigurationChecker {

	private IRainbowMaster m_master;
	private List<Problem> m_problems;
	private Collection<UtilityModelInstance> m_utilityModels;

	public UtilityConfigurationChecker() {
		m_problems = new LinkedList<Problem>();
		m_master = Rainbow.instance().getRainbowMaster();
	}

	@Override
	public void checkRainbowConfiguration() {
		loadUtilityModels();
		checkUtilityPreferencesConfiguration();
		checkScenariosConfiguration();
		checkImpactVectors();
	}

	private void checkImpactVectors() {
		Set<String> tactics = new HashSet<>();
		for (Stitch s : Ohana.instance().listStitches()) {
			for (Tactic t : s.script.tactics) {
				tactics.add(t.getName());
			}
		}

		for (UtilityModelInstance umi : m_utilityModels) {
			UtilityPreferenceDescription preferenceDesc = umi.getModelInstance();
			for (Entry<String, Map<String, Object>> av : preferenceDesc.attributeVectors.entrySet()) {
				if (!tactics.remove(av.getKey())) {
					m_problems.add (new Problem(ProblemT.ERROR, 
							MessageFormat.format("The tactic '{0}' in {1} does not exist.", av.getKey(), umi.getOriginalSource())));
				}
				for (String u : av.getValue().keySet()) {
					if (!preferenceDesc.getUtilities().containsKey(u)) {
						m_problems.add (new Problem(ProblemT.ERROR, 
								MessageFormat.format("The utility '{0}' for impact vector for {2} is not defined in {1}.", u, umi.getOriginalSource(), av.getKey())) );
					}
				}
			}
		}
	}

	private void checkScenariosConfiguration() {
		for (UtilityModelInstance umi : m_utilityModels) {
			UtilityPreferenceDescription preferenceDesc = umi.getModelInstance();
			for (Entry<String, Map<String, Double>> scenario : preferenceDesc.weights.entrySet()) {
				int sum = 0;
				for (Double d : scenario.getValue().values()) {
					sum += d;
				}
				if (sum != 1) {
					m_problems.add (new Problem(ProblemT.ERROR, 
							MessageFormat.format("The weights in scenario {0} in {1} do not sum to 1.", scenario.getKey(), umi.getOriginalSource())));
				}
			}
		}
	}

	private void checkUtilityPreferencesConfiguration() {
		for (UtilityModelInstance umi : m_utilityModels) {
			UtilityPreferenceDescription preferenceDesc = umi.getModelInstance();
			Pattern p = Pattern.compile("(?:\\[(.*)\\])?(.*)");
			// Find associated model
			IModelInstance<?> assocModel = m_master.modelsManager().getModelInstance(preferenceDesc.associatedModel);
			if (assocModel instanceof AcmeModelInstance) {
				AcmeModelInstance ami = (AcmeModelInstance) assocModel;
				for (Entry<String, UtilityAttributes> av : preferenceDesc.getUtilities().entrySet()) {
					UtilityAttributes value = av.getValue();
					Matcher matcher = p.matcher(value.mapping);
					if (matcher.matches()) {
						String type = matcher.group(1);
						String expr = matcher.group(2);
						switch (type) {
						case "[EXPR]":
						case "[EAvg]":
						default:
							Object property = ami.getProperty(value.mapping);
							if (property == null) {
								m_problems.add(new Problem(ProblemT.ERROR,
										MessageFormat.format(
												"'{0}' cannot be resolved in the model {1}:{2} from utility model {3}",
												value.mapping, ami.getModelName(), ami.getModelType(),
												umi.getOriginalSource())));
							}
							break;
						}
					}
					for (Number domain: value.values.values()) {
						if (domain.doubleValue() > 1.0 || domain.doubleValue() < 0) {
							m_problems.add(new Problem(ProblemT.ERROR,
									MessageFormat.format(
											"'{0}' must be in the range [0,1] in utility range for utility {1} from utility model {2}",
											domain.doubleValue(), value.label,
											umi.getOriginalSource())));
						}
					}

				}
			}
			else {
				m_problems.add(new Problem(ProblemT.ERROR, MessageFormat.format("The model referred to in this utility model: '{0}:{1}' is not an Acme model, from {2}", assocModel.getModelName(), assocModel.getModelType(), umi.getOriginalSource())));
			}

		}

	}

	protected void loadUtilityModels() {
		m_utilityModels = (Collection<UtilityModelInstance>) m_master.modelsManager()
				.getModelsOfType(UtilityModelInstance.UTILITY_MODEL_TYPE);
		if (m_utilityModels.isEmpty())
			m_problems.add(new Problem(ProblemT.WARNING, "There are no utility models even though the utility package is installed."));
	}

	@Override
	public void setRainbowMaster(IRainbowMaster master) {
		m_master = master;
	}

	@Override
	public Collection<Problem> getProblems() {
		return m_problems;
	}

}
