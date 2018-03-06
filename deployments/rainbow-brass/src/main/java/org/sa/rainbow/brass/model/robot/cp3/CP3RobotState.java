package org.sa.rainbow.brass.model.robot.cp3;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;

import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.core.models.ModelReference;

public class CP3RobotState extends RobotState {

	public static enum Sensors {KINECT, BACK_CAMERA, LIDAR, HEADLAMP}


	Deque<TimeStamped<EnumSet<Sensors>>> m_sensorHistory = new ArrayDeque<>();
	Deque<TimeStamped<Boolean>> m_bumpState = new ArrayDeque<> ();
	private volatile boolean m_everBumped = false;;

	
	public CP3RobotState(ModelReference model) {
		super(model);
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


	public boolean bumpState() {
		synchronized(m_bumpState) {
			return m_bumpState.peek().data;
		}
	}


	public void setSensor(Sensors sensor, boolean on) {
		synchronized (m_sensorHistory) {
			EnumSet<Sensors> currentState = m_sensorHistory.peek().data;
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


	public EnumSet<Sensors> getSensors() {
		synchronized(m_sensorHistory) {
			return EnumSet.<Sensors>copyOf(m_sensorHistory.peek().data);
		}
	}


	public boolean isKinectOn() {
		return getSensors().contains(Sensors.KINECT);
	}


	public boolean isLidarOn() {
		return getSensors().contains(Sensors.LIDAR);
	}


	public boolean isBackCameraOn() {
		return getSensors().contains(Sensors.BACK_CAMERA);
	}


	public boolean isHeadlampOn() {
		return getSensors().contains(Sensors.HEADLAMP);
	}

}
