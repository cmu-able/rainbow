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
 * Modified:  August 10, 2006, moved to org.sa.rainbow.stitch plug-in to prevent
 * cyclic dependency and to eliminate the undesirable dependency of the language
 * plug-in on the Editor plug-in.  Also removed implements of IRegion interface.
 */

package org.sa.rainbow.stitch.error;

import antlr.RecognitionException;

public class StitchProblem {
	public static final int UNKNOWN = 0;
	public static final int WARNING = 1;
	public static final int ERROR = 2;
	public static final int FATAL = 3;

	private RecognitionException sourceException = null;
	private int severity = UNKNOWN;

	public StitchProblem(RecognitionException exception, int severity) {
		sourceException = exception;
		this.severity = severity;
	}
    
	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.editor.parserhelper.IStitchProblem#getSeverity()
	 */
	public int getSeverity() {
		return severity;
	}
	
	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.editor.parserhelper.IStitchProblem#getMessage()
	 */
	public String getMessage() {
		return sourceException.getMessage();
	}
	
	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.editor.parserhelper.IStitchProblem#getLine()
	 */
	public int getLine() {
		return sourceException.getLine();
	}
	
	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.editor.parserhelper.IStitchProblem#getColumn()
	 */
	public int getColumn() {
		return sourceException.getColumn();
	}
	
	public boolean equals (Object o) {
		if (o instanceof StitchProblem) {
			StitchProblem p = (StitchProblem) o;
			return getMessage ().equals (p.getMessage ()) && getLine () == p.getLine () && getColumn () == p.getColumn () && severity == p.severity;
		}
		return false;
	}
	
	public int hashCode () {
		 final int prime = 31;
	        int result = 1;
	        result = prime * result + getMessage ().hashCode ();
	        result = result * prime + getColumn ();
	        result = result * prime + getLine ();
	        result = result * prime + getSeverity ();
	        return result;
	}
/*
	public int getOffset() {
		// TODO Calculate offset location
		return 1;
	}

	public int getLength() {
		// TODO Calculate length of problem
		return 1;
	}
*/	
}
