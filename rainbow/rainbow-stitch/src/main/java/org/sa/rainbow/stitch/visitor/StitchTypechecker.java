package org.sa.rainbow.stitch.visitor;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.acmestudio.acme.core.IAcmeType;
import org.acmestudio.acme.core.type.IAcmeBooleanType;
import org.acmestudio.acme.core.type.IAcmeDoubleType;
import org.acmestudio.acme.core.type.IAcmeFloatType;
import org.acmestudio.acme.core.type.IAcmeIntType;
import org.acmestudio.acme.core.type.IAcmeSequenceType;
import org.acmestudio.acme.core.type.IAcmeSetType;
import org.acmestudio.acme.core.type.IAcmeStringType;
import org.acmestudio.acme.element.IAcmeElement;
import org.acmestudio.acme.element.IAcmeElementType;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.DefaultAcmeModel;
import org.acmestudio.acme.model.root.AcmeRootDesignAnalysis;
import org.acmestudio.acme.rule.IAcmeDesignAnalysis;
import org.acmestudio.acme.rule.node.FormalParameterNode;
import org.acmestudio.acme.type.AcmeTypeHelper;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.core.Expression;
import org.sa.rainbow.stitch.core.Expression.Kind;
import org.sa.rainbow.stitch.core.IScope;
import org.sa.rainbow.stitch.core.ScopedEntity;
import org.sa.rainbow.stitch.core.Statement;
import org.sa.rainbow.stitch.core.StitchTypes;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Strategy.ConditionKind;
import org.sa.rainbow.stitch.core.Strategy.ExpressionKind;
import org.sa.rainbow.stitch.core.Strategy.StatementKind;
import org.sa.rainbow.stitch.core.StrategyNode;
import org.sa.rainbow.stitch.core.Tactic;
import org.sa.rainbow.stitch.core.Var;
import org.sa.rainbow.stitch.parser.StitchParser;
import org.sa.rainbow.stitch.parser.StitchParser.ConditionContext;
import org.sa.rainbow.stitch.parser.StitchParser.DataTypeContext;
import org.sa.rainbow.stitch.parser.StitchParser.EffectContext;
import org.sa.rainbow.stitch.parser.StitchParser.ExpressionContext;
import org.sa.rainbow.stitch.parser.StitchParser.PathExpressionContext;
import org.sa.rainbow.stitch.parser.StitchParser.QuantifiedExpressionContext;
import org.sa.rainbow.stitch.parser.StitchParser.SetExpressionContext;
import org.sa.rainbow.stitch.parser.StitchParser.TacticContext;
import org.sa.rainbow.stitch.parser.StitchParser.UnaryExpressionContext;
import org.sa.rainbow.stitch.parser.StitchParser.VarContext;
import org.sa.rainbow.stitch.util.Tool;

/**
 * Created by schmerl on 10/3/2016.
 */
public class StitchTypechecker extends BaseStitchBehavior {

	public Set<String> m_modelOperationsReferenced = new HashSet<>();

	public StitchTypechecker(Stitch/* State */ stitch) {
		super(stitch);
	}

	@Override
	public void beginScript(IScope scriptScope) {
//		pushScope(scriptScope);
//        setScript (new StitchScript (scope (), null, m_stitch));
//        pushScope (script ());
	}

	@Override
	public void endScript() {
//        popScope ();
//        popScope ();

	}

	@Override
	public void beginStrategy(TerminalNode nameAST) {
		IScope scope = scope();
		Strategy s = null;
		if (scope.isRoot()) {
			for (Strategy st : script().strategies) {
				if (nameAST.getText().equals(st.getName())) {
					if (s != null) {
						String msg = MessageFormat.format("Strategy ''{0}'' has already been defined",
								nameAST.getText());
						Tool.error(msg, (ParserRuleContext) nameAST.getParent(), stitchProblemHandler());
						break;
					} else
						s = st;
				}

			}
			if (s == null) {
				String msg = MessageFormat.format("Strategy ''{0}'' is not in scope", nameAST.getText());
				Tool.error(msg, (ParserRuleContext) nameAST.getParent(), stitchProblemHandler());
			} else {
				setExpression(s.getRootNode().getCondExpr());
			}
			pushScope(s);
		}

	}

	@Override
	public void beginTactic(TerminalNode nameAST) {
		IScope scope = scope();
		Tactic tactic = null;
		if (scope.isRoot()) {
			for (Tactic t : script().tactics) {
				if (nameAST.getText().equals(t.getName())) {
					if (tactic != null) {
						String msg = MessageFormat.format("Strategy ''{0}'' has already been defined",
								nameAST.getText());
						Tool.error(msg, (ParserRuleContext) nameAST.getParent(), stitchProblemHandler());
						break;
					} else
						tactic = t;
				}
			}
			if (tactic == null) {
				String msg = MessageFormat.format("Strategy ''{0}'' is not in scope", nameAST.getText());
				Tool.error(msg, (ParserRuleContext) nameAST.getParent(), stitchProblemHandler());
			} else {
			}
			pushScope(tactic);
		}
	}

	@Override
	public void endTactic(TerminalNode nameAST) {
		popScope();
		setExpression(null);
	}

	@Override
	public void endStrategy() {
		popScope();
		setExpression(null);
	}

	@Override
	public void beginStrategyNode(TerminalNode identifier, ParserRuleContext ctx) {
		IScope scope = scope();
		String label = identifier.getText();
		if (scope instanceof Strategy) {
			Strategy st = (Strategy) scope;
			StrategyNode strategyNode = st.nodes.get(label);
			if (strategyNode == null) {
				Tool.error("Strategy node '" + label + "' not found in Strategy. This should never happen.", ctx,
						stitchProblemHandler());
			}
			setExpression(strategyNode.scope.expressions().get(0));
			pushScope(strategyNode.scope);
		} else if (scope instanceof ScopedEntity) {
			ScopedEntity node = (ScopedEntity) scope;
			IScope childScope = null;
			for (int i = 0; i < node.getChildren().size() && childScope == null; i++) {
				if (label.equals(node.getChildren().get(i).getName()))
					childScope = (node.getChildren().get(i));
			}
			if (childScope == null) {
				Tool.error(MessageFormat.format("Strategy node ''{0}'' not found in ''{1}''. This should never happen.",
						label, node.getName()), ctx, stitchProblemHandler());

			}
			if (childScope.expressions().size() > 0)
				setExpression(childScope.expressions().get(0));
			pushScope(childScope);
		}
	}

	@Override
	public void endStrategyNode() {
		IScope s = scope();
		// Do typechecking of everything here.
		popScope();
	}

	@Override
	public void doStrategyCondition(ConditionKind type, ParserRuleContext ctx) {
		Expression expr = expr();
		IScope scope = scope();
		switch (type) {
		case APPLICABILITY: {
			Strategy st = (Strategy) scope;
			if (!expr.getType().equals(Expression.BOOLEAN)) {
				String msg = MessageFormat.format(
						"The applicability condition ''{0}'' in Strategy ''{1}'' is not boolean",
						ParserUtils.formatTree(expr.tree()), st.getName());
				Tool.error(msg, ctx, stitchProblemHandler());
			}
			break;
		}
		case EXPRESSION: {
			ScopedEntity sc = (ScopedEntity) scope;
			expr = sc.expressions().get(0);
			if (!Expression.BOOLEAN.equals(expr.getType())) {
				String msg = MessageFormat.format(
						"The strategy node condition ''{0}'' in strategy node ''{1}'' is not boolean",
						ParserUtils.formatTree(expr.tree()), sc.getName());
				Tool.error(msg, ctx, stitchProblemHandler());
			}
			break;
		}
		}

	}

