package incubator.dmgr;

import java.util.ArrayList;
import java.util.List;

/**
 * Listener that keeps track of {@link DataManager} requests.
 */
public class TestEntityListener implements EntityListener {
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
	 * Creates a new listener.
	 */
	TestEntityListener() {
		objectsAdded = new ArrayList<>();
		objectsRemoved = new ArrayList<>();
		numCalls = 0;
	}
	
	/**
	 * This method is called when an object was changed.
	 * 
	 * @param changes changes performed on the object
	 */
	@Override
	public void propertiesChanged(BeanPropertyChange changes) {
		numCalls++;
	}

	/**
	 * This method is called when an object was added.
	 * 
	 * @param object that was added
	 */
	@Override
	public final void objectAdded(Object object) {
		numCalls++;
		objectsAdded.add(object);
	}

	/**
	 * This method is called when an object was removed.
	 * 
	 * @param object that was removed
	 */
	@Override
	public final void objectRemoved(Object object) {
		numCalls++;
		objectsRemoved.add(object);		
	}	
	
	/**
	 * Clear all lists.
	 */
	final synchronized void clear() {
		objectsAdded.clear();
		objectsRemoved.clear();
		numCalls = 0;
	}
}

