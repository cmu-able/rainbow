package incubator.rcli;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Test case for command line generation and parsing.
 */
@SuppressWarnings("javadoc")
public class CommandLineParseTest extends DefaultTCase {
	private CommandLine[] m_cl;
	
	@Before
	public void set_up() {
		m_cl = new CommandLine[3];
	}
	
	@Test
	public void parse_simple_command() throws Exception {
		m_cl[0] = CommandLine.parse("foo");
		assertNotNull(m_cl[0]);
		m_cl[1] = new CommandLine("foo");
		m_cl[2] = CommandLine.parse(m_cl[1].to_single_line());
		for (int i = 0; i < m_cl.length; i++) {
			assertEquals("foo", m_cl[i].command());
			assertEquals(0, m_cl[i].arg_count());
		}
	}
	
	@Test
	public void parse_command_with_arguments() throws Exception {
		m_cl[0] = CommandLine.parse("foo bar 8 foo");
		assertNotNull(m_cl[0]);
		m_cl[1] = new CommandLine("foo", "bar", "8", "foo");
		m_cl[2] = CommandLine.parse(m_cl[1].to_single_line());
		
		for (int i = 0; i < m_cl.length; i++) {
			assertEquals("foo", m_cl[i].command());
			assertEquals(3, m_cl[i].arg_count());
			assertEquals("bar", m_cl[i].arg(0));
			assertEquals("8", m_cl[i].arg(1));
			assertEquals("foo", m_cl[i].arg(2));
			assertEquals(8, m_cl[i].argi(1));
		}
	}
	
	@Test
	public void parse_commmand_with_options() throws Exception {
		m_cl[0] = CommandLine.parse("fo-o --f g --h -i");
		assertNotNull(m_cl[0]);
		m_cl[1] = new CommandLine("fo-o", "--f", "g", "--h", "-i");
		m_cl[2] = CommandLine.parse(m_cl[1].to_single_line());
		
		for (int i = 0; i < m_cl.length; i++) {
			assertEquals("fo-o", m_cl[i].command());
			assertEquals(4, m_cl[i].arg_count());
			assertTrue(m_cl[i].is_option(0));
			assertEquals("f", m_cl[i].option(0));
			assertFalse(m_cl[i].is_option(1));
			assertEquals("g", m_cl[i].arg(1));
			assertTrue(m_cl[i].is_option(2));
			assertEquals("h", m_cl[i].option(2));
			assertFalse(m_cl[i].is_option(3));
			assertEquals("-i", m_cl[i].arg(3));
		}
	}
	
	@Test
	public void parse_command_with_quoting() throws Exception {
		m_cl[0] = CommandLine.parse("-gl\" \"u \"with space\" -\"-weird opt\"");
		assertNotNull(m_cl[0]);
		m_cl[1] = new CommandLine("-gl u", "with space", "--weird opt");
		m_cl[2] = CommandLine.parse(m_cl[1].to_single_line());
		
		for (int i = 0; i < m_cl.length; i++) {
			assertEquals("-gl u", m_cl[i].command());
			assertEquals(2, m_cl[i].arg_count());
			assertFalse(m_cl[i].is_option(0));
			assertTrue(m_cl[i].is_option(1));
			assertEquals("with space", m_cl[i].arg(0));
			assertEquals("weird opt", m_cl[i].option(1));
		}
	}
	
	@Test
	public void parse_command_with_escape_characters() throws Exception {
		m_cl[0] = CommandLine.parse("f\\\" \"\\\"\\\\\" \\q\\ \\\\");
		assertNotNull(m_cl[0]);
		m_cl[1] = new CommandLine("f\"", "\"\\", "q \\");
		m_cl[2] = CommandLine.parse(m_cl[1].to_single_line());
		
		for (int i = 0; i < m_cl.length; i++) {
			assertEquals("f\"", m_cl[i].command());
			assertEquals(2, m_cl[i].arg_count());
			assertFalse(m_cl[i].is_option(0));
			assertFalse(m_cl[i].is_option(1));
			assertEquals("\"\\", m_cl[i].arg(0));
			assertEquals("q \\", m_cl[i].arg(1));
		}
	}
	
	@Test
	public void parse_unmatched_quotes() throws Exception {
		assertNull(CommandLine.parse("\"foo"));
	}
	
	@Test
	public void parse_unpaired_backslash() throws Exception {
		assertNull(CommandLine.parse("\\"));
	}
	
	@Test
	public void parse_utf8_command() throws Exception {
		m_cl[0] = CommandLine.parse("\u2800 \u2800 --\u2800");
		assertNotNull(m_cl[0]);
		m_cl[1] = new CommandLine("\u2800", "\u2800", "--\u2800");
		m_cl[2] = CommandLine.parse(m_cl[1].to_single_line());
		
		for (int i = 0; i < m_cl.length; i++) {
			assertEquals("\u2800", m_cl[i].command());
			assertEquals(2, m_cl[i].arg_count());
			assertFalse(m_cl[i].is_option(0));
			assertTrue(m_cl[i].is_option(1));
			assertEquals("\u2800", m_cl[i].arg(0));
			assertEquals("\u2800", m_cl[i].option(1));
		}
	}
	
	@Test
	public void parse_empty_command() throws Exception {
		assertNull(CommandLine.parse(""));
	}
	
	@Test(expected = CommandSyntaxException.class)
	public void access_invalid_index() throws Exception {
		m_cl[0] = CommandLine.parse("foo");
		m_cl[0].is_option(0);
	}
	
	@Test(expected = CommandSyntaxException.class)
	public void access_argument_as_option() throws Exception {
		m_cl[0] = CommandLine.parse("foo bar");
		m_cl[0].option(0);
	}
	
	@Test(expected = CommandSyntaxException.class)
	public void access_option_as_argument() throws Exception {
		m_cl[0] = CommandLine.parse("foo --bar");
		m_cl[0].arg(0);
	}
	
	@Test(expected = CommandSyntaxException.class)
	public void access_string_as_integer() throws Exception {
		m_cl[0] = CommandLine.parse("foo glu");
		m_cl[0].argi(0);
	}
}