	@Override
	public void doStrategyProbability(StitchParser.StrategyCondContext ctx) {
		Expression expr = expr();
		if (expr.getType() != Expression.FLOAT) {
			Tool.error("Probability expression should be between [0,1]", ctx, stitchProblemHandler());
		}
	}

	@Override
	public void beginReferencedTactic(TerminalNode labelAST) {
		IScope scope = scope();
		Object tactic = scope.lookup(labelAST.getText());
		setExpression(scope.expressions().get(1));

		if (tactic == null) {
			String msg = MessageFormat.format("''{0}'' is not defined", labelAST.getText());
			Tool.error(msg, (ParserRuleContext) labelAST.getParent(), stitchProblemHandler());
		}

		if (!(tactic instanceof Tactic)) {
			String msg = MessageFormat.format("''{0}'' is not a tactic!", labelAST.getText());
			Tool.error(msg, (ParserRuleContext) labelAST.getParent(), stitchProblemHandler());
			return;
		}
	}

	@Override
	public void endReferencedTactic(TerminalNode labelAST) {
		IScope scope = scope();
		Expression expr = expr();
		Object t = scope.lookup(labelAST.getText());
		if (t instanceof Tactic) {
			Tactic tactic = (Tactic) t;
			if (expr.expressions().size() != tactic.args.size()) {
				String msg = MessageFormat.format(
						"Error in tactic reference: Expecting {0} argument{3} but got {1} for ''{2}''",
						tactic.args.size(), expr.expressions().size(), labelAST.getText(),
						tactic.args.size() == 1 ? "" : "s");
				Tool.error(msg, (ParserRuleContext) labelAST.getParent(), stitchProblemHandler());
			}
			int i = 0;
			for (Var p : tactic.args) {
				String atype = expr.expressions().get(i).getType();
				if ("int".equals(atype))
					atype = Expression.INTEGER;
				String fType = p.getType();
				if ("int".equals(fType))
					fType = Expression.INTEGER;
				if (!fType.equals(atype)) {
					String msg = MessageFormat.format(
							"Incompatible types: Passing an expression of type ''{0}'' to argument {1}({2}) of {3}, expecting {4}",
							expr.expressions().get(i).getType(), i, p.name, tactic.getName(), fType);
					Tool.error(msg, (ParserRuleContext) labelAST.getParent(), stitchProblemHandler());
				}
				i++;
			}

		}
		setExpression(scope.expressions().size() >= 3 ? scope.expressions().get(2) : null);
	}

	@Override
	public void doStrategyDuration(ParserRuleContext ctx, TerminalNode labelAST) {
		IScope scope = scope();
		Expression expr = expr();
		Object t = scope.lookup(labelAST.getText());
		if (t instanceof Tactic) {
			Tactic tactic = (Tactic) t;
			if (!tactic.hasDuration()) {
				String msg = MessageFormat.format(
						"Warning: The strategy condition in branch {0} has a duration but the referenced tactic does not. This might indicate use of the old timing syntax",
						scope.getName());
				Tool.warn(msg, ctx, stitchProblemHandler());
			}
			if (!Expression.INTEGER.equals(expr.getType()) && !Expression.LONG.equals(expr.getType())) {
				String msg = MessageFormat.format(
						"Error: The type of the duration in branch {0} must be an integer or long: {1}",
						scope.getName(), ParserUtils.formatTree(expr.tree()));
				Tool.error(msg, ctx, stitchProblemHandler());
			}
		}
	}

	@Override
	public void doStrategyLoop(Token vAST, Token iAST, Token labelAST) {
		IScope scope = scope();
		if (vAST != null) {
			Object amount = scope.lookup(vAST.getText());
			if (amount == null) {
				String msg = MessageFormat.format("Error: ''{0}'' is undefined in branch {1}", vAST.getText(),
						scope.getName());
				Tool.error(msg, null, stitchProblemHandler());

			} else if (amount instanceof Var) {
				if (!Expression.INTEGER.equals(((Var) amount).getType())) {
					String msg = MessageFormat.format(
							"Error: The loop variable ''{0}'' in branch {1} must be an integer", vAST.getText(),
							scope.getName());
					Tool.error(msg, null, stitchProblemHandler());
				}
			}
		}
		Object lookup = scope.lookup(labelAST.getText());
		if (lookup == null) {
			String msg = MessageFormat.format("Error: The loop reference ''{0}'' in branch {1} is undefined",
					labelAST.getText(), scope.getName());
			Tool.error(msg, null, stitchProblemHandler());
		} else if (!lookup.getClass().equals(ScopedEntity.class)) {
			String msg = MessageFormat.format(
					"Error: The loop reference ''{0}'' in branch {1} does not seem to refer to a labeled branch",
					labelAST.getText(), scope.getName());
			Tool.error(msg, null, stitchProblemHandler());
		}
	}

	@Override
	public void beginConditionBlock(ConditionContext nameAST) {
		StitchParser.TacticContext p = (TacticContext) nameAST.getParent();
		IScope scope = scope();
	}

	@Override
	public void beginCondition(int i) {
		Tactic scope = (Tactic) scope();
		setExpression(scope.conditions.get(i));
	}

	@Override
	public void endCondition(int i) {
		setExpression(null);
	}

	@Override
	public void beginAction(int i) {
		Tactic scope = (Tactic) scope();
		Statement statement = scope.actions.get(i);
		pushScope(statement);
	}

	@Override
	public void endAction(int i) {
		popScope();
	}

	@Override
	public void endEffectBlock(EffectContext nameAST) {
		Tactic scope = (Tactic) scope();
		for (Expression e : scope.effects) {
			if (e == scope.getDurationExpr())
				continue;
			if (!Expression.BOOLEAN.equals(e.getType())) {
				Tool.error(MessageFormat.format("All tactic effects must be boolean: {0} in tactic ''{1}''",
						ParserUtils.formatTree(e.tree()), scope.getName()), null, stitchProblemHandler());
			}
		}
		if (scope.getDurationExpr() != null) {
			String type = scope.getDurationExpr().getType();
			if (!Expression.INTEGER.equals(type)) {
				Tool.error(
						MessageFormat.format("Tactic duration must be an integer: {0} in tactic ''{1}'' is {2}",
								ParserUtils.formatTree(scope.getDurationExpr().tree()), scope.getName(), type),
						null, stitchProblemHandler());
			}
		}
	}

	@Override
	public void endConditionBlock() {
		Tactic scope = (Tactic) scope();
		for (int i = 0; i < scope.conditions.size(); i++) {
			Expression expr = scope.conditions.get(i);
			if (!Expression.BOOLEAN.equals(expr.getType())) {
				Tool.error(MessageFormat.format("All tactic conditions must be boolean: {0} in tactic ''{1}''",
						ParserUtils.formatTree(expr.tree()), scope.getName()), null, stitchProblemHandler());
			}
		}
	}

