package incubator.ctxaction;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to add fields of subclasses of {@link FixedKeyContextualAction}
 * which are marked with {@link Key} to force the action to be available only
 * when the key is present in the context and its value coercible to the field
 * type.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MandatoryKey {
	/*
	 * No attributes for MandatoryKey class.
	 */
}
