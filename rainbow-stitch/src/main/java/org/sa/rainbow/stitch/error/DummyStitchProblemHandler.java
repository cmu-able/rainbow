/**
 * Created August 16, 2006
 */
package org.sa.rainbow.stitch.error;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * This class serves as the default problem handler when the language module
 * is used alone, rather than by the Editor, to parse a Stitch script. 
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class DummyStitchProblemHandler implements StitchProblemHandler {

	private LinkedHashSet<StitchProblem> m_problems = null;

	/**
	 * Default constructor
	 */
	public DummyStitchProblemHandler() {
		m_problems = new LinkedHashSet<StitchProblem>();
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.error.StitchProblemHandler#setProblem(org.sa.rainbow.stitch.error.StitchProblem)
	 */
	public void setProblem(StitchProblem problem) {
		m_problems.add(problem);
	}

	public Collection<StitchProblem> getProblems () {
		return m_problems;
	}
	public void clearProblems () {
		m_problems.clear();
	}

	@Override
	public void addAll(Collection<StitchProblem> problems) {
		m_problems.addAll (problems);
	}

	@Override
	public Collection<StitchProblem> unreportedProblems() {
		return m_problems;
	}
}