	@Override
	public void doIdentifierExpression(ParserRuleContext idAST, Strategy.ExpressionKind kind) {
//		Expression expr = (Expression) scope();
		Expression expr = expr();
		String id = idAST.getText();
//		expr.setName(id);
		if (kind == Strategy.ExpressionKind.IDENTIFIER) {
			Object o = null;
			IScope scope = scope();
			if (scope.lookup("___Path_Filter") != null) {
				Object pv = scope.lookup("___Path_Filter");
				if (pv instanceof Var)
					pv = ((Var) pv).typeObj;
				if (pv instanceof IAcmeElementType) {
					o = ((IAcmeElementType) pv).lookupName(id);
				}
			}

			if (o == null)
				o = scope.lookup(id);
			if (o != null && o instanceof Var) {
				expr.addRefdVar((Var) o);
			}
			if (o == null) {
				Tool.error("Unresolved reference '" + id + "'! Perhaps model not accessible?", idAST,
						stitchProblemHandler());
				setType(expr, Expression.UNKNOWN);
			}

			else if (o instanceof Var) {
				setType(expr, ((Var) o).getType());
			} else if (o instanceof IAcmeProperty) {
				setType(expr, expr.getTypeFromAcme((IAcmeProperty) o));
			} else if (o instanceof Set) {
				setType(expr, Expression.SET);
			} else if (o instanceof AcmeModelInstance) {
				setType(expr, o.getClass().getCanonicalName());
			} else if (o != null) {
				setType(expr, o.getClass().getCanonicalName());
			} else // TODO: use components, connectors, ports, roles to set the type of the
					// ___Path_Filter var that needs to be set up in being pathExpression
					// Check Acme sets expressions
				setType(expr, Expression.UNKNOWN);
		} else {
			switch (kind) {
			case BOOLEAN:
				setType(expr, Expression.BOOLEAN);
				break;
			case CHAR:
				setType(expr, Expression.CHAR);
				break;
			case STRING:
				setType(expr, Expression.STRING);
				break;
			case FLOAT:
				setType(expr, Expression.FLOAT);
				break;
			case INTEGER:
				setType(expr, Expression.INTEGER);
				break;
			case NULL:
				setType(expr, null);
			}

		}
	}

	@Override
	public void doPostIdentifierExpression(StitchParser.PostIdExpressionContext identifier) {
		if (scope() != null) {
			if (scope().parent() != null) {
				Var var = scope().vars().get(identifier.IDENTIFIER().getText());
				if (var != null) {
					setType(expr(), var.getType());
					return;
				}
			}
		}
		Tool.error(identifier.IDENTIFIER().getText() + " is not defined in the tactic scope, and so "
				+ identifier.getText() + " cannot be used in the effect.", identifier, stitchProblemHandler());
	}

	@Override
	public void createVar(DataTypeContext type, TerminalNode id, ExpressionContext val, boolean isFunction,
			boolean isFormalParam) {
		IScope scope = scope();
		if (scope instanceof Expression && ((Expression) scope()).getKind() == Expression.Kind.QUANTIFIED)
			return;
		if (isFormalParam) {
			String vType = type.getText();
			switch (vType) {
			case Expression.BOOLEAN:
			case Expression.STRING:
			case Expression.FLOAT:
			case Expression.INTEGER:
			case "int":
			case Expression.LONG:
			case Expression.SEQ:
			case Expression.SET:
				break;
			default:
				Object lookup = scope.lookup(vType);
				if (lookup == null) {
					Tool.error(MessageFormat.format("Undefined type ''{0}'' in defintion of parameter {1}", vType,
							id.getText()), type, stitchProblemHandler());
				}
			}
		} else
//		else if (scope instanceof Tactic && ((Tactic )scope).)
		if (scope.expressions().size() > 0) {
			Expression e = scope.expressions().get(0);
			e.processed = false;
			String eType = e.getRawType();
			String vType = type.getText();
			if ("int".equals(vType))
				vType = Expression.INTEGER;
			if (!vType.equals(eType) && !Expression.UNKNOWN.equals(eType) && !"object".equals(vType)) {
				if (!(Expression.LONG.equals(vType) && Expression.INTEGER.equals(eType))) {
					if (!(Expression.FLOAT.equals(vType)
							&& (Expression.LONG.equals(eType) || Expression.INTEGER.equals(eType)))) {
						Tool.error(MessageFormat.format("Cannot assign {0} to a {1} when defining {2}", eType, vType,
								id.getText()), val, stitchProblemHandler());
					}
				}
			}
		}
	}

	@Override
	public void lOp() {
		expr().curOp.push(Expression.LOP);
	}

	@Override
	public void rOp() {
		Expression expr = expr();
		expr.curOp.push(Expression.ROP);
	}

	@Override
	public boolean beginExpression(ParserRuleContext ctx) {

		if (expr() != null) {
			expr().subLevel++;
			return true;
		}
		final IScope scope = scope();
		if (scope instanceof Statement && scope.expressions().size() > ((Statement) scope).curExprIdx) {
			setExpression(scope.expressions().get(((Statement) scope).curExprIdx++));
			expr().curExprIdx = 0;
		} else if (scope instanceof Expression) {
			Expression expr = (Expression) scope;
			if (expr.isComplex()) {
				int curExprIdx = expr.curExprIdx;
				if (expr.expressions().size() > curExprIdx) {
					Expression expr2 = expr.expressions().get(expr.curExprIdx++);
					setExpression(expr2);
					expr.subLevel = 0;
				}
			} else {
				setExpression((Expression) scope);
				expr().curExprIdx = 0;
			}
		}
		return false;
	}

	@Override
	public void processParameter() {
		Expression e = expr();
		if (e.parent() instanceof Expression) {
			Expression parent = (Expression) e.parent();
			if (parent.isComplex()) {
				if (parent.expressions().size() > parent.curExprIdx) {
					setExpression(parent.expressions().get(parent.curExprIdx++));
				} else
					setExpression(parent);

			}
		}
	}

	@Override
	public void endExpression(ParserRuleContext ctx, boolean pushed) {
//		super.endExpression(ctx); it's at the end
		Expression expr = expr();
		if (expr == null)
			return;
		if (expr.subLevel > 0) { // decr dept count first
			expr.subLevel--;
		} else { // check quantified expression special case
			if (expr.skipQuanPredicate) {
				expr.skipQuanPredicate = false;
			}
		}

		if (expr.getRawType() == null && expr.expressions().size() > 0) {
			Expression sub = expr.expressions().get(0);
			if (!sub.processed) {
				setType(expr, sub.getRawType());
				sub.processed = true;
			}
		}
//		setExpression(null);

	}

	@Override
	public void beginQuantifiedExpression(ParserRuleContext ctx) {
		super.beginQuantifiedExpression(ctx);
		doBeginComplexExpr();
	}

	@Override
	public void doQuantifiedExpression(ExpressionKind type, QuantifiedExpressionContext ctx) {
		Expression cExpr = (Expression) scope();
		if (cExpr.getKind() != Kind.QUANTIFIED) {
			Tool.error("Error! Expected quantified expression not found!!", null, stitchProblemHandler());
			return;
		}
		if (cExpr.vars().size() > 1) {
			Tool.error("Sorry, only one quantified variable is currently supported! " + cExpr, null,
					stitchProblemHandler());
			return;
		}

		Var v = (Var) cExpr.vars().values().toArray()[0];
		// - the set expression should have values
		String result = cExpr.expressions().get(0).getRawType();
		if (!Expression.SET.equals(result)) {
			Tool.error(MessageFormat.format("Quantifiers must quantify over a set: {0}",
					ParserUtils.formatTree(cExpr.expressions().get(0).tree())), ctx, stitchProblemHandler());
		}

		result = cExpr.expressions().get(1).getType();
		if (!Expression.BOOLEAN.equals(result)) {
			Tool.error(MessageFormat.format("Expected a boolean expression, got a {0}: {1}", result,
					ParserUtils.formatTree(cExpr.expressions().get(1).tree())), ctx, stitchProblemHandler());
		}

		setType(cExpr, ctx.SELECT() != null ? Expression.SET : Expression.BOOLEAN);
	}

