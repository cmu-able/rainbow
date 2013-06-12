package incubator.koolform;

import java.awt.Component;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ClassUtils;

/**
 * <p>
 * A <code>KoolBeanForm</code> is a form whose data is obtained from a bean.
 * This class further simplifies creating forms by automatically tying bean
 * fields to the form components.
 * </p>
 * <p>
 * The class provides methods to load and save a bean which handle getting a
 * setting bean properties automatically.
 * </p>
 * <p>
 * All components are automatically disabled when no object is loaded and
 * all components are automatically enabled when an object is loaded.
 * </p>
 * 
 * @param <T> the bean type
 */
public class KoolBeanForm<T> extends KoolForm {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The bean class.
	 */
	private final Class<T> beanClass;

	/**
	 * Maps property names to the components.
	 */
	private final Map<String, Component> attachs;

	/**
	 * Bean being edited (<code>null</code> if none).
	 */
	private T editing;
	
	/**
	 * Are we loading a bean? This is used to detect spurious changes during
	 * load.
	 */
	private boolean loading;

	/**
	 * Creates a new, empty form.
	 * 
	 * @param clazz the bean class
	 */
	public KoolBeanForm(Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz == null");
		}

		attachs = new HashMap<>();
		beanClass = clazz;
		editing = null;

		validateForm();
	}

	/**
	 * Attaches a component to a bean property. This method is usually not
	 * called directly because it is automatically invoked when
	 * {@link #addComponent(String, Component, String)} or
	 * {@link #addScrollableComponent(String, Component, String)} are
	 * invoked.
	 * 
	 * @param <E> the component type
	 * @param component the component
	 * @param property the property name
	 * 
	 * @return the component
	 */
	protected <E extends Component> E attach(E component, String property) {
		if (component == null) {
			throw new IllegalArgumentException("component == null");
		}

		if (property == null) {
			throw new IllegalArgumentException("property == null");
		}

		PropertyDescriptor[] descs = PropertyUtils
				.getPropertyDescriptors(beanClass);
		boolean propertyFound = false;
		for (int i = 0; i < descs.length; i++) {
			if (descs[i].getName().equals(property)) {
				propertyFound = true;
				break;
			}
		}

		if (!propertyFound) {
			throw new IllegalArgumentException("Bean class '"
					+ beanClass.getName() + "' does not have property '"
					+ property + "'.");
		}

		attachs.put(property, component);
		return component;
	}

	/**
	 * Adds a component to the form.
	 * 
	 * @param <E> the component type
	 * @param label the component's label
	 * @param component the component
	 * @param property the property name
	 * 
	 * @return the component
	 */
	protected <E extends Component> E addComponent(String label,
			E component, String property) {
		if (label == null) {
			throw new IllegalArgumentException("label == null");
		}

		if (component == null) {
			throw new IllegalArgumentException("component == null");
		}

		if (property == null) {
			throw new IllegalArgumentException("property == null");
		}

		addComponent(label, component);
		attach(component, property);
		validateForm();
		
		/*
		 * If we're editing, load the component value.
		 */
		if (getEditing() != null) {
			loadProperty(getEditing(), property);
		}
		
		return component;
	}

	/**
	 * Adds a component to the form placing a scrollable pane around it.
	 * 
	 * @param <E> the component type
	 * @param label the component's label
	 * @param component the component
	 * @param property the property name
	 * 
	 * @return the component
	 */
	protected <E extends Component> E addScrollableComponent(String label,
			E component, String property) {
		return addScrollableComponent(label, component, property, null);
	}

	/**
	 * Adds a component to the form placing a scrollable pane around it.
	 * 
	 * @param <E> the component type
	 * @param label the component's label
	 * @param component the component
	 * @param property the property name
	 * @param position the label position
	 * 
	 * @return the component
	 */
	protected <E extends Component> E addScrollableComponent(String label,
			E component, String property, LabelPosition position) {
		if (label == null) {
			throw new IllegalArgumentException("label == null");
		}

		if (component == null) {
			throw new IllegalArgumentException("component == null");
		}

		if (property == null) {
			throw new IllegalArgumentException("property == null");
		}

		addScrollableComponent(label, component, position);
		attach(component, property);
		validateForm();
		return component;
	}

	/**
	 * Loads an object into the form.
	 * 
	 * @param object the object to load (can be <code>null</code>)
	 */
	public void load(T object) {
		editing = object;

		if (loading) {
			throw new IllegalStateException("Cannot load during load.");
		}
		
		for (String k : attachs.keySet()) {
			loadProperty(object, k);
		}

		validateForm();
	}
	
	/**
	 * Loads the value of a property to the respectively attached component.
	 * 
	 * @param object the bean to load the data from
	 * @param k the property name (key in the {@link #attachs} map)
	 */
	private void loadProperty(T object, String k) {
		loading = true;
		
		try {
			Object value = null;
			if (object != null) {
				try {
					value = PropertyUtils.getSimpleProperty(object, k);
				} catch (Exception e) {
					// FIXME: Should throw a good exception.
					throw new RuntimeException("Failed to acess property '"
							+ k + "' of bean {" + object + "}.", e);
				}
			}
	
			setComponentValue(attachs.get(k), value);
		} finally {
			loading = false;
		}
	}

	/**
	 * Saves all fields into a given bean. Does nothing if no bean is being
	 * edited.
	 * 
	 * @param t the bean to save data to (cannot be <code>null</code>)
	 */
	public void saveTo(T t) {
		if (t == null) {
			throw new IllegalArgumentException("t == null");
		}

		if (editing == null) {
			return;
		}

		for (String k : attachs.keySet()) {
			if (!PropertyUtils.isWriteable(t, k)) {
				continue;
			}
			
			Object value = getComponentValue(attachs.get(k));
			try {
				Class<?> type = PropertyUtils.getPropertyType(t, k);
				if (value != null && !type.isInstance(value)) {
					type = ClassUtils.primitiveToWrapper(type);
					
					/*
					 * We may need to convert the value...
					 */
					value = convert(value, type);
				}
				
				PropertyUtils.setSimpleProperty(t, k, value);
			} catch (Exception e) {
				// FIXME: Should throw a good exception.
				throw new RuntimeException("Failed to set property '" + k
						+ "' of bean {" + t + "} to {" + value + "}.", e);
			}
		}
	}

	/**
	 * Creates a clone of the bean being edited, fills the clone with the
	 * current field data and returns the clone. The bean being edited is
	 * not changed.
	 * 
	 * @return a copy of the bean or <code>null</code> if no bean is being
	 * edited
	 */
	public T extractData() {
		if (editing == null) {
			return null;
		}

		T clone;
		try {
			@SuppressWarnings("unchecked")
			T cloneAux = (T) BeanUtils.cloneBean(editing);
			clone = cloneAux;
		} catch (Exception e) {
			throw new RuntimeException("Failed to clone bean {" + editing
					+ "}.", e);
		}

		saveTo(clone);
		return clone;
	}

	/**
	 * Enables or disables the form's fields.
	 */
	protected void validateForm() {
		if (editing == null) {
			setFormEnabled(false);
		} else {
			setFormEnabled(true);
		}
	}

	/**
	 * Obtains the bean currently being edited.
	 * 
	 * @return the bean or <code>null</code> if none
	 */
	public T getEditing() {
		return editing;
	}

	/**
	 * Obtains the bean's class.
	 * 
	 * @return the bean class
	 */
	public Class<T> getBeanClass() {
		return beanClass;
	}

	/**
	 * Determines if changes have been made in the form.
	 * 
	 * @return have changes been made to the fields in the form after the
	 * bean has been edited? Returns <code>false</code> if no bean is being
	 * edited
	 */
	public boolean isDirty() {
		if (editing == null) {
			return false;
		}

		T data = extractData();
		if (!editing.equals(data)) {
			return false;
		}

		return true;
	}

	/**
	 * Saves the changes to current editing bean. This method relies on
	 * {@link #saveTo(Object)} to perform the actual saving.
	 * 
	 * @return the current editing bean or <code>null</code> if no bean is
	 * being edited
	 */
	public T save() {
		if (editing == null) {
			return null;
		}

		saveTo(editing);
		return editing;
	}
	
	/**
	 * Reloads the data from the currently editing bean (if any) loosing any
	 * changes made in the form.
	 */
	public void reload() {
		if (editing != null) {
			load(editing);
		}
	}

	@Override
	protected void componentDataChanged(Component c) {
		if (!loading) {
			componentDataChangedInUi(c);
		}
	}
	
	/**
	 * Use this method instead of {@link #componentDataChanged(Component)} if
	 * you don't want to receive spurious updates during bean loading.
	 * 
	 * @param c the component
	 */
	protected void componentDataChangedInUi(Component c) {
		/*
		 * Default hook method does nothing.
		 */
	}
}
