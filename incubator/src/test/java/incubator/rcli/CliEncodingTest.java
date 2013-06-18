package incubator.rcli;

import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Tests encoding and decoding of {@link CliEncoding}.
 */
@SuppressWarnings("javadoc")
public class CliEncodingTest extends DefaultTCase {
	@Test
	public void empty_encoding() throws Exception {
		assertEquals("" ,CliEncoding.encode(""));
	}
	
	@Test
	public void basic_encoding() throws Exception {
		assertEquals("foo", CliEncoding.encode("foo"));
	}
	
	@Test
	public void encoding_decoding_percent_symbol() throws Exception {
		String enc = CliEncoding.encode("%");
		assertEquals("%25", enc);
		assertEquals("%", CliEncoding.decode(enc));
		assertEquals("\\%", CliEncoding.decode("\\%"));
	}
	
	@Test
	public void encoding_decoding_backslash() throws Exception {
		String enc = CliEncoding.encode("foo\\bar");
		assertEquals("foo%5Cbar", enc);
		assertEquals("foo\\bar", CliEncoding.decode(enc));
		assertEquals("foo\\\\bar", CliEncoding.decode("foo\\\\bar"));
	}
	
	@Test
	public void decoding_useless_backslash() throws Exception {
		assertEquals("\\f", CliEncoding.decode("\\f"));
	}
	
	@Test(expected = AssertionError.class)
	public void null_encoding() throws Exception {
		CliEncoding.encode(null);
	}
	
	@Test(expected = AssertionError.class)
	public void null_decoding() throws Exception {
		CliEncoding.decode(null);
	}
	
	@Test
	public void invalid_decoding() throws Exception {
		assertNull(CliEncoding.decode("\\"));
	}
	
	@Test
	public void quote_backslash_support() throws Exception {
		assertEquals("\"foo\\\"bar\"", CliEncoding.decode("\"foo\\\"bar\""));
	}
}
