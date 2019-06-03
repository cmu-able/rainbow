package org.sa.rainbow.core.ports.guava;

import java.util.List;

import org.sa.rainbow.translator.effectors.IEffector;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort;

public class LocalThreadedEffectorExecutionPort implements IEffectorExecutionPort {

	private IEffector m_effector;
	protected Outcome m_outcome;

	public LocalThreadedEffectorExecutionPort() {
	}
	
	void setEffector (IEffector effector) {
		m_effector = effector;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Outcome execute(List<String> args) {
		if (m_effector == null) return Outcome.CONFOUNDED;
		synchronized (this) {
			m_outcome = Outcome.UNKNOWN;
			Thread t = new Thread(new Runnable() {
				
				@Override
				public void run() {
					synchronized(LocalThreadedEffectorExecutionPort.this) {
						m_outcome = m_effector.execute(args);
						LocalThreadedEffectorExecutionPort.this.notifyAll();
					}
				}
			}, m_effector.id());
			t.start();
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return m_outcome;
		}
	}

}
