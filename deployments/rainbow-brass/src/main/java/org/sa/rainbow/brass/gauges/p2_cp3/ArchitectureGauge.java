package org.sa.rainbow.brass.gauges.p2_cp3;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sa.rainbow.brass.model.p2_cp3.acme.TurtlebotModelInstance.ActiveT;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.AbstractGaugeWithProbes;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.translator.probes.IProbeIdentifier;

public class ArchitectureGauge extends AbstractGaugeWithProbes {

	Set<String> m_nodesFromProbes = new HashSet<> ();
	
	Set<String> m_currentlyActiveNodes = new HashSet<> ();
	
	boolean m_reconfiguring = false;

	private Map<String, String> m_nodeToArch;
	
	protected ArchitectureGauge(String id, long beaconPeriod, TypedAttribute gaugeDesc,
			TypedAttribute modelDesc, List<TypedAttributeWithValue> setupParams,
			Map<String, IRainbowOperation> mappings) throws RainbowException {
		super("Architecture Gauge", id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
	}
	
	@Override
	public void reportFromProbe(IProbeIdentifier probe, String data) {
		super.reportFromProbe(probe, data);
		synchronized (m_nodesFromProbes) {
			m_nodesFromProbes.clear();
			String[] nodes = data.split("\n");
			m_nodesFromProbes.addAll(Arrays.asList(nodes));
		}
	}
	
	@Override
	protected void handleConfigParam(TypedAttributeWithValue tav) {
		super.handleConfigParam(tav);
		if (tav.getName().equals("mapping")) {
			String[] mappings = ((String )tav.getValue()).split(",");
			for (String m : mappings) {
				String[] map = m.split("=");
				m_nodeToArch.put(map[0].trim(), map[1].trim());
			}
		}
	}
	
	@Override
	protected void runAction() {
		HashSet<String> newNodes = new HashSet<String> ();
		HashSet<String> goneNodes = new HashSet<String> ();
		synchronized (m_nodesFromProbes) {
			newNodes.addAll(m_nodesFromProbes);
			newNodes.removeAll(m_currentlyActiveNodes);
			
			goneNodes.addAll(m_currentlyActiveNodes);
			goneNodes.removeAll(m_nodesFromProbes);
		}
		
		List<IRainbowOperation> ops = new LinkedList<> ();
		List<Map<String,String>> params = new LinkedList<>();
		
		for (String n : newNodes) {
			IRainbowOperation op = getCommand("set-active");
			Map<String, String> p = new HashMap<> ();
			String comp = m_nodeToArch.get(n);
			if (comp != null) {
				p.put(op.getTarget(), comp);
				p.put(op.getParameters()[0], ActiveT.ACTIVE.name());
				ops.add(op);
				params.add(p);
			}
		}
		String active = m_reconfiguring?ActiveT.INACTIVE.name():ActiveT.FAILED.name();

		for (String n : goneNodes) {
			IRainbowOperation op = getCommand("set-active");
			Map<String, String> p = new HashMap<>();
			String comp = m_nodeToArch.get(n);
			if (comp != null) {
				p.put(op.getTarget(), comp);
				p.put(op.getParameters()[0], active);
				ops.add(op);
				params.add(p);
			}
		}
			
		issueCommands(ops, params);
		super.runAction();
	}

}
