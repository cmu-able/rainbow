package org.sa.rainbow.brass.model.clock;

import org.sa.rainbow.core.models.ModelReference;

public class Clock {
	private ModelReference m_model;
	private double m_currentTime = 0.0;
	
	public Clock (ModelReference model) {
		m_model = model;
	}
	
	public void setCurrentTime (double time) {
		m_currentTime = time;
	}
	
	public double currentTime() {
		return m_currentTime;
	}
	
	public Clock copy() {
		Clock c = new Clock(m_model);
		c.m_currentTime = m_currentTime;
		return c;
	}
	
	public ModelReference getModelReference() {
		return m_model;
	}
}
