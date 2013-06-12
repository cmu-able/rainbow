package incubator.qxt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import incubator.obscol.ObservableListListener;

/**
 * Implementation of a cached computed property. Just like its superclass,
 * this class doesn't define how to compute the property value. It differs
 * from the super class because it caches the result of the computation of
 * the property avoiding recomputing it unless the bean changes.
 * 
 * @param <T> the property data type
 */
public abstract class QxtCachedComputedProperty<T> extends
		QxtComputedProperty<T> {
	/**
	 * Value used to signal a <code>null</code> value in the cache.
	 */
	private static final Object NULL_VALUE = new Object();

	/**
	 * Value cache. Maps beans to their cached values.
	 */
	private final Map<Object, Object> cache;

	/**
	 * Creates a new computed property.
	 * 
	 * @param name the property name
	 * @param description the property description
	 * @param propertyType the data type of the property
	 */
	public QxtCachedComputedProperty(String name, String description,
			Class<T> propertyType) {
		super(name, description, propertyType);

		cache = new HashMap<>();
	}

	@Override
	public final T getValue(Object bean) {
		if (bean == null) {
			throw new IllegalArgumentException("bean == null");
		}

		try {
			return load(bean);
		} catch (ValueNotFoundException e) {
			/*
			 * Ok, we'll just move ahead and compute it. The next statement
			 * avoids checkstyle warnings.
			 */
			assert true;
		}

		T computed = computeValue(bean);
		store(bean, computed);
		return computed;
	}

	/**
	 * Stores a computed value in the cache.
	 * 
	 * @param bean the bean whose value was computed
	 * @param value the computed value (which can be <code>null</code>)
	 */
	private void store(Object bean, Object value) {
		assert bean != null;

		if (value == null) {
			value = NULL_VALUE;
		}

		cache.put(bean, value);
	}

	/**
	 * Clears the cache entry for a bean, if it is defined.
	 * 
	 * @param bean the bean
	 */
	private void clear(Object bean) {
		assert bean != null;

		cache.remove(bean);
	}

	/**
	 * Loads a value from the cache.
	 * 
	 * @param bean the bean whose value we want to find
	 * 
	 * @return the cached value (can be <code>null</code>)
	 * 
	 * @throws ValueNotFoundException the computed value was not found in
	 * cache
	 */
	private T load(Object bean) throws ValueNotFoundException {
		assert bean != null;

		Object value = cache.get(bean);
		if (value == NULL_VALUE) {
			return null;
		}

		if (value == null) {
			throw new ValueNotFoundException();
		}

		return getPropertyClass().cast(value);
	}

	@Override
	<E> void init(Class<E> beanClass, QxtTableModel<E> model) {
		assert beanClass != null;
		assert model != null;

		/*
		 * We'll register ourselves as listeners of the observable list so that
		 * we can remove cached values when the beans are removed.
		 */
		ObservableListListener<E> oll = new ObservableListListener<E>() {
			@Override
			public void elementAdded(E e, int idx) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			public void elementChanged(E oldE, E newE, int idx) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			public void elementRemoved(E e, int idx) {
				if (e == null) {
					throw new IllegalArgumentException("e == null");
				}

				cache.remove(e);
			}

			@Override
			public void listCleared() {
				cache.clear();
			}
		};

		model.getData().addObservableListListener(oll);

		model.addBeanPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent pce) {
				clear(pce.getSource());
			}
		});
	}

	/**
	 * Computes the property value for a given bean.
	 * 
	 * @param bean the bean
	 * 
	 * @return the computed value (which can be <code>null</code>)
	 */
	protected abstract T computeValue(Object bean);

	/**
	 * Exception thrown when loading a value from the cache and the value
	 * has not been found.
	 */
	private static final class ValueNotFoundException extends Exception {
		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new exception.
		 */
		private ValueNotFoundException() {
			/*
			 * Nothing to do.
			 */
		}
	}
}
