package incubator.scb.scbset;

import java.util.Set;

import incubator.scb.CloneableScb;
import incubator.scb.MergeableIdScb;
import incubator.scb.Scb;

/**
 * Registry that keeps several SCB readable sets, indexed by name.
 */
public interface ScbReadableSetRegistry {
	/**
	 * Obtains all names registered in the registry.
	 * @return all names
	 */
	Set<String> names();
	
	/**
	 * Obtains the SCB set with the given key.
	 * @param key the key
	 * @param t_class the type of SCB
	 * @return the SCB set
	 */
	<T extends Scb<T> & MergeableIdScb<T> & CloneableScb<T>> ScbReadableSet<T>
	get(String key, Class<T> t_class);
}
