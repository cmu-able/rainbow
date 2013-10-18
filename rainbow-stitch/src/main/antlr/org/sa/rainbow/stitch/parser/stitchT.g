/*
 * Author:   Shang-Wen Cheng (zensoul@cs.cmu.edu)
 * Purpose:  Define the tree walker for Stitch, Rainbow's Qos-driven,
 *   Utility-informed, Architecture-based, System Adaptation Representation
 *   language (otherwise named the Rainbow QUASAR Language).
 * Created:  March 10, 2006
 * History:
 *   May 3, 2006 - Tactic and statement implementation completed
 *   August 20, 2006 - Strategy parsing completed
 *   August 21, 2006 - Added variable declarations to Strategy block
 *   February 05, 2008 - added "define" keyword for strategy function
 *     - moved timer from condition clause to action as explicit control of settling time
 *     - removed exit condition from language feature due to potential breach of tactic atomicity
 *     - gave explicit treatment to strategy root node as applicability condition
 *     - fixed bug with any tacticRef being followed by strategy branchv
 *   February 06, 2008 - added function "define" capability to script level
 *   February 07, 2008 - promoted strategy condition, special syntactic treatment
 *     - removed "root node" term as a consequence
 *     - removed parametrization for strategy
 *     - added distinction between VAR_LIST vs. FUNC_LIST for declarations 
 *     - renamed fail block (and FAIL) to error block
 *     - added FAILURE condition to strategy
 */

header {package org.sa.rainbow.stitch.parser;
}
options {
    language = "Java";
}

{import org.sa.rainbow.stitch.visitor.ILiloBehavior;
import org.sa.rainbow.stitch.core.IScope;
import org.sa.rainbow.stitch.core.Import;
import org.sa.rainbow.stitch.core.Expression;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.error.StitchProblem;
import org.sa.rainbow.stitch.error.StitchProblemHandler;
}

class StitchTreeWalker extends TreeParser;

options {
	importVocab = StitchParser;
    buildAST = true;
    defaultErrorHandler = true;
//    ASTLabelType = "StitchAST";
}

{
    // Accumulate intermediate information while tree walking
    private ILiloBehavior beh = null;
	private StitchProblemHandler stitchProblemHandler = null;
    
    public void setBehavior (ILiloBehavior lb) {
    	beh = lb;
    }

	public void setStitchProblemHandler (StitchProblemHandler handler) {
		stitchProblemHandler = handler;
	}

	private void processError (RecognitionException ex) {
		reportError(ex);
		stitchProblemHandler.setProblem(new StitchProblem(ex, StitchProblem.ERROR));
	}
}

