package org.sa.rainbow.brass.gauges.p2_cp3;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sa.rainbow.brass.model.p2_cp3.CP3ModelAccessor;
import org.sa.rainbow.brass.model.p2_cp3.ICP3ModelAccessor;
import org.sa.rainbow.brass.model.p2_cp3.acme.TurtlebotModelInstance.ActiveT;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.AbstractGaugeWithProbes;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.translator.probes.IProbeIdentifier;

public class ArchitectureGauge extends AbstractGaugeWithProbes {

	Set<String> m_nodesFromProbes = new HashSet<> ();
	
	Set<String> m_currentlyActiveNodes = new HashSet<> ();
	
	boolean m_reconfiguring = false;

	private Map<String, String> m_nodeToArch;
	private boolean m_newReport = false;

	private Boolean m_adapting = false;

	private IModelsManagerPort m_modelsPort;

	private ICP3ModelAccessor m_models;
	
	public ArchitectureGauge(String id, long beaconPeriod, TypedAttribute gaugeDesc,
			TypedAttribute modelDesc, List<TypedAttributeWithValue> setupParams,
			Map<String, IRainbowOperation> mappings) throws RainbowException {
		super("Architecture Gauge", id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
	}
	
	@Override
	public void reportFromProbe(IProbeIdentifier probe, String data) {
		super.reportFromProbe(probe, data);
//		if (isRainbowAdapting()) return;
		synchronized (m_nodesFromProbes) {
			m_nodesFromProbes.clear();
			String[] nodes = data.split("\\s+");
			m_nodesFromProbes.addAll(Arrays.asList(nodes));
			m_newReport = true;
		}
	}
	
	@Override
	protected void handleConfigParam(TypedAttributeWithValue tav) {
		super.handleConfigParam(tav);
		
	}
	
	@Override
	protected void runAction() {
		if (m_models == null) {
			try {
				m_modelsPort = RainbowPortFactory.createModelsManagerRequirerPort();
				m_models = new CP3ModelAccessor(m_modelsPort);
			} catch (RainbowConnectionException e) {
				e.printStackTrace();
			}
		}
		super.runAction();

		if (/*isRainbowAdapting() || */!m_newReport) return;
		if (m_nodeToArch == null) {
			m_nodeToArch = new HashMap<> ();
			String mapping = getSetupValue("mapping", String.class);
			String[] mappings = mapping.split(",");
			for (String m : mappings) {
				String[] map = m.split("=");
				m_nodeToArch.put(map[0].trim(), map[1].trim());
			}
		}
		HashSet<String> newNodes = new HashSet<String> ();
		HashSet<String> goneNodes = new HashSet<String> ();
		synchronized (m_nodesFromProbes) {
			newNodes.addAll(m_nodesFromProbes);
			newNodes.removeAll(m_currentlyActiveNodes);
			
			goneNodes.addAll(m_currentlyActiveNodes);
			goneNodes.removeAll(m_nodesFromProbes);
			
			m_currentlyActiveNodes.clear();
			m_currentlyActiveNodes.addAll(m_nodesFromProbes);
			m_newReport = false;
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
		String active = m_models.getMissionStateModel().getModelInstance().isReconfiguring()?ActiveT.INACTIVE.name():ActiveT.FAILED.name();

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
		if (!ops.isEmpty())
			issueCommands(ops, params);
	}

}
