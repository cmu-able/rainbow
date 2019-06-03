package org.sa.rainbow.gui.arch.model;

import java.awt.RenderingHints;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.gui.arch.controller.RainbowProbeController;
import org.sa.rainbow.util.Util;

public class RainbowSystemModel {
	
	public interface IRainbowModelVisitor {
		void visitSystem(RainbowSystemModel model);
		void postVisitSystem(RainbowSystemModel model);
		void visitProbe(RainbowArchProbeModel probe);
		void visitGauge(RainbowArchGaugeModel gauge);
		void visitModel(RainbowArchModelModel gauge);
		void visitAnalysis(RainbowArchAnalysisModel analysis);
		void visitAdaptationManager(RainbowArchAdapationManagerModel adaptationManager);
		void visitExecutor(RainbowArchExecutorModel executor);
		void visitEffector(RainbowArchEffectorModel effector);
	}

	protected Map<String, RainbowArchEffectorModel> m_effectors = new HashMap<>();
	protected Map<String, RainbowArchProbeModel> m_probes = new HashMap<>();
	protected Map<String, RainbowArchGaugeModel> m_gauges = new HashMap<>();
	protected Map<String, RainbowArchModelModel> m_models = new HashMap<>();
	protected Map<String, RainbowArchAnalysisModel> m_analyzers = new HashMap<>();
	protected Map<String, RainbowArchAdapationManagerModel> m_managers = new HashMap<>();
	protected Map<String, RainbowArchExecutorModel> m_executors = new HashMap<>();

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
		m_gauges.put(model.getId(), model);
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

	public void addModel(RainbowArchModelModel model) {
		m_models.put(model.getId(), model);
	}

	public boolean hasModel(String modelRef) {
		return m_models.containsKey(modelRef);
	}

	public RainbowArchModelModel getModel(String modelRef) {
		return m_models.get(modelRef);
	}

	public Collection<RainbowArchModelModel> getModels() {
		return m_models.values();
	}

	public void addAnalyzer(RainbowArchAnalysisModel model) {
		m_analyzers.put(model.getId(), model);
	}

	public boolean hasAnalyzer(String id) {
		return m_analyzers.containsKey(id);
	}

	public RainbowArchAnalysisModel getAnalyzer(String id) {
		return m_analyzers.get(id);
	}

	public Collection<RainbowArchAnalysisModel> getAnalyzers() {
		return m_analyzers.values();
	}

	public void addAdaptationManager(RainbowArchAdapationManagerModel model) {
		m_managers.put(model.getId(), model);
	}

	public RainbowArchAdapationManagerModel getAdaptationManager(String id) {
		return m_managers.get(id);
	}

	public boolean hasAdaptationManager(String id) {
		return m_managers.containsKey(id);
	}

	public Collection<RainbowArchAdapationManagerModel> getAdaptationManagers() {
		return m_managers.values();
	}

	public void addExecutor(RainbowArchExecutorModel model) {
		m_executors.put(model.getId(), model);
	}

	public boolean hasExecutor(String id) {
		return m_executors.containsKey(id);
	}

	public RainbowArchExecutorModel getExecutor(String id) {
		return m_executors.get(id);
	}

	public Collection<RainbowArchExecutorModel> getExecutors() {
		return m_executors.values();
	}

	public RainbowArchModelElement getRainbowElement(String id) {
		RainbowArchModelElement el = getProbe(id);
		if (el == null) el = getGauge(id);
		if (el == null) el = getModel(id);
		if (el == null) el = getAnalyzer(id);
		if (el == null) el = getAdaptationManager(id);
		if (el == null) el = getExecutor(id);
		if (el == null) el = getEffectorModel(id);
		return el;
	}
	
	public void visit(IRainbowModelVisitor visitor) {
		
		visitor.visitSystem(this);
		
		for (RainbowArchProbeModel p : m_probes.values()) {
			visitor.visitProbe(p);
		}
		
		for (RainbowArchGaugeModel g : m_gauges.values()) {
			visitor.visitGauge(g);
		}
		
		for (RainbowArchModelModel m : m_models.values()) {
			visitor.visitModel(m);
		}
		
		for (RainbowArchAnalysisModel a : m_analyzers.values()) {
			visitor.visitAnalysis(a);
		}
		
		for (RainbowArchAdapationManagerModel a : m_managers.values()) {
			visitor.visitAdaptationManager(a);
		}
		
		for (RainbowArchExecutorModel e : m_executors.values()) {
			visitor.visitExecutor(e);
		}
		
		for (RainbowArchEffectorModel e : m_effectors.values()) {
			visitor.visitEffector(e);
		}
		visitor.postVisitSystem(this);
	}

}
