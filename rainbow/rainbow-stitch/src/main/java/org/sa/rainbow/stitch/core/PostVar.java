package org.sa.rainbow.stitch.core;

import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;
import org.sa.rainbow.stitch.parser.StitchParser;

/**
 * Created by schmerl on 10/17/2016.
 */
public class PostVar extends Var {

    final Var m_preVar;

    public PostVar (Var pre) {
        m_preVar = pre;
    }

    public Var preVar () {
    	return m_preVar;
    }

    public void computeAndSetValue () {
        // Always recompute the value
    	// Let's exempt simple assignment from recomputation
        m_preVar.computeValue ();
        Expression e = m_preVar.scope.expressions ().get (0);
        this.setValue (e.getResult ());
    }
}