	@Override
	public void endQuantifiedExpression(ExpressionKind quant, QuantifiedExpressionContext quantifiedExpressionContext) {
		Expression cExpr = doEndComplexExpr();
		setType(expr(), cExpr.getType());
	}

	@Override
	public void beginPathExpression(ParserRuleContext ctx) {
		doBeginComplexExpr(); // Set up path var rather than in setupeFilter to allow base components to be
								// done
	}

	@Override
	public void beginMethodCallExpression(ParserRuleContext ctx) {
//		super.beginMethodCallExpression();
		doBeginComplexExpr();
	}

	public void endMethodCallExpression(TerminalNode id, StitchParser.MethodCallContext mc) {
		Expression cExpr = doEndComplexExpr();

		String[] args = new String[cExpr.getChildren().size()];
		for (int idx = 0; idx < args.length; idx++) {

			Expression e = (Expression) cExpr.getChildren().get(idx);
			args[idx] = e.getRawType(); // TODO Is a hack. Types should be put in cExpr not Expr
		}

		String name = id.getText();
		int dotIdx = name.indexOf(".");
		Object nameObj = null;
		if (dotIdx > -1) {
			Object n = scope().lookup(name.substring(0, dotIdx));
			if (n instanceof IAcmeElement) {
				nameObj = ((IAcmeElement) n).lookupName(name.substring(dotIdx + 1), true);
			} else if (n instanceof AcmeModelInstance) {
				Class<?> commandFactoryClass = ((AcmeModelInstance) n).getCommandFactory().getClass();
				Method[] methods = commandFactoryClass.getMethods();
				String m = name.substring(dotIdx + 1);
				if (!m.endsWith("Cmd")) {
					m += "Cmd";
				}
				for (int i = 0; i < methods.length && nameObj == null; i++) {
					if (methods[i].getName().equals(m)) {
						nameObj = methods[i];
						m_modelOperationsReferenced.add(name.substring(dotIdx + 1));
						break;
					}
				}

				if (nameObj == null) {
					nameObj = ((AcmeModelInstance) n).getModelInstance().lookupName(name.substring(dotIdx + 1), true);
				}
			}
		} else {
			nameObj = scope().lookup(name);
		}
		if (nameObj == null) {
			dotIdx = name.lastIndexOf(".");
			String methodClass = null;
			if (dotIdx > -1) {
				methodClass = name.substring(0, dotIdx);
				if (script().renames.containsKey(methodClass)) {
					methodClass = script().renames.get(methodClass);
				}
				name = name.substring(dotIdx + 1);
			}
			LinkedHashSet<Class> classesToSearch = new LinkedHashSet<Class>();
			for (Class opClass : m_stitch/* .stitch() */.script.ops) {
				// first, see if method class matches the imported method's class
				if (methodClass != null) {
					if (!opClass.getName().endsWith(methodClass)) {
						// not a match, don't waste time searching its methods
						continue;
					}
				}
				// add to list to search
				classesToSearch.add(opClass);
			}
			if (classesToSearch.size() == 0 && methodClass != null) {
				// attempt to load the method class and search it
				try {
					classesToSearch.add(Class.forName(methodClass));
				} catch (ClassNotFoundException e) {
					if (Tool.logger().isInfoEnabled()) {
						Tool.logger().info(
								"Attempt to load class " + methodClass + " failed while executing method " + name + "!",
								e);
					}
				}
			}
			Method method = null;
			// find this name reference in reduced list of classes
			OUTER: for (Class opClass : classesToSearch) {
				// iterate thru list of declared methods for whose name match,
				// and look to see if supplied param is a proper subtype
				for (Method m : opClass.getDeclaredMethods()) {
					if (m.getName().equals(name)) { // method name matches, check params
						if (Modifier.isStatic(m.getModifiers())) {
							method = m;
							break OUTER;
						} else {
							Tool.error(
									MessageFormat.format(
											"Applicable method for {0} is NOT STATIC; invocation will fail", name),
									null, stitchProblemHandler());
							return;
						}
					}
				}
			}
			if (method == null) {
				// Need to check if there is an effector
				// Ohana.instance().modelOperator().invoke (name, null);
				if (Ohana.instance().modelOperator().lookupOperator(name) == null) {
					Tool.error(MessageFormat.format(
							"{0} : Could not find method {1} in any scopes, and could not verify that"
									+ " it is an operator.",
							new File(m_stitch/* .stitch () */.path).getName(), id.getText()), mc,
							stitchProblemHandler());
				}
			} else {

				Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes.length != args.length) {
					String msg = MessageFormat.format("Expecting {0} argument{1}, got {2} in call to {3}",
							parameterTypes.length, parameterTypes.length > 1 ? "s" : "", args.length, name);
					Tool.error(msg, mc, stitchProblemHandler());
				} else {
					for (int i = 0; i < parameterTypes.length; i++) {
						boolean paramsOk = true;
						Class<?> c = parameterTypes[i];
						switch (args[i]) {
						case Expression.BOOLEAN:
							paramsOk &= c.equals(boolean.class) || c.equals(Boolean.class);
							break;
						case Expression.STRING:
							paramsOk &= c.equals(String.class);
							break;
						case Expression.FLOAT:
							paramsOk &= c.equals(float.class) || c.equals(double.class) || c.equals(Float.class)
									|| c.equals(Double.class);
							break;
						case Expression.INTEGER:
							paramsOk &= c.equals(int.class) || c.equals(long.class) || c.equals(Integer.class)
									|| c.equals(Long.class);
							break;
						case Expression.LONG:
							paramsOk &= c.equals(long.class) || c.equals(Long.class);
							break;
						case Expression.SET:
							paramsOk &= c.equals(Set.class) || c.equals(Collection.class);
							break;
						case Expression.SEQ:
							paramsOk &= c.equals(List.class) || c.equals(Collection.class);
							break;
						case Expression.UNKNOWN:
							Tool.warn(MessageFormat.format(
									"Cannot check compatibility of non-basic types currently: Attempting to pass {0} to {1}",
									args[i], c.getName()), mc, stitchProblemHandler());
						default:
							try {
								Class<?> aClass = Class.forName(args[i]);
								paramsOk &= c.isAssignableFrom(aClass);
							} catch (ClassNotFoundException e) {
								if (Expression.SET.equals(args[i]) && Set.class.isAssignableFrom(c)) {
									paramsOk &= true;
								} else if (Expression.SEQ.equals(args[i]) && List.class.isAssignableFrom(c)) {
									paramsOk &= true;
								} else
									Tool.warn(MessageFormat.format(
											"Cannot check compatibility of non-basic types currently: Attempting to pass {0} to {1}",
											args[i], c.getName()), mc, stitchProblemHandler());
							}
						}
						if (!paramsOk) {
							String msg = MessageFormat.format(
									"Attempting to pass ''{0}'', expecting ''{1}'' as parameter {3} in {2}", args[i],
									c.getName(), ParserUtils.formatTree(mc), i);
							Tool.error(msg, mc, stitchProblemHandler());
						}
					}
				}
				String et = javaTypeToExpressionType(method.getReturnType());
				if (et == Expression.UNKNOWN) {
					String msg = MessageFormat.format(
							"Unhandled type ''{0}''. Stitch function calls to Java currently only handle primitive return types",
							method.getReturnType().getName());
					Tool.error(msg, mc, stitchProblemHandler());
				}
				setType(cExpr, et);
//				String msg = MessageFormat.format(
//						"Warning: Call to {0} was not typechecked because it is not implemented yet", method.getName());
//				Tool.warn(msg, mc, stitchProblemHandler());
			}

		} else if (nameObj instanceof IAcmeDesignAnalysis) {
			IAcmeDesignAnalysis da = (IAcmeDesignAnalysis) nameObj;
			checkDesignAnalysisCall(mc, args, name, da);
			setType(cExpr, nameOfType(AcmeTypeHelper.extractTypeStructure(da.getResultTypeReference().getType())));
		} else if (nameObj instanceof AcmeRootDesignAnalysis) {
			AcmeRootDesignAnalysis ra = (AcmeRootDesignAnalysis) nameObj;
			checkDesignAnalysisCall(mc, args, name, ra.getDesignAnalysis());
			setType(cExpr, nameOfType(
					AcmeTypeHelper.extractTypeStructure(ra.getDesignAnalysis().getResultTypeReference().getType())));
		}
	}

	private String javaTypeToExpressionType(Class<?> t) {
		if (t == null)
			return Expression.UNKNOWN;
		if (t.equals(int.class) || t.equals(Integer.class))
			return Expression.INTEGER;
		if (t.equals(boolean.class) || t.equals(Boolean.class))
			return Expression.BOOLEAN;
		if (t.equals(float.class) || t.equals(Float.class) || t.equals(Double.class) || t.equals(double.class))
			return Expression.FLOAT;
		if (t.equals(char.class) || t.equals(Character.class))
			return Expression.CHAR;
		if (t.equals(void.class))
			return "void";
		if (t == Set.class || Set.class.isAssignableFrom(t))
			return Expression.SET;
		if (t == List.class || List.class.isAssignableFrom(t))
			return Expression.SEQ;
		return Expression.UNKNOWN;
	}

	protected void checkDesignAnalysisCall(StitchParser.MethodCallContext mc, String[] args, String name,
			IAcmeDesignAnalysis da) {
		List<FormalParameterNode> fp = da.getFormalParameters();
		if (fp.size() != args.length) {
			String msg = MessageFormat.format("Expecting {0} argument{1}, got {2} in call to {3}", fp.size(),
					fp.size() > 1 ? "s" : "", args.length, name);
			Tool.error(msg, mc, stitchProblemHandler());
		} else {
			for (int i = 0; i < fp.size(); i++) {
				IAcmeType fType = AcmeTypeHelper.extractTypeStructure(fp.get(0).getType());
				if (!typesCompatible(fType, args[i])) {
					String msg = MessageFormat.format(
							"Incompatible arguments in call to {0}: Got {1} expecting {2} for argument {3}", name,
							args[i], nameOfType(fType), i + 1);
					Tool.error(msg, mc, stitchProblemHandler());
				}
			}
		}
	}

	private String nameOfType(IAcmeType t) {
		if (t instanceof IAcmeSequenceType)
			return Expression.SEQ;
		if (t instanceof IAcmeSetType)
			return Expression.SET;
		if (t instanceof IAcmeIntType)
			return Expression.INTEGER;
		if (t instanceof IAcmeFloatType || t instanceof IAcmeDoubleType)
			return Expression.FLOAT;
		if (t instanceof IAcmeStringType)
			return Expression.STRING;
		if (t instanceof IAcmeBooleanType)
			return Expression.BOOLEAN;
		return Expression.UNKNOWN;
	}

	private boolean typesCompatible(IAcmeType type, String st) {
		if (type instanceof IAcmeStringType && Expression.STRING.equals(st))
			return true;
		if (type instanceof IAcmeBooleanType && Expression.BOOLEAN.equals(st))
			return true;
		if (type instanceof IAcmeSetType && Expression.SET.equals(st))
			return true;
		if (type instanceof IAcmeSequenceType && Expression.SEQ.equals(st))
			return true;
		if (type instanceof IAcmeIntType && (Expression.INTEGER.equals(st) || Expression.LONG.equals(st)))
			return true;
		if (type instanceof IAcmeFloatType && isNumber(st))
			return true;
		return false;
	}

	@Override
	public void beginStatement(StatementKind stmtAST, ParserRuleContext ctx) {
		if (stmtAST == StatementKind.VAR_DEF) {
			if (expr() == null) {
				IScope scope = scope();
				VarContext varContext = (StitchParser.VarContext) ctx;
				Var v;
				if (scope.isRoot()) {
					v = script().vars().get(varContext.IDENTIFIER().getText());
				} else {
					v = (Var) scope.lookup(varContext.IDENTIFIER().getText());
				}
				if (v != null) {
					Expression expr = (Expression) v.valStmt.getChildren().get(0);
					setExpression(expr);
					pushScope(v.scope);
				}
			}
		}
	}

	@Override
	public void endStatement(StatementKind stmtAST, ParserRuleContext ctx) {
		if (stmtAST == StatementKind.VAR_DEF) {
			Expression expr1 = expr();
			IScope scope = scope();
//			if (scope.parent() == stitch().script) {
			popScope();
			setExpression(null);
//			}
//			else {
//				int i = 0;
//			}
		}
	}

	@Override
	public void beginSetExpression(ParserRuleContext ctx) {
		doBeginComplexExpr();
	}

	@Override
	public void endSetExpression(SetExpressionContext setAST) {
		Expression cExpr = doEndComplexExpr();
		String settype = StitchTypes.SET;
		setType(cExpr, settype);

	}

