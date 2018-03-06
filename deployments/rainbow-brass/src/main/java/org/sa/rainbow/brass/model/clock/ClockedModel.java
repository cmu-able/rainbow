package org.sa.rainbow.brass.model.clock;

import java.util.Date;

public class ClockedModel {

	private Clock m_clock = null;

	public void setClock(Clock clock) {
		m_clock = clock;
	}

	public double clockTime() {
		if (m_clock == null) {
			return new Date().getTime();
		}
		else 
			return m_clock.currentTime ();
	}
	
	public class TimeStamped<T> {
		public double timestamp;
		public T data;
		
		public TimeStamped(T d) {
			data = d;
			timestamp = clockTime();
		}
	}

}
