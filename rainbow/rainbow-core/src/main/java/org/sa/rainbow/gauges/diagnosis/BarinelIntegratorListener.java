package org.sa.rainbow.gauges.diagnosis;

/**
 * Interface implemented by objects that listen to notifications about the
 * barinel integrator.
 */
public interface BarinelIntegratorListener {
	/**
	 * The staccato algorithm has started.
	 * @param r_id the run ID
	 * @param successes the number of successes
	 * @param failures the number of failures
	 */
	public void staccato_started(int r_id, int successes, int failures);
	
	/**
	 * Barinel has started.
	 * @param r_id the run ID
	 */
	public void barinel_started(int r_id);
	
	/**
	 * Diagnosis is completed.
	 * @param r_id the run ID
	 * @param candidates the number of candidates
	 */
	public void diagnosis_completed(int r_id, int candidates);
}
