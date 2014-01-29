package incubator.scb.delta;

import incubator.pval.Ensure;
import incubator.scb.MergeableScb;
import incubator.scb.ScbField;

import java.util.Set;

/**
 * Delta that deletes a value from a set in an SCB field.
 * @param <T> the type of SCB
 * @param <V> the type of value in the set
 */
public class ScbSetSubDeleteDelta<T extends MergeableScb<T>, V>
		extends AbstractScbDelta<T> implements ScbDelDelta<T, V> {
	/**
	 * The field accessor.
	 */
	private ScbField<T, Set<V>> m_f;
	
	/**
	 * The value to delete.
	 */
	private V m_v;
	
	/**
	 * Creates a new delta.
	 * @param target the target SCB
	 * @param source the source SCB
	 * @param f used to access the field 
	 * @param v the value to delete
	 */
	public ScbSetSubDeleteDelta(T target, T source, ScbField<T, Set<V>> f,
			V v) {
		super(target, source);
		
		Ensure.not_null(f, "f == null");
		Ensure.not_null(v, "v == null");
		Ensure.is_true(f.can_set(), "Cannot set field");
		
		m_f = f;
		m_v = v;
	}
	
	@Override
	public void apply() {
		T t = target();
		Set<V> s = m_f.get(t);
		Ensure.is_true(s.contains(m_v), "Value does not exist in set");
		s.remove(m_v);
		m_f.set(t, s);
	}
	
	@Override
	public void revert() {
		T t = target();
		Set<V> s = m_f.get(t);
		Ensure.is_false(s.contains(m_v), "Value already exists in set");
		s.add(m_v);
		m_f.set(t, s);
	}
	
	@Override
	public V deleted() {
		return m_v;
	}
}