//	@Override
//	public void endSetExpression(StitchParser.SetExpressionContext setAST) {
//		super.endSetExpression(setAST);
//	}

	@Override
	public void doExpression(ParserRuleContext exprAST) {
//		if (scope() instanceof Expression) {
//			Expression expr = (Expression) scope();
//			if (expr.getType() == null && expr.expressions().size() > 0) {
//				// transfer children result up
//				Expression sub = expr.expressions().get(0);
//				if (sub.getType() != Expression.UNKNOWN)
//					setType(expr, sub.getType());
//			}
//		}
	}

	@Override
	public void doAssignExpression(ParserRuleContext identifier, ParserRuleContext expression) {
		Expression expr = expr();
		expr.lrOps[Expression.LOP].pop();
		Object rType = expr.lrOps[Expression.ROP].pop();
		// the lVal can only be an identifier, otherwise, won't make sense!
		Object lObj = expr.lookup(identifier.getText());
		if (lObj != null && lObj instanceof Var) {
			Var v = (Var) lObj;
			if (v.getType() != null) {
				if (!v.getType().equals(rType)) {
					String msg = MessageFormat.format("Type mismatch for {0}: Attempting to assign ''{1}'' to ''2''",
							v.name, rType, v.getType());
					Tool.error(msg, expression, stitchProblemHandler());
				}
			}

		} else {
			// can't find lvalue reference
			Tool.error("Assignment expression cannot be evaluated because lvalue reference cannot be found! "
					+ expression.getText(), expression, stitchProblemHandler());
			return;
		}
	}

