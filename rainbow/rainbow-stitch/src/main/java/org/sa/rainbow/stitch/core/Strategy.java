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
 * Created March 15, 2006, separated from class Stitch April 4, 2006.
 */
package org.sa.rainbow.stitch.core;

import org.acmestudio.acme.element.IAcmeElement;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.adaptation.StitchExecutor;
import org.sa.rainbow.stitch.error.ArgumentMismatchException;
import org.sa.rainbow.stitch.history.ExecutionHistoryModelInstance;
import org.sa.rainbow.stitch.util.ExecutionHistoryData;
import org.sa.rainbow.stitch.util.Tool;
import org.sa.rainbow.stitch.visitor.Stitch;

import java.io.IOException;
import java.util.*;

/**
 * Represents a Strategy scoped object parsed from the script.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class Strategy extends ScopedEntity implements IEvaluableScope {

	public Outcome getOutcome() {
		return m_outcome;
	}

	public void setOutcome(Outcome outcome) {
		m_outcome = outcome;
	}

	/**
	 * Declares the states that the Strategy object might be in during parsing.
	 */
	public enum ParseState {
		UNKNOWN, IN_PARAMS, IN_VARS, PARSED
	}

	/**
	 * Enumerates the kinds of conditions that a StrategyNode can have.
	 */
	public enum ConditionKind {
		UNKNOWN, APPLICABILITY /* a strategy applicability condition */, EXPRESSION /* a full expression */, SUCCESS
        /* "success", meaning effect of parent tactic true */, FAILURE /* "failure", meaning parent tactic didn't
        complete execution */, DEFAULT /* "default" match, when no other ones match */
	}

	/**
	 * Enumerates the kinds of actions that a StrategyNode can have.
	 */
	public enum ActionKind {
        UNKNOWN, TACTIC /* a Tactic */, DOLOOP /* a do loop */, DONE /* "done" action, terminating Strategy with
        success */, NULL /* no-op, a null tactic */
	}

	/**
	 * Declares the states of results in which Strategy might be during evaluation
	 */
	public enum Outcome {
		UNKNOWN, SUCCESS, FAILURE, STATUSQUO
	}

	public enum StatementKind {
		ACTION, VAR_DEF, STMT_LIST, EXPRESSION, EMPTY_STMT, ERROR, IF, WHILE, FOR
	}

	public enum ExpressionKind {
		PLUS, MINUS, MULTIPLY, DIVIDE, MOD, IMPLIES, IFF, AND, OR, NE, EQ, LT, GE, LE, GT, INCR, DECR, UNARY_MINUS,
		UNARY_PLUS, NOT, FORALL, EXISTS, EXISTS_UNIQUE, SELECT, IDENTIFIER, INTEGER, FLOAT, STRING, CHAR, BOOLEAN, NULL,
		UNKNOWN
	}

	public interface NodeAction {
		void applyTactic(Tactic tactic);

		void execute(Map<String, Double> aggAtt, StrategyNode curNode, int level);
	}

	public class AttributeCollector implements NodeAction {
		private StrategyNode m_lastNode = null;
		private Set<String> attrKeySet = new HashSet<String>(); // add to when we hit a tactic

		public AttributeCollector(StrategyNode lastNode) {
			m_lastNode = lastNode;
		}

		@Override
		public void applyTactic(Tactic tactic) {
			if (tactic != null) {
				// collect attribute keys
				attrKeySet.addAll(tactic.attributeKeySet());
				if (Tool.logger().isTraceEnabled()) {
					Tool.logger().trace("  ..* cur attribute key set: " + attrKeySet);
				}
			}
		}

		@Override
		public void execute(Map<String, Double> aggAtt, StrategyNode curNode, int level) {
			if (curNode == m_lastNode)
				return; // do nothing if it IS last stopped node

			// grab the tactic, but only if the action for this node is TACTIC
			String tRef = (curNode.getActionFlag() == ActionKind.TACTIC) ? curNode.getTactic() : null;
			Tactic tactic = (tRef != null ? stitchState()./* stitch (). */findTactic(tRef) : null);
			String nullCaseSuffix = (curNode.getActionFlag() == ActionKind.NULL) ? "." + ActionKind.NULL.name() : null;
			double prob = curNode.getProbability();
			if (tactic != null) {
				tactic.setArgs(Tool.evaluateArgs(curNode.getTacticArgExprs()));
			}
			Map<String, Double> newAggAtt = null;
			// BRS: not sure what the levl has to do with it
//			if (level == 1) {  // create new aggregate attibute vector without subs
			newAggAtt = new HashMap<String, Double>();
//			}
			for (String k : attrKeySet) {
				// determine what current attribute label to use
				String curlabel = null;
				if (nullCaseSuffix == null) { // account for tactic and sublevel
					curlabel = k + "." + level;
				} else { // use NULL level only, this is the base aggAttr
					curlabel = k + nullCaseSuffix;
				}
				String upLabel = k + (level > 1 ? "." + (level - 1) : "");
				// compute the attribute value using accumulated value at this level
				// or NULL-case attribute value if that exists
				double v = (aggAtt.containsKey(curlabel) ? aggAtt.get(curlabel) : 0.0);
				if (nullCaseSuffix == null) { // add tactic's attribute
					v += ((tactic == null) ? 0.0 : tactic.computeAttribute(k));
				}
				// add new attribute value to the parent level's value
				double upV = 0.0;
				if (aggAtt.containsKey(upLabel)) {
					upV = aggAtt.get(upLabel);
				}
				upV += v * prob;
				if (level == 1) {
					newAggAtt.put(upLabel, upV);
				} else {
					aggAtt.put(upLabel, upV);
				}
				if (Tool.logger().isTraceEnabled()) {
					Tool.logger().trace(" .. putting new value pair (" + upLabel + ", " + upV + ") from (" + curlabel
							+ ", " + v + "), prob == " + prob);
				}
			}
			if (level == 1) { // clear out all the temporary computation results
				aggAtt.clear();
				aggAtt.putAll(newAggAtt);
			}
		}
	}

	public class TimeEstimator implements NodeAction {
		private StrategyNode m_lastNode = null;
		public long estAvgTime = 0L;

		public TimeEstimator(StrategyNode lastNode) {
			m_lastNode = lastNode;
		}

		@Override
		public void applyTactic(Tactic tactic) {
		}

		@Override
		public void execute(Map<String, Double> aggAtt, StrategyNode curNode, int level) {
			if (curNode == m_lastNode || curNode.getActionFlag() != ActionKind.TACTIC)
				return;

			// accumulate the time delays of the tactic and the node scaled by node
			// probability
			if (estAvgTime == 0L) {
				long tt = 0L;
				if (curNode.getTactic() != null) {
					Tactic tactic = stitchState()./* stitch (). */findTactic(curNode.getTactic());
					tt = tactic.estimateAvgTimeCost();
				}
				if (curNode.hasDuration()) {
					tt += curNode.getDuration();
				}
				estAvgTime += curNode.getProbability() * tt;
			}
		}
	}

	private static final String ROOT_NODE_LABEL = "*0*";

	public ParseState state = ParseState.UNKNOWN;
	public Map<String, StrategyNode> nodes = new TreeMap<String, StrategyNode>();
	/**
	 * This value indicates how many times this Strategy is expected to have to
	 * execute consecutively if it is chosen to run; this is affected by the
	 * presence of a strategy named like this one with "Leap-" prepended. The value
	 * affects only the computation of aggregate attribute vector.
	 */
	public int multiples = 1;

	/**
	 * Tracks the current strategy node being evaluated
	 */
	private StrategyNode m_lastNode = null;
	/**
	 * Tracks stack of executed nodes
	 */
	Stack<String> m_nodeStack = new Stack<String>();
	/**
	 * Tracks current do-loop counter
	 */
	private Map<StrategyNode, Integer> m_doCntMap = new HashMap<StrategyNode, Integer>();
	private Outcome m_outcome = Outcome.UNKNOWN;
	private long m_avgExecutionTime = 0L;

	private Boolean m_settlingCondition = null;
	private Observer m_conditionObserver = new Observer() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
		 */
		@Override
		public void update(Observable o, Object arg) {
			m_settlingCondition = (Boolean) arg;
			if (Tool.logger().isDebugEnabled()) {
				Tool.logger().debug("Settling condition observer updated!");
			}
		}
	};

	private StitchExecutor m_executor;

	/**
	 * Main Constructor for a new Strategy object.
	 *
	 * @param parent the parent scope
	 * @param name   the name of this scope
	 * @param stitch the Stitch evaluation context object
	 */
	public Strategy(IScope parent, String name, Stitch/* State */ stitch) {
		super(parent, name, stitch);
	}

	/**
	 * Clones a Strategy object, including the set of children StrategyNodes.
	 */
	@Override
	public synchronized Strategy clone(IScope parent) {
		Strategy newObj = new Strategy(parent, m_name, m_stitch);
		copyState(newObj);
		return newObj;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.stitchState.core.ScopedEntity#copyState(org.sa.rainbow.
	 * stitchState.core.Strategy)
	 */
	protected void copyState(Strategy target) {
		super.copyState(target);
		target.state = state;
		for (Map.Entry<String, StrategyNode> e : nodes.entrySet()) {
			target.nodes.put(e.getKey(), e.getValue().clone(parent()));
		}
		target.multiples = multiples;
		target.setOutcome(getOutcome());
		target.m_avgExecutionTime = m_avgExecutionTime;
		target.m_settlingCondition = m_settlingCondition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.stitchState.core.ScopedEntity#toString()
	 */
	@Override
	public String toString() {
		String str = "strategy: name \"" + m_name + "\" {" + "\n\t  vars [";
		for (Var v : m_vars.values()) {
			str += "\n\t\t" + v.toString();
		}
		str += "\n\t  ]";
		str += "\n\t  nodes [";
		for (StrategyNode node : nodes.values()) {
			str += "\n\t\t" + node.toString();
		}
		str += "\n\t  ]\n\t}";

		return str;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.stitchState.core.ScopedEntity#addVar(java.lang.String,
	 * org.sa.rainbow.stitchState.core.Var)
	 */
	@Override
	public boolean addVar(String id, Var var) {
		boolean rv = true;
		switch (state) {
		case IN_VARS:
			rv = super.addVar(id, var);
			break;
		case IN_PARAMS:
			rv = super.addVar(id, var);
			// args removed for strategy...
			break;
		}
		return rv;
	}

	/**
	 * Add a Strategy Node to this strategy.
	 *
	 * @param node the StrategyNode
	 */
	public void addNode(StrategyNode node) {
		nodes.put(node.label(), node);
	}

	/**
	 * Creates the root node of this strategy, which will contain the condition of
	 * applicability for the strategy, and also signal final collection point for
	 * aggregate attribute vector computation.
	 *
	 * @return StrategyNode the created root strategy node
	 */
	public StrategyNode createRootNode() {
		StrategyNode root = new StrategyNode(m_stitch, ROOT_NODE_LABEL);
		addNode(root);
		return root;
	}

	/**
	 * @return StrategyNode the root strategy node
	 */
	public StrategyNode getRootNode() {
		return nodes.get(ROOT_NODE_LABEL);
	}

	public double getFirstTacticArgumentValue() {
		double rv = Double.NaN;
		StrategyNode foundNode = null;
		StrategyNode curNode = getRootNode();
		if (curNode.getActionFlag() == ActionKind.TACTIC) { // root!
			foundNode = curNode;
		} else { // search only one level down
			for (StrategyNode child : gatherChildrenNodes(curNode)) {
				if (child.getActionFlag() == ActionKind.TACTIC) { // found it!
					foundNode = child;
					break;
				}
			}
		}
		if (foundNode != null) {
			// found it! get its first argument
			if (foundNode.getTacticArgExprs().size() > 0) {
				Expression e = foundNode.getTacticArgExprs().get(0);
				e.evaluate(null);
				if (e.getResult() instanceof MyNumber) {
					rv = ((MyNumber) e.getResult()).toJavaNumber().doubleValue();
				} else if (e.getResult() instanceof Double) {
					rv = ((Double) e.getResult()).doubleValue();
				}
			}
		}
		return rv;
	}

	/**
	 * Returns whether this Strategy is applicable given current model state. In
	 * other words, the applicability conditions of the Strategy (currently stored
	 * as the root node of the strategy) is evaluated, and result returned.
	 *
	 * @return <code>true</code> if the condition of the root StrategyNode evaluates
	 *         true, indicating that this Strategy applies; <code>false</code>
	 *         otherwise.
	 */
	public boolean isApplicable(Map<String, Object> moreVars) {
		boolean applicable = false;
		if (getRootNode().checkCondition(moreVars)) {
			applicable = true;
		}

		return applicable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.stitchState.core.IEvaluable#estimateMaxTimeCost()
	 */
	@Override
	public long estimateAvgTimeCost() {
		if (m_avgExecutionTime > 0)
			return m_avgExecutionTime;

		// accumulate the estimated execution time
		TimeEstimator estimator = new TimeEstimator(m_lastNode);
		walkTreeNodes(estimator, null);
		m_avgExecutionTime = estimator.estAvgTime; // store est. time
		if (Tool.logger().isInfoEnabled()) {
			Tool.logger().info("Estimated time for Strategy " + getName() + ": " + m_avgExecutionTime);
		}
		return m_avgExecutionTime;
	}

	/**
	 * Returns the expected execution time from the current strategy node.
	 *
	 * @return
	 */
	public long expectedExecutionTimeRemaining() {
		long rv = 0L;
		if (m_lastNode == null) { // estimate from root
			rv = estimateAvgTimeCost();
		} else {
			// traverse children nodes from last node, which TimeEstimator will do
			TimeEstimator estimator = new TimeEstimator(m_lastNode);
			walkTreeNodes(estimator, null);
			rv = estimator.estAvgTime; // get estimated time
		}
		return rv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.stitchState.core.IEvaluable#modelElementsUsed()
	 */
	@Override
	public Set<? extends IAcmeElement> modelElementsUsed() {
		Set<IAcmeElement> resultSet = new HashSet<IAcmeElement>();
		// accumulate "used" elements only from the tactics in this strategy
		for (StrategyNode node : nodes.values()) {
			if (node.getActionFlag() == Strategy.ActionKind.TACTIC) {
				resultSet.addAll(stitchState()./* stitch (). */findTactic(node.getTactic()).modelElementsUsed());
			}
		}
		return resultSet;
	}

	/**
	 * Calculates the aggregate attribute vector of this Strategy, using the
	 * probability of the branches and the attribute vector of the tactics on the
	 * branches.
	 *
	 * @return a SortedMap of aggregate attribute key-value pairs.
	 */
	public SortedMap<String, Double> computeAggregateAttributes() {
		AttributeCollector collector = new AttributeCollector(m_lastNode);
		SortedMap<String, Double> aggAtt = walkTreeNodes(collector, null);
		// compute aggregate attributes N-1 more times
		for (int factor = multiples; factor > 1; --factor) {
			aggAtt = walkTreeNodes(collector, aggAtt);
		}
		return aggAtt;
	}

	private SortedMap<String, Double> walkTreeNodes(NodeAction action, SortedMap<String, Double> nullCaseAtt) {

		// stores aggregate attribute computation result
		SortedMap<String, Double> aggAtt = new TreeMap<String, Double>();
		if (nullCaseAtt != null) { // convert all keys to <key>.TNULL
			String suffix = "." + ActionKind.NULL.name();
			for (String k : new ArrayList<String>(nullCaseAtt.keySet())) {
				double v = nullCaseAtt.remove(k);
				nullCaseAtt.put(k + suffix, v);
			}
		}
		// tracks where we've been, node and branch iterator
		Stack<StrategyNode> nodeStack = new Stack<StrategyNode>();
		Stack<Iterator<StrategyNode>> iterStack = new Stack<Iterator<StrategyNode>>();
		// tracks do loop counts, using latest count map
		Map<StrategyNode, Integer> doCntMap = new HashMap<StrategyNode, Integer>(m_doCntMap);
		// current pointers
		nodeStack.push(getRootNode()); // push root onto stack, computed last
		boolean backtrack = false;
		while (nodeStack.size() > 0) {
			StrategyNode curNode = nodeStack.peek();
			if (curNode.getActionFlag() == Strategy.ActionKind.TACTIC || curNode.isRoot()) {
				if (backtrack) {
					if (Tool.logger().isTraceEnabled()) {
						Tool.logger().trace(" . back tracking Tactic " + curNode.label());
					}
					if (iterStack.peek().hasNext()) {
						nodeStack.push(iterStack.peek().next());
						backtrack = false;
					} else {
						// this iter exhausted, pop it and backtrack more
						action.execute(aggAtt, curNode, nodeStack.size());
						iterStack.pop();
					}
				} else {
					// Tactic: attempt depth-first descent down the branches
					Tactic tactic = stitchState()./* stitch (). */findTactic(curNode.getTactic());
					if (Tool.logger().isTraceEnabled()) {
						Tool.logger().trace("Scanning Tactic " + curNode.label() + " -> "
								+ ((tactic == null) ? null : tactic.getName()));
					}
					action.applyTactic(tactic);
					if (curNode.getChildren().size() > 0) {
						// has branch, first check probabilities
						double allProb = 1.0;
						// - go thru the children twice, once to deduct all
						// existing probs, second time to distribute remaining
						// probs.
						// - the assumption here is that any branch with
						// undefined probs have equal likelihood of being visited
						Set<StrategyNode> unsetChildren = new HashSet<StrategyNode>();
						for (StrategyNode child : gatherChildrenNodes(curNode)) {
							if (child.hasProbability() && child.getProbability() > 0.0
									&& child.getProbability() < 1.0) {
								allProb -= child.getProbability();
								if (Tool.logger().isTraceEnabled()) {
									Tool.logger()
											.trace("  - prob of " + child.label() + " == " + child.getProbability());
								}
							} else {
								unsetChildren.add(child);
							}
						}
						double eachProb = allProb / unsetChildren.size();
						for (StrategyNode child : unsetChildren) {
							child.setProbability(eachProb);
							if (Tool.logger().isTraceEnabled()) {
								Tool.logger().trace(
										"  - setting prob of " + child.label() + " == " + child.getProbability());
							}
						}
						// then iterate in sequence, starting with first child
						Iterator<StrategyNode> curIter = gatherChildrenNodes(curNode).iterator();
						iterStack.push(curIter); // push new iterator on stack
						nodeStack.push(curIter.next()); // push new node on stack
						if (Tool.logger().isTraceEnabled()) {
							Tool.logger().trace(" - iterate first child " + nodeStack.peek().label());
						}
					} else { // backtrack time
						action.execute(aggAtt, curNode, nodeStack.size());
						backtrack = true;
					}
				}
			} else if (curNode.getActionFlag() == Strategy.ActionKind.DOLOOP) {
				if (backtrack) {
					// just pop up one more to let any other branches finish
					if (Tool.logger().isTraceEnabled()) {
						Tool.logger().trace(" . back tracking DO Loop " + curNode.label());
					}
					// first collect attributes up one level
					action.execute(aggAtt, curNode, nodeStack.size());
					// then pop stack
					nodeStack.pop();
					backtrack = false;
				} else {
					// LOOP! treat the target node as child node and proceed
					if (Tool.logger().isTraceEnabled()) {
						Tool.logger().trace("Scanning DO loop " + curNode.label());
					}
					int numDone = 1;
					if (doCntMap.containsKey(curNode)) {
						numDone = doCntMap.get(curNode) + 1;
					}
					doCntMap.put(curNode, numDone);
					if (Tool.logger().isTraceEnabled()) {
						Tool.logger().trace(" - numDone for " + curNode.getDoTarget() + " at " + numDone);
					}
					if (numDone <= curNode.getNumDoTrials()) {
						// treat do target as if child node and proceed
						nodeStack.push(nodes.get(curNode.getDoTarget()));
					} else {
						// continue no more, backtrack
						backtrack = true;
					}
				}
			} else {
				// we've hit a null-effect "leaf", is it a NULL node?
				if (curNode.getActionFlag() == Strategy.ActionKind.NULL) {
					// Yes! a NULL node, if there's a null-case aggAtt, collect using that!
					if (Tool.logger().isTraceEnabled()) {
						Tool.logger().trace(" . back tracking NULL branch " + curNode.label());
					}
					if (nullCaseAtt != null) {
						aggAtt.putAll(nullCaseAtt);
						action.execute(aggAtt, curNode, nodeStack.size());
					}
				}
				backtrack = true;
			}
			if (backtrack) {
				nodeStack.pop(); // no need to check stack size
			}
		}
		return aggAtt;
	}

	public void clearVars() {
		for (Var v : m_vars.values()) {
			v.clearValue();
		}
	}

	/**
	 * Evaluates the Strategy, which consists of traversing the StrategyNodes and
	 * executing Tactics as conditions apply. Algorithm:
	 * <ol>
	 * <li>Start with the phantom root Node, whose condition is the strategy
	 * applicability condition.
	 * <li>At each tree level, check all peer-level conditions for a match;
	 * <ul>
	 * <li>If multiple matches (except for DEFAULT), randomly choose one
	 * <li>If none matches whatsoever, then strategy terminates with failure
	 * </ul>
	 * <li>For each node, match the condition:
	 * <ul>
	 * <li>SUCCESS condition is equivalent to Tactic's effect being achieved
	 * <li>FAILURE condition means Tactic did not execute to completion
	 * <li>DEFAULT condition is equivalent to no other peer conditions matching
	 * </ul>
	 * <li>Upon a match of condition, execute the action:
	 * <ul>
	 * <li>If Tactic, evaluate the tactic and follow branching; in the case of no
	 * branching, then BOTH tactic completion and effect are required to consider
	 * the strategy execution successful, else it failed.
	 * <li>If done, Strategy evaluation terminates with success
	 * <li>If NULL Tactic, Strategy evaluation terminates with status-quo
	 * <li>If DO loop, repeat evaluating the target node (and all its descendants)
	 * until the max loop count is reached, at which time it means there was no
	 * SUCCESS, and Strategy terminates with failure
	 * </ul>
	 * </ol>
	 *
	 * @param argsIn the input arguments; there should be NONE
	 * @return one of the <code>Strategy.Outcome</code> enum values:
	 *         <code>SUCCESS</code>, <code>FAILURE</code>, <code>STATUSQUO</code>.
	 */
	@Override
	public Object evaluate(Object[] argsIn) {
		if (argsIn != null && argsIn.length > 0)
			throw new ArgumentMismatchException("Strategy should have NO argument!");

		m_outcome = Outcome.UNKNOWN;
		long startTime = System.currentTimeMillis();
		// tracks the path we went down, just the labels; reset it here
		m_nodeStack = new Stack<String>();
		// tracks do loop counts; "evaluate" resets the count map here
		m_doCntMap = new HashMap<StrategyNode, Integer>();
		// track current node, starting with root, push onto stack
		m_lastNode = getRootNode();
		m_nodeStack.push(m_lastNode.label());
//        m_executor.getOperationPublishingPort ().publishMessage (getStartMessage ());
		while (getOutcome() == Outcome.UNKNOWN && !m_stitch./* stitch (). */isCanceled()) {
			StrategyNode curNode = m_lastNode;
			boolean ok = evaluateFromNode(curNode);
			if (!ok) {
				break;
			}
		}
		if (Tool.logger().isInfoEnabled()) {
			Tool.logger().info("Strategy execution trail: " + m_nodeStack.toString());
		}

		// track time elapsed if NOT failure, and store exponential avg
		if (getOutcome() != Outcome.FAILURE) { // is this a good idea?
			try {
				long estTime = System.currentTimeMillis() - startTime;
				double alpha = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_MODEL_ALPHA, 0.33);
				m_avgExecutionTime = (long) ((1 - alpha) * m_avgExecutionTime + alpha * estTime);
			} catch (Throwable t) {
				// Running outside Rainbow (Rainbow.instance() throws exeception)
			}
		}

		// reset class field lastNode pointer
		if (getOutcome() != Outcome.UNKNOWN) {
			m_lastNode = null;
		}
//        m_executor.getOperationPublishingPort ().publishMessage (getEndMessage ());
		return getOutcome();
	}

//    private IRainbowMessage getEndMessage () {
//        IRainbowMessage msg = m_executor.getOperationPublishingPort ().createMessage ();
//        try {
//            msg.setProperty (IRainbowMessageFactory.EVENT_TYPE_PROP, IRainbowMessage.END_STRATEGY_TYPE);
//            msg.setProperty (IModelDSBusPublisherPort.STRATEGY_NAME, m_name);
//            msg.setProperty (IModelDSBusPublisherPort.STRATEGY_OUTCOME, m_outcome.name ());
//        }
//        catch (RainbowException e) {
//            // Should never happen
//        }
//        return msg;
//    }
//
//    private IRainbowMessage getStartMessage () {
//        IRainbowMessage msg = m_executor.getOperationPublishingPort ().createMessage ();
//        try {
//            msg.setProperty (IRainbowMessageFactory.EVENT_TYPE_PROP, IRainbowMessage.START_STRATEGY_TYPE);
//            msg.setProperty (IModelDSBusPublisherPort.STRATEGY_NAME, m_name);
//        }
//        catch (RainbowException e) {
//            // Should never happen
//        }
//        return msg;
//
//    }

	/**
	 * Like {@link #evaluate(Object[])}, this method evaluates the Strategy, but
	 * from the lastNode.
	 *
	 * @param argsIn the input arguments; there should be NONE
	 * @return one of the <code>Strategy.Outcome</code> enum values:
	 *         <code>SUCCESS</code>, <code>FAILURE</code>, <code>STATUSQUO</code>.
	 */
	public Object resumeEvaluate(Object[] argsIn) {
		if (m_lastNode == null)
			return evaluate(argsIn);
		else {
			if (Tool.logger().isInfoEnabled()) {
				Tool.logger().info("Resuming strategy execution from node " + m_lastNode.label() + ", trail thus far: "
						+ m_nodeStack.toString());
			}
			setOutcome(Outcome.UNKNOWN);
			while (getOutcome() == Outcome.UNKNOWN && !m_stitch./* stitch (). */isCanceled()) {
				StrategyNode curNode = m_lastNode; // no pushing into stack, already done
				boolean ok = evaluateFromNode(curNode);
				if (!ok) {
					break;
				}
			}
			if (getOutcome() != Outcome.UNKNOWN) {
				m_lastNode = null;
			}
			// not sure what to do with tracking of execution time with resumes...
			return getOutcome();
		}
	}

	public Outcome outcome() {
		return getOutcome();
	}

	private StrategyNode chooseNodeWithoutTimes(StrategyNode curNode) {
		StrategyNode defaultNode = null; // to track the DEFAULT cond node
		StrategyNode selected = null; // to track the chosen child node
		List<StrategyNode> matchingNodes = new ArrayList<StrategyNode>();
		final List<StrategyNode> childrenNodes = gatherChildrenNodes(curNode);
//        System.out.println (this.getName () + ": has " + childrenNodes.size () + " children");
		for (StrategyNode child : childrenNodes) {
			// check to see which child node's condition is satisfied
//            System.out.println (this.getName () + ": evaluating node condition for");
			switch (child.getCondFlag()) {
			case EXPRESSION:
				if (testCondition(child)) {
					matchingNodes.add(child);
				}
				break;
			case SUCCESS: // intentional fall-thru
			case FAILURE:
				// look at the parent node's tactic effect
				Tactic parentTactic = stitchState()./* stitch (). */findTactic(child.getParent().getTactic());
				if (parentTactic == null) { // something is wrong
					Tool.error(
							"Parent node " + child.getParent().label() + " appears not to have a tactic " + "action!",
							null, stitchState().stitchProblemHandler);
//                        System.out.println ("Parent node " + child.getParent ().label () + " appears not to have a " +
//                                                    "tactic" +
//                                                    " action!");
				} else {
					boolean effect = !parentTactic.hasError() && parentTactic.checkEffect();
					if (Tool.logger().isInfoEnabled()) {
						Tool.logger().info(child.label() + " " + child.getCondFlag().name() + " condition! " + effect);
					}
					if (effect) {
						if (child.getCondFlag() == ConditionKind.SUCCESS) {
							// parent node's tactic successfully achieved effect
							matchingNodes.add(child);
						}
					} else {
						if (child.getCondFlag() == ConditionKind.FAILURE) {
							// parent node's tactic failed to achieve effect
							matchingNodes.add(child);
						}
					}
				}
				break;
			case DEFAULT: // don't evaluate this, but store the default node
				if (Tool.logger().isInfoEnabled()) {
					Tool.logger().info(child.label() + " DEFAULT condition!");
				}
				defaultNode = child;
				break;
			default: // should NOT be the case
				Tool.error("Strategy node " + child.label() + "has unexpected condition kind! " + child.getCondFlag(),
						null, stitchState().stitchProblemHandler);
//                    System.out.print ("Strategy node " + child.label () + "has unexpected condition kind! " + child
//                            .getCondFlag ());
				break;
			}
		}
		// determine which child node to pick
		switch (matchingNodes.size()) {
		case 0: // none matched! check for DEFAULT
			if (defaultNode != null) { // choose the DEFAULT node
				selected = defaultNode;
			} else { // strategy failed
				setOutcome(Outcome.FAILURE);
//                    System.out.println ("No matching nodes in strategy, and no default node, so returning FAILURE
// for" +
//                                                " " +
//                                                getName ());
				return null;
			}
			break;
		case 1: // retrieve the only one
			selected = matchingNodes.get(0);
			break;
		default: // more than one satisfied
			// randomly pick one
			// BRS in the new stitchState semantics, this needs to change to calculate the
			// aggAtt for each matching node, assigning the utility, and picking the one
			// with the greatest utility, rather than being random
			int rand = new Random().nextInt(matchingNodes.size());
			selected = matchingNodes.get(rand);
			break;
		} // at this point, selected MUST be non null
		return selected;
	}

	// Stores whether any condition becomes true
	Boolean m_nodeTrue = new Boolean(false);

	/**
	 * Selects a node with that should be evaluated next, considering times
	 * Semantics are: (a) return the success or fail node, because that just depends
	 * on the tactic effect, otherwise (b) wait for node duration or until at least
	 * one child branch condition is true, then (c) if a branch condition as true,
	 * return one of those nodes, otherwise (d) if there is a default node, return
	 * that (e) otherwise set outcome to failure and return no node
	 *
	 * @param parentNode The node that has children we want to execute next
	 * @return the child to execute
	 */
	private StrategyNode chooseNodeWithTimes(StrategyNode parentNode) {
		try {
			long start = System.currentTimeMillis();
			StrategyNode defaultNode = null; // to track the default cond node
			StrategyNode selected = null; // to track the chosen child
			final List<StrategyNode> childrenNodes = gatherChildrenNodes(parentNode);

			// This is the amount of time to wait for any child conditions to evaluate to
			// true
			long branchWait = parentNode.getDuration();
			// Process success or failure immediately, save default for if all conditions
			// fail
			for (Iterator<StrategyNode> iterator = childrenNodes.iterator(); iterator.hasNext() && selected == null;) {

				StrategyNode child = iterator.next();
				switch (child.getCondFlag()) {
				case SUCCESS: // intentional fall-thru
				case FAILURE:
					// success or failure is condition in tactic effect, which has been calculated
					// by now. So just select these nodes.
					// look at parent node tactic effect
					iterator.remove(); // remove this node. Defensive in case we change the logic
					Tactic parentTactic = stitchState().findTactic(child.getParent().getTactic());
					if (parentTactic == null) { // something is wrong - there is no tactic
						Tool.error("Parent node " + child.getParent().label() + " appears not to have a tactic"
								+ " action!", null, stitchState().stitchProblemHandler);
					} else {
						boolean effect = !parentTactic.hasError() && parentTactic.checkEffect();
						if (Tool.logger().isInfoEnabled()) {
							Tool.logger()
									.info(child.label() + " " + child.getCondFlag().name() + " condition! " + effect);
						}
						if (effect) {
							if (child.getCondFlag() == ConditionKind.SUCCESS) {
								selected = child;
							}
						} else {
							if (child.getCondFlag() == ConditionKind.FAILURE) {
								selected = child;
							}
						}
					}
				case DEFAULT:
					// don't evaluate this, but store the default node
					if (Tool.logger().isInfoEnabled()) {
						Tool.logger().info(child.label() + " DEFAULT condition!");
					}
					defaultNode = child;
					// remove this from the list, so that we will be left with only conditions
					iterator.remove();
					break;
				case EXPRESSION:
					// Do nothing - childrenNodes will have all the conditional ones that need to be
					// timed
					break;
				default:
					// should NOT be the case
					Tool.error(
							"Strategy node " + child.label() + "has unexpected condition kind! " + child.getCondFlag(),
							null, stitchState().stitchProblemHandler);
					break;
				}
			}

			if (selected != null) {
				// Was success or fail
				return selected;
			}

			switch (childrenNodes.size()) {
			case 0: // there are no conditional children
				if (defaultNode != null)
					return defaultNode;
				else {
					setOutcome(Outcome.FAILURE);
					return null;
				}
			default:
				final Map<StrategyNode, Boolean> resultMap = new HashMap<>(); // store expression results
				// for every condition, register a timed condition to be evaluated in timing
				// thread
				for (final StrategyNode node : childrenNodes) {
					final Expression expr = node.getCondExpr();
					expr.clearState(); // make sure we're really reevaluating, not using cached value
					// register a condition. Should only process this for the node duration that is
					// dates back to when this method is called, otherwise the time will be @[x] +
					// the time taken to get to here, not @[x]
					ConditionTimer.instance().registerCondition(Collections.singletonList(expr),
							branchWait - (System.currentTimeMillis() - start), new Observer() {

								@Override
								public void update(Observable o, Object arg) {
									// register the result
									resultMap.put(node, (Boolean) arg);
									synchronized (this) {
										// indicate if at least one is true
										m_nodeTrue |= m_nodeTrue;
									}
								}
							});
				}

				// Wait for a condition to succeed, or until timeout
				boolean nodeTrue;
				long allottedTime = start + branchWait;
				do {
					// Problem here is that we will always wait at lease SLEEP_TIME_LONG (100ms) for
					// a condition to evaluate. Probably ok.
					try {
						Thread.sleep(ConditionTimer.SLEEP_TIME_LONG);
					} catch (InterruptedException e) {
					}
					// Need to synchronize because the timer thread will also
					// write to this
					synchronized (this) {
						nodeTrue = m_nodeTrue;
					}
					// while no condition is true, the stitch evaluation hasn't been cancelled,
					// and we haven't waited past the time it takes to evaluated,
				} while (!nodeTrue && !m_stitch.isCanceled() && System.currentTimeMillis() < allottedTime
				// (and each condition has been evaluated at least once?)
						&& resultMap.size() == childrenNodes.size());

				if (m_nodeTrue) {
					List<StrategyNode> matchingNodes = new ArrayList<>();
					for (Map.Entry<StrategyNode, Boolean> e : resultMap.entrySet()) {
						if (e.getValue())
							matchingNodes.add(e.getKey());
					}

					switch (matchingNodes.size()) {
					case 0: // none matched, break to default
						break;
					case 1:
						selected = matchingNodes.get(0);
						break;
					default:
						// Should really choose the branch with highest utility,
						// but this is consistent with the old version for now
						int rand = new Random().nextInt(matchingNodes.size());
						selected = matchingNodes.get(rand);
						break;
					}
				}
				if (selected != null)
					return selected;
				if (defaultNode != null) {
					return defaultNode;
				} else {
					setOutcome(Outcome.FAILURE);
					return null;
				}
			}
		} finally {
			// make sure to reset the node true flag
			m_nodeTrue = new Boolean(false);
		}
	}

	private boolean evaluateFromNode(StrategyNode curNode) {
		StrategyNode selected = null;
		if (!curNode.hasDuration()) {
			selected = chooseNodeWithoutTimes(curNode);
			if (selected == null && getOutcome() == Outcome.FAILURE)
				return false;
		} else {
			selected = chooseNodeWithTimes(curNode);
		}
		if (selected == null) {
			Tool.error("Serious failure? No node has been selected, impossible!!", null,
					stitchState().stitchProblemHandler);
			return false;
		}
		curNode = selected;
		m_nodeStack.push(curNode.label());
		m_lastNode = curNode;
		// do action on the selected node
		switch (selected.getActionFlag()) {
		case DONE:
			if (Tool.logger().isInfoEnabled()) {
				Tool.logger().info("DONE action!");
			}
			// a success branch, terminate successfully
			setOutcome(Outcome.SUCCESS);
			break;
		case NULL:
			if (Tool.logger().isInfoEnabled()) {
				Tool.logger().info("NULL action!");
			}
			// null action, so consider terminated with status quo
			setOutcome(Outcome.STATUSQUO);
			break;
		case TACTIC:
			doTactic(curNode);
			break;
		case DOLOOP:
			// LOOP! treat the target node as child node and proceed
			if (Tool.logger().isInfoEnabled()) {
				Tool.logger().info("LOOP action! " + curNode.label());
			}
			// update loop counter
			int numDone = 1;
			if (m_doCntMap.containsKey(curNode)) {
				numDone = m_doCntMap.get(curNode) + 1;
			}
			m_doCntMap.put(curNode, numDone);
			if (Tool.logger().isDebugEnabled()) {
				Tool.logger().debug(" - numDone for " + curNode.getDoTarget() + " at " + numDone);
			}
			if (numDone <= curNode.getNumDoTrials()) {
				// evaluate do target's condition and action
				curNode = nodes.get(curNode.getDoTarget());
				if (testCondition(curNode)) { // carry out its action
					doTactic(curNode);
				}
				// treat do target as if child node and proceed
				m_nodeStack.push(curNode.label());
			} else {
				// still no result after looping exceeds max count,
				// consider Strategy failed
				setOutcome(Outcome.FAILURE);
			}
			break;
		default: // serious error?
			Tool.error("Selected node " + selected.label() + "has unknown action kind! " + selected.getActionFlag(),
					null, stitchState().stitchProblemHandler);
//                System.out.println ("Selected node " + selected.label () + "has unknown action kind! " + selected
//                        .getActionFlag ());
			setOutcome(Outcome.FAILURE);
			break;
		}
		// at this point, evaluation occurred normally
		return true;
	}

	/**
	 * Given a node, evaluates its condition expression and return result.
	 *
	 * @param child StrategyNode whose condition to evaluate
	 * @return boolean <code>true</code> if condition evaluates to true;
	 *         <code>false</code> otherwise.
	 */
	private boolean testCondition(StrategyNode child) {
		boolean rv = false;
		Expression expr = child.getCondExpr();
		expr.clearState(); // make sure we're really re-evaluating, not using cached value
//        System.out.println (this.getName () + ": evaluating condition " + expr.toStringTree ());
		expr.evaluate(null);
		if (Tool.logger().isInfoEnabled()) {
			Tool.logger().info(child.label() + " expression condition! " + expr.getResult());
		}
		if (expr.getResult() != null && expr.getResult() instanceof Boolean) {
			if ((Boolean) expr.getResult()) { // condition satisfied!
				rv = true;
			}
		}
		return rv;
	}

	/**
	 * Evaluates tactic action of the given node.
	 *
	 * @param curNode the strategy node whose tactic action to evaluate.
	 */
	private void doTactic(StrategyNode curNode) {
		if (Tool.logger().isInfoEnabled()) {
			Tool.logger().info("Tactic action! " + curNode.getTactic());
		}
//        System.out.println ("Tactic action! " + curNode.getTactic ());
		Object[] args = Tool.evaluateArgs(curNode.getTacticArgExprs());
		Tactic tactic = stitchState()./* stitch (). */findTactic(curNode.getTactic());
		if (tactic.isExecuting() && tactic.stitchState() != curNode.stitch()) {
			try {
				Stitch stitch = Ohana.instance().findFreeStitch(stitchState()/* stitch () */);
				tactic = stitch.findTactic(curNode.getTactic());
			} catch (IOException e) {
				m_executor.getReportingPort().error(m_executor.getComponentType(),
						"Could not execute " + tactic.getName());
				setOutcome(Outcome.STATUSQUO);
			}
		}
//        if (tactic.isExecuting ()) {
//
//        }
		tactic.markExecuting(true);
		try {
			if (m_executor != null) {
				tactic.setHistoryModel(m_executor.getExecutionHistoryModel());
				m_executor.getHistoryModelUSPort().updateModel(m_executor.getExecutionHistoryModel().getCommandFactory()
						.strategyExecutionStateCommand(tactic.getQualifiedName(), ExecutionHistoryModelInstance.TACTIC,
								ExecutionHistoryData.ExecutionStateT.STARTED, null));
			}
			long start = new Date().getTime();
			tactic.evaluate(args);
			if (m_executor != null)
				m_executor.getHistoryModelUSPort().updateModel(m_executor.getExecutionHistoryModel().getCommandFactory()
						.strategyExecutionStateCommand(tactic.getQualifiedName(), ExecutionHistoryModelInstance.TACTIC,
								ExecutionHistoryData.ExecutionStateT.WAITING, Long.toString(tactic.getDuration())));
			// TODO: calculated settling based on Tactic effect settling
			boolean effectGood = tactic.awaitSettling();
			long end = new Date().getTime();
			// TODO: then await condition stuff
			if (m_executor != null) {
				m_executor.getHistoryModelUSPort().updateModel(m_executor.getExecutionHistoryModel().getCommandFactory()
						.strategyExecutionStateCommand(tactic.getQualifiedName(), ExecutionHistoryModelInstance.TACTIC,
								ExecutionHistoryData.ExecutionStateT.FINISHED, null));
				AbstractRainbowModelOperation recordTacticDurationCmd = m_executor.getExecutionHistoryModel()
						.getCommandFactory()
						.recordTacticDurationCmd(tactic.getQualifiedName(), end - start, effectGood);
				m_executor.getHistoryModelUSPort().updateModel(recordTacticDurationCmd);
			}
			// proceed with any branching
			if (curNode.getChildren().size() == 0) {
				// Tactic without branching, must be followed by done!
				// Check effect of tactic, and if not true, then consider strategy failed
				if (Tool.logger().isDebugEnabled()) {
					Tool.logger().debug("Tactic followed by done! effect == " + effectGood);
				}
				if (effectGood && !tactic.hasError()) {
					setOutcome(Outcome.SUCCESS);
				} else {
					setOutcome(Outcome.FAILURE);
				}
			}
		} finally {
			tactic.markExecuting(false);
		}
	}

	/**
	 * Given a node, awaits the effect of the node's tactic to evaluate to true, or
	 * until the node's duration times out.
	 *
	 * @param node StrategyNode whose tactic effect to await
	 * @return boolean <code>true</code> if tactic effect evaluates to true within
	 *         duration; <code>false</code> otherwise.
	 */
	private boolean awaitSettling(StrategyNode node) {
		if (node.getTactic() == null)
			return false; // function can't be eval'd if no tactic

		m_settlingCondition = null; // unset settling condition first
		clearVars();
		Tactic tactic = stitchState()./* stitch (). */findTactic(node.getTactic());
		ConditionTimer.instance().registerCondition(tactic.effects, tactic.getDuration(), m_conditionObserver);
		// wait for condition to be set...
		while (m_settlingCondition == null && !m_stitch./* stitch (). */isCanceled()) {
			try {
				Thread.sleep(ConditionTimer.SLEEP_TIME_LONG);
			} catch (InterruptedException e) {
				// intentional ignore
			}
		}
		if (m_stitch./* stitch (). */isCanceled())
			return false;
		else {
			if (Tool.logger().isInfoEnabled()) {
				Tool.logger().info("=> awaitSettling done! " + m_settlingCondition);
			}
			return m_settlingCondition.booleanValue();
		}
	}

	private List<StrategyNode> gatherChildrenNodes(StrategyNode node) {
		List<StrategyNode> children = new ArrayList<StrategyNode>();
		for (String label : node.getChildren()) {
			children.add(nodes.get(label));
		}
		return children;
	}

	public void setExecutor(StitchExecutor executor) {
		m_executor = executor;

	}

	public synchronized void markExecuting(boolean executing) {
		stitchState()./* stitch (). */markExecuting(executing);
	}

	public synchronized boolean isExecuting() {
		return stitchState()./* stitch (). */isExecuting();
	}

}
