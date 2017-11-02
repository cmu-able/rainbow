package incubator.qxt;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ClassUtils;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * <p>
 * Definition of an actual bean property. Bean properties can be simple
 * properties such as <code>name</code> (corresponding to methods
 * <code>getName()</code> and <code>setName</code>) but they can also be
 * compound properties such as <code>name.length</code> (corresponding to
 * methods <code>getName().getLength()</code> and
 * <code>getName().setLength()</code>).
 * </p>
 * <p>
 * Properties may be read only or read write. The writer methods need only
 * be available in the latter ones.
 * </p>
 * <p>
 * When getting the value for a compound property, if any of the
 * intermediate steps return <code>null</code>, <code>null</code> is
 * returned. Writing the value of a property will fail if any of the
 * intermediate values is <code>null</code>.
 * </p>
 * <p>
 * Properties as considered non-editable if they are read only or if any
 * intermediate value is <code>null</code> (and hence writing would fail).
 * </p>
 * <p>
 * Each property may only be used on one table.
 * </p>
 * 
 * @param <T> the bean type
 */
public class QxtRealProperty<T> extends AbstractQxtProperty<T> {

	/**
	 * The the property read only?
	 */
	private boolean readOnly;

	/**
	 * Bean class.
	 */
	private Class<?> beanClass;

	/**
	 * Method used to read the property.
	 */
	private Method readMethod;

	/**
	 * Method used to write to the property (may be <code>null</code> if the
	 * property is not writable).
	 */
	private Method writeMethod;

	/**
	 * Reader methods used to reach the bean which is the direct parent of
	 * the property. May be empty.
	 */
	private List<Method> path;

	/**
	 * Creates a new property (which is read only if the property is not
	 * writable).
	 * 
	 * @param name the property name (simple or compound)
	 * @param display the property display name
	 * @param propertyClass the class of the property type
	 */
	public QxtRealProperty(String name, String display,
			Class<T> propertyClass) {
		this(name, display, false, propertyClass);
	}

	/**
	 * Creates a new property.
	 * 
	 * @param name the property name (simple or compound)
	 * @param display the property display name
	 * @param readOnly should the property be read only? Note that a write
	 * method must be defined if this value is <code>false</code> or
	 * otherwise the property will be set as read only regardless of this
	 * parameter value
	 * @param propertyClass the class of the property type
	 */
	public QxtRealProperty(String name, String display, boolean readOnly,
			Class<T> propertyClass) {
		super(name, display, propertyClass);

		this.readOnly = readOnly;
	}

	@Override
	<E> void init(Class<E> beanClass, QxtTableModel<E> model) {
		if (beanClass == null) {
			throw new IllegalArgumentException("name == null");
		}

		if (this.beanClass != null) {
			throw new IllegalStateException("Bean class already defined.");
		}

		String[] names = getName().split("\\.");

		readMethod = null;
		path = new ArrayList<>();

		Class<?> current = beanClass;
		for (int i = 0; i < names.length; i++) {
			if (readMethod != null) {
				path.add(readMethod);
			}

			BeanInfo beanInfo;
			try {
				beanInfo = Introspector.getBeanInfo(current);
			} catch (IntrospectionException e) {
				throw new PropertyDefinitionException(
						"Failed to introspect " + "bean class '" + current
								+ "'.");
			}

			PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor pd : pds) {
				if (names[i].equals(pd.getName())) {
					readMethod = pd.getReadMethod();
					writeMethod = pd.getWriteMethod();

					current = pd.getPropertyType();
					current = ClassUtils.primitiveToWrapper(current);
				}
			}

			if (readMethod == null) {
				throw new PropertyDefinitionException("Property '"
						+ names[i] + "' is not readable in bean class '"
						+ current + "'.");
			}
		}

		if (writeMethod == null && !readOnly) {
			readOnly = true;
		}

		if (!getPropertyClass().isAssignableFrom(current)) {
			throw new PropertyDefinitionException("Property '" + getName()
					+ "' of bean class '" + beanClass + "' has type '"
					+ current + "' but '" + getPropertyClass()
					+ "' expected.");
		}

		this.beanClass = beanClass;
	}

	@Override
	public T getValue(Object bean) {
		if (beanClass == null) {
			throw new IllegalStateException("Property not yet initialized.");
		}

		if (bean == null) {
			return null;
		}

		if (beanClass != null
				&& !beanClass.isAssignableFrom(bean.getClass())) {
			throw new IllegalArgumentException("Bean '" + bean
					+ "' has type " + "'" + bean.getClass() + "' but '"
					+ beanClass + "' " + "was expected.");
		}

		try {
			bean = getContainer(bean);
			if (bean == null) {
				return null;
			}

			Object o = readMethod.invoke(bean);
			@SuppressWarnings("unchecked")
			T t = (T) o;
			return t;
		} catch (Exception e) {
			throw new PropertyAccessException("Failed to read property '"
					+ getName() + "' of bean '" + bean + "'.", e);
		}
	}

	@Override
	public boolean isEditable(Object bean) {
		if (beanClass == null) {
			throw new IllegalStateException("Property not yet initialized.");
		}

		bean = getContainer(bean);
		return bean != null && !isReadOnly();
	}

	@Override
	public void setValue(Object bean, Object value) {
		if (beanClass == null) {
			throw new IllegalStateException("Property not yet initialized.");
		}

		if (bean == null) {
			throw new PropertyAccessException("Cannot access property of a "
					+ "null bean.");
		}

		if (writeMethod == null) {
			throw new IllegalStateException("Cannot set value on a "
					+ "non-writable property.");

		}

		bean = getContainer(bean);
		if (bean == null) {
			throw new PropertyAccessException("Cannot access property of a "
					+ "null bean.");
		}

		value = convertFromEditorValue(value);

		try {
			writeMethod.invoke(bean, value);
		} catch (Exception e) {
			throw new PropertyAccessException("Failed to write property '"
					+ getName() + "' of bean '" + bean + "' with value '"
					+ value + "'.", e);
		}
	}

	@Override
	void setup(TableColumnExt tc) {
		assert tc != null;
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Obtains the bean that directly contains the property.
	 * 
	 * @param bean the bean where the path starts (may be <code>null</code>)
	 * 
	 * @return <code>null</code> if <code>bean</code> is <code>null</code>
	 * or if any of the intermediate path values is <code>null</code>.
	 * Returns the bean containing the property otherwise
	 */
	private Object getContainer(Object bean) {
		for (Method m : path) {
			if (bean == null) {
				continue;
			}

			try {
				bean = m.invoke(bean);
			} catch (Exception e) {
				throw new PropertyAccessException("Failed to invoke method "
						+ m + " in object '" + bean + "'.");
			}
		}

		return bean;
	}

	@Override
	public void setReadOnly(boolean ro) {
		if (!ro && writeMethod == null) {
			throw new IllegalArgumentException("Cannot set the property '"
					+ getName() + "' as read-write because no write method "
					+ "was found.");
		}

		this.readOnly = ro;
	}

	/**
	 * Converts the value returned by the cell editor into the value used by
	 * the property.
	 * 
	 * @param value the value returned by the editor
	 * 
	 * @return the value to store in the property
	 */
	protected Object convertFromEditorValue(Object value) {
		return value;
	}

	@Override
	public boolean supportsTab() {
		return true;
	}
}
