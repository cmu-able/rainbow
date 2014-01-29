package incubator.scb.scbset;

import incubator.pval.Ensure;
import incubator.scb.MergeableIdScb;
import incubator.scb.Scb;

/**
 * Immutable container for an SCB. The container is used to placed SCBs inside
 * collections that rely on <code>hashCode</code> not changing for an object.
 * @param <T> the type of SCB
 */
public class ScbIw<T extends Scb<T> & MergeableIdScb<T>> {
	/**
	 * The SCB.
	 */
	private T m_scb;
	
	/**
	 * Obtains the SCB.
	 * @param scb the SCB
	 */
	public ScbIw(T scb) {
		Ensure.not_null(scb, "scb == null");
		m_scb = scb;
	}
	
	/**
	 * Obtains the SCB.
	 * @return the SCB
	 */
	public T scb() {
		return m_scb;
	}
	
	@Override
	public int hashCode() {
		return m_scb.id();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ScbIw)) {
			return false;
		}
		
		ScbIw<?> other = (ScbIw<?>) obj;
		return other.scb().equals(m_scb);
	}
}
