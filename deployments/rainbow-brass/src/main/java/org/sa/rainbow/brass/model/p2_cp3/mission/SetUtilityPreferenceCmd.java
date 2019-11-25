package org.sa.rainbow.brass.model.p2_cp3.mission;

import org.sa.rainbow.brass.model.AbstractSimpleRainbowModelOperation;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState.UtilityPreference;
import org.sa.rainbow.core.error.RainbowException;

public class SetUtilityPreferenceCmd extends AbstractSimpleRainbowModelOperation<UtilityPreference, MissionState> {

	private UtilityPreference m_preference;

	public SetUtilityPreferenceCmd(String commandName, MissionStateModelInstance model,
			String target, String preference) {
		super(commandName, "setUtilityPreference", model, target, preference);
		m_preference = UtilityPreference.getValue(preference);
		if (m_preference == null)
			m_preference = UtilityPreference.valueOf(preference);
	}

	@Override
	protected void subExecute() throws RainbowException {
		getModelContext().getModelInstance().setUtilityPreference(m_preference);
		setResult(m_preference);
	}

}
