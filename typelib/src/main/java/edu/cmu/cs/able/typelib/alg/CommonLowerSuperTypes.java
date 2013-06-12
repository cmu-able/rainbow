package edu.cmu.cs.able.typelib.alg;

import incubator.pval.Ensure;

import java.util.HashSet;
import java.util.Set;

import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Algorithm that, given two data types, finds the common super types
 * between both types. If several super types are found, only the lower ones
 * are provided, that is, only the super types which are not themselves
 * super types of other super types found.
 */
public class CommonLowerSuperTypes {
	/**
	 * Utility class: no constructor.
	 */
	private CommonLowerSuperTypes() {
		/*
		 * No constructor.
		 */
	}
	
	/**
	 * Runs the algorithm.
	 * @param t1 the first data type
	 * @param t2 the second data type
	 * @param include_base should the provided types, <code>t1</code> and
	 * <code>t2</code>, be included?
	 * @return the set of common base types
	 */
	public static Set<DataType> run(DataType t1, DataType t2, 
			boolean include_base) {
		Ensure.not_null(t1);
		Ensure.not_null(t2);
		
		Set<DataType> super_1 = AllSuperTypes.run(t1, include_base);
		Set<DataType> super_2 = AllSuperTypes.run(t2, include_base);
		
		Set<DataType> common = new HashSet<>(super_1);
		common.retainAll(super_2);
		
		for (DataType t : new HashSet<>(common)) {
			boolean is_super_of_any = false;
			for (DataType c : common) {
				if (t.super_of(c)) {
					is_super_of_any = true;
					break;
				}
			}
			
			if (is_super_of_any) {
				common.remove(t);
			}
		}
		return common;
	}
}