//	private Expression doEndComplexExpr() {
//		Expression cExpr = (Expression) scope();
//		if (expr.parent)
//		setExpression((Expression) cExpr.parent());
//		popScope();
//		return cExpr;
//	}

	@Override
	public void setupPathFilter(TerminalNode identifier) {
		Expression expr = expr();
		IScope scope = scope();
		if (identifier != null) {
			Var v = new Var();
			v.name = "__path_filter_type";
			v.setType(identifier.toString());
			scope().addVar(v.name, v);
			v.scope = scope();
			v.computeClass();
		}
	}

	@Override
	public boolean pathExpressionFilter(TypeFilterT filter, TerminalNode identifier, ExpressionContext expression) {
		Expression cExpr = (Expression) scope();
		if (cExpr.getKind() != Kind.PATH) {
			Tool.error("Error! Expected path expression not found!", null, stitchProblemHandler());
			return false;
		}

		pathVariable.set(new Var()); // TODO: Remove Path filter creation from here
		Var var = pathVariable.get();
		var.name = "___Path_Filter";
		var.scope = scope();

		if (identifier != null) {
			var.setType(identifier.getText());
			Class clazz = var.computeClass();
			if (clazz == null) {
				String msg = MessageFormat.format("Unknown type: {0} in {1}", identifier.getText(),
						ParserUtils.formatTree(cExpr.tree()));
				Tool.error(msg, expression.getParent(), stitchProblemHandler());
				return false;
			}
		}
		List<Expression> expressions = cExpr.expressions();
//		setType(cExpr,pathVariable.get().getT);
		if (expression != null) {
			scope().addVar(var.name, var);
			exprIndex.set(1);
			Expression expr = expressions.get(exprIndex.get());
			expr.evaluate(null, m_walker);
			if (expr.getRawType() != Expression.BOOLEAN) {
				String msg = MessageFormat.format("Path filter expression must be boolean: {0} has type {1}",
						ParserUtils.formatTree(expr.tree()), expr.getRawType());
				Tool.error(msg, expression.getParent(), stitchProblemHandler());
			}
			expr.skipQuanPredicate = true;
//			scope().vars().remove(var.name);
			return true;
		}
		return false;
	}

	@Override
	public void continueExpressionFilter(TypeFilterT filter, TerminalNode setIdentifier, TerminalNode typeIdentifier,
			ExpressionContext expression, boolean mustBeSet, boolean resultisSet) {
		if (mustBeSet && !resultisSet) {
			Tool.error("Sequence spreads (...) should only appear on the last continuance", expression,
					stitchProblemHandler());
			return;
		}

		Expression cExpr = (Expression) scope();
		Var var = pathVariable.get();
		Object o = null;
		if (var.typeObj != null) {
			String text = setIdentifier.getText();
			o = var.typeObj.lookupName(text);
			if (o == null) {
				o = var.typeObj.getPrototype().lookupName(text);
				if (o == null) {
					String msg = MessageFormat.format("Unknown attribute: {0} is not a valid attribute of {1}", text,
							var.getType());
					Tool.error(msg, (ParserRuleContext) setIdentifier.getParent(), stitchProblemHandler());
				} else {
					if (o instanceof IAcmeProperty) {

					} else {
						setVarTypeBasedOnAcmeCollection(var, text);
					}
				}
				return;
			}
		} else {
			// if pathVariable is null, we're really screwed
			return;
		}
		if (typeIdentifier != null) {
			var.setType(typeIdentifier.toString());
			var.typeObj = null;
			Class clazz = var.computeClass();
			if (clazz == null) {
				String msg = MessageFormat.format("Unknown type: {0} in {1}", typeIdentifier.getText(),
						ParserUtils.formatTree(cExpr.tree()));
				Tool.error(msg, expression.getParent(), stitchProblemHandler());
				return;
			}
		}

		if (expression != null) {
			exprIndex.set(exprIndex.get() + 1);
			Expression expr = cExpr.expressions().get(exprIndex.get());
			expr.evaluate(null, m_walker);
			Tool.warn(
					MessageFormat.format("Warning: path expressions with more than one continuation is untestsed: {0}",
							ParserUtils.formatTree(cExpr.tree())),
					expression.getParent(), stitchProblemHandler());
		}

		if (resultisSet)
			setType(cExpr, Expression.SET);
		else
			setType(cExpr, Expression.SEQ);
	}

	protected void setVarTypeBasedOnAcmeCollection(Var var, String text) {
		switch (text.toLowerCase()) {
		case "ports":
			var.typeObj = DefaultAcmeModel.defaultPortType();
			break;
		case "roles":
			var.typeObj = DefaultAcmeModel.defaultRoleType();
			break;
		case "components":
			var.typeObj = DefaultAcmeModel.defaultComponentType();
			break;
		case "connectors":
			var.typeObj = DefaultAcmeModel.defaultConnectorType();
			break;
		}
	}

	final ThreadLocal<Var> pathVariable = new ThreadLocal<Var>() {
		@Override
		protected Var initialValue() {
			return null;
		}
	};

	final ThreadLocal<Integer> exprIndex = new ThreadLocal<Integer>() {
		protected Integer initialValue() {
			return -1;
		}
	};

	@Override
	public void endPathExpression(PathExpressionContext ctx) {
		pathVariable.set(null);
		scope().vars().remove("__Path_Filter");
		Expression cExpr = doEndComplexExpr();
		setType(expr(), cExpr.getRawType() == null ? Expression.SET : cExpr.getRawType());
	}

	@Override
	public void doRelationalExpression(ExpressionKind kind, ParserRuleContext ctx) {
		Expression expr = expr();

		if (expr.lrOps[Expression.LOP].isEmpty() || expr.lrOps[Expression.ROP].isEmpty()
				|| expr.lrOps[Expression.LOP].peek() == null || expr.lrOps[Expression.ROP].peek() == null) {
			// if either is NULL, result is NULL
			final String msg = "One relational operand is NULL:  ... " + ParserUtils.formatTree(ctx);
			Tool.warn(msg, ctx, stitchProblemHandler());
			setType(expr, Expression.BOOLEAN);
			return;
		}

		String lOp = (String) expr.lrOps[Expression.LOP].pop();
		String rOp = (String) expr.lrOps[Expression.ROP].pop();
		if (kind == ExpressionKind.EQ || kind == ExpressionKind.NE) {
			if (lOp.equals(rOp)) {
				setType(expr, Expression.BOOLEAN);
			} else if (isNumber(lOp) && isNumber(rOp)) {
				setType(expr, Expression.BOOLEAN);
			} else {
				Tool.error(MessageFormat.format("Cannot do compare the types in expression {0} {1} {2}", lOp,
						kind.image(), rOp), ctx, stitchProblemHandler());
				setType(expr, Expression.BOOLEAN);
			}
		} else if (isNumber(lOp) && isNumber(rOp)) {
			setType(expr, Expression.BOOLEAN);
		} else {
			Tool.error(MessageFormat.format("Cannot do compare the types in expression {0} {1} {2}", lOp, kind.image(),
					rOp), ctx, stitchProblemHandler());
			setType(expr, Expression.BOOLEAN);
		}
//		if (!lOp.equals(rOp)) {
//			if (!lOp.equals(Expression.UNKNOWN) && !rOp.equals(Expression.UNKNOWN)) {
//				Tool.error(MessageFormat.format("Incompatibale types: {0} {1} {2}", lOp, kind.image(),rOp), opAST, stitchProblemHandler());
//			}
//		}
		setType(expr, Expression.BOOLEAN);

	}

	@Override
	public void doUnaryExpression(ExpressionKind opAST, UnaryExpressionContext ctx) {
		Expression expr = expr();
		Object pType = expr.lrOps[Expression.LOP].peek();
		if (pType == null || Expression.UNKNOWN.equals(pType)) {
			Tool.error("Unary operand is null: " + expr.lrOps[Expression.LOP].pop(), ctx, stitchProblemHandler());
			setType(expr, opAST == Strategy.ExpressionKind.NOT ? Expression.BOOLEAN : Expression.UNKNOWN);
		}
		String cType = (String) expr.lrOps[Expression.LOP].pop();
		switch (opAST) {
		case NOT:
			setType(expr, Expression.BOOLEAN);
			break;
		case DECR:
		case INCR:
		case UNARY_MINUS:
		case UNARY_PLUS:
			setType(expr, cType);
			break;
		default:
			Tool.error("Unknown unary operand: " + opAST, ctx, stitchProblemHandler());
			expr.lrOps[Expression.LOP].push(Expression.UNKNOWN);
		}

	}

	@Override
	public void doArithmeticExpression(ExpressionKind kind, ParserRuleContext ctx) {
		Expression expr = expr();
		if (Expression.STRING.equals(expr.lrOps[Expression.LOP].peek()) && kind == ExpressionKind.PLUS) {
			expr.lrOps[Expression.LOP].pop();
			expr.lrOps[Expression.ROP].pop();
			setType(expr, Expression.STRING);
			return;
		}

		String lOp = expr.lrOps[Expression.LOP].peek() != null ? (String) expr.lrOps[Expression.LOP].pop()
				: Expression.UNKNOWN;
		String rOp = expr.lrOps[Expression.ROP].peek() != null ? (String) expr.lrOps[Expression.ROP].pop()
				: Expression.UNKNOWN;

		switch (lOp) {
		case Expression.UNKNOWN:
			switch (rOp) {
			case Expression.UNKNOWN:
				setType(expr, lOp);
				break;
			case Expression.FLOAT:
			case Expression.LONG:
			case Expression.INTEGER:
			case "int":
				switch (kind) {
				case PLUS:
				case MINUS:
				case MULTIPLY:
					setType(expr, rOp);
					break;
				default:
					Tool.error(MessageFormat.format("Cannot deterimine type for operation: {0} {1} {2}", lOp,
							kind.image(), rOp), ctx, stitchProblemHandler());
				}
				break;
			case Expression.STRING:
				if (kind == ExpressionKind.PLUS)
					setType(expr, Expression.STRING);
				else {
					Tool.error(MessageFormat.format("Cannot use string on right hand of ''{0}''", kind.image()), ctx,
							stitchProblemHandler());
					setType(expr, Expression.UNKNOWN);
				}
			}
			break;
		case Expression.FLOAT:
			switch (kind) {
			case PLUS:
			case MINUS:
			case DIVIDE:
			case MULTIPLY:
			case MOD:
				switch (rOp) {
				case Expression.STRING:
				case Expression.BOOLEAN:
				case Expression.CHAR:
				case Expression.SEQ:
				case Expression.SET: {
					Tool.error(MessageFormat.format("Cannot use {1} on right hand of ''{0}''", kind.image(), rOp), ctx,
							stitchProblemHandler());
					setType(expr, Expression.UNKNOWN);
					break;
				}
				default:
					setType(expr, lOp);

				}
			}
			break;
		case Expression.LONG:
			switch (kind) {
			case PLUS:
			case MINUS:
			case DIVIDE:
			case MULTIPLY:
			case MOD:
				switch (rOp) {
				case Expression.LONG:
				case Expression.INTEGER:
				case Expression.UNKNOWN:
					setType(expr, Expression.LONG);
					break;
				case Expression.FLOAT:
					setType(expr, Expression.FLOAT);
					break;
				default:
					Tool.error(MessageFormat.format("Cannot use {1} on right hand of ''{0}''", kind.image(), rOp), ctx,
							stitchProblemHandler());
					setType(expr, Expression.UNKNOWN);
					break;
				}
				break;
			}
			break;
		case Expression.INTEGER:
		case "int":
			switch (kind) {
			case PLUS:
			case MINUS:
			case DIVIDE:
			case MULTIPLY:
			case MOD:
				switch (rOp) {
				case Expression.STRING:
				case Expression.BOOLEAN:
				case Expression.CHAR:
				case Expression.SEQ:
				case Expression.SET:
					Tool.error(MessageFormat.format("Cannot use {1} on right hand of ''{0}''", kind.image(), rOp), ctx,
							stitchProblemHandler());
					setType(expr, Expression.UNKNOWN);
					break;
				default:
					setType(expr, rOp);
				}

			}
			break;
		default:
			Tool.error(MessageFormat.format("Cannot use {0} in an arithmetic expression: {0} {1} {2}", lOp,
					kind.image(), rOp), ctx, stitchProblemHandler());
			setType(expr, Expression.UNKNOWN);
		}
	}

	@Override
	public void doLogicalExpression(ExpressionKind kind, ParserRuleContext ctx) {
		Expression expr = expr();

		String lOp = expr.lrOps[Expression.LOP].peek() != null ? (String) expr.lrOps[Expression.LOP].pop()
				: Expression.UNKNOWN;
		String rOp = expr.lrOps[Expression.ROP].peek() != null ? (String) expr.lrOps[Expression.ROP].pop()
				: Expression.UNKNOWN;

		setType(expr, Expression.BOOLEAN);
		boolean error = false;
		switch (lOp) {
		case Expression.BOOLEAN:
		case Expression.UNKNOWN:
			break;
		default:
			error = true;
		}

		switch (rOp) {
		case Expression.BOOLEAN:
		case Expression.UNKNOWN:
			break;
		default:
			error = true;
		}

		if (error) {
			Tool.error(
					MessageFormat.format("Incompatible types for {3}: Cannot perform operation on {0} {1} {2}", lOp,
							kind.image(), rOp, ParserUtils.formatTokens(ctx.getStart(), ctx.getStop())),
					ctx, stitchProblemHandler());
		}

	}

	protected boolean isNumber(String type) {
		if (type == null)
			return false;
		switch (type) {
		case Expression.FLOAT:
		case Expression.LONG:
		case Expression.INTEGER:
		case "int":
			return true;
		}
		return false;

	}

	protected void setType(Expression expr, String type) {
		if (expr.curOp.size() == 0)
			expr.setType(type);
		else
			expr.lrOps[expr.curOp.pop().intValue()].push(type);
	}

