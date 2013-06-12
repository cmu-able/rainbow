package incubator.dmgr;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

/**
 * Listener that keeps track of {@link DataManager} requests. The listeners that
 * implement this interface receive requests for add, remove and update and can
 * veto these updates to the list of objects.
 */
public class TestVetoPropertyListener implements BeanVetoPropertyListener {
	/**
	 * All objects that were added to be listeners.
	 */
	public final List<Object> objectsAdded;

	/**
	 * All objects that were removed from listeners.
	 */
	public final List<Object> objectsRemoved;

	/**
	 * Counts the number of operations
	 */
	public int numCalls;

	/**
	 * Listener that keeps track of {@link DataManager} requests.
	 */
	public TestVetoPropertyListener() {
		objectsAdded = new ArrayList<>();
		objectsRemoved = new ArrayList<>();
		numCalls = 0;
	}

	/**
	 * This method is called when a object is going to be changed.
	 * 
	 * @param changes set of properties that are going to be changed
	 * 
	 * @throws PropertyVetoException throw a PropertyVetoException if it veto's
	 * the change
	 */
	@Override
	public void propertiesChanged(BeanPropertyChange changes)
			throws PropertyVetoException {
		numCalls = numCalls + 1;

		// Veto change if it has at least one object associated
		if (objectsAdded.size() > 0) {
			throw new PropertyVetoException("Does not contain any objects",
					new PropertyChangeEvent(changes, "size", changes
							.getOldValue(), changes.getNewValue()));
		}
	}

	/**
	 * This method is called when a object is added.
	 * 
	 * @param object that is going to be added
	 * 
	 * @throws PropertyVetoException throw a PropertyVetoException if it veto's
	 * the change
	 */
	@Override
	public final void objectAdded(Object object) throws PropertyVetoException {
		numCalls = numCalls + 1;

		// Veto change if it has more than one object
		if (objectsAdded.size() > 0) {
			throw new PropertyVetoException("Does not contain any objects",
					new PropertyChangeEvent(object, "size", object, object));
		}

		// Other wise adds a new object to the list
		objectsAdded.add(object);
	}

	/**
	 * This method is called when a object is removed.
	 * 
	 * @param object that is going to be removed
	 * 
	 * @throws PropertyVetoException throw a PropertyVetoException if it veto's
	 * the change
	 */
	@Override
	public final void objectRemoved(Object object) throws PropertyVetoException {
		numCalls = numCalls + 1;

		// Veto change if it has no objects in the list
		if (objectsAdded.size() == 0) {
			throw new PropertyVetoException("Does not contain any objects",
					new PropertyChangeEvent(object, "size", object, object));
		}
		// Removes object from add list and adds object to remove list
		objectsAdded.remove(object);
		objectsRemoved.add(object);
	}
}
