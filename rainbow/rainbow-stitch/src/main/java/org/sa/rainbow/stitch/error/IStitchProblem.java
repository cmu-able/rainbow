package org.sa.rainbow.stitch.error;

/**
 * Created by schmerl on 9/28/2016.
 */
public interface IStitchProblem {
    int UNKNOWN = 0;
    int WARNING = 1;
    int ERROR   = 2;
    int FATAL   = 3;

    /* (non-Javadoc)
         * @see org.sa.rainbow.stitch.editor.parserhelper.IStitchProblem#getSeverity()
         */
    int getSeverity ();

    /* (non-Javadoc)
         * @see org.sa.rainbow.stitch.editor.parserhelper.IStitchProblem#getMessage()
         */
    String getMessage ();

    /* (non-Javadoc)
         * @see org.sa.rainbow.stitch.editor.parserhelper.IStitchProblem#getLine()
         */
    int getLine ();

    /* (non-Javadoc)
         * @see org.sa.rainbow.stitch.editor.parserhelper.IStitchProblem#getColumn()
         */
    int getColumn ();

    public IStitchProblem clone ();
}
