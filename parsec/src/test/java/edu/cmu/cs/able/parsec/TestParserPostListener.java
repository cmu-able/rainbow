package edu.cmu.cs.able.parsec;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.able.parsec.parser.ParseException;

/**
 * Listener that is informed of parsing, used for testing. It keeps track
 * of all invocations.
 */
public class TestParserPostListener extends ParsecParserPostListener {
	/**
	 * One entry per invocation of
	 * {@link #statement_recognized(String, TextRegionMatch)}.
	 */
	public List<String> m_stmt_txt;
	
	/**
	 * One entry per invocation of
	 * {@link #statement_recognized(String, TextRegionMatch)}.
	 */
	public List<TextRegionMatch> m_stmt_m;
	
	/**
	 * One entry per invocation of
	 * {@link #block_recognized(String, TextRegionMatch, String,
	 * TextRegionMatch)}
	 */
	public List<String> m_blk_hdr_txt;
	
	/**
	 * One entry per invocation of
	 * {@link #block_recognized(String, TextRegionMatch, String,
	 * TextRegionMatch)}
	 */
	public List<TextRegionMatch> m_blk_hdr_m;
	
	/**
	 * One entry per invocation of
	 * {@link #block_recognized(String, TextRegionMatch, String,
	 * TextRegionMatch)}
	 */
	public List<String> m_blk_txt_txt;
	
	/**
	 * One entry per invocation of
	 * {@link #block_recognized(String, TextRegionMatch, String,
	 * TextRegionMatch)}
	 */
	public List<TextRegionMatch> m_blk_txt_m;
	
	/**
	 * Creates a new listener.
	 */
	TestParserPostListener() {
		m_stmt_txt = new ArrayList<>();
		m_stmt_m = new ArrayList<>();
		m_blk_hdr_txt = new ArrayList<>();
		m_blk_hdr_m = new ArrayList<>();
		m_blk_txt_txt = new ArrayList<>();
		m_blk_txt_m = new ArrayList<>();
	}

	@Override
	void statement_recognized(String text, TextRegionMatch loc)
			throws ParseException {
		m_stmt_txt.add(text);
		m_stmt_m.add(loc);
	}

	@Override
	void block_recognized(String header, TextRegionMatch h_loc, String text,
			TextRegionMatch t_loc) throws ParseException {
		m_blk_hdr_txt.add(header);
		m_blk_hdr_m.add(h_loc);
		m_blk_txt_txt.add(text);
		m_blk_txt_m.add(t_loc);
	}
}
