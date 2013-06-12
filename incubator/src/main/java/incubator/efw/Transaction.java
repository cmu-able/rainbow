package incubator.efw;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation placed in methods that define what is the transaction requirement
 * of the method.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Transaction {
	/**
	 * Determines what is the transactional requirement of the method.
	 * 
	 * @return the requirement
	 */
	TransactionRequirement requirement();
}
