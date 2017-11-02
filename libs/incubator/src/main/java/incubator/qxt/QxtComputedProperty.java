package incubator.qxt;

import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Property whose value is computed from a bean's data. The actual
 * computation performed is left for subclasses.
 * 
 * @param <T> the data type of the computed property
 */
public abstract class QxtComputedProperty<T> extends AbstractQxtProperty<T> {
	/**
	 * Creates a new computed property.
	 * 
	 * @param name the property name
	 * @param description the property description
	 * @param propertyType the property type
	 */
	public QxtComputedProperty(String name, String description,
			Class<T> propertyType) {
		super(name, description, propertyType);
	}

	@Override
	public boolean isEditable(Object bean) {
		return false;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public void setReadOnly(boolean ro) {
		if (!ro) {
			throw new IllegalArgumentException("Computed properties cannot "
					+ "be set read/write.");
		}
	}

	@Override
	public void setValue(Object bean, Object value) {
		throw new UnsupportedOperationException("Cannot set the value of "
				+ "computed properties.");
	}

	@Override
	void setup(TableColumnExt tc) {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	public boolean supportsTab() {
		return false;
	}

	@Override
	<E> void init(Class<E> beanClass, QxtTableModel<E> model) {
		/*
		 * Nothing to do.
		 */
	}
}
