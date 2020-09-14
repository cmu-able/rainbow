package incubator.scb;

import incubator.Pair;
import incubator.pval.Ensure;

import java.util.HashSet;
import java.util.Set;

/**
 * Result of comparing two sets of mergeable SCBs. Comparison is made by
 * defining the changes that have to be performed to transform a source
 * set into a destination set.
 * @param <T> the type of SCB
 */
public class MergeableScbSetComparationResult
		<T extends MergeableIdScb<T>> {
	/**
	 * SCBs that need to be created a the destination (these are source
	 * SCBs).
	 */
	private Set<T> m_to_create;
	
	/**
	 * SCBs that need to be delete from the destination (these are
	 * destination SCBs).
	 */
	private Set<T> m_to_delete;
	
	/**
	 * SCBs that are different in the source to the destination. The first
	 * element in the pair is the source SCB and the second one is the
	 * destination SCB.
	 */
	private Set<Pair<T, T>> m_different;
	
	/**
	 * SCBs that are equal in the source and destination. The first element in
	 * the pair is the source SCB and the second one is the destination SCB.
	 */
	private Set<Pair<T, T>> m_unchanged;
	
	/**
	 * Creates a new comparison result.
	 * @param to_create source SCBs that need to be created at the destination
	 * @param to_delete source SCBs that need to be delete at the destination 
	 * @param different pairs (source / destination) of SCBs that exist in
	 * both but are different
	 * @param unchanged pairs (source / destination) of SCBs that exist in
	 * both and are equal
	 */
	public MergeableScbSetComparationResult(Set<T> to_create, Set<T> to_delete,
			Set<Pair<T, T>> different, Set<Pair<T, T>> unchanged) {
		Ensure.not_null(to_create, "to_create == null");
		Ensure.not_null(to_delete, "to_delete == null");
		Ensure.not_null(different, "different == null");
		Ensure.not_null(unchanged, "equal == null");
		
		m_to_create = new HashSet<>(to_create);
		m_to_delete = new HashSet<>(to_delete);
		m_different = new HashSet<>(different);
		m_unchanged = new HashSet<>(unchanged);
	}
	
	/**
	 * Obtains all source SCBs that need to be created at the destination.
	 * @return the SCBs to create
	 */
	public Set<T> to_create() {
		return new HashSet<>(m_to_create);
	}
	
	/**
	 * Obtains all destination SCBs that need to be deleted.
	 * @return the SCBs to delete
	 */
	public Set<T> to_delete() {
		return new HashSet<>(m_to_delete);
	}
	
	/**
	 * Obtains all SCBs that are different at the source and destination.
	 * @return pairs with source / destination SCBs.
	 */
	public Set<Pair<T, T>> different() {
		return new HashSet<>(m_different);
	}
	
	/**
	 * Obtains all SCBs that have not been changed in the source and
	 * destination.
	 * @return paris with source / destination SCBs.
	 */
	public Set<Pair<T, T>>  unchanged() {
		return new HashSet<>(m_unchanged);
	}
}
