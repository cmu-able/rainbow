package org.sa.rainbow.gauges.diagnosis;

import java.util.HashSet;
import java.util.Set;

/**
 * A fault candidate represents a set of elements which may be failing
 * independently. If this candidate has components <em>a</em> and <em>b</em>
 * this means that both components <em>a</em> and <em>b</em> are faulty and 
 * they both may fail at any time. Their failures are not, however, correlated.
 * Fault candidates have an associated probability used when ranking several
 * candidates.
 */
public class FaultCandidate {
	/**
	 * Set of names of faulty elements.
	 */
	private Set<String> elements;
	
	/**
	 * Fault probability (probability that this candidate is correct).
	 */
	private float probability;
	
	/**
	 * Creates a new fault candidate.
	 * @param elements the fault elements
	 * @param probability the probability that this fault candidate is the
	 * correct one
	 */
	public FaultCandidate(Set<String> elements, float probability) {
		if (elements == null) {
			throw new IllegalArgumentException("elements == null");
		}
		
		if (elements.size() == 0) {
			throw new IllegalArgumentException("elements.size() == 0");
		}
		
		if (probability < 0) {
			throw new IllegalArgumentException("probability < 0");
		}
		
		if (probability > 1) {
			throw new IllegalArgumentException("probability > 1");
		}
		
		this.elements = new HashSet<>(elements);
		this.probability = probability;
	}
	
	/**
	 * Obtains the names of the elements in this candidate.
	 * @return the names
	 */
	public Set<String> getElements() {
		return new HashSet<>(elements);
	}
	
	/**
	 * Obtains the probability that this candidate is the correct one.
	 * @return the probability
	 */
	public float getProbability() {
		return probability;
	}
}