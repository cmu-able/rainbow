package org.sa.rainbow.gauges.diagnosis;

import java.util.HashSet;
import java.util.Set;

/**
 * The diagnosis report contains information about all fault candidates in
 * a system. Each fault candidate is ranked with a probability and all
 * probabilities must add to <code>1</code>.
 */
public class DiagnosisReport {
	/**
	 * Probabilities must add to <code>1</code> up to this value.
	 */
	private static final float PROBABILITY_ULP = 0.001f;
	
	/**
	 * Set of candidates.
	 */
	private Set<FaultCandidate> candidates;
	
	/**
	 * Creates a new diagnosis report with a given set of candidates.
	 * @param candidates the candidates or empty if there are no failures
	 */
	public DiagnosisReport(Set<FaultCandidate> candidates) {
		if (candidates == null) {
			throw new IllegalArgumentException("candidates == null");
		}
		
		float sum = 0;
		for (FaultCandidate fc : candidates) {
			sum += fc.getProbability();
		}
		
		if (candidates.size() > 0) {
			if (Math.abs(1 - sum) > PROBABILITY_ULP) {
				throw new IllegalArgumentException("Probabilities do not add "
						+ "up to 1, they add to " + sum + ".");
			}
		}
		
		this.candidates = new HashSet<>(candidates);
	}
	
	/**
	 * Obtains all fault candidates.
	 * @return all fault candidates or an empty set if there are no failures
	 */
	public Set<FaultCandidate> getCandidates() {
		return new HashSet<>(candidates);
	}
}