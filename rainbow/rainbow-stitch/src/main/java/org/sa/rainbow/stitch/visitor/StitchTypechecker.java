package org.sa.rainbow.stitch.visitor;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.core.IAcmeType;
import org.acmestudio.acme.core.type.IAcmeBooleanType;
import org.acmestudio.acme.core.type.IAcmeDoubleType;
import org.acmestudio.acme.core.type.IAcmeFloatType;
import org.acmestudio.acme.core.type.IAcmeIntType;
import org.acmestudio.acme.core.type.IAcmeSequenceType;
import org.acmestudio.acme.core.type.IAcmeSetType;
import org.acmestudio.acme.element.IAcmeElement;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.type.AcmeTypeHelper;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.core.Expression;
import org.sa.rainbow.stitch.core.IScope;
import org.sa.rainbow.stitch.core.MyDouble;
import org.sa.rainbow.stitch.core.MyInteger;
import org.sa.rainbow.stitch.core.Statement;
import org.sa.rainbow.stitch.core.StitchTypes;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Var;
import org.sa.rainbow.stitch.core.Expression.Kind;
import org.sa.rainbow.stitch.core.Strategy.ExpressionKind;
import org.sa.rainbow.stitch.parser.StitchParser;
import org.sa.rainbow.stitch.parser.StitchParser.DataTypeContext;
import org.sa.rainbow.stitch.parser.StitchParser.ExpressionContext;
import org.sa.rainbow.stitch.parser.StitchParser.PathExpressionContext;
import org.sa.rainbow.stitch.parser.StitchParser.QuantifiedExpressionContext;
import org.sa.rainbow.stitch.parser.StitchParser.SetExpressionContext;
import org.sa.rainbow.stitch.parser.StitchParser.UnaryExpressionContext;
import org.sa.rainbow.stitch.util.Tool;

/**
 * Created by schmerl on 10/3/2016.
 */
public class StitchTypechecker extends StitchScopeEstablisher {

	public Set<String> m_modelOperationsReferenced = new HashSet<>();
	StitchBeginEndVisitor m_walker;

	public StitchTypechecker(Stitch/* State */ stitch) {
		super(stitch);
	}

	@Override
	public void setWalker(StitchBeginEndVisitor walker) {
		m_walker = walker;
	}

