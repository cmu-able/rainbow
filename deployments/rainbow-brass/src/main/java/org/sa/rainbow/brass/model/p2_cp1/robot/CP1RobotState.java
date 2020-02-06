package org.sa.rainbow.brass.model.p2_cp1.robot;

import java.util.ArrayDeque;
import java.util.Deque;

import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.core.models.ModelReference;

public class CP1RobotState extends RobotState {

	Deque<TimeStamped<String>> m_configHistory = new ArrayDeque<>();
	
	public CP1RobotState(ModelReference model) {
		super(model);
	}
	
	public String getConfigId() {
		synchronized(m_configHistory) {
			TimeStamped<String> peek = m_configHistory.peek();
			return peek!=null?peek.data:"";
		}
	}
	
	public void setConfigId(String configId) {
		synchronized(m_configHistory) {
			m_configHistory.push(new TimeStamped<String>(configId));
		}
	}

}
