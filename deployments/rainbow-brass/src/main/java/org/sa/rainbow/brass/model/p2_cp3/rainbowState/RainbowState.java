package org.sa.rainbow.brass.model.p2_cp3.rainbowState;

import java.util.EnumSet;

import org.sa.rainbow.core.models.ModelReference;

public class RainbowState {
	public static enum CP3ModelState {
		TOO_DARK,
		INSTRUCTION_GRAPH_FAILED,
		OUT_OF_BATTERY,
		LOW_ON_BATTERY,
		ARCHITECTURE_ERROR
	}
	
	private ModelReference m_model;
	private EnumSet<CP3ModelState> m_problems;
	private boolean m_planIssued;
	
	public RainbowState(ModelReference model) {
		m_model = model;
		m_problems = EnumSet.noneOf(CP3ModelState.class);
	}
	
	synchronized void setModelProblem(CP3ModelState problem) {
		m_problems.add(problem);
	}
	
	synchronized void removeModelProblem(CP3ModelState problem) {
		m_problems.remove(problem);
	}
	
	synchronized void clearModelProblems() {
		m_problems.clear();
	}
	
	synchronized void setPlanIssued(boolean issued) {
		m_planIssued = issued;
	}
	
	synchronized public boolean isPlanIssued() {
		return m_planIssued;
	}

	public ModelReference getModelReference() {
		return m_model;
	}

	synchronized public RainbowState copy() {
		RainbowState rs = new RainbowState(getModelReference());
		rs.m_problems = EnumSet.copyOf(m_problems);
		rs.m_planIssued = m_planIssued;
		return rs;
	}

	public EnumSet<CP3ModelState> getProblems() {
		return m_problems;
	}
	
}
