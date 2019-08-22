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
    	Expression expression = preVar.valStmt.expressions().get(0);
    	expression.clearState();
		Object o = expression.evaluate(null);
        //preVar.computeValue ();
        Expression e = expression;
        this.setValue (e.getResult ());
    }
}
