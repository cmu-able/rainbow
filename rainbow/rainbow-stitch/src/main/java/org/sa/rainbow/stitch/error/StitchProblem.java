/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
/**
 * Stitch Editor
 *
 * @author Ali Almossawi <aalossaw@cs.cmu.edu>
 * @version 0.1
 * @created July 1, 2006
 * <p/>
 * Parts of the code in this project adapted from or inspired by
 * XMLAuthor © 2005 Nick Wilson (SvcDelivery) released under the
 * GNU GPL and the Java Editor example © 2000, 2005 IBM Corporation
 * and others released under the Eclipse Public License v1.0
 * @author Shang-Wen Cheng <zensoul@cs.cmu.edu>
 * Modified:  August 10, 2006, moved to org.sa.rainbow.stitchState plug-in to prevent
 * cyclic dependency and to eliminate the undesirable dependency of the language
 * plug-in on the Editor plug-in.  Also removed implements of IRegion interface.
 */

package org.sa.rainbow.stitch.error;


public class StitchProblem implements IStitchProblem {

    private RecognitionException sourceException = null;
    private int                  severity        = IStitchProblem.UNKNOWN;

    public StitchProblem (RecognitionException exception, int severity) {
        sourceException = exception;
        this.severity = severity;
    }

    public Exception getSourceException () {
        return sourceException;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitchState.editor.parserhelper.IStitchProblem#getSeverity()
     */
    public int getSeverity () {
        return severity;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitchState.editor.parserhelper.IStitchProblem#getMessage()
     */
    public String getMessage () {
        return sourceException.getMessage ();
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitchState.editor.parserhelper.IStitchProblem#getLine()
     */
    public int getLine () {
        return sourceException.getLine ();
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.stitchState.editor.parserhelper.IStitchProblem#getColumn()
     */
    public int getColumn () {
        return sourceException.getColumn ();
    }

    @Override
    public boolean equals (Object o) {
        if (o instanceof StitchProblem) {
            StitchProblem p = (StitchProblem) o;
            return getMessage ().equals (p.getMessage ()) && getLine () == p.getLine () && getColumn () == p
                    .getColumn () && severity == p.severity;
        }
        return false;
    }

    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + getMessage ().hashCode ();
        result = result * prime + getColumn ();
        result = result * prime + getLine ();
        result = result * prime + getSeverity ();
        return result;
    }

    @Override
    public IStitchProblem clone () {
        return new StitchProblem (sourceException, severity);
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
