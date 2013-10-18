/**
 * Created July 5, 2006.
 */
package org.sa.rainbow.stitch.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sa.rainbow.stitch.core.Strategy.ActionKind;
import org.sa.rainbow.stitch.core.Strategy.ConditionKind;
import org.sa.rainbow.stitch.visitor.Stitch;

/**
 * A class whose object represents a node in the Strategy Tree.
 * 
 * History:<ol>
 *   <li>[2008.02.07] Removed exit condition and expiration for tactic action;
 *       replaced with original condition "duration";
 *       all associated member methods removed.
 * </ol>
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class StrategyNode {
	/** Default loop max count if none provided */
	public static final int DEFAULT_LOOP_MAX = 3;

	/** Enclosing Stitch object */
	private Stitch m_stitch = null;
	/** Label of node */
	private String m_label = null;

	/** Boolean indicating that a probability value is specified */
	private boolean m_hasProb = false;
	/** Key string for retrieving the probability value, means value is cached;
	 *  if null, then probability should be a literal. */
	private String m_probKey = null;
	/** Probability value of branch, if applicable, default to full likelihood */
	private double m_prob = 1.0;

	/** Flag to hold type of condition */
	private ConditionKind m_condFlag = ConditionKind.UNKNOWN;
	/** The condition expression */
	private Expression m_condExpr = null;

	/** Boolean indicating that a durationg value is specified */
	private boolean m_hasDuration = false;
	/** A simple Expression for duration (though full expression is allowed,
	 *  not much is available on which to express, so this is effectively
	 *  reduced (hopefully) to arithmetic expressions. */
	private Expression m_durExpr = null;

	/** Flag to hold type of referenced action */
	private ActionKind m_actionFlag = ActionKind.UNKNOWN;
	/** Referenced tactic */
	private String m_tacticID = null;
	/** List of Tactic argument expressions */
	private List<Expression> m_tacticArgExprs = null;
	/** Number of times to repeat DO */
	private int m_numDoTrials = 0;
	/** Target label identified by the DO expression */
	private String m_doTarget = null;

	/** List of labels of branch nodes;
	 *  if empty and m_actionFlag indicates a Tactic,
	 *  then equivalent to tactic | done in the strategy spec. */
	private List<String> m_children = null;
	private StrategyNode m_parent = null;

	public StrategyNode (Stitch stitch, String label) {
		m_stitch = stitch;
		m_label = label;
		m_tacticArgExprs = new ArrayList<Expression>();
		m_children = new ArrayList<String>();
	}

	/**
	 * Returns a shallow clone of this Strategy Node object.
	 * @return StrategyNode the cloned StrategyNode object.
	 */
	public StrategyNode clone () {
		StrategyNode newNode = new StrategyNode(m_stitch, m_label);
		newNode.m_parent = m_parent;
		newNode.m_hasProb = m_hasProb;
		newNode.m_probKey = m_probKey;
		newNode.m_prob = m_prob;
		newNode.m_condFlag = m_condFlag;
		newNode.m_condExpr = m_condExpr;
		newNode.m_hasDuration = m_hasDuration;
		newNode.m_durExpr = m_durExpr;
		newNode.m_actionFlag = m_actionFlag;
		// no need to clone tactic as it is not concurrently evaluated
		newNode.m_tacticID = m_tacticID;
		for (Expression e : m_tacticArgExprs) {
			// no need to clone argument expression as it is not concurrently evaluated
			newNode.m_tacticArgExprs.add(e);
		}
		newNode.m_numDoTrials = m_numDoTrials;
		newNode.m_doTarget = m_doTarget;
		for (String label : m_children) {
			newNode.m_children.add(label);
		}
		return newNode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = label() + ": ";
		str += (hasProbability() ? "#" + getProbKey() + " " : "");
		str += (getCondExpr().ast() != null ? getCondExpr().ast().toStringList() : "") + " ";
		if (getActionFlag() == ActionKind.TACTIC) {
			str += "-> " + getTactic() + "(";
			for (Expression e : getTacticArgExprs()) {
				str += e.ast().toStringList() + " ";
			}
			str += ") ";
			str += (hasDuration() ? "@" + getDurationExpr().ast().toStringList() + " " : "");
		} else {
			str += (getActionFlag() == ActionKind.DOLOOP? "-> do[" + getNumDoTrials()
					+ "] " + getDoTarget() : "");
			str += (getActionFlag() == ActionKind.DONE? "DONE " : "");
			str += (getActionFlag() == ActionKind.NULL? "NULL Tactic " : "");
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
	public boolean checkCondition (Map<String,Object> moreVars) {
		boolean rv = false;
		if (m_condExpr != null) {
			m_condExpr.clearState();
			// put in the temp vars
			for (Map.Entry<String,Object> pair : moreVars.entrySet()) {
				Var v = new Var();
				v.scope = m_condExpr.stitch().scope;
				v.setType(pair.getValue().getClass().getSimpleName());
				v.name = pair.getKey();
				v.setValue(pair.getValue());
				m_condExpr.stitch().script.addVar(v.name, v);
			}
			m_condExpr.evaluate(null);
			if (m_condExpr.getResult() != null && m_condExpr.getResult() instanceof Boolean) {
				rv = (Boolean )m_condExpr.getResult();
			}
			// remove temp vars
			for (Map.Entry<String,Object> pair : moreVars.entrySet()) {
				m_condExpr.stitch().script.vars().remove(pair.getKey());
			}
		}  // opposite shouldn't be the case, but consider it false if so

		return rv;
	}

	public boolean checkParentTacticFailure () {
		boolean rv = false;
		StrategyNode parent = getParent();
		if (parent != null) {
			Tactic tactic = m_stitch.findTactic(parent.getTactic());
			if (tactic != null) {
				rv = tactic.hasError();
			}
		}
		return rv;
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
	 * @return the m_probKey
	 */
	public String getProbKey () {
		return m_probKey;
	}

	/**
	 * @param key the m_probKey to set
	 */
	public void setProbKey (String key) {
		m_probKey = key;
	}

	/**
	 * @return the m_prob
	 */
	public double getProbability () {
		return m_prob;
	}

	/**
	 * @param m_prob the m_prob to set
	 */
	public void setProbability (double prob) {
		m_prob = prob;
	}


	/**
	 * @return the m_condFlag
	 */
	public ConditionKind getCondFlag () {
		return m_condFlag;
	}

	/**
	 * @param flag the m_condFlag to set
	 */
	public void setCondFlag (ConditionKind flag) {
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
	 * @return the duration value in milliseconds
	 */
	public long getDuration () {
		if (hasDuration()) {  // evaluate the expression
			m_durExpr.evaluate(null);
			if (m_durExpr.getResult() != null) {
				return ((MyInteger )m_durExpr.getResult()).longValue();
			}
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
	 * @param dur the m_duration to set
	 */
	public void setDurationExpr (Expression durExpr) {
		m_durExpr = durExpr;
	}

	
	/**
	 * @return the m_actionFlag
	 */
	public ActionKind getActionFlag () {
		return m_actionFlag;
	}

	/**
	 * Sets flag of new referenced action. 
	 * @param flag the m_actionFlag to set
	 */
	public void setActionFlag (ActionKind flag) {
		m_actionFlag = flag;
	}

	/**
	 * @return the m_tactic
	 */
	public String getTactic () {
		return m_tacticID;
	}

	/**
	 * @param m_tacticID the new tactic reference identifier to set to
	 */
	public void setTactic (Tactic tactic) {
		m_tacticID = tactic.getName();
	}

	/**
	 * Adds an expression that will evaluate to an argument into the Tactic
	 * @param expr
	 */
	public void addTacticArgExpr (Expression expr) {
		m_tacticArgExprs.add(expr);
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
	 * @param node  the StrategyNode to add to child list
	 */
	public void addBranch (StrategyNode node) {
		m_children.add(node.label());
	}

	/**
	 * Returns whether this node is the root node.
	 * @return boolean  <code>true</code> if node is the root node, i.e., parent node is null;
	 *                  <code>false</code> otherwise.
	 */
	public boolean isRoot () {
		return m_parent == null;
	}

	/**
	 * @return the Parent StrategyNode
	 */
	public StrategyNode getParent() {
		return m_parent;
	}

	/**
	 * @param m_parent the parent StrategyNode to set
	 */
	public void setParent(StrategyNode parent) {
		m_parent = parent;
	}

}
