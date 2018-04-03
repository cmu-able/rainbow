package org.sa.rainbow.brass.model.p2_cp3.robot;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;

import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.core.models.ModelReference;

public class CP3RobotState extends RobotState {

	public static enum Sensors {KINECT, BACK_CAMERA, LIDAR, HEADLAMP}


	Deque<TimeStamped<EnumSet<Sensors>>> m_sensorHistory = new ArrayDeque<>();
	Deque<TimeStamped<EnumSet<Sensors>>> m_sensorFailedHistory = new ArrayDeque<>();
	Deque<TimeStamped<Boolean>> m_bumpState = new ArrayDeque<> ();
	Deque<TimeStamped<Double>>	m_lightingHistory = new ArrayDeque<>();
	private volatile boolean m_everBumped = false;;

	
	public CP3RobotState(ModelReference model) {
		super(model);
		setBumped(false);
		
	}



	public void setBumped(boolean bump) {
		synchronized(m_bumpState) {
			m_bumpState.push(new TimeStamped<Boolean>(bump));
			if (bump) m_everBumped = true;
		}
	}


	public boolean everBumped() {
		synchronized(m_bumpState) {
			return m_everBumped;
		}
	}


	public boolean bumpState() throws IllegalStateException {
		synchronized(m_bumpState) {
			TimeStamped<Boolean> peek = m_bumpState.peek();
			return peek.data;
		}
	}


	public void setSensor(Sensors sensor, boolean on) {
		synchronized (m_sensorHistory) {
			TimeStamped<EnumSet<Sensors>> peek = m_sensorHistory.peek();
			
			EnumSet<Sensors> currentState = peek!=null?peek.data:EnumSet.noneOf(Sensors.class);
			EnumSet<Sensors> nextState = EnumSet.<Sensors>copyOf(currentState);
			if (on &&  !currentState.contains(sensor)) {
				nextState.add(sensor);
				m_sensorHistory.push(new TimeStamped<EnumSet<Sensors>>(nextState));
			}
			else if (!on && currentState.contains(sensor)) {
				nextState.remove(sensor);
				m_sensorHistory.push(new TimeStamped<EnumSet<Sensors>>(nextState));
			}
		}
	}

	public void setSensorFailed(Sensors sensor) {
		synchronized (m_sensorFailedHistory) {
			TimeStamped<EnumSet<Sensors>> peek = m_sensorFailedHistory.peek();
			EnumSet<Sensors> currentState = peek!=null?peek.data:EnumSet.noneOf(Sensors.class);
			EnumSet<Sensors> nextState = EnumSet.<Sensors>copyOf(currentState);
			if (!currentState.contains(sensor)) {
				nextState.add(sensor);
				m_sensorFailedHistory.push(new TimeStamped<EnumSet<Sensors>>(nextState));
			}
		}
	}

	public EnumSet<Sensors> getAvailableSensors() throws IllegalStateException {
		synchronized (m_sensorFailedHistory) {
			TimeStamped<EnumSet<Sensors>> peek = m_sensorFailedHistory.peek();
			EnumSet<Sensors> currentState = peek!=null?peek.data:EnumSet.noneOf(Sensors.class);
			EnumSet<Sensors> available = EnumSet.allOf(Sensors.class);
			available.remove(Sensors.HEADLAMP);
			available.removeAll(currentState);
			return available;
		}
	}
	
	public EnumSet<Sensors> getSensors() throws IllegalStateException  {
		synchronized(m_sensorHistory) {
			TimeStamped<EnumSet<Sensors>> peek = m_sensorHistory.peek();
			return peek != null?EnumSet.<Sensors>copyOf(peek.data):EnumSet.<Sensors>noneOf(Sensors.class);
		}
	}


	public boolean isKinectOn() throws IllegalStateException {
		return getSensors().contains(Sensors.KINECT);
	}


	public boolean isLidarOn() throws IllegalStateException {
		return getSensors().contains(Sensors.LIDAR);
	}


	public boolean isBackCameraOn() throws IllegalStateException {
		return getSensors().contains(Sensors.BACK_CAMERA);
	}


	public boolean isHeadlampOn() throws IllegalStateException {
		return getSensors().contains(Sensors.HEADLAMP);
	}
	
	public void setIllumination(double ill) {
		synchronized (m_lightingHistory) {
			m_lightingHistory.push(new TimeStamped<Double>(ill));
			
		}
	}
	
	public double getIllumination() throws IllegalStateException {
		synchronized (m_lightingHistory) {
			TimeStamped<Double> peek = m_lightingHistory.peek();
			if (peek == null) throw new IllegalStateException ("No value for illumination has been set");
			return peek.data;
		}
	}
	
	@Override
	public CP3RobotState copy() {
		CP3RobotState c = new CP3RobotState(getModelReference());
		this.copyInto(c);
		return c;
	}
	
	@Override
	protected void copyInto(RobotState r) {
		super.copyInto(r);
		CP3RobotState c = (CP3RobotState )r;
		c.m_lightingHistory.addAll (m_lightingHistory);
		c.m_sensorHistory.addAll(m_sensorHistory);
		c.m_bumpState.addAll(m_bumpState);
	}

}
