package incubator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Utility class used to iterate over collections taking a synchronized
 * snapshot of the collection. This is used when iterating over collections
 * that can be concurrently modified.
 */
public class CollectionSnapshot {
	/**
	 * Takes a synchronized snapshot of a set.
	 * @param s the set
	 * @return the snapshot
	 */
	public static <T> Iterable<T> take(Set<T> s) {
		synchronized (s) {
			List<T> snapshot = new ArrayList<>(s);
			return snapshot;
		}
	}
	
	/**
	 * Takes a synchronized snapshot of a list.
	 * @param l the List
	 * @return the snapshot
	 */
	public static <T> Iterable<T> take(List<T> l) {
		synchronized (l) {
			List<T> snapshot = new ArrayList<>(l);
			return snapshot;
		}
	}
}
