package edu.cmu.cs.able.typelib.alg;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.scope.Scope;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;

/**
 * Algorithm which extracts the primitive scope associated with a data type.
 * This algorithm will only work on data types inserted in scopes whose
 * topmost parent scope is a primitive scope.
 */
public class PrimitiveScopeExtraction {
	/**
	 * Utility class: no constructor.
	 */
	private PrimitiveScopeExtraction() {
		/*
		 * No constructor.
		 */
	}
	
	/**
	 * Runs the algorithm.
	 * @param dt the data type
	 * @return the primitive scope
	 */
	public static PrimitiveScope run(DataType dt) {
		Ensure.not_null(dt);
		DataTypeScope dts = dt.parent_dts();
		while (dts.parent() != null) {
			Scope<DataType> p = dts.parent();
			Ensure.is_instance(p, DataTypeScope.class);
			dts = (DataTypeScope) dts.parent();
		}
		
		Ensure.is_instance(dts, PrimitiveScope.class);
		return (PrimitiveScope) dts;
	}
}
