package edu.cmu.cs.able.eseb.rpc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation added to methods in a service which return values. This
 * annotation maps the return type to a <em>typelib</em> type name. This
 * annotation should be added to all methods if and only if they have a
 * return type.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReturnTypeMapping {
	/**
	 * The name of the <em>typelib</em> type to convert the result to.
	 * @return the type name
	 */
	public String value();
}
