package incubator.dmgr;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A data manager is a class which manages the veto listeners and informs the
 * also listeners of changes in the objects. The class is not responsible itself
 * for determining when the objects are created, changed or deleted. It
 * simplifies the work of managing and informing listeners (and handling
 * vetoing). The veto requests must occur prior to the change notifications if a
 * multi-threaded process is using this objects, then it must take into account
 * this constraint, since these objects don't provide any multi-threaded
 * support.
 */
public class DataManager {

	/**
	 * This variable represent the set of veto listeners. Even if it doen't make
	 * sense to have two object as veto listeners the equal of the set object
	 * might not work for all cases. Is up to the users of this object not to
	 * register twice. This list is not synchronous.
	 */
	private final transient List<BeanVetoPropertyListener> vetoListener;
	/**
	 * This variable represent the set of listeners. Even if it doen't make
	 * sense to have two object as veto listeners the equal of the set object
	 * might not work for all cases. Is up to the users of this object not to
	 * register twice. This list is not synchronous.
	 */
	private final transient List<EntityListener> listener;

	/**
	 * Constructor of the DataManger object responsible for instancing the list
	 * objects for veto and entity listeners.
	 */
	public DataManager() {
		vetoListener = Collections
				.synchronizedList(new ArrayList<BeanVetoPropertyListener>());
		listener = Collections
				.synchronizedList(new ArrayList<EntityListener>());
	}

	/**
	 * This method given an object to add notifies the listeners and based on
	 * the responses decides whether it can be added or not.
	 * 
	 * @param object that is going to be added
	 * 
	 * @throws PropertyVetoException object cannot be added
	 */
	public final void canAdd(Object object) throws PropertyVetoException {
		// Check if the object is not null
		if (object == null) {
			throw new IllegalArgumentException("object==null");
		}

		/**
		 * Iterates all veto listeners and call the objetAdded method if one
		 * throws an veto exception it returns false otherwise it returns true.
		 */
		for (BeanVetoPropertyListener objAuxliary : vetoListener) {
			objAuxliary.objectAdded(object);
		}
	}

	/**
	 * This method is called to notify listeners that an object has been added.
	 * 
	 * @param object added
	 */
	public final void added(Object object) {
		// Check if the object is not null
		if (object == null) {
			throw new IllegalArgumentException("object==null");
		}

		/**
		 * Iterates all listeners and call the objetAdded method to inform the
		 * object as been added.
		 */
		for (EntityListener objAuxliary : listener) {
			objAuxliary.objectAdded(object);
		}
	}

	/**
	 * This method notifies the listeners to check whether an object can be
	 * removed or not.
	 * 
	 * @param object that is going to be removed
	 * 
	 * @throws PropertyVetoException if the object cannot be removed
	 */
	public final void canRemove(Object object) throws PropertyVetoException {
		// Check if the object is not null
		if (object == null) {
			throw new IllegalArgumentException("object==null");
		}

		/**
		 * Iterates all veto listeners and call the objetRemoved method if one
		 * throws an veto exception it returns false otherwise it returns true.
		 */
		for (BeanVetoPropertyListener objAuxliary : vetoListener) {
			objAuxliary.objectRemoved(object);
		}
	}

	/**
	 * This method is called to notify listeners that an object has been
	 * removed.
	 * 
	 * @param object removed
	 */
	public final void removed(Object object) {
		// Check if the object is not null
		if (object == null) {
			throw new IllegalArgumentException("object==null");
		}

		/**
		 * Iterates all listeners and call the objetRemoved method to inform the
		 * object as been added.
		 */
		for (EntityListener objAuxliary : listener) {
			objAuxliary.objectRemoved(object);
		}
	}

	/**
	 * Given the old and new object the method notifies the listeners and based
	 * on their responses decides whether an object can change. If the two
	 * objects are equal then it doesn't ask the listeners.
	 * 
	 * @param oldObject is the old version of the object to be changed (property
	 * value)
	 * @param newObject is the new version of the object to be changed (property
	 * value)
	 * 
	 * @throws PropertyVetoException object change has been vetoed
	 */
	public final void canChange(Object oldObject, Object newObject)
			throws PropertyVetoException {
		// Check if the old object is not null
		if (oldObject == null) {
			throw new IllegalArgumentException("oldObject==null");
		}

		// Check if the new object is not null
		if (newObject == null) {
			throw new IllegalArgumentException("newObject==null");
		}

		// Compares the old and the new value if they are equal then
		// it returns true. Otherwise continues to notify veto listeners
		if (oldObject.equals(newObject)) {
			return;
		}

		// Create the BeanPropertyChange
		BeanPropertyChange auxBeanPropChange = new BeanPropertyChange(
				oldObject, newObject);

		// Call all veto listeners
		for (BeanVetoPropertyListener objAuxliary : vetoListener) {
			objAuxliary.propertiesChanged(auxBeanPropChange);
		}
	}

	/**
	 * This method given an object to be modified notifies the listeners based
	 * on the responses (veto or not). If the two objects are equal then no
	 * change is performed.
	 * 
	 * @param oldObject is the old version of the object (property value)
	 * @param newObject is the new version of the object (property value)
	 */
	public final void changed(Object oldObject, Object newObject) {
		// Check if the old object is not null
		if (oldObject == null) {
			throw new IllegalArgumentException("oldObject==null");
		}

		// Check if the new object is not null
		if (newObject == null) {
			throw new IllegalArgumentException("newObject==null");
		}

		// Compares the old and the new value if they are then it returns true
		// otherwise continues to notify veto listeners
		if (!oldObject.equals(newObject)) {
			// Create the BeanPropertyChange
			BeanPropertyChange auxBeanPropChange = new BeanPropertyChange(
					oldObject, newObject);

			// Call all veto listeners
			for (EntityListener objAuxliary : listener) {
				objAuxliary.propertiesChanged(auxBeanPropChange);
			}
		}
	}

	/**
	 * Adds veto listener to the veto listeners list. If the object is null is
	 * not added to the list.
	 * 
	 * @param object that is a veto listener
	 */
	public final synchronized void addVetoListener(
			BeanVetoPropertyListener object) {
		// Check if the object is not null
		if (object == null) {
			throw new IllegalArgumentException("object==null");
		}

		this.vetoListener.add(object);
	}

	/**
	 * Adds a listener to the changes listeners list. If the object is null is
	 * not added to the list.
	 * 
	 * @param object that is a listener
	 */
	public final synchronized void addListener(EntityListener object) {
		// Check if the object is not null
		if (object == null) {
			throw new IllegalArgumentException("object==null");
		}

		this.listener.add(object);
	}

	/**
	 * Removes veto listener to the veto listeners list. If the object is null
	 * is not removed from the list.
	 * 
	 * @param object that is a veto listener
	 */
	public final synchronized void removeVetoListener(
			BeanVetoPropertyListener object) {
		// Check if the object is not null
		if (object == null) {
			throw new IllegalArgumentException("object==null");
		}

		this.vetoListener.remove(object);
	}

	/**
	 * Removes a listener to the changes listeners list. If the object is null
	 * is not removed from the list.
	 * 
	 * @param object that is a listener
	 */
	public final synchronized void removeListener(EntityListener object) {
		// Check if the object is not null
		if (object == null) {
			throw new IllegalArgumentException("object==null");
		}

		this.listener.remove(object);
	}
}