//	@Override
//	public void doImports() {
//		resolveImports();
//	}

	private void doBeginComplexExpr() {
		Expression expr = expr();
		if (expr == null && scope() instanceof Expression)
			expr = (Expression) scope();
		Expression cExpr = null;
		if (expr.expressions().size() > 0) {
			Expression nextExpr = expr.expressions().get(expr.curExprIdx++);
			if (nextExpr.isComplex()) {
				cExpr = nextExpr;
			} else if (expr.isComplex()) {
				cExpr = expr;
			}
		} else {
			if (expr.isComplex()) {
				// this SHOULD be the case, by nature of tree walk
				cExpr = expr;
			}
		}

		if (cExpr != null) {
			// reset expr count for descending into element exprs
			cExpr.curExprIdx = 0;
			setExpression(null);
			pushScope(cExpr);
		}
	}

	private Expression doEndComplexExpr() {
		Expression cExpr = (Expression) scope();
		setExpression((Expression) cExpr.parent());
		popScope();
		return cExpr;
	}

//	private void resolveImports() {
//		for (Import imp : m_stitch/* .stitch() */.script.imports) {
//			if (imp.type == Import.Kind.LIB) {
//				// read in the script and add its imports, tactics
//				// and strategies into this script's scope
//				File f = determinePath(imp.path);
//				try {
//					Stitch stitch = Ohana.instance().findStitch(f.getAbsolutePath());
//					if (stitch == null) { // imported script file not previously
//						// parsed
//						stitch = Stitch.newInstance(f.getAbsolutePath(), stitchProblemHandler());
//						stitch.setBehavior(Stitch.SCOPER_PASS, null /* new StitchTypechecker(stitch) */);
//						Ohana.instance().parseFile(stitch);
//						// BRS: Added so that tactics aren't imported twice
////	                        Ohana.instance ().storeStitch (f.getAbsolutePath (), stitch);
//					}
//					m_stitch/* .stitch() */.script.renames.putAll(stitch.script.renames);
//					m_stitch/* .stitch() */.script.tactics.addAll(stitch.script.tactics);
//					m_stitch/* .stitch() */.script.strategies.addAll(stitch.script.strategies);
//					m_stitch./* stitch(). */script.models.addAll(stitch.script.models);
//					m_stitch./* stitch(). */script.ops.addAll(stitch.script.ops);
//					m_stitch.stitchProblemHandler.addAll(stitch.stitchProblemHandler.unreportedProblems());
//				} catch (IOException e) {
//					// ALI: Modified
//					Tool.error("Cannot find library file '" + imp.path + "' to import!", e, null,
//							stitchProblemHandler());
//					e.printStackTrace();
//				}
//			} else if (imp.type == Import.Kind.ACME) {
//				try {
//					IAcmeResource resource = StandaloneResourceProvider.instance().acmeResourceForString(imp.path);
//					AcmeModelInstance m = new StitchImportedDirectAcmeModelInstance(
//							resource.getModel().getSystems().iterator().next(), imp.path);
//					m_stitch.script.models.add(m);
//				} catch (ParsingFailureException | IOException e) {
//					Tool.error("Could not import Acme from " + imp.path, null, stitchProblemHandler());
//				}
//			} else if (imp.type == Import.Kind.MODEL) {
//				if (Rainbow.instance().getRainbowMaster().modelsManager() != null) {
//					ModelReference model = Util.decomposeModelReference(imp.path);
//					if (model.getModelType() == null) {
//
//						model = new ModelReference(imp.path.split("\\.")[0], "Acme");
//					}
//					Object o = Rainbow.instance().getRainbowMaster().modelsManager().getModelInstance(model);
//					if (o instanceof AcmeModelInstance) {
//						AcmeModelInstance ami = (AcmeModelInstance) o;
//						m_stitch./* stitch(). */script.models.add(ami);
//					} else {
//						Tool.warn("Could not import unknown model " + imp.path, null, stitchProblemHandler());
//					}
//				}
//				/*
//				 * else if (Ohana2.instance ().modelRepository () != null) { // TODO: Assumes
//				 * that we run on same VM as the models manager
//				 * 
//				 * try { Object o = Rainbow.instance ().getRainbowMaster ().modelsManager ()
//				 * .getModelInstanceByResource (determinePath (imp.path).getCanonicalPath ());
//				 * 
//				 * if (o instanceof IAcmeModel) { IAcmeModel model = (IAcmeModel )o; //
//				 * m_stitch.script.models.add (model); throw new NotImplementedException
//				 * ("Need to implement this for standard files"); } else if (o instanceof
//				 * AcmeModelInstance) { AcmeModelInstance s = (AcmeModelInstance )o;
//				 * m_stitch.script.models.add (s); } else { Tool.warn
//				 * ("No AcmeModel loaded, perhaps a dummy repository is used?!", null,
//				 * stitchProblemHandler ()); } } catch (IOException e) { Tool.warn
//				 * ("Could not import model from '" + imp.path + "'", null, stitchProblemHandler
//				 * ()); } }
//				 */
//				else {
//					Tool.error("Model repository not set yet! Unable to load model file " + imp.path, null,
//							stitchProblemHandler());
//				}
//			} else if (imp.type == Import.Kind.OP) {
//				// import a file of operator, or a path to a package of classes,
//				// as in Java's import statement: import java.util.*;
//				// TODO: support JAR file later
//
//				Class<?>[] classes = null;
//				// see if name is a package, i.e., ends in *
//				if (imp.path.endsWith(".*")) { // yes, list classes in package
//					String pkgname = imp.path.substring(0, imp.path.length() - 2);
//					try {
//						classes = Util.getClasses(pkgname);
//					} catch (ClassNotFoundException e) {
//						Tool.warn("Package name in OP import appears invalid: " + pkgname, e, imp.tree,
//								stitchProblemHandler());
//						continue;
//					}
//				} else {
//
//					// Treat imported name as name of class
//					classes = new Class<?>[1];
//					String className = imp.path;
//					if (className.endsWith(".class")) { // truncate
//						className = className.substring(0, className.length() - 6);
//					}
//					String packageName = imp.path.substring(0, imp.path.lastIndexOf("."));
//					List<ClassLoader> classLoaderList = new LinkedList<ClassLoader>();
//					classLoaderList.add(ClasspathHelper.contextClassLoader());
//					classLoaderList.add(ClasspathHelper.staticClassLoader());
//					Reflections reflections = new Reflections(new ConfigurationBuilder()
//							.setScanners(new SubTypesScanner(false), new ResourcesScanner())
//							.setUrls(ClasspathHelper.forClassLoader(classLoaderList.toArray(new ClassLoader[0])))
//							.filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(packageName))));
//					Set<Class<?>> foundClasses = reflections.getSubTypesOf(Object.class);
//					for (Class<?> candidate : foundClasses) {
//						String name = candidate.getName();
//						if (name.equals(className)) {
//							classes[0] = candidate;
//							break;
//						}
//					}
//					if (classes[0] == null) {
//						Tool.warn("Class name in OP import appears invalid: " + className, null, imp.tree,
//								stitchProblemHandler());
//						continue;
//					}
//				}
//				StringBuffer mStr = new StringBuffer("[ "); // for debug
//				if (classes != null) {
//					for (Class<?> clazz : classes) {
//						// store class object
//						m_stitch./* stitch(). */script.ops.add(clazz);
//						// build list of accessible methods and method meta info
//						Method[] methods = clazz.getDeclaredMethods();
//						for (Method m : methods) {
//							mStr.append(m.getName()).append(" ");
//						}
//					}
//				}
//				Tool.logger().debug("Imported library has methods: " + mStr.append("]"));
//			}
//		}
//	}
}
