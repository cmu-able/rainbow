package incubator.ctxaction;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

/**
 * Class representing an action context. This class may only be used from
 * the AWT event dispatcher thread.
 * 
 * FIXME: Need to document (and test) the parent stuff.
 */
public class ActionContext {
	/**
	 * Should the AWT check thread be disabled (useful for unit testing)?
	 */
	private static boolean awtCheckDisabled = false;

	/**
	 * Context data.
	 */
	private Map<String, Object> data;

	/**
	 * Context listeners.
	 */
	private final List<ActionContextListener> listeners;
	
	/**
	 * The action context's parent.
	 */
	private ActionContext parent;

	/**
	 * Creates a new action context.
	 */
	public ActionContext() {
		data = new HashMap<>();
		listeners = new ArrayList<>();
	}

	/**
	 * Creates a new action context.
	 * 
	 * @param parent an optional parent context
	 */
	public ActionContext(ActionContext parent) {
		data = new HashMap<>();
		listeners = new ArrayList<>();
		this.parent = parent;
	}

	/**
	 * Adds a new listener that will be informed when the context is
	 * changed.
	 * 
	 * FIXME: Need to talk about registering on parent changes
	 * 
	 * @param l the listener
	 */
	public void addActionContextListener(ActionContextListener l) {
		if (l == null) {
			throw new IllegalArgumentException("l == null");
		}

		checkAwtThread();

		listeners.add(l);
		if (parent != null) {
			parent.addActionContextListener(l);
		}
	}

	/**
	 * Removes a previously added listener.
	 * 
	 * @param l the listener
	 */
	public void removeActionContextListener(ActionContextListener l) {
		if (l == null) {
			throw new IllegalArgumentException("l == null");
		}

		if (!listeners.contains(l)) {
			throw new IllegalStateException("Listener is not registered");
		}

		checkAwtThread();

		listeners.remove(l);
		if (parent != null) {
			parent.removeActionContextListener(l);
		}
	}

	/**
	 * Removes all context data.
	 */
	public void clear() {
		checkAwtThread();

		data.clear();
		fireContextChanged();
	}

	/**
	 * Removes an object from the context, if it exists.
	 * 
	 * FIXME: Talk about parent stuff.
	 * 
	 * @param key the object key
	 */
	public void clear(String key) {
		if (key == null) {
			throw new IllegalArgumentException("key == null");
		}

		checkAwtThread();

		if (data.remove(key) != null) {
			fireContextChanged();
		}
	}

	/**
	 * Sets the value of a context property.
	 * 
	 * @param key the property key
	 * @param value the property value. If <code>null</code>, the key is
	 * removed from the map
	 */
	public void set(String key, Object value) {
		if (key == null) {
			throw new IllegalArgumentException("key == null");
		}

		checkAwtThread();

		Object currentValue = data.get(key);
		if (ObjectUtils.equals(currentValue, value)) {
			return;
		}

		if (value == null) {
			data.remove(key);
		} else {
			data.put(key, value);
		}

		fireContextChanged();
	}

	/**
	 * Redefines the whole context. Calling this method is equivalent to
	 * call {@link #clear()} and the calling {@link #set(String, Object)}
	 * for all elements. The main advantage of this method (besides maybe
	 * being easier to use) is that listeners are notified only once.
	 * 
	 * @param keys keys for all context element. No <code>null</code> keys
	 * are allowed. If there are keys with repeated values, one of the
	 * values will be in the final context but which is undefined.
	 * @param values values to place in the context (equivalent to keys).
	 * Some elements may be <code>null</code> in which case the object will
	 * behave as if the equivalent key/value pair had not been specified.
	 */
	public void redefine(String[] keys, Object[] values) {
		if (keys == null) {
			throw new IllegalArgumentException("keys == null");
		}

		if (values == null) {
			throw new IllegalArgumentException("values == null");
		}

		if (keys.length != values.length) {
			throw new IllegalArgumentException(
					"keys.length != values.length");
		}

		for (int i = 0; i < keys.length; i++) {
			if (keys[i] == null) {
				throw new IllegalArgumentException("keys[" + i + "] == null");
			}
		}

		checkAwtThread();

		data.clear();
		for (int i = 0; i < keys.length; i++) {
			if (values[i] != null) {
				data.put(keys[i], values[i]);
			}
		}

		fireContextChanged();
	}

