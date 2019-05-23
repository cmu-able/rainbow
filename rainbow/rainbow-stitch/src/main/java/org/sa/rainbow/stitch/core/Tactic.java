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
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.adaptation.StitchExecutor;
import org.sa.rainbow.stitch.error.ArgumentMismatchException;
import org.sa.rainbow.stitch.history.ExecutionHistoryModelInstance;
import org.sa.rainbow.stitch.util.ExecutionHistoryData;
import org.sa.rainbow.stitch.util.Tool;
import org.sa.rainbow.stitch.visitor.IStitchBehavior;
import org.sa.rainbow.stitch.visitor.Stitch;
import org.sa.rainbow.stitch.visitor.StitchBeginEndVisitor;

import java.text.MessageFormat;
import java.util.*;

/**
 * Represents a Tactic scoped object parsed from the script.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class Tactic extends ScopedEntity implements IEvaluableScope {

	/**
	 * Declares the states that the Tactic object might be in during parsing.
	 */
	public enum ParseState {
	UNKNOWN, IN_PARAMS, IN_VARS, IN_CONDITION, IN_ACTION, IN_EFFECT, preState, PARSED
	}

	private static final String ATTRIBUTE_ARG_PREF = "_arg_";

	public ParseState state = ParseState.UNKNOWN;

	public List<Var> args = new ArrayList<Var>();
	public List<Expression> conditions = new ArrayList<Expression>();
	public List<Statement> actions = new ArrayList<Statement>();
	public List<Expression> effects = new ArrayList<Expression>();
	protected Map<String, Var> m_postVars = new HashMap<String, Var>();

	private Map<String, Object> m_attributes = new HashMap<String, Object>();

	private ExecutionHistoryModelInstance m_executionHistoryModel;

	/**
	 * Boolean indicating that a duration value is specified
	 */
	private boolean m_hasDuration = false;
	/**
	 * A simple Expression for duration (though full expression is allowed, not much
	 * is available on which to express, so this is effectively reduced (hopefully)
	 * to arithmetic expressions.
	 */
	private Expression m_durExpr = null;

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

	/**
	 * Main Constructor for a new Tactic object.
	 *
	 * @param parent the parent scope
	 * @param name   the name of this scope
	 * @param stitch the Stitch evaluation context object
	 */
	public Tactic(IScope parent, String name, Stitch/* State */ stitch) {
		super(parent, name, stitch);
	}

	/**
	 * Clones a Tactic object, including just the collection objects, but not
	 * deep-copying elements held in the collections.
	 */
	@Override
	public synchronized Tactic clone(IScope parent) {
		Tactic newT = new Tactic(parent, m_name, m_stitch/* .clone () */);
		copyState(newT);
		return newT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.stitchState.core.ScopedEntity#copyState(org.sa.rainbow.
	 * stitchState.core.ScopedEntity)
	 */
	protected void copyState(Tactic target) {
		super.copyState(target);
		target.state = state;
		target.args = new ArrayList<Var>(args.size());
		for (Var v : args) {
			target.args.add(v.clone());
		}
		target.conditions = new ArrayList<Expression>(conditions.size());
		for (Expression c : conditions) {
			target.conditions.add(c.clone(target.parent()));
		}
		target.actions = new ArrayList<Statement>(actions.size());
		for (Statement s : actions) {
			final Statement statement = new Statement(m_parent, m_name, m_stitch);
			statement.setTree(s.tree());
			target.actions.add(statement);
		}
		target.effects = new ArrayList<Expression>(effects.size());
		for (Expression e : effects) {
			target.effects.add(e.clone(target.parent()));
		}
		target.m_attributes = new HashMap<String, Object>(m_attributes);
		target.m_hasDuration = m_hasDuration;
		target.m_durExpr = m_durExpr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.stitchState.core.ScopedEntity#toString()
	 */
	@Override
	public String toString() {
		String str = "tactic: name \"" + m_name + "\" {" + "\n\t  vars [";
		for (Var v : m_vars.values()) {
			str += "\n\t\t" + v.toString();
		}
		str += "\n\t  ]\n\t  conditions [";
		for (Expression e : conditions) {
			str += "\n\t\t" + e.toString();
		}
		str += "\n\t  ]\n\t  actions [";
		for (Statement s : actions) {
			str += "\n\t\t" + s.toString();
		}
		str += "\n\t  ]\n\t  effects [";
		for (Expression e : effects) {
			str += "\n\t\t" + e.toString();
		}
		str += "\n\t  ]\n\t  m_attributes " + m_attributes.toString();
		str += "\n\t}";

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
			args.add(var);
			break;
		case IN_EFFECT:
			rv = addPostVar(id, var);
		}
		return rv;
	}

	private boolean addPostVar(String id, Var var) {
		boolean rv = true;
		if (m_postVars.containsKey(id)) {
			rv = false; // cannot add variable
		} else {
			m_postVars.put(id, var);
		}

		return rv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sa.rainbow.stitchState.core.ScopedEntity#addExpression(org.sa.rainbow.
	 * stitchState.core.Expression)
	 */
	@Override
	public void addExpression(Expression expr) {
		super.addExpression(expr);

		// add expression to either condition or effect block if applicable
		switch (state) {
		case IN_CONDITION:
			addCondition(expr);
			break;
		case IN_EFFECT:
			addEffect(expr);
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sa.rainbow.stitchState.core.ScopedEntity#addStatement(org.sa.rainbow.
	 * stitchState.core.Statement)
	 */
	@Override
	public void addStatement(Statement stmt) {
		super.addStatement(stmt);

		// add statement to action block if applicable
		if (state == ParseState.IN_ACTION) {
			addAction(stmt);
		}
	}

	/**
	 * Puts a new key-valueObj attribute pair into the attribute map. The value
	 * object could be a number literal, or an expression string that is evaluated
	 * later at value reading time.
	 *
	 * @param name the label of the attribute, e.g., uD
	 * @param vObj the value literal or expression string of the cost-benefit
	 *             attribute
	 */
	public void putAttribute(String name, Object vObj) {
		if (vObj instanceof Number) {
			m_attributes.put(name, ((Number) vObj).doubleValue());
		} else {
			// otherwise, grab expression string, munge it, and evaluate
			String exprStr = vObj + ";"; // append ending semicolon
			for (int i = 0; i < args.size(); ++i) {
				exprStr = exprStr.replace("{" + i + "}", ATTRIBUTE_ARG_PREF + i);
			}
			m_attributes.put(name, exprStr);
		}
	}

	/**
	 * Returns the set of keys in the attribute map.
	 *
	 * @return set of keys
	 */
	public Set<String> attributeKeySet() {
		return m_attributes.keySet();
	}

	/**
	 * Computes and returns the attribute value for the given key.
	 *
	 * @param key name of the attribute
	 * @return double value, either of the literal, or as a result of evaluating the
	 *         value expression
	 */
	public double computeAttribute(String key) {
		if (!m_attributes.containsKey(key))
			return 0.0;
		Object vObj = m_attributes.get(key);
		if (vObj instanceof Double)
			return (Double) vObj;

		// otherwise, grab expression string, and parse it
		String exprStr = (String) vObj;
		Expression expr = Ohana.instance().parseExpressionString(exprStr);
		// populate values for required arguments, then evaluate expression
		expr.vars().clear();
		for (int i = 0; i < args.size(); ++i) {
			String argName = ATTRIBUTE_ARG_PREF + i;
			if (exprStr.contains(argName)) { // only create a var if called for
				Var v = new Var();
				v.scope = expr.parent();
				v.name = argName;
				Object o = args.get(i).getValue(); // there should be a value already
				// TODO sloppy treatment of value types!
				if (o instanceof Number) {
					v.setType("float"); // Stitch float accommodates double and int
				} else if (o instanceof Set<?>) {
					v.setType("Set");
				} else {
					v.setType("Object");
				}
				v.setValue(o);
				expr.addVar(v.name, v);
			}
		}

		Object rv = expr.evaluate(null);
		if (rv instanceof MyNumber)
			return ((MyNumber) rv).toJavaNumber().doubleValue();
		else
			return (Double) rv;
	}

	/**
	 * Sets the tactic arguments with the supplied array of values.
	 *
	 * @param argsIn array of argument values to set
	 */
	public void setArgs(Object[] argsIn) {
		if (args.size() > 0 && argsIn != null && argsIn.length == args.size()) {
			int i = 0;
			for (Var argV : args) {
				argV.setValue(argsIn[i++]);
			}
		} else {
			if (argsIn == null)
				throw new ArgumentMismatchException("Argument array is NULL!");
			else if (argsIn.length != args.size())
				throw new ArgumentMismatchException("Argument count does NOT match!");
		}
	}

	/**
	 * Evaluates the Tactic, which consists of evaluating the conditions, executing
	 * the action block if conditions match, and evaluating the effects to make sure
	 * they are met.
	 *
	 * @param argsIn the input arguments
	 * @throws StitchExecutionException
	 */
	@Override
	public Object evaluate(Object[] argsIn) throws StitchExecutionException {
		IStitchBehavior beh = m_stitch./* stitch (). */getBehavior(Stitch.EVALUATOR_PASS);
		if (beh == null) {
			System.out.println("Could not find a Stitch.EVALUATOR_PASS behavior");
			return Boolean.FALSE; // probably disposed
		}
		StitchBeginEndVisitor walker = new StitchBeginEndVisitor(beh, this/* m_stitch.scope () */);
		walker.setBehavior(beh);
		beh.stitch().setScope(this);
		return evaluate(argsIn, walker);
	}

	public Map<String, Var> postVars() {
		return m_postVars;
	}

	public Object evaluate(Object[] argsIn, StitchBeginEndVisitor walker) throws StitchExecutionException {
		long start = new Date().getTime();
		StitchExecutor executor = stitchState().executor();
		if (executor != null) executor.reportingPort().info(executor.getComponentType(), MessageFormat.format("[[{0}]]: Tactic started: {1}({2})", executor.id(),this.m_name,Arrays.toString(argsIn)));
		setArgs(argsIn);
		ExecutionHistoryModelInstance modelInstance = m_executionHistoryModel;

		// mark disruption level with model
		double level = computeAttribute("uD");
		if (modelInstance != null) {
			modelInstance.markDisruption(level);
		}

		// create snapshot of model
		stitchState()./* stitch(). */script.freezeModel();

		try {

			// clear states of action statements first
			for (Statement stmt : actions) {
				stmt.clearState();
			}

			// process the variables
			for (Var v : vars().values()) {
//            if (v.name.startsWith ("__post__"))
//                continue; // ignore post expression until effect is checked
				if (v.valStmt != null) {
					// evaluate the initial var value
					v.valStmt.evaluate(null, walker);
				}
				if (!stitchState().stitchProblemHandler.unreportedProblems().isEmpty()) {
					throw new StitchExecutionException("Failed to evaluate '" + v.name + "'");
				}
			}

			// evaluate the condition statements
			if (!checkCondition()) { // don't continue execute?
				Tool.warn("Tactic condition of applicability NOT met!", null, stitchState().stitchProblemHandler);
				// "unfreeze" model
//            System.out.println ("Tactic condition of applicability NOT met!");
				stitchState()./* stitch(). */script.unfreezeModel();
				if (modelInstance != null) {
					modelInstance.markDisruption(0.0); // reset disruption
				}
				return null;
			}
			// execute the action statements
			for (Statement stmt : actions) {
				stmt.evaluate(null, walker);
				// check and abort if failure occurred
				if (stmt.hasError()) {
					m_hasError = true;
					Tool.error("Statement failed to evaluate! " + stmt.toString(), null,
							stitchState().stitchProblemHandler);
					throw new StitchExecutionException ("Statement failed to evaluate! " + stmt.toString());
					// TODO: keep running, or break?
//                System.out.print ("Statement failed to evaluate! " + stmt.toString ());
				}
			}

			// evaluate the effect statements
//        for (Var v : m_postVars.values ()) {
//            final String preVName = v.name.substring ("__post__" .length ());
//            Var preV = vars ().get (preVName);
//            preV.computeValue ();
//            Expression e = preV.scope.expressions ().get (0);
//            v.setValue (e.getResult ());
////            if (v.name.startsWith ("__post__"))
////                continue; // ignore post expression until effect is checked
////            if (v.valStmt != null) {
////                 evaluate the initial var value
////                v.valStmt.evaluate(null, walker);
////            }
//        }
			boolean effectObserved = checkEffect();
			long end = new Date().getTime();

			if (Tool.logger().isInfoEnabled()) {
				Tool.logger().info("Tactic expected effect: " + effectObserved);
			}
//        System.out.println ("Tactic expected effect: " + effectObserved);

			// "unfreeze" model
			stitchState()./* stitch(). */script.unfreezeModel();
			if (modelInstance != null) {
				modelInstance.markDisruption(0.0); // reset disruption
			}
			return null; // Tactic doesn't return a result
		} finally {
			stitchState()./* stitch(). */script.unfreezeModel();
			if (executor != null) executor.reportingPort().info(executor.getComponentType(), MessageFormat.format("[[{0}]]: Tactic finished: {1}", executor.id(), m_name));

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.stitchState.core.IEvaluable#estimateMaxTimeCost()
	 */
	@Override
	public long estimateAvgTimeCost() {
		long t = 0L;
		// first check if we have history data on tactic execution
		IModelInstance<Map<String, ExecutionHistoryData>> historyModel = Rainbow.instance().getRainbowMaster()
				.modelsManager().getModelInstance(new ModelReference("tacticExecutionHistoryModel",
						ExecutionHistoryModelInstance.EXECUTION_HISTORY_TYPE));
		ExecutionHistoryData ehd = null;
		if (historyModel != null && ((ehd = historyModel.getModelInstance().get(getQualifiedName()))) != null) {
			t = (long) ehd.getMeanDuration();
		} else { // estimate based on action statements
			for (Statement stmt : actions) {
				t += stmt.estimateAvgTimeCost();
			}
		}
		return t + getDuration();
	}

	/**
	 * Evaluates Tactic's conditions of applicability and returns result.
	 *
	 * @return boolean <code>true</code> if conditions of applicability are true,
	 *         <code>false</code> otherwise.
	 */
	public boolean checkCondition() {
		boolean condMet = true;
		for (Expression expr : conditions) {
			expr.clearState();
			condMet &= (Boolean) expr.evaluate(null);
			if (!stitchState().stitchProblemHandler.unreportedProblems().isEmpty())
				return false;
		}
		return condMet;
	}

	/**
	 * Evaluates Tactic's effects and returns result.
	 *
	 * @return boolean <code>true</code> if effects are realized, <code>false</code>
	 *         otherwise.
	 */
	public boolean checkEffect() {
		boolean effMet = true;
		// Last expression might be duration expression
		List<Expression> effectsToCheck = effects;
		if (this.hasDuration())
			effectsToCheck = effects.subList(0, effects.size()-1);
		
		
		for (Expression expr : effectsToCheck) {
			expr.clearState();
			effMet &= (Boolean) expr.evaluate(null);
			if (!stitchState().stitchProblemHandler.unreportedProblems().isEmpty())
				return false;
		}
		return effMet;
	}

	@Override
	public Object lookup(String name) {
		if (name == null)
			return null;
		Object v = postVars().get(name);
		if (v == null)
			v = super.lookup(name);
		return v;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.stitchState.core.IEvaluable#modelElementsUsed()
	 */
	@Override
	public Set<? extends IAcmeElement> modelElementsUsed() {
		Set<IAcmeElement> resultSet = new HashSet<IAcmeElement>();
		// calculate "used" only from action statements
		for (Statement stmt : actions) {
			resultSet.addAll(stmt.modelElementsUsed());
		}
		return resultSet;
	}

	protected void addCondition(Expression cond) {
		conditions.add(cond);
	}

	protected void addAction(Statement action) {
		actions.add(action);
	}

	protected void addEffect(Expression effect) {
		effects.add(effect);
	}

	public void setHistoryModel(ExecutionHistoryModelInstance executionHistoryModel) {
		m_executionHistoryModel = executionHistoryModel;
	}

	public void markExecuting(boolean executing) {
		stitchState()./* stitch (). */markExecuting(executing);
	}

	public boolean isExecuting() {
		return stitchState()./* stitch (). */isExecuting();
	}

	public boolean hasDuration() {
		return m_hasDuration;
	}

	public void setHasDuration(boolean duration) {
		m_hasDuration = duration;
	}

	public long getDuration() {
		if (hasDuration()) {
			m_durExpr.evaluate(null);
			if (m_durExpr.getResult() != null)
				return ((MyInteger) m_durExpr.getResult()).longValue();
		}
		return 0;
	}

	public Expression getDurationExpr() {
		return m_durExpr;
	}

	public void setDurationExpr(Expression durExpr) {
		m_durExpr = durExpr;
	}

	public boolean awaitSettling() {
		StitchExecutor e = stitchState().executor();
		boolean ret = false;
		m_settlingCondition = null;
		long duration = getDuration();
		if (e != null) e.getReportingPort().info(e.getComponentType(), MessageFormat.format("[[{0}]]: Tactic {1} @{2} check effect", e.id(), m_name, duration));
		ConditionTimer.instance().registerCondition(hasDuration()?effects.subList(0, effects.size()-1):effects, duration, m_conditionObserver);
		// wait for condition to be set...
		while (m_settlingCondition == null && !m_stitch.isCanceled()) {
			try {
				Thread.sleep(ConditionTimer.SLEEP_TIME_LONG);
			} catch (InterruptedException ex) {
			}
		}
		if (m_stitch.isCanceled()) {
			ret = false;
		} else {
			if (Tool.logger().isInfoEnabled()) {
				Tool.logger().info("=> awaitTacticSettling done! " + m_settlingCondition);
			}
			ret = m_settlingCondition.booleanValue();
		}
		if (e != null) e.getReportingPort().info(e.getComponentType(), MessageFormat.format("[[{0}]]: Tactic {1} effect: {2}", e.id(), m_name, ret));
		return ret;
	}

}