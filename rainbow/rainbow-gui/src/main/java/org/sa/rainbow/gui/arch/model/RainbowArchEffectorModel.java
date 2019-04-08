package org.sa.rainbow.gui.arch.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.gui.arch.controller.AbstractRainbowController;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.util.Util;

public class RainbowArchEffectorModel extends RainbowArchModelElement {

	public static final String EFFECTOR_EXECUTING = "effectorExecuting";
	public static final String EFFECTOR_EXECUTED = "effectorExecuted";

	public static class EffectorExecutions {
		public List<String> args;
		public Outcome outcome;
		public long executionDuration; // If outcome == null, this is the time execution started
	}
	
	private EffectorAttributes m_effDesc;
	private List<EffectorExecutions> m_executions = new LinkedList<>(); // Most recent execution is at head

	public EffectorAttributes getEffectorAttributes() {
		return m_effDesc;
	}

	public RainbowArchEffectorModel(EffectorAttributes effDesc) {
		super();
		m_effDesc = effDesc;
	}
	
	 @Override
	public AbstractRainbowController getController() {
		return (AbstractRainbowController )super.getController();
	}
	 
	@Override
	public String getId() {
		return Util.genID(m_effDesc.name, m_effDesc.location);
	}

	public void executed(Outcome outcome, List<String> args) {
		EffectorExecutions ex = null;
		try {
		if (m_executions.isEmpty()) {
			ex = new EffectorExecutions();
			ex.args = args;
			ex.outcome = outcome;
			ex.executionDuration = 0;
			m_executions.add(0, ex);
			return;
		}
		
		ex = m_executions.get(0);
		if (ex == null) {
			ex = new EffectorExecutions();
			ex.args = args;
			ex.outcome = outcome;
			ex.executionDuration = 0;
			m_executions.add(0, ex);
		}
		else {
			ex.outcome = outcome;
			ex.executionDuration = new Date().getTime() - ex.executionDuration;
		}
		}
		finally {
			pcs.firePropertyChange(EFFECTOR_EXECUTED, null, ex);
		}
	}

	public void executing(List<String> args) {
		EffectorExecutions ex = null;
		try {
			ex = new EffectorExecutions();
			ex.args = args;
			ex.executionDuration = new Date().getTime();
			m_executions.add(0,ex);
		}
		finally {
			pcs.firePropertyChange(EFFECTOR_EXECUTING, null, ex);

		}
	}
	
	
	

}