	@Override
	public void doIdentifierExpression(ParserRuleContext idAST, Strategy.ExpressionKind kind) {
		storeExprTree(idAST);
		Expression expr = (Expression) scope();
		expr.setKind(translateExpressionKind(kind));
		String id = idAST.getText();
		expr.setName(id);
		Object o = scope().lookup(id);
		if (o != null && o instanceof Var) {
			expr.addRefdVar((Var) o);
		}
		if (o == null) {
			Tool.error("Unresolved reference '" + id + "'! Perhaps model not accessible?", idAST,
					stitchProblemHandler());
		} else {
			switch (expr.getKind()) {
			case ASSIGNMENT:
				setType(expr, "void");
				break;
			case BOOLEAN:
			case LOGICAL:
			case QUANTIFIED:
			case RELATIONAL:
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
				setType(expr,Expression.INTEGER);
				break;
			case ARITHMETIC:
				setType(expr, Expression.UNKNOWN);
				break;
			case IDENTIFIER: {
				if (o == null)
					setType(expr, Expression.UNKNOWN);
				else if (o instanceof Var) {
					setType(expr, ((Var) o).getType());
				} else if (o instanceof IAcmeProperty) {
					setType(expr, expr.getTypeFromAcme((IAcmeProperty) o));

				} else
					setType(expr,Expression.UNKNOWN);
			}
			}

		}
//		super.doIdentifierExpression(idAST, kind);
//		Expression expr = expr();
//		if (kind == Strategy.ExpressionKind.IDENTIFIER) {
//			// find identifier, lookup entire string first
//			String iden = idAST.getText();
//			Object o = null;
//			if (scope().lookup("__path_variable") != null) {
//				expr.setType(StitchTypes.UNKNOWN);
//				return;
////				Object pv = scope().lookup("__path_variable");
////				if (pv instanceof Var) {
////					pv = ((Var) pv).getValue();
////				}
////				if (pv instanceof IAcmeElement) {
////					o = ((IAcmeElement) pv).lookupName(iden);
////				} else if (pv instanceof IAcmeRecordValue) {
////					o = ((IAcmeRecordValue) pv).getField(iden);
////					if (o instanceof IAcmePropertyValue) {
////						o = ModelHelper.propertyValueToJava((IAcmePropertyValue) o);
////					}
////				}
//			} else if (scope().lookup("__path_filter_type") != null) {
//				expr.setType(StitchTypes.UNKNOWN);
//				Var pv = (Var) scope().lookup("__path_filter_type");
////				if (pv.typeObj != null) {
////					Object o1 = pv.typeObj.lookupName(iden);
////					if (o1 == null) {
////						Tool.error("Unresolved reference '" + iden + "'! Perhaps model not accessible?", idAST,
////								stitchProblemHandler());
////					}
////					return;
////				}
//				return;
//
//			}
//			if (o == null)
//				o = scope().lookup(iden);
//			if (o == null) { // break up dot notation
//				int dotIdx = iden.indexOf(".");
//				if (dotIdx > -1) { // looking for v.something
//					o = scope().lookup(iden.substring(0, dotIdx));
//					if (o != null && o instanceof Var) {
//						Var v = (Var) o;
//						// find idx sub within object's scope
//						o = v.scope.lookup(iden.substring(dotIdx + 1));
//						if (o == null) {
//							// treat var as model element and access rest as its
//							// attribute
//							String dotVal = iden.substring(dotIdx);
//							Object val = v.getValue();
//							if (val instanceof IAcmeElement) {
//								IAcmeElement elem = (IAcmeElement) val;
//								o = scope().lookup(elem.getQualifiedName() + dotVal);
//								if (o == null) {
//									// this may mean an invalid reference to
//									// element attribute
//									Tool.error("Invalid reference '" + iden + "' encountered!", idAST,
//											stitchProblemHandler());
//								}
//							} else {
//								o = scope().lookup(v.name + dotVal);
//							}
//							// if (o != null && o instanceof IAcmeProperty) {
//							// get the Acme Property value
//							// o = Tool.deriveValue(((IAcmeProperty
//							// )o).getValue());
//							// }
//						}
//					}
//				}
//			}
//			if (o == null) {
//				int dotIdx = iden.lastIndexOf(".");
//				String methodClass = null;
//				if (dotIdx > -1) {
//					methodClass = iden.substring(0, dotIdx);
//					// mangle any method class renaming
//					if (script().renames.containsKey(methodClass)) { // replace
//						methodClass = script().renames.get(methodClass);
//					}
//					iden = iden.substring(dotIdx + 1);
//				}
//				int i;
//
//				// construct list of classes in which to search for method name,
//				// look in imports
//				List<Class> classesToSearch = new ArrayList<Class>();
//				for (Class opClass : m_stitch./* stitch (). */script.ops) {
//					// first, see if method class matches the imported method's
//					// class
//					if (methodClass != null) {
//						if (!opClass.getName().endsWith(methodClass)) {
//							// not a match, don't waste time searching its methods
//							continue;
//						}
//					}
//					// add to list to search
//					classesToSearch.add(opClass);
//				}
//				if (classesToSearch.size() == 0 && methodClass != null) {
//					// attempt to load the method class and search it
//					try {
//						classesToSearch.add(Class.forName(methodClass));
//					} catch (ClassNotFoundException e) {
//						if (Tool.logger().isInfoEnabled()) {
//							Tool.logger().info("Attempt to load class " + methodClass
//									+ " failed while executing method " + iden + "!", e);
//						}
//					}
//				}
//
//				// find this name reference in reduced list of classes
//				OUTER: for (Class fClass : classesToSearch) {
//					for (Field f : fClass.getDeclaredFields()) {
//						if (f.getName().equals(iden)) {
//							if (Modifier.isStatic(f.getModifiers())) {
//								o = f;
//								break OUTER;
//							} else {
//								Tool.error("Reference field " + iden + " is not STATIC", null, stitchProblemHandler());
//							}
//						}
//					}
//				}
//				// lookup of various combo failed, could indicate invalid
//				// reference
//				if (o == null)
//					Tool.error("Unresolved reference '" + iden + "'!", idAST,
//							stitchProblemHandler());
//				else {
//					if (o instanceof Field) {
//						Field field = (Field) o;
//						try {
//							Object rv = field.getType();
//							if (rv == Integer.class)
//								expr.setType(StitchTypes.INTEGER);
//							else if (rv == Long.class) 
//								expr.setType(StitchTypes.LONG);
//							else if (rv == Float.class || rv == Double.class) 
//								expr.setType(StitchTypes.FLOAT);
//							else 
//								expr.setType(rv.getClass().getCanonicalName());
//							
//						} catch (IllegalArgumentException e) {
//							Tool.error("Reference field " + iden + " is not STATIC", null, stitchProblemHandler());
//						}
//					} else
//						Tool.error("Unresolved reference '" + iden + "'! Perhaps model not accessible?", idAST,
//								stitchProblemHandler());
//
//				}
//			} else {
//				if (o instanceof Var) {
//					Var v = (Var) o;
//					expr.setType(((Var) o).getType());
//				} else if (o instanceof IAcmeProperty) {
//					IAcmeType pt = ((IAcmeProperty )o).getType();
//					IAcmeType t = AcmeTypeHelper.extractTypeStructure(pt);
//					if (t instanceof IAcmeIntType) 
//						expr.setType(StitchTypes.INTEGER);
//					else if (t instanceof IAcmeFloatType || t instanceof IAcmeDoubleType)
//						expr.setType(StitchTypes.FLOAT);
//					else if (t instanceof IAcmeBooleanType)
//						expr.setType(StitchTypes.BOOLEAN);
//					else if (t instanceof IAcmeSetType) {
//						expr.setType(StitchTypes.SET);
//					}
//					else if (t instanceof IAcmeSequenceType) {
//						expr.setType(StitchTypes.SEQ);
//					}
//					else expr.setType(pt.getName());;
//					
//				} else { // store the object directly
//					expr.setType(StitchTypes.UNKNOWN);
//				}
//			}
//		} else if (kind == Strategy.ExpressionKind.INTEGER) {
//			expr.setType(StitchTypes.INTEGER);
//		} else if (kind == Strategy.ExpressionKind.BOOLEAN) {
//			expr.setType(StitchTypes.BOOLEAN);
//
//		} else if (kind == Strategy.ExpressionKind.FLOAT) {
//			expr.setType(StitchTypes.FLOAT);
//		} else if (kind == Strategy.ExpressionKind.STRING) {
//			expr.setType(StitchTypes.STRING);
//		} else if (kind == Strategy.ExpressionKind.CHAR) {
//			// strip the single quotes, so char is at index 1
//			expr.setType(StitchTypes.CHAR);
//		} else if (kind == Strategy.ExpressionKind.NULL) {
//			expr.setType(StitchTypes.UNKNOWN);
//		}
	}

