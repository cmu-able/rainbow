package incubator;

import java.util.Comparator;

/**
 * Comparator that compares comparables.
 * @param <C> the comparable object
 */
public class ComparableComparator<C extends Comparable<C>>
		implements Comparator<C> {
	/**
	 * Creates a new comparator.
	 */
	public ComparableComparator() {
	}

	@Override
	public int compare(C o1, C o2) {
		return o1.compareTo(o2);
	}
}
