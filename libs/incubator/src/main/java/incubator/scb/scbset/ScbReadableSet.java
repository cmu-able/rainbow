package incubator.scb.scbset;

import incubator.scb.CloneableScb;
import incubator.scb.MergeableIdScb;
import incubator.scb.Scb;

import java.util.Set;

/**
 * An SCB readable set contains SCBs and allows them to be accessed. It can
 * inform listeners of changes in the set through a {@link ChangeLog}. The
 * set registers itself as a listener to all its SCBs to inform listeners
 * of changes in the SCBs contained in the set.
 * @param <T> the type of SCB
 */
public interface ScbReadableSet
		<T extends Scb<T> & MergeableIdScb<T> & CloneableScb<T>> {
	/**
	 * Obtains the set's change log where listeners can be added to be informed
	 * of changes.
	 * @return the change log
	 */
	ChangeLog<T> changelog();
	
	/**
	 * Obtains all SCBs in the set.
	 * @return all SCBs
	 */
	Set<ScbIw<T>> all();
	
	/**
	 * Obtains the SCB with the given ID.
	 * @param id the ID
	 * @return the SCB or <code>null</code> if none
	 */
	T get(int id);
}
