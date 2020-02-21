package org.sa.rainbow.brass.p3_cp1.gauges;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.AbstractGaugeWithProbes;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.translator.probes.IProbeIdentifier;

public class PowerModelChangeGauge extends AbstractGaugeWithProbes {

	private static final String NAME = "PowerModelGauge";

	public PowerModelChangeGauge(String id, long beaconPeriod, TypedAttribute gaugeDesc,
			TypedAttribute modelDesc, List<TypedAttributeWithValue> setupParams,
			Map<String, IRainbowOperation> mappings) throws RainbowException {
		super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
	}

	private String m_newFile = null;
	
	
	@Override
	public void reportFromProbe(IProbeIdentifier probe, String data) {
		super.reportFromProbe(probe, data);
		synchronized (this) {
			m_newFile = data;
		}
	}
	
	@Override
	protected void runAction() {
		super.runAction();
		String newFile = null;
		synchronized (this) {
			if (m_newFile != null) {
				newFile = m_newFile;
				m_newFile = null;
			}
		}
		if (newFile != null) {
			IRainbowOperation op = m_commands.get("update");
			Map<String,String> pMap = new HashMap<>();
			pMap.put(op.getParameters()[0], newFile);
			issueCommand(op, pMap);
		}
	}
}
