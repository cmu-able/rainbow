package org.sa.rainbow.stitch.core;

/**
 * Created by schmerl on 10/17/2016.
 */
public class PostVar extends Var {

    final Var preVar;

    public PostVar (Var pre) {
        preVar = pre;
    }


    public void computeAndSetValue () {
        // Always recompute the value
        preVar.computeValue ();
        Expression e = preVar.scope.expressions ().get (0);
        this.setValue (e.getResult ());
    }
}
