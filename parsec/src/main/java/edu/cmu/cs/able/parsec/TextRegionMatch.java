package edu.cmu.cs.able.parsec;

import incubator.pval.Ensure;

/**
 * Class representing a location within a text region.
 */
public class TextRegionMatch {
	/**
	 * Filename reported in {@link #toString()} when no file exists.
	 */
	public static final String NO_FILE_NAME = "(no file)";
	
	/**
	 * The region.
	 */
	private TextRegion m_region;
	
	/**
	 * The index within the region text.
	 */
	private int m_idx;
	
	/**
	 * The location in row/column.
	 */
	private LCCoord m_coord;
	
	/**
	 * Creates a new match.
	 * @param region the region
	 * @param idx the index within the region
	 * @param coord the location in row/column that corresponds to the index
	 */
	TextRegionMatch(TextRegion region, int idx, LCCoord coord) {
		Ensure.notNull(region);
		Ensure.isTrue(idx >= 0);
		Ensure.isTrue(idx < region.end());
		Ensure.notNull(coord);
		
		m_region = region;
		m_idx = idx;
		m_coord = coord;
	}
	
	/**
	 * Obtains the region where this match is located.
	 * @return the region
	 */
	public TextRegion region() {
		return m_region;
	}
	
	/**
	 * Obtains the index in the region's text of the match.
	 * @return the index
	 */
	public int idx_in_region() {
		return m_idx;
	}
	
	/**
	 * Obtains the coordinates within the region of the match.
	 * @return the coordinates
	 */
	public LCCoord coord_in_region() {
		return m_coord;
	}
	
	@Override
	public String toString() {
		String fn;
		if (m_region.file() != null) {
			fn = m_region.file().file().getAbsolutePath();
		} else {
			fn = NO_FILE_NAME;
		}
		
		LCCoord c = coord_in_region();
		return fn + ":" + c;
	}
}
