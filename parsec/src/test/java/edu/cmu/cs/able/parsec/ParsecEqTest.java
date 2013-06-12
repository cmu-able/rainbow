package edu.cmu.cs.able.parsec;

import org.apache.commons.lang.SystemUtils;
import org.junit.Before;
import org.junit.Test;

import auxtestlib.DefaultTCase;
import auxtestlib.FileContentWorker;
import auxtestlib.TemporaryFile;

@SuppressWarnings("javadoc")
public class ParsecEqTest extends DefaultTCase {
	private TestParserPostListener m_listener;
	private TemporaryFile m_temporary;
	private Parsec<Integer> m_parsec;
	private ParsecFileReader m_reader;
	
	@Before
	public void set_up() throws Exception {
		m_temporary = new TemporaryFile(false);
		m_parsec = new Parsec<>();
		m_reader = new ParsecFileReader();
		m_listener = new TestParserPostListener();
	}
	
	@Test
	public void parse_empty_file() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(), "");
		m_parsec.parse_i(m_reader.read(m_temporary.getFile()), m_listener);
		
		assertEquals(0, m_listener.m_stmt_txt.size());
		assertEquals(0, m_listener.m_blk_hdr_txt.size());
		assertEquals(0, m_listener.m_blk_txt_txt.size());
	}
	
	@Test
	public void parse_single_statement() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(), " foo ; ");
		m_parsec.parse_i(m_reader.read(m_temporary.getFile()), m_listener);
		
		assertEquals(1, m_listener.m_stmt_txt.size());
		assertEquals(0, m_listener.m_blk_hdr_txt.size());
		assertEquals(0, m_listener.m_blk_txt_txt.size());
		
		assertEquals("foo", m_listener.m_stmt_txt.get(0));
		TextRegionMatch m = m_listener.m_stmt_m.get(0);
		assertEquals(1, m.idx_in_region());
		assertEquals(1, m.coord_in_region().line());
		assertEquals(2, m.coord_in_region().column());
		assertEquals(m_temporary.getFile(), m.region().file().file());
	}
	
	@Test
	public void parse_single_block() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(),
				"{ some text }");
		m_parsec.parse_i(m_reader.read(m_temporary.getFile()), m_listener);
		
		assertEquals(0, m_listener.m_stmt_txt.size());
		assertEquals(1, m_listener.m_blk_hdr_txt.size());
		assertEquals(1, m_listener.m_blk_txt_txt.size());
		
		assertEquals("", m_listener.m_blk_hdr_txt.get(0));
		assertEquals("some text", m_listener.m_blk_txt_txt.get(0));
		TextRegionMatch mh = m_listener.m_blk_hdr_m.get(0);
		assertEquals(0, mh.idx_in_region());
		assertEquals(1, mh.coord_in_region().line());
		assertEquals(1, mh.coord_in_region().column());
		assertEquals(m_temporary.getFile(), mh.region().file().file());
		TextRegionMatch mt = m_listener.m_blk_txt_m.get(0);
		assertEquals(2, mt.idx_in_region());
		assertEquals(1, mt.coord_in_region().line());
		assertEquals(3, mt.coord_in_region().column());
		assertEquals(m_temporary.getFile(), mt.region().file().file());
	}
	
	@Test
	public void parse_complex_block() throws Exception {
		String blk_i_txt = "some text; with { another; block; }";
		String blk_txt = "{ " + blk_i_txt + " }";
		FileContentWorker.setContents(m_temporary.getFile(), blk_txt);
		m_parsec.parse_i(m_reader.read(m_temporary.getFile()), m_listener);
		
		assertEquals(0, m_listener.m_stmt_txt.size());
		assertEquals(1, m_listener.m_blk_hdr_txt.size());
		assertEquals(1, m_listener.m_blk_txt_txt.size());
		
		assertEquals("", m_listener.m_blk_hdr_txt.get(0));
		TextRegionMatch mh = m_listener.m_blk_hdr_m.get(0);
		assertEquals(0, mh.idx_in_region());
		assertEquals(1, mh.coord_in_region().line());
		assertEquals(1, mh.coord_in_region().column());
		assertEquals(m_temporary.getFile(), mh.region().file().file());
		assertEquals(blk_i_txt, m_listener.m_blk_txt_txt.get(0));
		TextRegionMatch mt = m_listener.m_blk_txt_m.get(0);
		assertEquals(2, mt.idx_in_region());
		assertEquals(1, mt.coord_in_region().line());
		assertEquals(3, mt.coord_in_region().column());
		assertEquals(m_temporary.getFile(), mt.region().file().file());
	}
	
	@Test
	public void parse_multiple_statements_and_blocks() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(),
				"some text;with { another; block; } which { has } some;");
		m_parsec.parse_i(m_reader.read(m_temporary.getFile()), m_listener);
		
		assertEquals(2, m_listener.m_stmt_txt.size());
		assertEquals(2, m_listener.m_blk_hdr_txt.size());
		assertEquals(2, m_listener.m_blk_txt_txt.size());
		
		assertEquals("some text", m_listener.m_stmt_txt.get(0));
		TextRegionMatch m = m_listener.m_stmt_m.get(0);
		assertEquals(0, m.idx_in_region());
		assertEquals(1, m.coord_in_region().line());
		assertEquals(1, m.coord_in_region().column());
		assertEquals(m_temporary.getFile(), m.region().file().file());
		
		assertEquals("some", m_listener.m_stmt_txt.get(1));
		m = m_listener.m_stmt_m.get(1);
		assertEquals(49, m.idx_in_region());
		assertEquals(1, m.coord_in_region().line());
		assertEquals(50, m.coord_in_region().column());
		assertEquals(m_temporary.getFile(), m.region().file().file());
		
		assertEquals("with", m_listener.m_blk_hdr_txt.get(0));
		m = m_listener.m_blk_hdr_m.get(0);
		assertEquals(10, m.idx_in_region());
		assertEquals(1, m.coord_in_region().line());
		assertEquals(11, m.coord_in_region().column());
		assertEquals(m_temporary.getFile(), m.region().file().file());
		
		assertEquals("another; block;", m_listener.m_blk_txt_txt.get(0));
		m = m_listener.m_blk_txt_m.get(0);
		assertEquals(17, m.idx_in_region());
		assertEquals(1, m.coord_in_region().line());
		assertEquals(18, m.coord_in_region().column());
		assertEquals(m_temporary.getFile(), m.region().file().file());
		
		assertEquals("which", m_listener.m_blk_hdr_txt.get(1));
		m = m_listener.m_blk_hdr_m.get(1);
		assertEquals(35, m.idx_in_region());
		assertEquals(1, m.coord_in_region().line());
		assertEquals(36, m.coord_in_region().column());
		assertEquals(m_temporary.getFile(), m.region().file().file());
		
		assertEquals("has", m_listener.m_blk_txt_txt.get(1));
		m = m_listener.m_blk_txt_m.get(1);
		assertEquals(43, m.idx_in_region());
		assertEquals(1, m.coord_in_region().line());
		assertEquals(44, m.coord_in_region().column());
		assertEquals(m_temporary.getFile(), m.region().file().file());
	}
	
	@Test
	public void parse_with_several_lines() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(),
				SystemUtils.LINE_SEPARATOR
				+ "Line 2;" + SystemUtils.LINE_SEPARATOR
				+ " line3;which{" + SystemUtils.LINE_SEPARATOR
				+ " has a blk };");
		m_parsec.parse_i(m_reader.read(m_temporary.getFile()), m_listener);
		
		int ls_len = SystemUtils.LINE_SEPARATOR.length();
		
		assertEquals(3, m_listener.m_stmt_txt.size());
		assertEquals(1, m_listener.m_blk_hdr_txt.size());
		assertEquals(1, m_listener.m_blk_txt_txt.size());
		
		assertEquals("Line 2", m_listener.m_stmt_txt.get(0));
		TextRegionMatch m = m_listener.m_stmt_m.get(0);
		assertEquals(ls_len, m.idx_in_region());
		assertEquals(2, m.coord_in_region().line());
		assertEquals(1, m.coord_in_region().column());
		assertEquals(m_temporary.getFile(), m.region().file().file());
		
		assertEquals("line3", m_listener.m_stmt_txt.get(1));
		m = m_listener.m_stmt_m.get(1);
		assertEquals(8 + 2 * ls_len, m.idx_in_region());
		assertEquals(3, m.coord_in_region().line());
		assertEquals(2, m.coord_in_region().column());
		assertEquals(m_temporary.getFile(), m.region().file().file());
		
		assertEquals("", m_listener.m_stmt_txt.get(2));
		m = m_listener.m_stmt_m.get(2);
		assertEquals(32 + 3 * ls_len, m.idx_in_region());
		assertEquals(4, m.coord_in_region().line());
		assertEquals(13, m.coord_in_region().column());
		assertEquals(m_temporary.getFile(), m.region().file().file());
		
		assertEquals("which", m_listener.m_blk_hdr_txt.get(0));
		m = m_listener.m_blk_hdr_m.get(0);
		assertEquals(14 + 2 * ls_len, m.idx_in_region());
		assertEquals(3, m.coord_in_region().line());
		assertEquals(8, m.coord_in_region().column());
		assertEquals(m_temporary.getFile(), m.region().file().file());
		
		assertEquals("has a blk", m_listener.m_blk_txt_txt.get(0));
		m = m_listener.m_blk_txt_m.get(0);
		assertEquals(21 + 3 * ls_len, m.idx_in_region());
		assertEquals(4, m.coord_in_region().line());
		assertEquals(2, m.coord_in_region().column());
		assertEquals(m_temporary.getFile(), m.region().file().file());
	}
	
	@Test
	public void strips_single_line_comments() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(),
				"foo " + SystemUtils.LINE_SEPARATOR
				+ " // ; bar " + SystemUtils.LINE_SEPARATOR
				+ " g;");
		m_parsec.parse_i(m_reader.read(m_temporary.getFile()), m_listener);
		
		assertEquals(1, m_listener.m_stmt_txt.size());
		assertEquals(0, m_listener.m_blk_hdr_txt.size());
		assertEquals(0, m_listener.m_blk_txt_txt.size());
		
		assertEquals("foo " + SystemUtils.LINE_SEPARATOR + "  g",
				m_listener.m_stmt_txt.get(0));
		TextRegionMatch m = m_listener.m_stmt_m.get(0);
		assertEquals(0, m.idx_in_region());
		assertEquals(1, m.coord_in_region().line());
		assertEquals(1, m.coord_in_region().column());
		assertEquals(m_temporary.getFile(), m.region().file().file());
	}
	
	@Test
	public void strips_multiple_line_comments() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(),
				"foo " + SystemUtils.LINE_SEPARATOR
				+ " /* ; bar " + SystemUtils.LINE_SEPARATOR
				+ " ;*/bar " + SystemUtils.LINE_SEPARATOR
				+ " g;");
		m_parsec.parse_i(m_reader.read(m_temporary.getFile()), m_listener);
		
		assertEquals(1, m_listener.m_stmt_txt.size());
		assertEquals(0, m_listener.m_blk_hdr_txt.size());
		assertEquals(0, m_listener.m_blk_txt_txt.size());
		
		assertEquals("foo " + SystemUtils.LINE_SEPARATOR + " bar "
				+ SystemUtils.LINE_SEPARATOR + " g",
				m_listener.m_stmt_txt.get(0));
		TextRegionMatch m = m_listener.m_stmt_m.get(0);
		assertEquals(0, m.idx_in_region());
		assertEquals(1, m.coord_in_region().line());
		assertEquals(1, m.coord_in_region().column());
		assertEquals(m_temporary.getFile(), m.region().file().file());
	}
	
	@Test
	public void treats_strings_correctly() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(),
				"foo \"hello\\\"; {this}\";");
		m_parsec.parse_i(m_reader.read(m_temporary.getFile()), m_listener);
		
		assertEquals(1, m_listener.m_stmt_txt.size());
		assertEquals(0, m_listener.m_blk_hdr_txt.size());
		assertEquals(0, m_listener.m_blk_txt_txt.size());
		
		assertEquals("foo \"hello\\\"; {this}\"",
				m_listener.m_stmt_txt.get(0));
		TextRegionMatch m = m_listener.m_stmt_m.get(0);
		assertEquals(0, m.idx_in_region());
		assertEquals(1, m.coord_in_region().line());
		assertEquals(1, m.coord_in_region().column());
		assertEquals(m_temporary.getFile(), m.region().file().file());
	}
	
	@Test
	public void parses_stmt_with_two_parsers_first_succeeds()
			throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(),
				"foo;bar;");
		TestDelegateParser p1 = new TestDelegateParser();
		TestDelegateParser p2 = new TestDelegateParser();
		m_parsec.add(p1);
		m_parsec.add(p2);
		
		m_parsec.parse(m_reader.read(m_temporary.getFile()), null);
		
		assertEquals(2, p1.m_statements.size());
		assertEquals("foo", p1.m_statements.get(0));
		assertEquals("bar", p1.m_statements.get(1));
		assertEquals(0, p1.m_block_headers.size());
		assertEquals(0, p1.m_block_texts.size());
		assertEquals(0, p2.m_statements.size());
		assertEquals(0, p2.m_block_headers.size());
		assertEquals(0, p2.m_block_texts.size());
 	}
	
	@Test
	public void parses_block_with_two_parsers_first_succeeds()
			throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(),
				"foo {}bar {}");
		TestDelegateParser p1 = new TestDelegateParser();
		TestDelegateParser p2 = new TestDelegateParser();
		m_parsec.add(p1);
		m_parsec.add(p2);
		
		m_parsec.parse(m_reader.read(m_temporary.getFile()), null);
		
		assertEquals(0, p1.m_statements.size());
		assertEquals(2, p1.m_block_headers.size());
		assertEquals(2, p1.m_block_texts.size());
		assertEquals("foo", p1.m_block_headers.get(0));
		assertEquals("", p1.m_block_texts.get(0));
		assertEquals("bar", p1.m_block_headers.get(1));
		assertEquals("", p1.m_block_texts.get(1));
		assertEquals(0, p2.m_statements.size());
		assertEquals(0, p2.m_block_headers.size());
		assertEquals(0, p2.m_block_texts.size());
 	}
	
	@Test
	public void parses_stmt_with_two_parsers_first_fails() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(),
				"foo;bar;");
		TestDelegateParser p1 = new TestDelegateParser();
		TestDelegateParser p2 = new TestDelegateParser();
		m_parsec.add(p1);
		m_parsec.add(p2);
		
		p1.m_throw_statements.add(new LocalizedParseException("fail",
				new LCCoord(1, 1)));
		m_parsec.parse(m_reader.read(m_temporary.getFile()), null);
		
		assertEquals(2, p1.m_statements.size());
		assertEquals("foo", p1.m_statements.get(0));
		assertEquals("bar", p1.m_statements.get(1));
		assertEquals(0, p1.m_block_headers.size());
		assertEquals(0, p1.m_block_texts.size());
		assertEquals(1, p2.m_statements.size());
		assertEquals("foo", p2.m_statements.get(0));
		assertEquals(0, p2.m_block_headers.size());
		assertEquals(0, p2.m_block_texts.size());
 	}
	
	@Test
	public void parses_block_with_two_parsers_first_fails() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(),
				"foo {}bar {}");
		TestDelegateParser p1 = new TestDelegateParser();
		TestDelegateParser p2 = new TestDelegateParser();
		m_parsec.add(p1);
		m_parsec.add(p2);
		
		p1.m_throw_h_blocks.add(new LocalizedParseException("fail",
				new LCCoord(1, 1)));
		m_parsec.parse(m_reader.read(m_temporary.getFile()), null);
		
		assertEquals(0, p1.m_statements.size());
		assertEquals(2, p1.m_block_headers.size());
		assertEquals(2, p1.m_block_texts.size());
		assertEquals("foo", p1.m_block_headers.get(0));
		assertEquals("", p1.m_block_texts.get(0));
		assertEquals("bar", p1.m_block_headers.get(1));
		assertEquals("", p1.m_block_texts.get(1));
		assertEquals(0, p2.m_statements.size());
		assertEquals(1, p2.m_block_headers.size());
		assertEquals(1, p2.m_block_texts.size());
		assertEquals("foo", p2.m_block_headers.get(0));
		assertEquals("", p2.m_block_texts.get(0));
 	}
	
	@Test
	public void parses_with_two_parsers_both_fail_returns_latest_failure()
			throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(),
				"foo;bar;");
		TestDelegateParser p1 = new TestDelegateParser();
		TestDelegateParser p2 = new TestDelegateParser();
		m_parsec.add(p1);
		m_parsec.add(p2);
		
		p1.m_throw_statements.add(null);
		p1.m_throw_statements.add(new LocalizedParseException("fail 1",
				new LCCoord(1, 1)));
		p2.m_throw_statements.add(new LocalizedParseException("fail 2",
				new LCCoord(1, 2)));
		
		try {
			m_parsec.parse(m_reader.read(m_temporary.getFile()), null);
			fail();
		} catch (LocalizedParseException pe) {
			assertEquals("fail 2", pe.getMessage());
			assertEquals(1, pe.location().line());
			assertEquals(6, pe.location().column());
		}
		
		assertEquals(2, p1.m_statements.size());
		assertEquals("foo", p1.m_statements.get(0));
		assertEquals("bar", p1.m_statements.get(1));
		assertEquals(0, p1.m_block_headers.size());
		assertEquals(0, p1.m_block_texts.size());
		assertEquals(1, p2.m_statements.size());
		assertEquals("bar", p2.m_statements.get(0));
		assertEquals(0, p2.m_block_headers.size());
		assertEquals(0, p2.m_block_texts.size());
 	}
	
	@Test
	public void parse_block_with_multiline_header() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(),
				"f " + SystemUtils.LINE_SEPARATOR + " g {h}");
		TestDelegateParser p1 = new TestDelegateParser();
		m_parsec.add(p1);
		
		m_parsec.parse(m_reader.read(m_temporary.getFile()), null);
		
		assertEquals(0, p1.m_statements.size());
		assertEquals(1, p1.m_block_headers.size());
		assertEquals(1, p1.m_block_texts.size());
		assertEquals("f " + SystemUtils.LINE_SEPARATOR + " g",
				p1.m_block_headers.get(0));
		assertEquals("h", p1.m_block_texts.get(0));
	}
	
	@Test
	public void parse_fail_in_multiline_header() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(),
				SystemUtils.LINE_SEPARATOR + "f " + SystemUtils.LINE_SEPARATOR
				+ " glu {h}");
		TestDelegateParser p1 = new TestDelegateParser();
		m_parsec.add(p1);
		p1.m_throw_h_blocks.add(new LocalizedParseException("Foo",
				new LCCoord(2, 3)));
		
		try {
			m_parsec.parse(m_reader.read(m_temporary.getFile()), null);
			fail();
		} catch (LocalizedParseException pe) {
			assertEquals("Foo", pe.getMessage());
			assertEquals(3, pe.location().line());
			assertEquals(3, pe.location().column());
		}
		
		assertEquals(0, p1.m_statements.size());
		assertEquals(1, p1.m_block_headers.size());
		assertEquals(1, p1.m_block_texts.size());
		assertEquals("f " + SystemUtils.LINE_SEPARATOR + " glu",
				p1.m_block_headers.get(0));
		assertEquals("h", p1.m_block_texts.get(0));
	}
	
	@Test
	public void parse_fail_in_block_header() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(),
				" hdr{}");
		TestDelegateParser p1 = new TestDelegateParser();
		m_parsec.add(p1);
		
		p1.m_throw_h_blocks.add(new LocalizedParseException("fail",
				new LCCoord(1, 1)));
		
		try {
			m_parsec.parse(m_reader.read(m_temporary.getFile()), null);
			fail();
		} catch (LocalizedParseException pe) {
			assertEquals("fail", pe.getMessage());
			assertEquals(1, pe.location().line());
			assertEquals(2, pe.location().column());
		}
		
		assertEquals(0, p1.m_statements.size());
		assertEquals(1, p1.m_block_headers.size());
		assertEquals(1, p1.m_block_texts.size());
		assertEquals("hdr", p1.m_block_headers.get(0));
		assertEquals("", p1.m_block_texts.get(0));
	}
	
	@Test
	public void parse_fail_in_block_text() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(),
				" hdr{vvv}");
		TestDelegateParser p1 = new TestDelegateParser();
		m_parsec.add(p1);
		
		p1.m_throw_t_blocks.add(new LocalizedParseException("fail",
				new LCCoord(1, 2)));
		
		try {
			m_parsec.parse(m_reader.read(m_temporary.getFile()), null);
			fail();
		} catch (LocalizedParseException pe) {
			assertEquals("fail", pe.getMessage());
			assertEquals(1, pe.location().line());
			assertEquals(7, pe.location().column());
		}
		
		assertEquals(0, p1.m_statements.size());
		assertEquals(1, p1.m_block_headers.size());
		assertEquals(1, p1.m_block_texts.size());
		assertEquals("hdr", p1.m_block_headers.get(0));
		assertEquals("vvv", p1.m_block_texts.get(0));
	}
	
	@Test
	public void parse_two_parsers_fail_in_block_header_and_text()
			throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(),
				" hdr{vvv}");
		TestDelegateParser p1 = new TestDelegateParser();
		m_parsec.add(p1);
		TestDelegateParser p2 = new TestDelegateParser();
		m_parsec.add(p2);
		
		p1.m_throw_h_blocks.add(new LocalizedParseException("fail H",
				new LCCoord(1, 1)));
		p2.m_throw_t_blocks.add(new LocalizedParseException("fail T",
				new LCCoord(1, 2)));
		
		try {
			m_parsec.parse(m_reader.read(m_temporary.getFile()), null);
			fail();
		} catch (LocalizedParseException pe) {
			assertEquals("fail T", pe.getMessage());
			assertEquals(1, pe.location().line());
			assertEquals(7, pe.location().column());
		}
		
		assertEquals(0, p1.m_statements.size());
		assertEquals(1, p1.m_block_headers.size());
		assertEquals(1, p1.m_block_texts.size());
		assertEquals("hdr", p1.m_block_headers.get(0));
		assertEquals("vvv", p1.m_block_texts.get(0));
		assertEquals(0, p2.m_statements.size());
		assertEquals(1, p2.m_block_headers.size());
		assertEquals(1, p2.m_block_texts.size());
		assertEquals("hdr", p2.m_block_headers.get(0));
		assertEquals("vvv", p2.m_block_texts.get(0));
	}
	
	@Test
	public void passes_context_to_delegates() throws Exception {
		FileContentWorker.setContents(m_temporary.getFile(),
				"a;b{c}");
		Integer context = new Integer(6);
		
		TestDelegateParser p = new TestDelegateParser();
		m_parsec.add(p);
		
		m_parsec.parse(m_reader.read(m_temporary.getFile()), context);
		
		assertEquals(1, p.m_statements.size());
		assertEquals("a", p.m_statements.get(0));
		assertEquals(1, p.m_statement_contexts.size());
		assertEquals(context, p.m_statement_contexts.get(0));
		
		assertEquals(1, p.m_block_headers.size());
		assertEquals(1, p.m_block_texts.size());
		assertEquals("b", p.m_block_headers.get(0));
		assertEquals("c", p.m_block_texts.get(0));
		assertEquals(1, p.m_block_contexts.size());
		assertEquals(context, p.m_block_contexts.get(0));
	}
	
	@Test
	public void parse_in_memory_string() throws Exception {
		TestDelegateParser p = new TestDelegateParser();
		m_parsec.add(p);
		
		m_parsec.parse(m_reader.read_memory("xx; yy{zz}"), null);
		
		assertEquals(1, p.m_statements.size());
		assertEquals("xx", p.m_statements.get(0));
		assertEquals(1, p.m_statement_contexts.size());
		assertNull(p.m_statement_contexts.get(0));
		
		assertEquals(1, p.m_block_headers.size());
		assertEquals(1, p.m_block_texts.size());
		assertEquals("yy", p.m_block_headers.get(0));
		assertEquals("zz", p.m_block_texts.get(0));
		assertEquals(1, p.m_block_contexts.size());
		assertNull(p.m_block_contexts.get(0));
	}
	
	@Test(expected = LocalizedParseException.class)
	public void parse_in_memory_string_with_parse_error() throws Exception {
		TestDelegateParser p = new TestDelegateParser();
		m_parsec.add(p);
		
		m_parsec.parse(m_reader.read_memory("xx; yy{zz{}"), null);
	}
}
