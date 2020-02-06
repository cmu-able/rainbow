package incubator.scb.delta;

import incubator.pval.Ensure;
import incubator.scb.MergeableScb;
import incubator.scb.ScbField;

/**
 * SCB delta that applies a change to a field.
 * @param <T> the type of SCB
 * @param <V> the type of field
 */
public class ScbFieldDelta<T extends MergeableScb<T>, V>
		extends AbstractScbDelta<T> {
	/**
	 * The field.
	 */
	private ScbField<T, V> m_f;
	
	/**
	 * The old value.
	 */
	private V m_o;
	
	/**
	 * The new value.
	 */
	private V m_n;
	
	/**
	 * Creates a new delta.
	 * @param t the target of the change
	 * @param s the source SCB
	 * @param f the field being changed
	 * @param o the field's old value
	 * @param n the field's new value
	 */
	public ScbFieldDelta(T t, T s, ScbField<T, V> f, V o, V n) {
		super(t, s);
		Ensure.not_null(f, "f == null");
		Ensure.is_true(f.can_set(), "Cannot set field.");
		
		m_f = f;
		m_o = o;
		m_n = n;
	}
	
	@Override
	public void apply() {
		T t = target();
		Ensure.equals(m_o, m_f.get(t), "Old value not what was expected.");
		m_f.set(t, m_n);
	}
	
	@Override
	public void revert() {
		T t = target();
		Ensure.equals(m_n, m_f.get(t), "New value not what was expected.");
		m_f.set(t, m_o);
	}
}
