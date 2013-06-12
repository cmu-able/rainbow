package incubator.qxt;

import java.util.ArrayList;
import java.util.List;

import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Common structure of a <code>qxt</code> property.
 * 
 * @param <T> the property type
 */
public abstract class AbstractQxtProperty<T> {
	/**
	 * The bean property name.
	 */
	private final String name;

	/**
	 * The property display name.
	 */
	private String display;

	/**
	 * Class of the property type.
	 */
	private final Class<T> propertyClass;

	/**
	 * Listeners of the property.
	 */
	private List<AbstractQxtPropertyListener> listeners;

	/**
	 * Creates a new property (which is read only if the property is not
	 * writable).
	 * 
	 * @param name the property name (simple or compound)
	 * @param display the property display name
	 * @param propertyClass the class of the property type. This should not be a
	 * primitive type
	 */
	public AbstractQxtProperty(String name, String display,
			Class<T> propertyClass) {
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}

		if (name.trim().length() == 0) {
			throw new IllegalArgumentException("Empty name");
		}

		if (display == null) {
			throw new IllegalArgumentException("display == null");
		}

		if (propertyClass == null) {
			throw new IllegalArgumentException("propertyClass == null");
		}

		if (propertyClass.isPrimitive()) {
			throw new IllegalArgumentException("propertyClass is a primitive "
					+ "type.");
		}

		this.name = name;
		this.display = display;
		this.propertyClass = propertyClass;
		this.listeners = new ArrayList<>();
	}

	/**
	 * Obtains the value of the property on a bean.
	 * 
	 * @param bean the bean (may be <code>null</code>)
	 * 
	 * @return <code>null</code> if the bean is <code>null</code>, any of the
	 * intermediate values is <code>null</code> (if the property is a composite)
	 * or if the property value itself is <code>null</code>. Otherwise returns
	 * the property value
	 */
	public abstract T getValue(Object bean);

	/**
	 * Determines whether the property is editable for a given bean. A property
	 * is editable if it is not read only, the bean is defined and all
	 * intermediate values are non-<code>null</code> (if the property is a
	 * compound property).
	 * 
	 * @param bean the bean
	 * 
	 * @return is the property editable?
	 */
	public abstract boolean isEditable(Object bean);

	/**
	 * Sets the value of a property on a bean. This method will fail if:
	 * <ul>
	 * <li>The bean is <code>null</code></li>
	 * <li>The property is compound and any of the intermediate values is
	 * <code>null</code></li>
	 * <li>The property is read-only</li>
	 * <li>Invoking the set method fails</li>
	 * </ul>
	 * 
	 * @param bean the bean (cannot be <code>null</code>)
	 * 
	 * @param value the value to set (can be <code>null</code>)
	 */
	public abstract void setValue(Object bean, Object value);

	/**
	 * Obtains the display name of the property.
	 * 
	 * @return the display name
	 */
	public String getDisplay() {
		return display;
	}

	/**
	 * Defines a new display name for the property.
	 * 
	 * @param display the new display name
	 */
	public void setDisplay(String display) {
		if (display == null) {
			throw new IllegalArgumentException("display == null");
		}

		if (display.equals(this.display)) {
			return;
		}

		this.display = display;

		for (AbstractQxtPropertyListener l : listeners) {
			l.propertyDescriptionChanged(this);
		}
	}

	/**
	 * Determines if the property is read only.
	 * 
	 * @return is read only?
	 */
	public abstract boolean isReadOnly();

	/**
	 * Obtains the class type of this property.
	 * 
	 * @return the class
	 */
	public Class<T> getPropertyClass() {
		return propertyClass;
	}

	/**
	 * Obtains the property name.
	 * 
	 * @return the property name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Adds a new listener to the property.
	 * 
	 * @param l the listener
	 */
	public void addAbstractQxtPropertyListener(AbstractQxtPropertyListener l) {
		if (l == null) {
			throw new IllegalArgumentException("l == null");
		}

		listeners.add(l);
	}

	/**
	 * Remvoes a listener from the property.
	 * 
	 * @param l the listener
	 */
	public void removeAbstractQxtPropertyListener(AbstractQxtPropertyListener l) {
		if (l == null) {
			throw new IllegalArgumentException("l == null");
		}

		if (!listeners.remove(l)) {
			throw new IllegalStateException("Listener not registered.");
		}
	}

	/**
	 * Sets whether the property is read only or not. The property may only be
	 * set as non-read only if a write method is defined.
	 * 
	 * @param ro should the property be set as read only?
	 */
	public abstract void setReadOnly(boolean ro);

	/**
	 * Does the editor associated with this property supports tab-switching?
	 * 
	 * @return supports tab switching?
	 */
	public abstract boolean supportsTab();

	/**
	 * Prepares the table column for use with this property.
	 * 
	 * @param tc the table column
	 */
	abstract void setup(TableColumnExt tc);

	/**
	 * Initializes the property. This method should be called as soon as the
	 * bean class is known.
	 * 
	 * @param <E> the bean type
	 * @param beanClass the bean class
	 * @param model the data model
	 */
	abstract <E> void init(Class<E> beanClass, QxtTableModel<E> model);
}
