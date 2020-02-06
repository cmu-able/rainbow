package org.sa.rainbow.brass.model.p2_cp3.clock;

import java.util.Date;
/**
 * A model that stores histories of values (like locations, etc.) shoud be
 * timestamped with respect to a clock. Therefore, all such models should inherit
 * this. If a clock is provided, this is used; otherwise we use system time.
 * @author schmerl
 *
 */
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
