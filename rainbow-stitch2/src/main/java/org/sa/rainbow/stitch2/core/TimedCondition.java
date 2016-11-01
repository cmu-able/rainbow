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
 * Created August 29, 2006.
 */
package org.sa.rainbow.stitch2.core;


import org.sa.rainbow.stitch2.Ohana2;
import org.sa.rainbow.stitch2.util.Tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * An Observable object encapsulating a condition expression for a StrategyNode,
 * along with the duration of truthfulness.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class TimedCondition extends Observable {

    private List<Expression> m_exprList        = null;
    private long             m_period          = 0L;  // ms
    private long             m_duration        = 0L;  // ms
    private long             m_timerStart      = 0L;  // timestamp of the start of timer
    private long             m_timerTrueDuring = 0L;  // timestamp of the start of truth within duration
    /**
     * Result indicates whether condition turned true any time during the
     * designated period and remained true for the duration
     */
    private boolean          m_result          = false;

    /**
     * Constructor expects the condition expression and the time period.
     * Duration of truth is not considered.
     *
     * @param exprList List of condition expressions to be evaluated for truth result
     * @param per      the time period within which to observe for truth value
     */
    public TimedCondition (List<Expression> exprList, long per) {
        this (exprList, per, 0L);
    }

    /**
     * Constructor expects the condition expression, the time period, and a
     * duration for which truth must be maintained.
     *
     * @param exprList List of condition expressions to be evaluated for truth result
     * @param per      the time period within which to observe for truth value
     * @param dur      the duration for which truth must be maintained for result
     *                 to be considered true
     */
    public TimedCondition (List<Expression> exprList, long per, long dur) {
        if (exprList == null) {  // init with empty list
            m_exprList = new ArrayList<Expression> ();
        } else {
            m_exprList = exprList;
        }
        m_period = per;
        m_duration = dur;
    }

    /* (non-Javadoc)
     * @see java.util.Observable#notifyObservers(java.lang.Object)
     */
    @Override
    public void notifyObservers (Object arg) {
        if (arg == null) {  // supply the expression result as Boolean object
            super.notifyObservers (Boolean.valueOf (m_result));
        }
    }

    public List<Expression> expressions () {
        return m_exprList;
    }

    public long period () {
        return m_period;
    }

    public long duration () {
        return m_duration;
    }

    /**
     * Returns the condition outcome.  The condition outcome should NOT be
     * considered to be valid until isTimeUp() returns true;
     *
     * @return <code>true</code> if condition evaluated to true at any point
     * during the time period AND, if duration was set, remained true for the
     * duration of time; <code>false</code> otherwise.
     */
    public boolean result () {
        return m_result;
    }

    public void resetTimer () {
        m_timerStart = System.currentTimeMillis ();
        if (Tool.logger ().isInfoEnabled ()) {
            StringBuffer exprBuf = new StringBuffer ("[ ");
            for (Expression expr : m_exprList) {
                if (expr.tree () != null) {
                    exprBuf.append (expr.tree ().toStringTree ());
                } else {
                    exprBuf.append (m_exprList.toString ());
                }
                exprBuf.append (' ');
            }
            exprBuf.append (']');
            Tool.logger ().info ("Timer started: " + m_timerStart + " " + exprBuf.toString ());
        }
    }

    public boolean isTimeUp () {
        return m_timerStart > 0L
                && (System.currentTimeMillis () - m_timerStart > m_period);
    }

    /**
     * Computes condition and updates the result according to the following
     * rules:
     * <ol>
     * <li> if period doesn't matter (0), update result
     * <li> if period timer was set but period expired, no change occurs to
     * truth flag and timer is unset
     * <li> if period timer was set and not expired, then focus on two case of
     * interest:  <ol>
     * <li> if stored result was false but current condition turned true,
     * then we check if duration matters; result is updated to <code>true</code>
     * if either (a) duration doesn't matter or (b) duration matters and we've
     * satisfied truth in that duration.  In the subcase (b), duration timer
     * gets started if truth hasn't been satisfied for the duration
     * <li> if stored result was true but current condition turned false,
     * then we basically consider outcome to be false and upate result to
     * <code>false</code> (and clear duration timer if necessary)
     * </ol>
     * <li> There is, however, one duration semantics worth considering:  If
     * current condition changes to false, but truth has been maintained for
     * the required duration, should result be considered to be true regardless?
     * </ol>
     */
    public void updateResult () {
        if (Ohana2.isDisposed ()) return;

        if (m_exprList != null && m_timerStart > 0L) {
            long curTime = System.currentTimeMillis ();
            if (Tool.logger ().isDebugEnabled ()) {
                Tool.logger ().debug ("Expressions are being evaluated...");
            }
            // initial true boolean will be conjoined with at least one expression result;
            // if exprList empty, then automatic true, which is consistent with tactic condition/effect interpretation
            boolean curResult = true;
            for (Expression expr : m_exprList) {  // evaluate all expressions, conjoined
                expr.clearState ();  // make sure we're really re-evaluating, not using cached value
                expr.evaluate (null);
                if (expr.getResult () != null && expr.getResult () instanceof Boolean) {
                    if (Tool.logger ().isDebugEnabled ()) {
                        Tool.logger ().debug ("expr: " + expr.getResult () + "; " + expr.toString ());
                    }
                    curResult &= (Boolean) expr.getResult ();
                }
            } // curResult has outcome of evaluating conjunction of expressions
            if (Tool.logger ().isDebugEnabled ()) {
                Tool.logger ().debug ("Result updated");
            }
            if (m_period == 0L) {
                m_result = curResult;
                if (Tool.logger ().isDebugEnabled ()) {
                    Tool.logger ().debug ("No period, condition changed: " + m_result);
                }
                setChanged ();
            } else if (curTime - m_timerStart <= m_period) {
                if (!m_result && curResult) {
                    // condition just turned true from false within time period
                    // what about duration?
                    if (m_duration == 0L) {
                        // no need to consider duration, so result is true
                        m_result = true;
                        if (Tool.logger ().isDebugEnabled ()) {
                            Tool.logger ().debug ("Within time period but no duration, condition changed: " + m_result);
                        }
                        setChanged ();
                    } else if (m_timerTrueDuring == 0L) {
                        // duration timer hasn't started, start it
                        m_timerTrueDuring = curTime;
                    } else if (curTime - m_duration >= m_duration) {
                        // duration was specified, timer started, AND
                        // we've met duration
                        m_result = true;
                        if (Tool.logger ().isDebugEnabled ()) {
                            Tool.logger ().debug ("Within time period AND met duration, condition changed: " +
                                                          m_result);
                        }
                        setChanged ();
                    }
                } else if (m_result && !curResult) {
                    // condition just turned false from true within time period
                    if (m_duration == 0L) {
                        // no duration to consider, result turns false
                        m_result = false;
                        if (Tool.logger ().isDebugEnabled ()) {
                            Tool.logger ().debug ("Condition turned false, no duration");
                        }
                        setChanged ();
                    } else if (m_timerTrueDuring > 0L) {
                        // duration timer started, check it
                        if (curTime - m_timerTrueDuring < m_duration) {
                            // condition remained true insufficiently long
                            m_result = false;
                            m_timerTrueDuring = 0L;  // unset duration timer
                            if (Tool.logger ().isDebugEnabled ()) {
                                Tool.logger ().debug ("Condition turned false, not true sufficiently long!");
                            }
                            setChanged ();
                        }
                        // TODO: if condition was true for duration, but turned false before period is up, should we
                        // consider result to be true then???
                    }
                }
            } else {  // period timer no longer relevant, unset it
                m_timerStart = 0L;
                if (Tool.logger ().isDebugEnabled ()) {
                    Tool.logger ().debug ("Period exceeded, unset timer, end result: " + m_result);
                }
                setChanged ();
            }
        }
    }

}
