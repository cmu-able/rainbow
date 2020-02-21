package edu.cmu.cs.able.eseb.rpc;

import java.lang.annotation.*;

/**
 * Annotation added to methods of remote interfaces that maps the method
 * arguments to textual descriptions of their equivalent <em>typelib</em>
 * data types. All methods remote interfaces should be annotated with this
 * annotation.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ParametersTypeMapping {
	/**
	 * Obtains the name of the <em>typelib</em> data types.
	 * @return the names of the <em>typelib</em> data types; there should be
	 * exactly one element in the array per argument in the method signature
	 */
	String[] value ();
}
