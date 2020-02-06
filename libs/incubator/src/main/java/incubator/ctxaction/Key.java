package incubator.ctxaction;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fields in objects that access the context can be marked with this annotation.
 * If the object is run through the {@link KeyFieldProcessor} its fields will
 * be automatically filled in with values extracted from the
 * {@link ActionContext}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Key {
	/**
	 * Name of the context key.
	 * 
	 * @return the name of the context key
	 */
	String contextKey();
}
