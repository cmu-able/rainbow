/*
 * Author:   Shang-Wen Cheng (zensoul@cs.cmu.edu)
 * Purpose:  Define the grammar parser for Stitch, Rainbow's Qos-driven,
 *   Utility-informed, Architecture-based, System Adaptation Representation
 *   language (otherwise named the Rainbow QUASAR Language).
 * Credits:  Grammar for "statement" and "expression" derived/inspired/copied
 *   from Java 1.5 example on ANTLR website (http://www.antlr.org/grammar/list).
 * Created:  March 10, 2006
 * History:
 *   August 21, 2006 - added variable declaration support to Strategy block
 *   February 05, 2008 - added "define" keyword for strategy function
 *     - eliminated extraneous "| done" after null-tactic;
 *       makes "done" meaning consistent to be "successful completion"
 *     - moved timer from condition clause to action as explicit control of settling time
 *     - removed exit condition from language feature due to potential breach of tactic atomicity
 *     - gave explicit treatment to strategy root node as applicability condition
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

{import antlr.collections.impl.LList;
import java.util.ArrayList;
import org.sa.rainbow.stitch.error.StitchProblem;
import org.sa.rainbow.stitch.error.StitchProblemHandler;
}

class StitchParser extends Parser;

options {
	k = 2;  // default lookahead
	importVocab = StitchLexer;
    buildAST = true;
}

tokens {
    IMPORTS; IMPORT_LIB; IMPORT_MODEL; IMPORT_OP;
    TACTICS; FUNC_LIST; VAR_LIST;
    STRATEGIES; STRATEGY_COND; STRATEGY_BRANCH; /*STRATEGY_EXITCOND;*/
    DO_UNSPEC;
    STMT_LIST; EMPTY_STMT; VAR_DEF;
    EXPR_LIST; EXPR; METHOD_CALL;
    FOR_INIT; FOR_COND; FOR_ITER; FOR_EACH;
    PARAM_LIST; PARAM; 
}


{
	//ALI: ADDED stitchProblemHandler to parser
	private StitchProblemHandler stitchProblemHandler = null;
	private ArrayList<AST> definedTactics = new ArrayList<AST>();

	public void setStitchProblemHandler (StitchProblemHandler handler) {
		stitchProblemHandler = handler;
	}

	public ArrayList<AST> getDefinedTactics() {
		return definedTactics;
	}

	private void processError (RecognitionException ex, BitSet tokenSet)
	throws TokenStreamException {
		reportError(ex);
		stitchProblemHandler.setProblem(new StitchProblem(ex, StitchProblem.ERROR));
		recover(ex, tokenSet);
	}
}

script
    :   MODULE^ IDENTIFIER SEMICOLON!
        imports
        functions
        tactics
        strategies
        EOF
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}


