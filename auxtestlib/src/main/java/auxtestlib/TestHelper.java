package auxtestlib;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to add in fields of test cases which are subclasses of
 * {@link DefaultTCase}\ whose type is a subclass of {@link AbstractTestHelper}.
 * Fields marked with this annotation will be automatically created by this
 * class and disposed of when the test ends.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TestHelper {
	/*
	 * No properties.
	 */
}