	/**
	 * Redefines the whole context. This method is equivalent to
	 * {@link #redefine(String[], Object[])} but receives a {@link Map}
	 * instead of two arrays.
	 * 
	 * @param data the new context data
	 */
	public void redefine(Map<String, Object> data) {
		if (data == null) {
			throw new IllegalArgumentException("data == null");
		}

		checkAwtThread();

		this.data.clear();
		this.data.putAll(data);

		fireContextChanged();
	}

	/**
	 * Redefines the context to be equal to the given one.
	 * 
	 * @param context the context to copy
	 */
	public void redefine(ActionContext context) {
		if (context == null) {
			throw new IllegalArgumentException("context == null");
		}

		assert context != null;

		checkAwtThread();

		data = new HashMap<>(context.data);
		fireContextChanged();
	}

	/**
	 * Informs all listeners that the context has been changed.
	 */
	private void fireContextChanged() {
		for (ActionContextListener l : new ArrayList<>(listeners)) {
			l.contextChanged(this);
		}
	}

	/**
	 * Obtains the value of a context object with the given key.
	 * 
	 * FIXME: Talk about parent stuff.
	 * 
	 * @param key the key
	 * 
	 * @return the value or <code>null</code> if none
	 */
	public Object get(String key) {
		if (key == null) {
			throw new IllegalArgumentException("key == null");
		}

		checkAwtThread();
		
		Object res = data.get(key);
		if (res == null && parent != null) {
			res = parent.get(key);
		}
		
		return res;
	}

	/**
	 * Obtains the value of a context object with the given key.
	 * 
	 * FIXME: Talk about parent
	 * 
	 * @param key the key
	 * @param type the value type
	 * 
	 * @param <T> the type of value to obtain
	 * 
	 * @return the value, <code>null</code> if none and also returns
	 * <code>null</code> if the key's value hasn't the right type
	 */
	public <T> T get(String key, Class<T> type) {
		if (key == null) {
			throw new IllegalArgumentException("key == null");
		}

		checkAwtThread();

		Object value = get(key);
		if (value == null || !type.isInstance(value)) {
			return null;
		}
		
		return type.cast(value);
	}

	/**
	 * Obtains a copy of the whole context.
	 * 
	 * FIXME: Talk about parent
	 * 
	 * @return a map which maps all context keys to their values
	 */
	public Map<String, Object> getAll() {
		checkAwtThread();

		return Collections.unmodifiableMap(new HashMap<>(data));
	}

	/**
	 * Informs all listeners that the context has changed (independently if
	 * there was a real change or not). This method may be useful if a
	 * context object has changed in a way that the <code>equals</code>
	 * method cannot detect and it is necessary to inform the listeners.
	 */
	public void forceChange() {
		checkAwtThread();

		fireContextChanged();
	}

	/**
	 * Checks that the current thread is a AWT dispatcher thread. Throws
	 * IllegalThreadStateException if it isn't.
	 */
	protected void checkAwtThread() {
		if (awtCheckDisabled) {
			return;
		}

		if (!EventQueue.isDispatchThread()) {
			throw new IllegalThreadStateException(
					"Action context is being used from thread '"
							+ Thread.currentThread()
							+ "' but it can only be "
							+ "used from an AWT event dispatcher");
		}
	}

	/**
	 * Disables the check for the AWT thread. Used in unit tests.
	 */
	static void disableAwtThreadCheck() {
		awtCheckDisabled = true;
	}
}