	@Override
	public void doPostIdentifierExpression(StitchParser.PostIdExpressionContext identifier) {
		if (scope() != null) {
			if (scope().parent() != null) {
				if (scope().parent().vars().containsKey(identifier.IDENTIFIER().getText())) {
					return;
				}
			}
		}
		Tool.error(identifier.IDENTIFIER().getText() + " is not defined in the tactic scope, and so "
				+ identifier.getText() + " cannot be used in the effect.", identifier, stitchProblemHandler());
	}





	@Override
	public void createVar(DataTypeContext type, TerminalNode id, ExpressionContext val, boolean isFunction) {
		if (scope().expressions().size() > 0) {
			Var v = scope().vars().get(id.getText());
			if (v != null && v.valStmt != null) {
				Expression e = scope().expressions().get(0);
				// make sure expr assocaited with var has AST and no known
				// result
//				if (e.tree() != null && (e.getResult() == null || v.isFunction())) {
//					e.evaluate(null, m_walker);
//				}
				v.setType(e.getType());
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
	public void beginExpression() {
		super.beginExpression();
		if (expr() != null) {
			expr().subLevel++;
			return;
		}
		final IScope scope = scope();
		if (scope instanceof Statement && scope.expressions().size() > ((Statement) scope).curExprIdx) {
			setExpression(scope.expressions().get(((Statement) scope).curExprIdx++));
			expr().curExprIdx = 0;
		} else if (scope instanceof Expression) {
			Expression expr = (Expression) scope;
			if (expr.isComplex()) {
				if (expr.expressions().size() > expr.curExprIdx) {
					setExpression(expr.expressions().get(expr.curExprIdx++));
					expr.subLevel = 0;
				}
			} else {
				setExpression((Expression) scope);
				expr().curExprIdx = 0;
			}
		}
	}
	
	@Override
	public void endExpression(ParserRuleContext ctx) {
		super.endExpression(ctx);
		Expression expr = expr();
		if (expr.subLevel > 0) { // decr dept count first
			expr.subLevel--;
			return;
		} else { // check quantified expression special case
			if (expr.skipQuanPredicate) {
				expr.skipQuanPredicate = false;
			}
		}

		if (expr.getType() == null && expr.expressions().size() > 0) {
			setType(expr,expr.expressions().get(0).getType());
		}
		

		setExpression(null); // clear eval expression reference
//		Expression expr = (Expression )scope();
//		if (expr.subLevel > 0) { // decr dept count first
//			expr.subLevel--;
//		} else { // check quantified expression special case
//			boolean addToParentList = false;
//		}
//		// transfer any child result
//		if ((expr.getType() == StitchTypes.UNKNOWN || expr.getType() == null) && expr.expressions().size() > 0) {
//			// transfer children result up
//			Expression sub = expr.expressions().get(0);
//			expr.setType(sub.getType());
//		}
//
//		setExpression(null); // clear eval expression reference
	}
	
	private void doBeginComplexExpr() {
		// get the next expression in list, checking to see if it's complex.
		// if it is, we're in a straight complex expression;
		// if not, check current expr to see if it is complex, in which case,
		// we're within a complex expression already...
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
	
	private Expression doEndComplexExprTC() {
		Expression cExpr = (Expression) scope();
		setExpression((Expression) cExpr.parent());
		popScope();
		return cExpr;
	}

	@Override
	public void beginQuantifiedExpression() {
		super.beginQuantifiedExpression();
		doBeginComplexExpr();
	}
	
	@Override
	public void doQuantifiedExpression(ExpressionKind type, QuantifiedExpressionContext ctx) {
		Expression cExpr = (Expression )scope();
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
		String result = cExpr.expressions().get(0).getType();
	}

	@Override
	public void endQuantifiedExpression(ExpressionKind quant, QuantifiedExpressionContext quantifiedExpressionContext) {
		Expression cExpr = doEndComplexExprTC();
		expr().setType(cExpr.getType());
		super.endQuantifiedExpression(quant, quantifiedExpressionContext);
	}
	
	@Override
	public void beginMethodCallExpression() {
		super.beginMethodCallExpression();
		doBeginComplexExpr();
	}
	
	public void endMethodCallExpression(TerminalNode mc, StitchParser.MethodCallContext id) {
//		Expression cExpr = (Expression )scope ();
//
//		Object [] args = new Object [((Expression )cExpr.getChildren().iterator ().next ()).expressions().size ()];
//		int i = 0;
//		for (Expression e : cExpr.expressions ()) {
//			e.evaluate (null);
//		}

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
			List<Class> classesToSearch = new ArrayList<Class>();
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
					Tool.warn(MessageFormat.format(
							"{0} : Could not find method {1} in any scopes, and could not verify that"
									+ " it is an operator.",
							new File(m_stitch/* .stitch () */.path).getName(), id.getText()), id,
							stitchProblemHandler());
				}
			}
			
		}
		doEndComplexExprTC();
	}
	
	@Override
	public void beginSetExpression() {
		super.beginSetExpression();
		doBeginComplexExpr();
	}

	@Override
	public void endSetExpression(SetExpressionContext setAST) {
		Expression cExpr = doEndComplexExprTC();
		String settype = StitchTypes.SET;
		setType(cExpr, settype);
		super.endSetExpression(setAST);
		return;
//		// iterate through elements and make sure they are the same types
//		// complain if NOT, or form the proper set if yes
//		Var v = new Var(); // declare a Var object to be able to use typeMatches
//		for (Expression e : cExpr.expressions()) {
//			if (e.getType() == null) {
//				if (v.getType() != null) {
//					// consider this a type mismatch?
//					Tool.error("Unexpected null value in a SET!", setAST, stitchProblemHandler());
//					return;
//				}
//			} else if (v.getType() != null && !Tool.typesCompatible(v.getType(), e.getType())) {
//				// explicit type mismatch
//				// TODO: support subtypes?
//				Tool.warn("Type mismatch between elements in set: " + e.getResult().getClass() + " vs "
//						+ v.getValue().getClass(), setAST, stitchProblemHandler());
//			} else {
//				// store type if type not already set
////				if (v.getValue() == null) {
////					v.setValue(e.getResult());
////					v.setType(e.getResult().getClass().getName());
////				}
//				settype=e.getType();
//			}
//		}
//		if (set.size() == 1 && set.iterator().next() instanceof Set
//				&& cExpr.expressions().iterator().next().getKind() == Kind.QUANTIFIED) {
//			Tool.warn(
//					"It seems that this expression has a set that has one item that is a set. This might mean you have an extra set of curly braces.",
//					null, stitchProblemHandler());
//		}
//		cExpr.setType(settype);
	}

//	@Override
//	public void endSetExpression(StitchParser.SetExpressionContext setAST) {
//		super.endSetExpression(setAST);
//	}

	@Override
	public void doExpression(ParserRuleContext exprAST) {
		super.doExpression(exprAST);
		if (scope() instanceof Expression) {
			Expression expr = (Expression) scope();
			if (expr.getType() == null && expr.expressions().size() > 0) {
				// transfer children result up
				Expression sub = expr.expressions().get(0);
				if (sub.getType() != StitchTypes.UNKNOWN)
					expr.setType(sub.getType());
			}
		}
	}

	@Override
	public void doAssignExpression(ParserRuleContext identifier, ParserRuleContext expression) {
		super.doAssignExpression(identifier, expression);
		
		Expression expr = expr();
		Object rVal = expr.lrOps[Expression.ROP].pop();
		// the lVal can only be an identifier, otherwise, won't make sense!
		Object lObj = expr.lookup(identifier.getText());
		if (lObj != null && lObj instanceof Var) {
			Var v = (Var) lObj;
			if (Tool.typeMatches(v, rVal)) {
				v.setValue(rVal);
				expr.setResult(rVal);
			} else {
				Tool.error("Assignment expression cannot be evaluated due to mismatched value types! " + v.getType()
						+ " vs. " + rVal.getClass().getName(), expression, stitchProblemHandler());
				return;
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
	}

	@Override
	public void pathExpressionFilter(TypeFilterT filter, TerminalNode identifier, ExpressionContext expression) {
	}

	@Override
	public void continueExpressionFilter(TypeFilterT filter, TerminalNode setIdentifier, TerminalNode typeIdentifier,
			ExpressionContext expression, boolean mustBeSet, boolean resultisSet) {
	}

	final ThreadLocal<Var> pathVariable = new ThreadLocal<Var>() {
		@Override
		protected Var initialValue() {
			return null;
		}
	};

	@Override
	public void endPathExpression(PathExpressionContext ctx) {
		pathVariable.set(null);
		scope().vars().remove("__path_filter_type");
		Expression cExpr = doEndComplexExpr();
		expr().setType(cExpr.getType());
	}

	@Override
	public void doRelationalExpression(ExpressionKind kind, ParserRuleContext opAST) {
		((Expression) scope()).setKind(Expression.Kind.RELATIONAL);
		scope().setName(opAST.getText());

		Expression expr = (Expression )scope();

		if (expr.lrOps[Expression.LOP].isEmpty() || expr.lrOps[Expression.ROP].isEmpty()
				|| expr.lrOps[Expression.LOP].peek() == null || expr.lrOps[Expression.ROP].peek() == null) {
			// if either is NULL, result is NULL
			final String msg = "One relational operand is NULL:  ... " + opAST.toStringTree();
			Tool.warn(msg, opAST, stitchProblemHandler());
//	            System.out.println (msg);
			expr.setResult(null);
			expr.setType(Expression.BOOLEAN);
			return;
		}
		
		Object lOp = expr.lrOps[Expression.LOP].pop();
		Object rOp = expr.lrOps[Expression.ROP].pop();
		
		if (!lOp.equals(rOp)) {
			if (!lOp.equals(Expression.UNKNOWN) && !rOp.equals(Expression.UNKNOWN)) {
				Tool.error(MessageFormat.format("Incompatibale types: {0} {1} {2}", lOp, kind.image(),rOp), opAST, stitchProblemHandler());
			}
		}
		setType(expr, Expression.BOOLEAN);

	}
	
	@Override
	public void doUnaryExpression(ExpressionKind opAST, UnaryExpressionContext ctx) {
		super.doUnaryExpression(opAST, ctx);
		
		Expression expr = expr();
		Object pType = expr.lrOps[Expression.LOP].peek();
		if (pType == null || Expression.UNKNOWN.equals(pType)) {
			Tool.error("Unary operand is null: " + expr.lrOps[Expression.LOP].pop(), ctx, stitchProblemHandler());
			expr.setType(opAST == Strategy.ExpressionKind.NOT?Expression.BOOLEAN:Expression.UNKNOWN);
		}
		String cType = (String) expr.lrOps[Expression.LOP].pop();
		switch(opAST) {
		case NOT: 
			expr.lrOps[Expression.LOP].push(Expression.BOOLEAN);
			break;
		case DECR:
		case INCR:
		case UNARY_MINUS:
		case UNARY_PLUS:
			expr.lrOps[Expression.LOP].push(cType);
		
		default:
			Tool.error("Unknown unary operand: " + opAST, ctx, stitchProblemHandler());
			expr.lrOps[Expression.LOP].push(Expression.UNKNOWN);
		}

	}
	
	@Override
	public void doArithmeticExpression(ExpressionKind kind, ParserRuleContext ctx) {
		super.doArithmeticExpression(kind, ctx);
		Expression expr = expr();
		if (Expression.STRING.equals(expr.lrOps[Expression.LOP].peek()) && kind == ExpressionKind.PLUS) {
			expr.lrOps[Expression.LOP].pop();
			expr.lrOps[Expression.ROP].pop();
			setType(expr,Expression.STRING);
			return;
		}
		
		String lOp = expr.lrOps[Expression.LOP].peek()!=null?(String) expr.lrOps[Expression.LOP].pop():Expression.UNKNOWN;
		String rOp = expr.lrOps[Expression.ROP].peek()!=null?(String) expr.lrOps[Expression.ROP].pop():Expression.UNKNOWN;
	
		switch (lOp) {
		case Expression.UNKNOWN:
			switch (rOp) {
			case Expression.UNKNOWN:
				setType(expr, lOp);
				break;
			case Expression.FLOAT:
			case Expression.LONG:
			case Expression.INTEGER:
				switch (kind) {
				case PLUS:
				case MINUS:
				case MULTIPLY:
					setType(expr, rOp);
					break;
				default: 
					Tool.error(MessageFormat.format("Cannot deterimine type for operation: {0} {1} {2}", lOp, kind.image(), rOp), ctx, stitchProblemHandler());
				}
				break;
			case Expression.STRING:
				if (kind == ExpressionKind.PLUS)
					setType(expr, Expression.STRING);
				else {
					Tool.error(MessageFormat.format("Cannot use string on right hand of ''{0}''", kind.image()), ctx, stitchProblemHandler());
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
				case Expression.SET:{
					Tool.error(MessageFormat.format("Cannot use {1} on right hand of ''{0}''", kind.image(), rOp), ctx, stitchProblemHandler());
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
						Tool.error(MessageFormat.format("Cannot use {1} on right hand of ''{0}''", kind.image(), rOp), ctx, stitchProblemHandler());
						setType(expr, Expression.UNKNOWN);
						break;
				}
				break;
			}
			break;
		case Expression.INTEGER:
			switch(kind) {
			case PLUS:
			case MINUS:
			case DIVIDE:
			case MULTIPLY:
			case MOD:
				switch(rOp) {
				case Expression.STRING:
				case Expression.BOOLEAN:
				case Expression.CHAR:
				case Expression.SEQ:
				case Expression.SET:
					Tool.error(MessageFormat.format("Cannot use {1} on right hand of ''{0}''", kind.image(), rOp), ctx, stitchProblemHandler());
					setType(expr, Expression.UNKNOWN);
					break;
				default:
					setType(expr,rOp);
				}
				
			}
			break;
		 default:
			 Tool.error(MessageFormat.format("Cannot use {0} in an arithmetic expression: {0} {1} {2}" , lOp, kind.image(), rOp), ctx, stitchProblemHandler());
			 setType(expr, Expression.UNKNOWN);
		}		
	}
	
	@Override
	public void doLogicalExpression(ExpressionKind kind, ParserRuleContext ctx) {
		super.doLogicalExpression(kind, ctx);
		Expression expr = expr();
		
		String lOp = expr.lrOps[Expression.LOP].peek()!=null?(String) expr.lrOps[Expression.LOP].pop():Expression.UNKNOWN;
		String rOp = expr.lrOps[Expression.ROP].peek()!=null?(String) expr.lrOps[Expression.ROP].pop():Expression.UNKNOWN;
	
		if (kind == ExpressionKind.EQ || kind == ExpressionKind.NE) {
			if (lOp.equals(rOp)) {
				setType(expr, Expression.BOOLEAN);
			}
			else if (isNumber(lOp) && isNumber(rOp)) {
				setType(expr, Expression.BOOLEAN);
			}
			else {
				Tool.error(MessageFormat.format("Cannot do compare the types in expression {0} {1} {2}", lOp, kind.image(), rOp), ctx, stitchProblemHandler());
				setType(expr, Expression.BOOLEAN);
			}
		}
		else if (isNumber(lOp) && isNumber(rOp)) {
			setType(expr, Expression.BOOLEAN);
		}
		else {
			Tool.error(MessageFormat.format("Cannot do compare the types in expression {0} {1} {2}", lOp, kind.image(), rOp), ctx, stitchProblemHandler());
			setType(expr, Expression.BOOLEAN);
		}
		
	}
	
	protected boolean isNumber(String type) {
		switch (type) {
			case Expression.FLOAT:
			case Expression.LONG:
			case Expression.INTEGER:
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
}
