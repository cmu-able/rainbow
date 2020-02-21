package incubator.jcodegen;

import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Unit tests for the {@link JavaType} class.
 */
@SuppressWarnings("javadoc")
public class JavaTypeTests extends DefaultTCase {
	@Test
	public void type_name() throws Exception {
		JavaType jt = new JavaType("foo");
		assertEquals("foo", jt.name());
	}
	
	@Test
	public void type_raw_name() throws Exception {
		JavaType jt = new JavaType("Foo");
		assertEquals("Foo", jt.raw_class_name());
	}
	
	@Test
	public void type_class_expression() throws Exception {
		JavaType jt = new JavaType("Foo");
		assertEquals("Foo.class", jt.class_expression());
	}
	
	@Test
	public void default_type_is_not_enum() throws Exception {
		assertFalse(new JavaType("foo").is_enumeration());
	}
	
	@Test
	public void enumerations_are_enumerations() throws Exception {
		JavaEnumerationType et = new JavaEnumerationType("foo");
		assertEquals("foo", et.name());
		assertTrue(et.is_enumeration());
	}
	
	@Test
	public void copy_expression_is_same_in_default_case() throws Exception {
		JavaType foo = new JavaType("foo");
		assertEquals("bar", foo.copy_expression("bar"));
	}
	
	@Test
	public void copy_expression_is_copy_constructor_if_available()
			throws Exception {
		JavaType foo = new JavaClassWithCopyConstructorType("Foo");
		assertEquals("(bar == null? null : new Foo(bar))",
				foo.copy_expression("bar"));
	}
	
	@Test
	public void set_type_name() throws Exception {
		JavaType foo_set = new JavaSetType(new JavaType("Foo"));
		assertEquals("java.util.Set<Foo>", foo_set.name());
	}
	
	@Test
	public void set_type_copy() throws Exception {
		JavaType foo_set = new JavaSetType(new JavaType("Foo"));
		assertEquals("new java.util.HashSet<>(x)", foo_set.copy_expression(
				"x"));
	}
	
	@Test
	public void set_raw_name() throws Exception {
		JavaType jt = new JavaSetType(new JavaType("Foo"));
		assertEquals("java.util.Set", jt.raw_class_name());
	}
	
	@Test
	public void set_class_expression() throws Exception {
		JavaType jt = new JavaSetType(new JavaType("Foo"));
		assertEquals("(Class<java.util.Set<Foo>>) (Object) java.util.Set.class",
				jt.class_expression());
	}
}
