package org.sa.rainbow.gui.arch.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.gui.arch.controller.RainbowModelController;

public class RainbowArchModelModel extends RainbowArchModelElement {

	private ModelReference m_modelRef;
	private Set<String> m_gauges = new HashSet<>();

	public RainbowArchModelModel(ModelReference m) {
		super();
		m_modelRef = m;
	}
	
	@Override
	public String getId() {
		return m_modelRef.toString();
	}
	
	public ModelReference getModelRef() {
		return m_modelRef;
	}

	public void addGaugeReference(String gid) {
		m_gauges.add(gid);
	}
	
	public Collection<String> getGaugeReferences() {
		return m_gauges;
	}
	
	@Override
	public RainbowModelController getController() {
		return (RainbowModelController) super.getController();
	}

}
