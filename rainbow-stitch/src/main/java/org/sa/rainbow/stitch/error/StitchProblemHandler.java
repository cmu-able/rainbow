/**
 * Stitch Editor
 * @author Ali Almossawi <aalossaw@cs.cmu.edu>
 * @version 0.1
 * @created July 1, 2006
 * 
 * Parts of the code in this project adapted from or inspired by
 * XMLAuthor © 2005 Nick Wilson (SvcDelivery) released under the 
 * GNU GPL and the Java Editor example © 2000, 2005 IBM Corporation 
 * and others released under the Eclipse Public License v1.0
 * 
 * @author Shang-Wen Cheng <zensoul@cs.cmu.edu>
 * Modified:  August 10, 2006, use the extracted IStitchProblem interface.
 */

package org.sa.rainbow.stitch.error;

import java.util.Collection;

public interface StitchProblemHandler {
    void setProblem(StitchProblem problem);
    void addAll (Collection<StitchProblem> problems);
	Collection<StitchProblem> unreportedProblems();
}
