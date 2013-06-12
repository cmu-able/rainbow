package edu.cmu.cs.able.typelib.alg;

import incubator.pval.Ensure;

import java.util.HashSet;
import java.util.Set;

import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Identifies the set of all super types of a data type.
 */
public class AllSuperTypes {
	/**
	 * Utility class: no constructor.
	 */
	private AllSuperTypes() {
		/*
		 * No constructor.
		 */
	}
	
	/**
	 * Executes the algorithm.
	 * @param t the data type
	 * @param itself include itself in the results?
	 * @return the set all the data type's super types
	 */
	public static Set<DataType> run(DataType t, boolean itself) {
		Ensure.not_null(t);
		
		Set<DataType> pending = new HashSet<>();
		pending.add(t);
		Set<DataType> result = new HashSet<>();
		if (itself) {
			result.add(t);
		}
		
		while (pending.size() > 0) {
			DataType dt = pending.iterator().next();
			pending.remove(dt);
			
			Set<DataType> pending_super = dt.super_types();
			result.addAll(pending_super);
			pending.addAll(pending_super);
		}
		
		return result;
	}
}
