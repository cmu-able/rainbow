package org.sa.rainbow.gui.arch.model;

import java.awt.RenderingHints;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.gui.arch.controller.RainbowProbeController;
import org.sa.rainbow.util.Util;

public class RainbowSystemModel {

	protected Map<String,RainbowArchEffectorModel> m_effectors = new HashMap<>();
	protected Map<String,RainbowArchProbeModel> m_probes = new HashMap<>();
	protected Map<String,RainbowArchGaugeModel> m_gauges = new HashMap<>();
	
	public void addEffector(RainbowArchEffectorModel m) {
		m_effectors.put(m.getId(), m);
	}
	
	public Collection<RainbowArchEffectorModel> getEffectors() {
		return m_effectors.values();
	}

	public RainbowArchEffectorModel getEffectorModel(String id) {
		return m_effectors.get(id);
	}

	public boolean hasEffector(EffectorAttributes ea) {
		String genID = Util.genID(ea.name, ea.location);
		return m_effectors.containsKey(genID);
	}

	public boolean hasEffector(String id) {
		return m_effectors.containsKey(id);

	}

	public boolean hasProbe(String probeId) {
		return m_probes.containsKey(probeId);
	}

	public RainbowArchProbeModel getProbe(String pid) {
		return m_probes.get(pid);
	}

	public void addProbe(RainbowArchProbeModel model) {
		m_probes.put(model.getId(), model);
	}

	public Collection<RainbowArchProbeModel> getProbes() {
		return m_probes.values();
	}

	public void addGauge(RainbowArchGaugeModel model) {
		m_gauges.put(model.getId(),model);
	}
	
	public boolean hasGauge(String gid) {
		return m_gauges.containsKey(gid);
	}
	
	public RainbowArchGaugeModel getGauge(String gid) {
		return m_gauges.get(gid);
	}
	
	public Collection<RainbowArchGaugeModel> getGauges() {
		return m_gauges.values();
	}
	
}
