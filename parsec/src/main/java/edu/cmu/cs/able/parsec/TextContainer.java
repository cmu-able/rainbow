package edu.cmu.cs.able.parsec;

import incubator.pval.Ensure;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

/**
 * Class containing the text to parse. This is usually the text of a file but
 * can contain text of multiple files if there are includes. Containers are
 * immutable. Containers are created using {@link ParsecFileReader}.
 */
public class TextContainer {
	/**
	 * Regions that build this text in order.
	 */
	private List<TextRegion> m_regions;
	
	/**
	 * Contains one entry per line with all text in the line which may span
	 * multiple regions.
	 */
	private List<List<TextRegionLine>> m_index;
	
	/**
	 * Creates a new container from a given list of regions.
	 * @param regions the regions
	 */
	TextContainer(List<TextRegion> regions) {
		Ensure.notNull(regions);
		m_regions = new ArrayList<>(regions);
		
		m_index = new ArrayList<>();
		List<TextRegionLine> current_line = new ArrayList<>();
		m_index.add(current_line);
		for (TextRegion r : regions) {
			String region_text = r.text();
			String[] split =
					StringUtils.splitByWholeSeparatorPreserveAllTokens(
					region_text, SystemUtils.LINE_SEPARATOR);
			int index_in_text = 0;
			for (int s_idx = 0; s_idx < split.length; s_idx++) {
				if (s_idx > 0) {
					current_line = new ArrayList<>();
					m_index.add(current_line);
					index_in_text += SystemUtils.LINE_SEPARATOR.length();
				}
				
				current_line.add(new TextRegionLine(r, index_in_text,
						s_idx, split[s_idx]));
				index_in_text += split[s_idx].length();
			}
		}
	}
	
	/**
	 * Obtains a reader that allows reading the container text.
	 * @return the text reader
	 */
	public Reader reader() {
		return new RegionReader();
	}
	
	/**
	 * Finds which region matches the given coordinate.
	 * @param c the coordinate
	 * @return the match
	 */
	public TextRegionMatch locate(LCCoord c) {
		Ensure.not_null(c);
		Ensure.greater(c.line(), 0);
		Ensure.less_equal(c.line(), m_index.size());
		Ensure.greater(c.column(), 0);
		
		int ln = c.line() - 1;
		int cn = c.column() - 1;
		
		List<TextRegionLine> line = m_index.get(ln);
		int count = 0;
		for (TextRegionLine l : line) {
			if (count + l.m_text.length() >= cn) {
				return new TextRegionMatch(l.m_region, l.m_start + cn - count,
						new LCCoord(l.m_line + 1, cn - count + 1));
			}
			
			count += l.m_text.length();
		}
		
		Ensure.is_true(false);
		return null;
	}
	
	/**
	 * Line of a text region.
	 */
	private static class TextRegionLine {
		/**
		 * The region.
		 */
		private TextRegion m_region;
		
		/**
		 * The index within the region in which this line starts.
		 */
		private int m_start;
		
		/**
		 * The line number in the region (<code>1</code>-based)
		 */
		private int m_line;
		
		/**
		 * The text in this line in the region.
		 */
		private String m_text;
		
		/**
		 * Creates a new line of a region.
		 * @param region the region
		 * @param start the index within the region where the line starts
		 * @param line the line number in the region (<code>1</code>-based)
		 * @param text the text
		 */
		TextRegionLine(TextRegion region, int start, int line, String text) {
			m_region = region;
			m_start = start;
			m_line = line;
			m_text = text;
		}
	}
	
	/**
	 * Reader that allows output of the whole container text.
	 */
	private class RegionReader extends Reader {
		/**
		 * From which region are we currently reading?
		 */
		private int m_current_index;
		
		/**
		 * The current region reader.
		 */
		private Reader m_current_reader;
		
		/**
		 * Are we done?
		 */
		private boolean m_terminated;
		
		/**
		 * Creates a new reader.
		 */
		RegionReader() {
			m_current_index = -1;
			m_current_reader = null;
			advance();
		}
		
		/**
		 * Advances to the next file or marks the reader as terminated.
		 */
		private void advance() {
			assert m_terminated == false;
			assert m_current_index < m_regions.size();
			
			if (m_current_reader != null) {
				try {
					m_current_reader.close();
				} catch (IOException e) {
					/*
					 * In-memory handling: no failures.
					 */
					assert false;
				}
				
				m_current_reader = null;
			}
			
			m_current_index++;
			if (m_current_index == m_regions.size()) {
				m_terminated = true;
			} else {
				m_current_reader = m_regions.get(m_current_index).reader();
			}
		}

		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			Ensure.notNull(cbuf);
			Ensure.isTrue(off >= 0);
			Ensure.isTrue(off < cbuf.length);
			Ensure.isTrue(len >= 0);
			Ensure.isTrue(off + len <= cbuf.length);
			
			do {
				if (m_terminated) {
					return -1;
				}
				
				assert m_current_reader != null;
				int r = m_current_reader.read(cbuf, off, len);
				if (r == -1) {
					advance();
					continue;
				}
				
				return r;
			} while (true);
		}

		@Override
		public void close() throws IOException {
			while (!m_terminated) {
				advance();
			}
		}
	}
}
