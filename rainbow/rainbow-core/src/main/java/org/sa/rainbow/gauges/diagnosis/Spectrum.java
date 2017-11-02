package org.sa.rainbow.gauges.diagnosis;

import java.util.HashSet;
import java.util.Set;

/**
 * A spectrum represents a run of a computation over a set of architectural
 * elements which has been classified as either a success or a failure.
 */
public class Spectrum {
	/**
	 * The architectural elements.
	 */
	private Set<String> elements;
	
	/**
	 * Is the computation correct?
	 */
	private boolean correct;
	
	/**
	 * Creates a new spectrum.
	 * @param elements the set of elements over which computation occurs
	 * @param correct does this correspond to a successful computation
	 * (<code>true</code>) or to a failure (<code>false</code>)?
	 */
	public Spectrum(Set<String> elements, boolean correct) {
		if (elements == null) {
			throw new IllegalArgumentException("elements == null");
		}
		
		if (elements.size() == 0) {
			throw new IllegalArgumentException("elements.size() == 0");
		}
		
		this.elements = new HashSet<>(elements);
		this.correct = correct;
	}
	
	/**
	 * Obtains the architectural elements in this spectrum.
	 * @return the set of elements
	 */
	public Set<String> getElements() {
		return new HashSet<>(elements);
	}
	
	/**
	 * Does this spectrum corresponds to a correct computation?
	 * @return is it correct?
	 */
	public boolean isCorrect() {
		return correct;
	}
}
