package incubator.scb.sdl;

import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Tests for the {@link PropertyObject} class.
 */
@SuppressWarnings("javadoc")
public class PropertyObjectTests extends DefaultTCase {
	@Test
	public void get_nonexisting_property() throws Exception {
		PropertyObject po = new PropertyObject();
		assertNull(po.property(String.class, "foo"));
	}
	
	@Test
	public void get_existing_property_with_right_type() throws Exception {
		PropertyObject po = new PropertyObject();
		po.property("foo", "bar");
		assertEquals("bar", po.property(String.class, "foo"));
	}
	
	@Test
	public void get_existing_property_with_wrong_type() throws Exception {
		PropertyObject po = new PropertyObject();
		po.property("foo", 45);
		assertNull(po.property(String.class, "foo"));
	}
	
	@Test
	public void setting_property_multiple_times() throws Exception {
		PropertyObject po = new PropertyObject();
		po.property("foo", "bar");
		assertEquals("bar", po.property(String.class, "foo"));
		po.property("foo", "foo");
		assertEquals("foo", po.property(String.class, "foo"));
	}
	
	@Test
	public void reset_property() throws Exception {
		PropertyObject po = new PropertyObject();
		po.property("foo", "bar");
		po.property("foo", null);
		assertNull(po.property(String.class, "foo"));
	}
}
