package incubator.scb.delta;

import incubator.pval.Ensure;
import incubator.scb.MergeableScb;

/**
 * Delta that performs changes in one SCB, the <em>target</em> of the change.
 * @param <T> the type of SCB
 */
public abstract class AbstractScbDelta<T extends MergeableScb<T>>
		implements ScbDelta<T> {
	/**
	 * The target of the change.
	 */
	private T m_target;
	
	/**
	 * The source of the change.
	 */
	private T m_source;
	
	/**
	 * Creates a new change.
	 * @param target the target of the change
	 * @param source the source of the change
	 */
	public AbstractScbDelta(T target, T source) {
		Ensure.not_null(target, "target == null");
		Ensure.not_null(source, "source == null");
		m_target = target;
		m_source = source;
	}
	
	@Override
	public T target() {
		return m_target;
	}
	
	@Override
	public T source() {
		return m_source;
	}
}
