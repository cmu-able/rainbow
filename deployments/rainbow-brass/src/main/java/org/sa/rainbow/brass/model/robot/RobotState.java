package org.sa.rainbow.brass.model.robot;

import java.util.ArrayDeque;
import java.util.Deque;

import org.sa.rainbow.brass.model.p2_cp3.clock.ClockedModel;
import org.sa.rainbow.core.models.ModelReference;

public class RobotState extends ClockedModel {
	
	Deque<TimeStamped<Double>> m_chargeHistory = new ArrayDeque<>();
	Deque<TimeStamped<Double>> m_speedHistory = new ArrayDeque<>();
	private ModelReference m_model;
	public RobotState(ModelReference model) {
		m_model = model;
	}
	
	public ModelReference getModelReference() {
		return m_model;
	}
	
	public void setCharge(double charge) {
		synchronized (m_chargeHistory) {
			m_chargeHistory.push(new TimeStamped<Double> (charge));
		}
	}
	
	public double getCharge() throws IllegalStateException {
		synchronized (m_chargeHistory) {
			TimeStamped<Double> peek = m_chargeHistory.peek();
			if (peek == null) throw new IllegalStateException("No value for charge has been set yet");
			return peek.data;
		}
	}
	
	public void setSpeed(double speed) {
		synchronized (m_speedHistory) {
			m_speedHistory.push(new TimeStamped<Double> (speed));
		}
	}
	
	public double getSpeed() throws IllegalStateException {
		synchronized (m_speedHistory) {
			TimeStamped<Double> peek = m_speedHistory.peek();
			if (peek == null) 
				throw new IllegalStateException("No value for speed has been set yet");
			return peek.data;
		}
	}

	public RobotState copy() {
		RobotState r = new RobotState(getModelReference());
		this.copyInto(r);
		return r;
	}
	
	protected void copyInto(RobotState r) {
		r.m_chargeHistory.addAll(this.m_chargeHistory);
		r.m_speedHistory.addAll(this.m_speedHistory);
	}
	
	
	
	
	
	
	
}