script [IScope parentScope]
    {   beh.beginScript(parentScope); }
    : #(MODULE
        id:IDENTIFIER  { beh.createModule(#id); }
        #(IMPORTS (importSt)*)  { beh.doImports(); }
        functions
        #(TACTICS (tactic)*)
        #(STRATEGIES (strategy)*)
       )
    {   beh.endScript(); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}

importSt
    {   Import imp = null; }
    : #(i1:IMPORT_LIB s1:STRING_LIT   { imp = beh.createImport(#i1, #s1); }
        (importRenames)*
       )
    | #(i2:IMPORT_MODEL s2:STRING_LIT { imp = beh.createImport(#i2, #s2); }
        (importRenames)*
       )
    | #(i3:IMPORT_OP s3:STRING_LIT    { imp = beh.createImport(#i3, #s3); }
        (importRenames)*
       )
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
importRenames
    : #(AS id1:IDENTIFIER id2:IDENTIFIER)
    {   beh.addImportRename(#id1, #id2); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}

functions
    {   beh.beginVarList(); }
    : #(FUNC_LIST (var)*)
    {   beh.endVarList(); }
    ;
    exception catch [RecognitionException ex] {
        processError(ex);
        if (_t!=null) {_t = _t.getNextSibling();}
    }

tactic
    : #(TACTIC
        id:IDENTIFIER  { beh.beginTactic(#id); }
        params
        vars
        condition
        action
        effect
       )
    {   beh.endTactic(); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
vars
    {   beh.beginVarList(); }
    : #(VAR_LIST (var)*)
    {   beh.endVarList(); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
var
    : #(v:VAR_DEF  { beh.beginStatement(#v); }
        t:TYPE
        id:IDENTIFIER
        (#( ASSIGN
            expr
          )
        )?
       )
    {
        beh.createVar(#t, #id);
    	beh.endStatement();
    }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
condition
    : #(cn:CONDITION  { beh.beginConditionBlock(#cn); }
        (expr)*
       )
    {   beh.endConditionBlock(); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
action
    : #(ac:ACTION     { beh.beginActionBlock(#ac); }
        (statement)*
       )
    {   beh.endActionBlock(); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
effect
    : #(ef:EFFECT     { beh.beginEffectBlock(#ef); }
        (expr)*
       )
    {   beh.endEffectBlock(); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}

strategy
    : #(STRATEGY
        id:IDENTIFIER  { beh.beginStrategy(#id); }
        expr           { beh.doStrategyCondition(Strategy.ConditionKind.APPLICABILITY); }
        functions
        (strategyNode)*
       )
    {   beh.endStrategy(); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
strategyNode
    : #(l:IDENTIFIER   { beh.beginStrategyNode(#l); }
        strategyCond
        tacticRef
       )
    {   beh.endStrategyNode(); }
	;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
strategyCond
	: #(STRATEGY_COND
	    (#( HASH
	        (#( pid1:IDENTIFIER
	            (pid2:IDENTIFIER)?
	          )
	        |   pl:FLOAT_LIT
	        )
	      )  { beh.doStrategyProbability(#pid1, #pid2, #pl); }
	    )?
	    (  expr     { beh.doStrategyCondition(Strategy.ConditionKind.EXPRESSION); }
	    |  SUCCESS  { beh.doStrategyCondition(Strategy.ConditionKind.SUCCESS); }
	    |  FAILURE  { beh.doStrategyCondition(Strategy.ConditionKind.FAILURE); }
	    |  DEFAULT  { beh.doStrategyCondition(Strategy.ConditionKind.DEFAULT); }
	    )
	   )
	;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
tacticRef
	: #(t1:IDENTIFIER   { beh.beginReferencedTactic(#t1); }
	    exprs
       )                { beh.endReferencedTactic(); }
//       (strategyExit)?  // [SWC 2008.02.05] Removed, see strategyExit
       (#( AT  // [SWC 2008.02.05] moved here to allow explicit control of settling time
           expr
         )              { beh.doStrategyDuration(/*expression is implicit*/); }
       )?
       (strategyBranch
       | DONE
       )
	|  DONE             { beh.doStrategyAction(Strategy.ActionKind.DONE); }
	|  NULLTACTIC       { beh.doStrategyAction(Strategy.ActionKind.NULL); }
	| #(DO
	    ( v:IDENTIFIER
	    | i:INTEGER_LIT
	    )
	    t2:IDENTIFIER   { beh.doStrategyLoop(#v, #i, #t2); }
	   )
	| #(DO_UNSPEC
	    t3:IDENTIFIER   { beh.doStrategyLoop(null, null, #t3); }
	   )
	;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
// [SWC 2008.02.05] Removed from language feature due to potential breach of tactic atomicity
//strategyExit
//	: #(STRATEGY_EXITCOND
//	    expr        { beh.doStrategyExitCondition(/*expression is implicit*/); }
//	    #(AT expr)  { beh.doStrategyExpiration(/*expression is implicit*/); }
//	   )
//	;
//	exception catch [RecognitionException ex] {
//		processError(ex);
//		if (_t!=null) {_t = _t.getNextSibling();}
//	}
strategyBranch
	: #(STRATEGY_BRANCH  { beh.beginBranching(); }
	    (strategyNode)+
	   )                 { beh.endBranching(); }
	;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}

statement
    :
    ( #(sl:STMT_LIST   { beh.beginStatement(#sl); }
         (statement)*
         (errorHandler)?
       )               { beh.endStatement(); }
    |   var
    | #(x:EXPR         { beh.beginStatement(#x); }
        expr           { beh.endStatement(); }
       )
    |   ifStmt
    |   whileStmt
    |   forStmt
    |   es:EMPTY_STMT  { beh.beginStatement(#es); beh.endStatement(); }
    )
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
errorHandler
    : #(e:ERROR      { beh.beginStatement(#e); }
        ( expr
          statement
        )*
       )            { beh.endStatement(); }
	;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
