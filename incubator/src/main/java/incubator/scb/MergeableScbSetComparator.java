package incubator.scb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import incubator.Pair;
import incubator.pval.Ensure;

/**
 * Comparator that receives to sets of SCBs and computes the changes that have
 * to be made to the source set to match the destination set.
 */
public class MergeableScbSetComparator {
	/**
	 * Utility class - no constructor.
	 */
	private MergeableScbSetComparator() {
		/*
		 * Nothing to do.
		 */
	}
	
	/**
	 * Compares two sets. SCBs with ID <code>0</code> never match another
	 * SCB.
	 * @param src the source set
	 * @param dst the destination set
	 * @return the changes that have to be made to the source set to match the
	 * destination set
	 */
	public static <T extends MergeableIdScb<T>>
	MergeableScbSetComparationResult<T> compare(Set<T> src, Set<T> dst) {
		Ensure.not_null(src, "src == null");
		Ensure.not_null(dst, "dst == null");
		
		Set<T> to_create = new HashSet<>();
		Set<T> to_delete = new HashSet<>();
		Set<Pair<T, T>> unchanged = new HashSet<>();
		Set<Pair<T, T>> different = new HashSet<>();
		
		Map<Integer, T> src_ids = new HashMap<>();
		for (T s : src) {
			if (s.id() > 0) {
				Ensure.is_false(src_ids.containsKey(s.id()));
				src_ids.put(s.id(), s);
			}
		}
		
		Map<Integer, T> dst_ids = new HashMap<>();
		for (T d : dst) {
			if (d.id() > 0) {
				Ensure.is_false(dst_ids.containsKey(d.id()));
				dst_ids.put(d.id(), d);
			}
		}
		
		for (T s : src) {
			T d = dst_ids.get(s.id());
			if (d == null) {
				to_delete.add(s);
			} else if (s.equals(d)) {
				unchanged.add(new Pair<>(s, d));
			} else {
				different.add(new Pair<>(s, d));
			}
		}
		
		for (T d : dst) {
			T s = src_ids.get(d.id());
			if (s == null) {
				to_create.add(d);
			}
		}
		
		return new MergeableScbSetComparationResult<>(to_create, to_delete,
				different, unchanged);
	}
}
