/**
 * Created April 21, 2009.
 */
package org.sa.rainbow.stitch.util;

/**
 * Captures one data entry of execution history for the Tactic (or any unit of
 * adaptation execution as determined in the future).  As implied by the field
 * data {@code mean} and {@code variance}, statistical values of an execution
 * history datum assume a Normal distribution, which is guaranteed us by the
 * Central Limit Theorem when more than 30 samples have been gathered.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public final class ExecutionHistoryData {

	private String m_qualifiedIden = null;
	private int m_sampleSize = 0;
	private double m_mean = 0.0;
	private double m_variance = 0.0;
	private long m_min = Long.MAX_VALUE;
	private long m_max = Long.MIN_VALUE;

	/**
	 * Default constructor, use method {@link #initData(String, int, double, double, long, long)} to initialize data items.
	 */
	public ExecutionHistoryData () {
	}

	/**
	 * Class constructor, which uses {@link #initData(String, int, double, double, long, long)} to initialize data items.
	 * @param iden        fully-qualified string identifier for this data
	 * @param sampleSize  initial sample size read from storage
	 * @param mean        initial mean duration value read from storage
	 * @param variance    initial variance value read from storage
	 * @param min         initial minimum duration value read from storage
	 * @param max         initial maximum duration value read from storage
	 */
	public ExecutionHistoryData (String iden, int sampleSize,
			double mean, double variance, long min, long max) {
		initData(iden, sampleSize, mean, variance, min, max);
	}

	/**
	 * Initializes the data items for this history data entry, called only once.
	 * With the exception of the identifier, no other set method is provided to
	 * convey the semantic that updates to data values occur internally when a
	 * new sample measurement is supplied via {@link #addDurationSample(long)}.
	 * 
	 * @param iden        fully-qualified string identifier for this data
	 * @param sampleSize  initial sample size read from storage
	 * @param mean        initial mean duration value read from storage
	 * @param variance    initial variance value read from storage
	 * @param min         initial minimum duration value read from storage
	 * @param max         initial maximum duration value read from storage
	 */
	public void initData (String iden, int sampleSize,
			double mean, double variance, long min, long max) {

		if (m_qualifiedIden != null) return;

		m_qualifiedIden = iden;
		m_sampleSize = sampleSize;
		m_mean = mean;
		m_variance = variance;
		m_min = min;
		m_max = max;
	}

	/**
	 * @return the (fully-qualified) string identifier that identifies this history data
	 */
	public String getIdentifier () {
		return m_qualifiedIden;
	}

	/**
	 * Sets the identifier for this history data entry.  Note that identifiers
	 * should be fully-qualified with the Stitch package name to ensure
	 * uniqueness in the Tactic history database.
	 * 
	 * @param iden  new, fully-qualified string to identify this history data
	 */
	public void setIdentifier (String iden) {
		m_qualifiedIden = iden;
	}

	/**
	 * @return the sample size for this history data
	 */
	public int getSampleSize () {
		return m_sampleSize;
	}

	/**
	 * @return the statistical mean of historical execution durations.
	 */
	public double getMeanDuration () {
		return m_mean;
	}

	/**
	 * @return the statistical variance of historical execution duration.
	 */
	public double getDurationVariance () {
		return m_variance;
	}

	/**
	 * @return the minimum duration in collected samples of execution history
	 */
	public long getMinDuration () {
		return m_min;
	}

	/**
	 * @return the maximum duration in collected samples of execution history
	 */
	public long getMaxDuration () {
		return m_max;
	}

	/**
	 * Adds an execution duration sample (dur) to history, which causes updates
	 * to potentially all the values:<ul>
	 * <li> Sample size (N):  <code>N' := N+1</code>
	 * <li> Mean (mu):  <code>mu' := (N*mu + dur)/(N')</code>
	 * <li> Variance (sigma^2):  <code>sigma'^2 = (N*sigma^2 + (dur - mu')^2)/(N')</code>
	 * <li> Min:  if dur smaller than original min, then stored as new minimum
	 * <li> Max:  if dur greater than original max, then stored as new maximum
	 * </ul>
	 * @param dur  new execution duration sample to add to history
	 */
	public void addDurationSample (long dur) {
		int oldN = m_sampleSize;
		++m_sampleSize;  // N' (will be greater than 1)
		assert m_sampleSize > 0;
		m_mean = (oldN * m_mean + dur)/m_sampleSize;
		m_variance = (oldN * m_variance + Math.pow(dur-m_mean,2))/m_sampleSize;
		if (dur < m_min) {
			m_min = dur;
		}
		if (dur > m_max) {  // when N==1, it's possible for max and min to be the same value.
			m_max = dur;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(m_qualifiedIden);
		sb.append("\t").append(m_sampleSize)
			.append("\t").append(m_mean).append("\t")
			.append(m_variance).append("\t").append(m_min)
			.append("\t").append(m_max);
		return sb.toString();
	}
	
}