ifStmt
    : #(si:IF     { beh.beginStatement(#si); }
        expr
        statement
        (statement)?
       )          { beh.endStatement(); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
whileStmt
    : #(w:WHILE   { beh.beginStatement(#w); }
        expr
        statement
       )          { beh.endStatement(); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
forStmt
    : #(f:FOR       { beh.beginStatement(#f); }
        ( forInit   { beh.markForCondition(); }
          forCond
          forIter
          statement
        | forEach   { beh.markForEach(); }
          statement
        )
       )            { beh.endStatement(); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
forInit
    : #(FOR_INIT ((var)+ | exprs)?)
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
forCond
    : #(FOR_COND (expr)?)
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
forIter
    : #(FOR_ITER (exprs)?)
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
forEach
    : #(FOR_EACH param expr)
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}

exprs
    : #(EXPR_LIST (expr)*)
    ;
expr
    {   beh.beginExpression(); }
    :
    ( #(EXPR e:expr)  { beh.doExpression(#e); }
    |   quanExpr
    |   setExpr
    |   assignExpr
    |   logicalExpr
    |   relationalExpr
    |   arithmeticExpr
    |   unaryExpr
    |   idExpr
    )
    {   beh.endExpression(); }
	;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
quanExpr
    : #(fa:FORALL         { beh.beginQuantifiedExpression(); }
        params
        expr              { beh.doQuantifiedExpression(); }
        expr
       )                  { beh.endQuantifiedExpression(#fa); }
    | #(e:EXISTS          { beh.beginQuantifiedExpression(); }
        params
        expr              { beh.doQuantifiedExpression(); }
        expr
       )                  { beh.endQuantifiedExpression(#e); }
    | #(eu:EXISTS_UNIQUE  { beh.beginQuantifiedExpression(); }
        params
        expr              { beh.doQuantifiedExpression(); }
        expr
       )                  { beh.endQuantifiedExpression(#eu); }
    | #(s:SELECT          { beh.beginQuantifiedExpression(); }
        params
        expr              { beh.doQuantifiedExpression(); }
        expr
       )                  { beh.endQuantifiedExpression(#s); }
	;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
setExpr
    {   beh.beginSetExpression(); }
    : #(s:SET (expr (expr)*)?)  // elements use "expr" rule to reuse expr code
    {   beh.endSetExpression(#s); }
	;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
assignExpr
    : #(aa:ASSIGN       {beh.lOp();} lv1:expr {beh.rOp();} expr)  { beh.doAssignExpression(#aa, #lv1); }
/* [Apr 25, 2006] the other assignment ops are syntactic sugar, so currently unimplemented */
//    | #(pa:PLUS_ASSIGN  {beh.lOp();} lv2:expr {beh.rOp();} expr)  { beh.doAssignExpression(#pa, #lv2); }
//    | #(ma:MINUS_ASSIGN {beh.lOp();} lv3:expr {beh.rOp();} expr)  { beh.doAssignExpression(#ma, #lv3); }
//    | #(ta:STAR_ASSIGN  {beh.lOp();} lv4:expr {beh.rOp();} expr)  { beh.doAssignExpression(#ta, #lv4); }
//    | #(da:DIV_ASSIGN   {beh.lOp();} lv5:expr {beh.rOp();} expr)  { beh.doAssignExpression(#da, #lv5); }
//    | #(ra:MOD_ASSIGN   {beh.lOp();} lv6:expr {beh.rOp();} expr)  { beh.doAssignExpression(#ra, #lv6); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
logicalExpr
    : #(imp:IMPLIES     {beh.lOp();} expr {beh.rOp();} expr)  { beh.doLogicalExpression(#imp); }
    | #(iff:IFF         {beh.lOp();} expr {beh.rOp();} expr)  { beh.doLogicalExpression(#iff); }
    | #(lor:LOGICAL_OR  {beh.lOp();} expr {beh.rOp();} expr)  { beh.doLogicalExpression(#lor); }
    | #(lnd:LOGICAL_AND {beh.lOp();} expr {beh.rOp();} expr)  { beh.doLogicalExpression(#lnd); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
relationalExpr
    : #(ne:NE {beh.lOp();} expr {beh.rOp();} expr)  { beh.doRelationalExpression(#ne); }
    | #(eq:EQ {beh.lOp();} expr {beh.rOp();} expr)  { beh.doRelationalExpression(#eq); }
    | #(lt:LT {beh.lOp();} expr {beh.rOp();} expr)  { beh.doRelationalExpression(#lt); }
    | #(le:LE {beh.lOp();} expr {beh.rOp();} expr)  { beh.doRelationalExpression(#le); }
    | #(ge:GE {beh.lOp();} expr {beh.rOp();} expr)  { beh.doRelationalExpression(#ge); }
    | #(gt:GT {beh.lOp();} expr {beh.rOp();} expr)  { beh.doRelationalExpression(#gt); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
arithmeticExpr
    : #(p:PLUS  {beh.lOp();} expr {beh.rOp();} expr)  { beh.doArithmeticExpression(#p); }
    | #(m:MINUS {beh.lOp();} expr {beh.rOp();} expr)  { beh.doArithmeticExpression(#m); }
    | #(t:STAR  {beh.lOp();} expr {beh.rOp();} expr)  { beh.doArithmeticExpression(#t); }
    | #(d:SLASH {beh.lOp();} expr {beh.rOp();} expr)  { beh.doArithmeticExpression(#d); }
    | #(r:MOD   {beh.lOp();} expr {beh.rOp();} expr)  { beh.doArithmeticExpression(#r); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
unaryExpr
    : #(ic:INCR        {beh.lOp();} expr)  { beh.doUnaryExpression(#ic); }
    | #(dc:DECR        {beh.lOp();} expr)  { beh.doUnaryExpression(#dc); }
    | #(um:UNARY_MINUS {beh.lOp();} expr)  { beh.doUnaryExpression(#um); }
    | #(up:UNARY_PLUS  {beh.lOp();} expr)  { beh.doUnaryExpression(#up); }
    | #(ln:LOGICAL_NOT {beh.lOp();} expr)  { beh.doUnaryExpression(#ln); }
/* [Apr 25, 2006] postfix ops are more difficult and currently unnecessary */
//    | #(pi:POST_INCR   {beh.lOp();} expr)  { beh.doUnaryExpression(#pi); }
//    | #(pd:POST_DECR   {beh.lOp();} expr)  { beh.doUnaryExpression(#pd); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
idExpr
    :   id:IDENTIFIER  { beh.doIdentifierExpression(#id, Expression.Kind.IDENTIFIER); }
    |   methodCall
    |   i:INTEGER_LIT  { beh.doIdentifierExpression(#i, Expression.Kind.INTEGER); }
    |   d:FLOAT_LIT    { beh.doIdentifierExpression(#d, Expression.Kind.FLOAT); }
    |   s:STRING_LIT   { beh.doIdentifierExpression(#s, Expression.Kind.STRING); }
    |   c:CHAR_LIT     { beh.doIdentifierExpression(#c, Expression.Kind.CHAR); }
    |   t:TRUE         { beh.doIdentifierExpression(#t, Expression.Kind.BOOLEAN); }
    |   f:FALSE        { beh.doIdentifierExpression(#f, Expression.Kind.BOOLEAN); }
    |   n:NULL         { beh.doIdentifierExpression(#n, Expression.Kind.NULL); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
methodCall
    {   beh.beginMethodCallExpression(); }
    : #(mc:METHOD_CALL
        id:IDENTIFIER
        ( exprs
        | // or nothing
        )
       )
    {   beh.endMethodCallExpression(#mc, #id); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}

params
    {   beh.beginParamList(); }
    : #(PARAM_LIST (param)*)
    {   beh.endParamList(); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
param
    : #(PARAM t:TYPE id:IDENTIFIER)
    {   beh.createVar(#t, #id); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex);
		if (_t!=null) {_t = _t.getNextSibling();}
	}
