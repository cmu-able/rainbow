package incubator.exh;

import java.util.Comparator;

/**
 * Comparator that sorts throwable contexts by date.
 */
public class ThrowableContextComparator
		implements Comparator<ThrowableContext> {
	/**
	 * Creates a new comparator.
	 */
	public ThrowableContextComparator() {
	}
	
	@Override
	public int compare(ThrowableContext o1, ThrowableContext o2) {
		if (o1.when().before(o2.when())) {
			return -1;
		}
		
		if (o1.when().after(o2.when())) {
			return 1;
		}
		
		return 0;
	}
}
