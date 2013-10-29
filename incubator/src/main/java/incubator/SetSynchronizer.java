package incubator;

import incubator.pval.Ensure;

import java.util.HashSet;
import java.util.Set;

/**
 * Class that can synchronize two sets: add all elements in a source set that
 * do not exist in a destination set to the destination set and remove all
 * elements in the destination set which do not exist in the source set.
 */
public class SetSynchronizer {
	/**
	 * Checks what are the synchronization needs between two sets.
	 * @param dst the destination set
	 * @param src the source set
	 * @return returns a pair with the elements that need to be added to the
	 * destination set and a set with the elements that need to be removed from
	 * the destination set.
	 */
	public static <T> Pair<Set<T>, Set<T>> synchronization_changes(
			Set<T> dst, Set<T> src) {
		Ensure.not_null(dst);
		Ensure.not_null(src);
		
		if (src == dst) {
			return new Pair<>((Set<T>) new HashSet<T>(),
					(Set<T>) new HashSet<T>());
		}
		
		Set<T> to_add = new HashSet<T>(src);
		to_add.removeAll(dst);
		Set<T> to_del = new HashSet<>(dst);
		to_del.removeAll(src);
		
		return new Pair<>(to_add, to_del);
	}
	
	/**
	 * Synchronizes two sets.
	 * @param dst the destination set
	 * @param src the source set
	 */
	public static <T> void synchronize(Set<T> dst, Set<T> src) {
		Ensure.not_null(dst);
		Ensure.not_null(src);
		
		Pair<Set<T>, Set<T>> sc = synchronization_changes(dst, src);
		dst.addAll(sc.first());
		dst.removeAll(sc.second());
	}
}
