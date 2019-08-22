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
 * Created July 5, 2006.
 */
package org.sa.rainbow.stitch.core;

import org.sa.rainbow.stitch.visitor.Stitch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A class whose object represents a node in the Strategy Tree.
 * <p/>
 * History:<ol>
 * <li>[2008.02.07] Removed exit condition and expiration for tactic action;
 * replaced with original condition "duration";
 * all associated member methods removed.
 * </ol>
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class StrategyNode {
    /**
     * Default loop max count if none provided
     */
    public static final int DEFAULT_LOOP_MAX = 3;

    /**
     * Enclosing Stitch object
     */
    private Stitch/*State*/ m_stitch = null;

    /**
     * Label of node
     */
    private String m_label = null;

    /**
     * Boolean indicating that a probability value is specified
     */
    private boolean    m_hasProb  = false;
    /**
     * Key string for retrieving the probability value, means value is cached;
     * if null, then probability should be a literal.
     */
    private Expression m_probExpr = null;
    /**
     * Probability value of branch, if applicable, default to full likelihood
     */
    private double     m_prob     = 1.0;

    /**
     * Flag to hold type of condition
     */
    private Strategy.ConditionKind m_condFlag = Strategy.ConditionKind.UNKNOWN;
    /**
     * The condition expression
     */
    private Expression             m_condExpr = null;

    /**
     * Boolean indicating that a durationg value is specified
     */
    private boolean    m_hasDuration = false;
    /**
     * A simple Expression for duration (though full expression is allowed,
     * not much is available on which to express, so this is effectively
     * reduced (hopefully) to arithmetic expressions.
     */
    private Expression m_durExpr     = null;

    /**
     * Flag to hold type of referenced action
     */
    private Strategy.ActionKind m_actionFlag     = Strategy.ActionKind.UNKNOWN;
    /**
     * Referenced tactic
     */
    private String              m_tacticID       = null;
    /**
     * List of Tactic argument expressions
     */
    private List<Expression>    m_tacticArgExprs = null;
    /**
     * Number of times to repeat DO
     */
    private int                 m_numDoTrials    = 0;
    /**
     * Target label identified by the DO expression
     */
    private String              m_doTarget       = null;

    /**
     * List of labels of branch nodes;
     * if empty and m_actionFlag indicates a Tactic,
     * then equivalent to tactic | done in the strategy spec.
     */
    private List<String> m_children = null;
    private StrategyNode m_parent   = null;
    
    public IScope scope = null;

    public StrategyNode (Stitch/*State*/ stitch, String label) {

        m_stitch = stitch;
        m_label = label;
        m_tacticArgExprs = new ArrayList<Expression> ();
        m_children = new ArrayList<String> ();
    }

    /**
     * Returns a shallow clone of this Strategy Node object.
     *
     * @return StrategyNode the cloned StrategyNode object.
     */

    public StrategyNode clone (IScope parent) {
        StrategyNode newNode = new StrategyNode (m_stitch, m_label);
        newNode.m_parent = m_parent;
        newNode.m_hasProb = m_hasProb;
        newNode.m_probExpr = m_probExpr;
        newNode.m_prob = m_prob;
        newNode.m_condFlag = m_condFlag;
        newNode.m_condExpr = m_condExpr != null ? m_condExpr.clone (parent) : null;
        newNode.m_hasDuration = m_hasDuration;
        newNode.m_durExpr = m_durExpr != null ? m_durExpr.clone (parent) : null;
        newNode.m_actionFlag = m_actionFlag;
        // no need to clone tactic as it is not concurrently evaluated
        newNode.m_tacticID = m_tacticID;
        for (Expression e : m_tacticArgExprs) {
            // no need to clone argument expression as it is not concurrently evaluated
            // ^^^ is no longer true, so we are cloning
            newNode.m_tacticArgExprs.add (e.clone (parent));
        }
        newNode.m_numDoTrials = m_numDoTrials;
        newNode.m_doTarget = m_doTarget;
        for (String label : m_children) {
            newNode.m_children.add (label);
        }
        return newNode;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        String str = label () + ": ";
        str += (hasProbability () ? "#" + getProbabilityExpr ().tree ().toStringTree () + " " : "");
        str += getCondExpr () == null ? "" : (getCondExpr ().tree () != null ? getCondExpr ().tree ().toStringTree ()
                : "") + " ";
        if (getActionFlag () == Strategy.ActionKind.TACTIC) {
            str += "-> " + getTactic () + "(";
            for (Expression e : getTacticArgExprs ()) {
                str += e.tree ().toStringTree () + " ";
            }
            str += ") ";
            str += (hasDuration () ? "@" + getDurationExpr ().tree ().toStringTree () + " " : "");
        } else {
            str += (getActionFlag () == Strategy.ActionKind.DOLOOP ? "-> do[" + getNumDoTrials ()
                    + "] " + getDoTarget () : "");
            str += (getActionFlag () == Strategy.ActionKind.DONE ? "DONE " : "");
            str += (getActionFlag () == Strategy.ActionKind.NULL ? "NULL Tactic " : "");
        }
        str += "subnodes[";
        for (String label : m_children) {
            str += " " + label;
        }
        str += " ]";
        return str;
    }

    /**
     * Checks the state of node condition without considering time.
     *
     * @return boolean  <code>true</code> if condition expression evaluates to true,
     * <code>false</code> otherwise.
     */
    public boolean checkCondition (Map<String, Object> moreVars) {
        boolean rv = false;
        if (m_condExpr != null) {
            m_condExpr.clearState ();
            // put in the temp vars
            for (Map.Entry<String, Object> pair : moreVars.entrySet ()) {
                Var v = new Var ();
                v.scope = m_condExpr.stitchState ().scope ();
                v.setType (pair.getValue ().getClass ().getSimpleName ());
                v.name = pair.getKey ();
                v.setValue (pair.getValue ());
                m_condExpr.stitchState ()./*stitch().*/script.addVar (v.name, v);
            }
            m_condExpr.evaluate (null);
            if (m_condExpr.getResult () != null && m_condExpr.getResult () instanceof Boolean) {
                rv = (Boolean) m_condExpr.getResult ();
            }
            // remove temp vars
            for (Map.Entry<String, Object> pair : moreVars.entrySet ()) {
                m_condExpr.stitchState ()./*stitch().*/script.vars ().remove (pair.getKey ());
            }
        }  // opposite shouldn't be the case, but consider it false if so

        return rv;
    }

    public boolean checkParentTacticFailure () {
        boolean rv = false;
        StrategyNode parent = getParent ();
        if (parent != null) {
            Tactic tactic = m_stitch./*stitch ().*/findTactic (parent.getTactic ());
            if (tactic != null) {
                rv = tactic.hasError ();
            }
        }
        return rv;
    }

    public Stitch/*State*/ stitch () {
        return m_stitch;
    }

    /**
     * @return the m_label
     */
    public String label () {
        return m_label;
    }

    /**
     * @return the m_hasProb
     */
    public boolean hasProbability () {
        return m_hasProb;
    }

    /**
     * @param prob the m_hasProb to set
     */
    public void setHasProbability (boolean prob) {
        m_hasProb = prob;
    }


    /**
     * @param expr the m_probKey to set
     */
    public void setProbabilityExpr (Expression expr) {
        m_probExpr = expr;
    }

    /**
     * @return the m_prob
     */
    public Expression getProbabilityExpr () {
        return m_probExpr;
    }

    /**
     * Sets the probability for nodes that do not have an expression
     *
     * @param prob
     */
    public void setProbability (double prob) {
        m_prob = prob;
    }

    public double getProbability () {
        if (hasProbability ()) {
            m_probExpr.evaluate (null);
            if (m_probExpr.getResult () != null) return ((MyDouble) m_probExpr.getResult ()).doubleValue ();
        }
        return m_prob;
    }

    /**
     * @return the m_condFlag
     */
    public Strategy.ConditionKind getCondFlag () {
        return m_condFlag;
    }

    /**
     * @param flag the m_condFlag to set
     */
    public void setCondFlag (Strategy.ConditionKind flag) {
        m_condFlag = flag;
    }

    /**
     * @return the m_condExpr
     */
    public Expression getCondExpr () {
        return m_condExpr;
    }

    /**
     * @param expr the m_condExpr to set
     */
    public void setCondExpr (Expression expr) {
        m_condExpr = expr;
    }


    /**
     * @return the m_hasDuration
     */
    public boolean hasDuration () {
        return m_hasDuration;
    }

    /**
     * @param duration the m_hasDuration to set
     */
    public void setHasDuration (boolean duration) {
        m_hasDuration = duration;
    }

    /**
     * Evaluates the duration expression and returns it.
     *
     * @return the duration value in milliseconds
     */
    public long getDuration () {
        if (hasDuration ()) {  // evaluate the expression
            m_durExpr.evaluate (null);
            if (m_durExpr.getResult () != null) return ((MyInteger) m_durExpr.getResult ()).longValue ();
        } // otherwise, there's no duration
        // return 0 (no) duration... or should we throw exception?
        return 0L;
    }

    /**
     * @return the m_duration
     */
    public Expression getDurationExpr () {
        return m_durExpr;
    }

    /**
     * @param durExpr the m_duration to set
     */
    public void setDurationExpr (Expression durExpr) {
        m_durExpr = durExpr;
    }


    /**
     * @return the m_actionFlag
     */
    public Strategy.ActionKind getActionFlag () {
        return m_actionFlag;
    }

    /**
     * Sets flag of new referenced action.
     *
     * @param flag the m_actionFlag to set
     */
    public void setActionFlag (Strategy.ActionKind flag) {
        m_actionFlag = flag;
    }

    /**
     * @return the m_tactic
     */
    public String getTactic () {
        return m_tacticID;
    }

    /**
     * @param tactic the new tactic reference identifier to set to
     */
    public void setTactic (Tactic tactic) {
        m_tacticID = tactic.getName ();
    }

    /**
     * Adds an expression that will evaluate to an argument into the Tactic
     *
     * @param expr
     */
    public void addTacticArgExpr (Expression expr) {
        m_tacticArgExprs.add (expr);
    }

    /**
     * @return the list of expressions
     */
    public List<Expression> getTacticArgExprs () {
        return m_tacticArgExprs;
    }

    /**
     * @return the m_numDoTrials
     */
    public int getNumDoTrials () {
        return m_numDoTrials;
    }

    /**
     * @param doTrials the m_numDoTrials to set
     */
    public void setNumDoTrials (int doTrials) {
        m_numDoTrials = doTrials;
    }

    /**
     * @return the m_DoTarget
     */
    public String getDoTarget () {
        return m_doTarget;
    }

    /**
     * @param doTarget the m_DoTarget to set
     */
    public void setDoTarget (String doTarget) {
        m_doTarget = doTarget;
    }


    /**
     * @return the list of children StrategyNode labels
     */
    public List<String> getChildren () {
        return m_children;
    }

    /**
     * @param node the StrategyNode to add to child list
     */
    public void addBranch (StrategyNode node) {
        m_children.add (node.label ());
    }

    /**
     * Returns whether this node is the root node.
     *
     * @return boolean  <code>true</code> if node is the root node, i.e., parent node is null;
     * <code>false</code> otherwise.
     */
    public boolean isRoot () {
        return m_parent == null;
    }

    /**
     * @return the Parent StrategyNode
     */
    public StrategyNode getParent () {
        return m_parent;
    }

    /**
     * @param parent the parent StrategyNode to set
     */
    public void setParent (StrategyNode parent) {
        m_parent = parent;
    }

}
