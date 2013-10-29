package incubator.scb;

import incubator.pval.Ensure;

/**
 * SCB field whose value is a composition of two fields. An outer field obtains
 * a value that is used as object for an inner field.
 * @param <T> the type of the inner object
 * @param <V> the type of the inner field value
 * @param <U> the type of the outer object
 */
public class ScbCompositionField<T, V, U> extends ScbField<U, V> {
	/**
	 * Field that accesses the inner object in the outer object.
	 */
	private ScbField<U, T> m_outer;
	
	/**
	 * Field the accesses the value of the field in the inner object.
	 */
	private ScbField<T, V> m_inner;
	
	/**
	 * Creates a new composition.
	 * @param name the name of the composite field
	 * @param outer the field giving access to the inner object as a field of
	 * the outer object
	 * @param inner the field of the inner object to obtain
	 * @param help an optional help
	 */
	public ScbCompositionField(String name, ScbField<U, T> outer,
			ScbField<T, V> inner, String help) {
		super(name, Ensure.not_null(inner).can_set(), help,
				inner.value_type());
		Ensure.not_null(outer);
		
		m_outer = outer;
		m_inner = inner;
	}

	@Override
	public void set(U u, V value) {
		T t = m_outer.get(u);
		if (t == null) {
			throw new NullPointerException("null inner object: cannot set "
					+ "field value.");
		}
		
		m_inner.set(t, value);
	}

	@Override
	public V get(U u) {
		T t = m_outer.get(u);
		if (t == null) {
			throw new NullPointerException("null inner object: cannot get "
					+ "field value.");
		}
		
		return m_inner.get(t);
	}
	
	/**
	 * Obtains the outer field.
	 * @return the outer field
	 */
	public ScbField<U, T> outer() {
		return m_outer;
	}
	
	/**
	 * Obtains the inner field.
	 * @return the inner field
	 */
	public ScbField<T, V> inner() {
		return m_inner;
	}
}
