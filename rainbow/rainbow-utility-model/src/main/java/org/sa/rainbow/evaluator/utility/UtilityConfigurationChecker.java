package org.sa.rainbow.evaluator.utility;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.core.IRainbowMaster;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.UtilityPreferenceDescription;
import org.sa.rainbow.core.models.UtilityPreferenceDescription.UtilityAttributes;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.utility.UtilityModelInstance;
import org.sa.rainbow.util.IRainbowConfigurationChecker;
import org.sa.rainbow.util.RainbowConfigurationChecker.Problem;
import org.sa.rainbow.util.RainbowConfigurationChecker.ProblemT;

public class StitchConfigurationChecker implements IRainbowConfigurationChecker {

	private IRainbowMaster m_master;
	private List<Problem> m_problems;

	public StitchConfigurationChecker() {
		m_problems = new LinkedList<Problem>();
	}

	@Override
	public void checkRainbowConfiguration() {
		checkUtilityPreferencesConfiguration();
	}

	private void checkUtilityPreferencesConfiguration() {
		Collection<UtilityModelInstance> modelsOfType = (Collection<UtilityModelInstance>) m_master.modelsManager()
				.getModelsOfType(UtilityModelInstance.UTILITY_MODEL_TYPE);
		if (modelsOfType.isEmpty())
			m_problems.add(new Problem(ProblemT.WARNING, "There are no utility models even though the utility package is installed."));
		for (UtilityModelInstance umi : modelsOfType) {
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

				}
			}
			else {
				m_problems.add(new Problem(ProblemT.ERROR, MessageFormat.format("The model referred to in this utility model: '{0}:{1}' is not an Acme model, from {2}", assocModel.getModelName(), assocModel.getModelType(), umi.getOriginalSource())));
			}

		}

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
