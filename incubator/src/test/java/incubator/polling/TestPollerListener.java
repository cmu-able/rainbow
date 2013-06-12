package incubator.polling;

import java.util.ArrayList;
import java.util.List;

/**
 * Listener which keeps track of invocations made and their data.
 */
class TestPollerListener implements PollerListener<Object> {
	/**
	 * Every operation (add or remove) adds a boolean in this table indicating
	 * whether it was an add or a remove.
	 */
	List<Boolean> added;
	
	/**
	 * All objects that were added.
	 */
	List<Object> objectsAdded;
	
	/**
	 * The positions at which the objects were added (each position corresponds
	 * to the object in {@link #objectsAdded}).
	 */
	List<Integer> positionsAdded;
	
	/**
	 * All objects taht were removed.
	 */
	List<Object> objectsRemoved;
	
	/**
	 * The positions at which the objects were removed (each position
	 * corresponds to the object in {@link #objectsRemoved}).
	 */
	List<Integer> positionsRemoved;
	
	
	/**
	 * Creates a new listener.
	 */
	TestPollerListener() {
		added = new ArrayList<>();
		objectsAdded = new ArrayList<>();
		objectsRemoved = new ArrayList<>();
		positionsAdded = new ArrayList<>();
		positionsRemoved = new ArrayList<>();
	}


	@Override
	public synchronized void objectAdded(Object object, int idx) {
		objectsAdded.add(object);
		positionsAdded.add(idx);
		added.add(true);
	}


	@Override
	public synchronized void objectRemoved(Object object, int idx) {
		objectsRemoved.add(object);
		positionsRemoved.add(idx);
		added.add(false);
	}
	
	/**
	 * Clears all data in the listener.
	 */
	synchronized void clear() {
		added.clear();
		objectsAdded.clear();
		objectsRemoved.clear();
		positionsAdded.clear();
		positionsRemoved.clear();
	}
}