imports
    :   (importSt)*
    {   #imports = #([IMPORTS,"IMPORTS"], #imports); }
    ;
importSt
    :   i:IMPORT^
        ( tl:LIB!    { #i.setType(IMPORT_LIB); #i.setText(#tl.getText()); }
        | tm:MODEL!  { #i.setType(IMPORT_MODEL); #i.setText(#tm.getText()); }
        | to:OP!     { #i.setType(IMPORT_OP); #i.setText(#to.getText()); }
        )
        STRING_LIT (importRenameClause)? SEMICOLON!
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
importRenameClause
    :   LBRACE! importRenamePhrase (COMMA! importRenamePhrase)* RBRACE!
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
importRenamePhrase
    :   IDENTIFIER AS^ IDENTIFIER
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}

functions
    :   (function)*
    {   #functions = #([FUNC_LIST,"FUNC_LIST"], #functions); }
	;
function
    :   DEFINE! declaration SEMICOLON!
    ;
    exception catch [RecognitionException ex] {
        processError(ex, $FOLLOW);
    }

tactics
    :   (tactic)*
    {   #tactics = #([TACTICS,"TACTICS"], #tactics); }
    ;
tactic
    :   TACTIC^ signature
        LBRACE! tacticBody RBRACE!
    {
		//Ali: Added (trying to catch all tactics and throw them into a ds that I can then give to the content assist processor)
		definedTactics.add(#tactic.getFirstChild());
	}
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
tacticBody
    :   tacticVars tacticConditionBlock tacticActionBlock tacticEffectBlock
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
tacticVars
    :   (declaration SEMICOLON!)*
    {   #tacticVars = #([VAR_LIST,"VAR_LIST"], #tacticVars); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
tacticConditionBlock
    :   CONDITION^ LBRACE! (expression SEMICOLON!)* RBRACE!
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
tacticActionBlock
    :   ACTION^ LBRACE! (statement)* RBRACE!
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
tacticEffectBlock
    :   EFFECT^ LBRACE! (expression SEMICOLON!)* RBRACE!
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}


strategies
    :   (strategy)*
    {   #strategies = #([STRATEGIES,"STRATEGIES"], #strategies); }
    ;
strategy
    :   STRATEGY^ IDENTIFIER
        LBRACKET! expression RBRACKET!
        LBRACE! strategyBody RBRACE!
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
strategyBody
    :   functions
        (strategyExpr)*
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
strategyExpr
    :   IDENTIFIER^ COLON!
        strategyCond
        IMPLIES!
        strategyOutcome
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
strategyCond
    :   LPAREN!
          (strategyProbExpr)?
          (expression | SUCCESS | FAILURE | DEFAULT)
        RPAREN!
    {   #strategyCond = #([STRATEGY_COND,"STRATEGY_COND"], #strategyCond); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
strategyOutcome
    :   strategyClosedOutcome SEMICOLON!
    |   strategyOpenOutcome
//        (strategyExitCond)?  // [SWC 2008.02.05] Removed, see strategyExitCond 
        (strategyTimingExpr)?  // [SWC 2008.02.05] moved here to allow explicit control of settling time
        ( BAR! DONE SEMICOLON!
        | strategyBranchOutcome
        )
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
strategyClosedOutcome
    :   DONE
    |   NULLTACTIC
    |   d:DO^ LBRACKET!
        (IDENTIFIER | INTEGER_LIT | { #d.setType(DO_UNSPEC); } )
        RBRACKET! IDENTIFIER
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
strategyOpenOutcome
    :   IDENTIFIER^ LPAREN! argList RPAREN!
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
strategyBranchOutcome
    :   lb:LBRACE^ { #lb.setType(STRATEGY_BRANCH); #lb.setText("STRATEGY_BRANCH"); }
        (strategyExpr)+ RBRACE!
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
/*// [SWC 2008.02.05] Removed from language feature due to potential breach of tactic atomicity
strategyExitCond
    :   lb:LBRACKET^ { #lp.setType(STRATEGY_EXITCOND); #lp.setText("STRATEGY_EXITCOND"); }
        expression strategyTimingExpr RBRACKET!
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
*/
strategyProbExpr
    :   HASH^ LBRACKET! strategyProbValue RBRACKET!
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
strategyProbValue
/*    :   FLOAT_LIT
    |   IDENTIFIER^ (LBRACE! IDENTIFIER RBRACE!)?
    ;
    */
    : expression
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
strategyTimingExpr
    :   AT^ LBRACKET! strategyTimingValue RBRACKET!
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
strategyTimingValue  // assume in milliseconds (to avoid unit hassle), allow expr
    :   expression
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}


statement
        // makes prediction awfully complicated, but works!
    :   (   (compoundStatement)=> compoundStatement  // [06.03.13] allows nested scopes, are we sure??
        |   (declaration)=> declaration SEMICOLON!
        |   expression SEMICOLON!
        |   (ifThenElseStatement)=> ifThenElseStatement
        |   ifThenStatement
        |   forStatement
        |   whileStatement
        |   s:SEMICOLON { #s.setType(EMPTY_STMT); #s.setText("EMPTY_STMT"); }
        )
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
compoundStatement
    :   lb:LBRACE^ { #lb.setType(STMT_LIST); #lb.setText("STMT_LIST"); }
        (statement)*
        RBRACE!
        ( (ERROR)=> errorBlock
        | // no fail block if no fail keyword
        )
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
errorBlock
    :   ERROR^ LBRACE! (LPAREN! expression RPAREN! statement)* RBRACE!
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
declaration!
    :   t:dataType v:varDefinition[#t]
    {   #declaration = #v; }
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
varDefinition[AST t]
    :   varDeclarator[getASTFactory().dupList(t)]  //dupList copies siblings
        (COMMA! varDeclarator[getASTFactory().dupList(t)])*
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
varDeclarator![AST t]
    :   id:IDENTIFIER v:varInitializer
    {   #varDeclarator = #([VAR_DEF,"VAR_DEF"], #t, #id, #v); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
varInitializer
    :   (ASSIGN^ initializer)?
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
initializer
    :   expression  // [06.03.13] can potentially add array initializer
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
ifThenStatement
    :   IF^ LPAREN! expression RPAREN! statement
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
ifThenElseStatement
    :   IF^ LPAREN! expression RPAREN! statement
        ELSE! statement
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
whileStatement
    :   WHILE^ LPAREN! expression RPAREN! statement
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
forStatement
    :   FOR^ LPAREN!
        ( (forInit SEMICOLON)=> traditionalForClause
        | forEachClause
        )
        RPAREN!
        statement
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
traditionalForClause
    :   forInit SEMICOLON!
        forCond SEMICOLON!
        forIter
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
forInit
    :   ((declaration)=> declaration | expressionList)?
    {   #forInit = #([FOR_INIT,"FOR_INIT"], #forInit); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
forCond
    :   (expression)?
    {   #forCond = #([FOR_COND,"FOR_COND"], #forCond); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
forIter
    :   (expressionList)?
    {   #forIter = #([FOR_ITER,"FOR_ITER"], #forIter); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
forEachClause
    :   p:parameterDeclaration COLON! expression
    {   #forEachClause = #([FOR_EACH,"FOR_EACH"], #forEachClause); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}

//////// expression ////////
// Extracted relevant operator expressions from Java grammar,
// AND incorporated Armani predicate expression support.
// Note that most of these expressions follow the pattern
//   thisLevelExpression :
//	   nextHigherPrecedenceExpression
//		   (OPERATOR nextHigherPrecedenceExpression)*
// which is a standard recursive definition for parsing an expression.
// The operators in Java+Armani have the following precedences:
// (unimplemented operators are marked with an X, see java grammar file for sample)
//    lowest  (13)  = *= /= %= += -=  X->  <<= >>= >>>= &= ^= |=
//          (12.8)  forall  exists
//          (12.5)  ->   (Armani IMPLIES)
//          (12.3)  <->  (Armani IFF)
//            (12)X ?:
//            (11)  ||
//            (10)  &&
//            ( 9)X |
//            ( 8)X ^
//            ( 7)X &
//            ( 6)  == !=
//            ( 5)  < <= > >=
//            ( 4)X << >>   instanceof
//            ( 3)  +(binary) -(binary)
//            ( 2)  * / %
//            ( 1)  ++ -- +(unary) -(unary)  ~  !   X-> (type)
//            (-1)  [] <-X   () (method call)  . (dot -- identifier qualification)
//            (-2)  new <-X  ()  (explicit parenthesis)
//
// the last two are not usually on a precedence chart; I put them in
// to point out that new has a higher precedence than '.', so you
// can validy use
//     new Frame().show()
// Note that the above precedence levels map to the rules below...
// Once you have a precedence chart, writing the appropriate rules as below
//   is usually very straightfoward
//

// mother of all expressions
expression
    :   assignmentExpression
    {   #expression = #([EXPR,"EXPR"], #expression); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
// list of expressions
expressionList
    :   expression (COMMA! expression)*
    {   #expressionList = #([EXPR_LIST,"EXPR_LIST"], #expressionList); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
// assignment expression (level 13)
assignmentExpression
    :   //conditionalExpression  //replaced with logicalOr
        booleanExpression
        ( ( ASSIGN^
          | PLUS_ASSIGN^
          | MINUS_ASSIGN^
          | STAR_ASSIGN^
          | DIV_ASSIGN^
          | MOD_ASSIGN^
/*X
          | SR_ASSIGN^
          | BSR_ASSIGN^
          | SL_ASSIGN^
          | BAND_ASSIGN^
          | BXOR_ASSIGN^
          | BOR_ASSIGN^
X*/
          )
          assignmentExpression
        )?
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
booleanExpression
    :  
    impliesExpression
    |   quantifiedExpression
    
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
quantifiedExpression
    :   ( FORALL^
        | e:EXISTS^
          (UNIQUE! { #e.setType(EXISTS_UNIQUE); #e.setText("exists unique"); })?
        )
        quantifierDeclaration
        IN! (setExpression | identifierPrimary)
        BAR! booleanExpression
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
quantifierDeclaration
    {
        LList idList = new LList();  // to track identifiers
    }
    :   id:IDENTIFIER  { idList.append(#id); }
        (COMMA! id2:IDENTIFIER  { idList.append(#id2); })*
        COLON! t:dataType
    {
        ASTArray params = new ASTArray(idList.length()+1);  //+1 for root
        params.add(getASTFactory().create(PARAM_LIST,"PARAM_LIST"));
        while (idList.length() > 0) {
            AST type = getASTFactory().dup(#t);
            AST param = getASTFactory().dup((AST )idList.pop());
            params.add(#([PARAM,"PARAM"], type, param));
        }
        #quantifierDeclaration = getASTFactory().make(params);
    }
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
impliesExpression
    :   iffExpression (IMPLIES^ impliesExpression)?
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
iffExpression
    :   logicalOrExpression (IFF^ iffExpression)?
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
/*X
// conditional test (level 12)
conditionalExpression
    :   logicalOrExpression
        ( QUESTION^ assignmentExpression COLON! conditionalExpression )?
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
X*/
// logical OR (||) (level 11)
logicalOrExpression 
    :   logicalAndExpression (LOGICAL_OR^ logicalAndExpression)*
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
// logical AND (&&) (level 10)
logicalAndExpression 
    :   //inclusiveOrExpression (LOGICAL_AND^ inclusiveOrExpression)*
        equalityExpression (LOGICAL_AND^ equalityExpression)*
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
/*X
// bitwise or non-short-circuiting OR (|) (level 9)
inclusiveOrExpression 
    :   exclusiveOrExpression (BIT_OR^ exclusiveOrExpression)*
    ;
// exclusive OR (^) (level 8)
exclusiveOrExpression 
    :   andExpression (BIT_XOR^ andExpression)*
    ;
// bitwise or non-short-circuiting and (&) (level 7)
andExpression 
    :   equalityExpression (BIT_AND^ equalityExpression)*
    ;
X*/
// equality/inequality (==/!=) (level 6)
equalityExpression 
    :   relationalExpression ((NE^ | EQ^) relationalExpression)*
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
// boolean relational expressions (level 5)
relationalExpression 
    :   //shiftExpression ((LT^ | LE^ | GE^ | GT^) shiftExpression)* instaneof typeSpec[true]
        additiveExpression ((LT^ | LE^ | GE^ | GT^) additiveExpression)*
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
/*X
// bit shift expressions (level 4)
shiftExpression 
    :   additiveExpression
        ((SHIFT_LEFT^ | SHIFT_RIGHT^ | BIT_SHIFT_RIGHT^) additiveExpression)*
    ;
X*/
// binary addition/subtraction (level 3)
additiveExpression 
    :   multiplicativeExpression ((PLUS^ | MINUS^) multiplicativeExpression)*
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
// multiplication/division/modulo (level 2)
multiplicativeExpression 
    :   unaryExpression ((STAR^ | SLASH^ | MOD^) unaryExpression)*
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
// unary addition/subtraction (level 1)
unaryExpression 
    :   INCR^ unaryExpression
    |   DECR^ unaryExpression
    |   m:MINUS^ { #m.setType(UNARY_MINUS); } unaryExpression
    |   p:PLUS^  { #p.setType(UNARY_PLUS); }  unaryExpression
    |   unaryExpressionNotPlusMinus
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
//X:  removed the bit NOT and typecast stuff
unaryExpressionNotPlusMinus
    :   LOGICAL_NOT^ unaryExpression
    |   postfixExpression
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
// qualified names, array expressions, method invocation, post inc/dec (level 1/-1)
//X:  removed (1) type argument from qualified name, (2) array expression,
//X:          (3) super constructor call/this/new
postfixExpression
    :   primaryExpression
        ( in:INCR^ { #in.setType(POST_INCR); }
        | de:DECR^ { #de.setType(POST_DECR); }
        )?
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
// basic element of an expression
//X:  removed (1) .class, (2) this, (3) super, and (4) new expressions
primaryExpression
    :   identifierPrimary
    |   setExpression
    |   constant
    |   LPAREN! assignmentExpression RPAREN!
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
//X:  removed type argument, and no recursive dot notation for now [06.03.14]?
identifierPrimary
    :   IDENTIFIER
        ( lp:LPAREN^ { #lp.setType(METHOD_CALL); #lp.setText("METHOD_CALL"); }
          argList RPAREN!
        )?
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
argList
    :   ( expressionList
        | // nothing is ok
          { #argList = #[EXPR_LIST,"EXPR_LIST"]; }
        )
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
setExpression
    :   setConstructor
    |   literalSet
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
setConstructor
    :   LBRACE! SELECT^ quantifierDeclaration
        IN! (setExpression | identifierPrimary)
        BAR! booleanExpression RBRACE!
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
literalSet
    :   lb:LBRACE^ { #lb.setType(SET); #lb.setText("SET"); }
        ((identifierPrimary | constant) (COMMA! (identifierPrimary | constant))*)?
        RBRACE!
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
constant
    :   INTEGER_LIT
    |   FLOAT_LIT
    |   STRING_LIT
    |   CHAR_LIT
    |   TRUE
    |   FALSE
    |   NULL
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}


signature
    :   IDENTIFIER LPAREN! parameterList RPAREN!
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
parameterList
    :   (parameterDeclaration (COMMA! parameterDeclaration)*)?
    {   #parameterList = #([PARAM_LIST,"PARAM_LIST"], #parameterList); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
parameterDeclaration
    :   dataType IDENTIFIER
    {   #parameterDeclaration = #([PARAM,"PARAM"], #parameterDeclaration); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}

dataType  // declare this as a TYPE token
    :
    (   OBJECT
    |   INT
    |   FLOAT
    |   BOOLEAN
    |   CHAR
    |   STRING
    |   SET^ (LBRACE! dataType RBRACE!)?
    |   SEQUENCE^ (LT! dataType GT!)?
    |   RECORD^ (LBRACKET! (IDENTIFIER (COMMA! IDENTIFIER)* (COLON^ dataType)? SEMICOLON!)* RBRACKET!)?
    |   ENUM^ (LBRACE! (IDENTIFIER (COMMA! IDENTIFIER)*)? RBRACE!)?
    |   IDENTIFIER
    )
    {   #dataType.setType(TYPE); }
    ;
	exception catch [RecognitionException ex] {
		processError(ex, $FOLLOW);
	}
