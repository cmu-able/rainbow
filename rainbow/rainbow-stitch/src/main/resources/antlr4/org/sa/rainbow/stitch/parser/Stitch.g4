grammar Stitch;
options {
  language = Java;
}

@parser::header{
import org.sa.rainbow.stitch.error.StitchProblemHandler;
import org.sa.rainbow.stitch.error.StitchProblem;
import java.util.*;
}

tokens {
    TYPE, EXISTS_UNIQUE,UNARY_MINUS,UNARY_PLUS,POST_INCR,POST_DECR, DO_UNSPEC, EMPTY_STMT, STMT_LIST,
    EXRR_LIST, STRATEGY_BRANCH, METHOD_CALL, IMPORT_LIB, IMPORT_MODEL, IMPORT_OP
}


@parser::members{
	//ALI: ADDED stitchProblemHandler to parser
	private StitchProblemHandler stitchProblemHandler = null;
	private ArrayList<String> definedTactics = new ArrayList<String>();

	public void setStitchProblemHandler (StitchProblemHandler handler) {
		stitchProblemHandler = handler;
	}

	public ArrayList<String> getDefinedTactics() {
		return definedTactics;
	}

	public void defineTactic (Token id) {
	  definedTactics.add (id.getText ());
	}


// Replace this with an error listener, see http://stackoverflow.com/questions/18132078/handling-errors-in-antlr4
//	private void processError (RecognitionException ex, BitSet tokenSet)
//	throws TokenStreamException {
//		reportError(ex);
//		stitchProblemHandler.setProblem(new StitchProblem(ex, StitchProblem.ERROR));
//		recover(ex, tokenSet);
//	}
}

script
  : MODULE i=IDENTIFIER SEMICOLON /*{beh.createModule($i);}*/
  importSt* /*{beh.doImports();}*/
  functions
  tactic*
  strategy*
  EOF
  ;


importSt
  : i = IMPORT
    ( tl = LIB {((CommonToken )$i).setType(IMPORT_LIB);}
    | tm = MODEL {((CommonToken )$i).setType(IMPORT_MODEL);}
    | to = OP {((CommonToken )$i).setType(IMPORT_OP); }
    )
    s = STRING_LIT /*{beh.createImport($i, $s);}*/
    (LBRACE importRename (COMMA importRename)* RBRACE)? SEMICOLON
  ;


importRename
  : id1=IDENTIFIER AS id2=IDENTIFIER /*{beh.addImportRename(id1,id2);}*/
  ;

functions
  : /*{beh.beginVarList();}*/
    (var SEMICOLON)*
    /*{beh.endVarList();}*/
  ;


tactic
@after{defineTactic($id);}
  : TACTIC id=IDENTIFIER /*{beh.beginTactic($id);}*/
    LPAREN tacticParams? RPAREN LBRACE vars condition action effect RBRACE
//    {beh.endTactic();}
  ;

tacticParams
  : tacticParam (COMMA tacticParam)*
  ;

tacticParam
  : dataType IDENTIFIER
  ;

vars
  : /*{beh.beginVarList();}*/
    (var SEMICOLON)*
    /*{beh.endVarList();}*/
  ;

var
  : (FUNCTION | DEFINE)? t=dataType i=IDENTIFIER /*{beh.beginStatemetn($i);}*/ (ASSIGN expression)?
    /*{beh.createVar ($t, $i); beh.endStatement();}*/
  ;

condition
  :   CONDITION LBRACE (expression SEMICOLON)* RBRACE
  ;

action
  : ACTION LBRACE statement* RBRACE
  ;

effect
  : EFFECT (AT LBRACKET expression RBRACKET)? LBRACE (expression SEMICOLON)* RBRACE
  ;


strategy
  : STRATEGY IDENTIFIER
    LBRACKET expression RBRACKET
    LBRACE functions strategyNode* RBRACE
  ;


strategyNode
  : (IDENTIFIER COLON)?
    strategyCond
    IMPLIES
    tacticRef
  ;

strategyCond
  : LPAREN (HASH expression)?
    (expression | SUCCESS | FAILURE | DEFAULT)
    RPAREN
  ;

tacticRef
  : (t1=IDENTIFIER LPAREN (expression (COMMA expression)*)? RPAREN (AT LBRACKET expression RBRACKET)?
    LBRACE (strategyBranch | DONE) RBRACE)
  | DONE SEMICOLON
  | NULLTACTIC SEMICOLON
  | DO LBRACKET (v=IDENTIFIER | i=INTEGER_LIT)? RBRACKET t2=IDENTIFIER SEMICOLON
  | BAR DONE SEMICOLON
  | LBRACE strategyNode+ RBRACE
  /*| DO_UNSPEC*/
  ;

strategyBranch
  : strategyNode+
  ;

/*strategyOutcome
  : strategyClosedOutcome SEMICOLON
  | strategyOpenOutcome
    (strategyTimingExpr)?
    (BAR DONE SEMICOLON
    | strategyBranchOutcome
    )
  ;*/

/*
strategyClosedOutcome
  : DONE
  | NULLTACTIC
  | d = DO LBRACKET
    (IDENTIFIER | INTEGER_LIT | {((CommonToken )$d).setType(DO_UNSPEC);})
    RBRACKET IDENTIFIER
  ;

strategyOpenOutcome
  : IDENTIFIER LPAREN argList RPAREN
  ;

strategyBranchOutcome
  : lb = LBRACE {((CommonToken )$lb).setType(STRATEGY_BRANCH);}
    strategyExpr+ RBRACE
  ;

strategyProbExpr
  : HASH LBRACKET strategyProbValue RBRACKET
  ;

strategyProbValue
  : expression
  ;

strategyTimingExpr
  : AT LBRACKET strategyTimingValue RBRACKET
  ;

strategyTimingValue
  : expression
  ;
*/

statement
  : LBRACE statement* RBRACE errorHandler?
  | var SEMICOLON
  | expression SEMICOLON
  | ifStmt
  | whileStmt
  | forStmt
  | SEMICOLON
  ;

errorHandler
  : ERROR LBRACE (LPAREN expression RPAREN statement)* RBRACE
  ;


ifStmt
  : IF LPAREN expression RPAREN statement (ELSE statement)?
  ;

whileStmt
  : WHILE LPAREN expression RPAREN statement
  ;

forStmt
  : FOR LPAREN
    ( tacticParam COLON expression
    | forInit SEMICOLON forCond SEMICOLON forIter) RPAREN statement
  ;


forInit
  : (var+ | expression)?
  ;

forCond
  : expression?
  ;

forIter
  : expressions?
  ;

expressions
  : expression (COMMA expression)*
  ;

expression
  : assignmentExpression
  ;

assignmentExpression
  : booleanExpression
    ((ASSIGN | PLUS_ASSIGN | MINUS_ASSIGN | STAR_ASSIGN | DIV_ASSIGN | MOD_ASSIGN)
      assignmentExpression)?
  ;

booleanExpression
  : impliesExpression
  | quantifiedExpression
  ;

impliesExpression
  : iffExpression (IMPLIES impliesExpression)?
  ;

iffExpression
  : logicalOrExpression (IFF iffExpression)?
  ;

logicalOrExpression
  : logicalAndExpression (LOGICAL_OR logicalOrExpression)?
  ;

logicalAndExpression
  : equalityExpression (LOGICAL_AND logicalAndExpression)?
  ;

equalityExpression
  : relationalExpression ((NE | EQ) equalityExpression)?
  ;

relationalExpression
  : additiveExpression ((LT | LE | GE | GT) relationalExpression)?
  ;

additiveExpression
  : multiplicativeExpression ((PLUS | MINUS) additiveExpression)?
  ;

multiplicativeExpression
  : unaryExpression ((STAR | SLASH | MOD) multiplicativeExpression)?
  ;



//expression
//  : LPAREN e=expression RPAREN
//  | idExpression
//  | postIdExpression
//  | setExpression
//  | expression (STAR | SLASH | MOD) expression
//  //| expression SLASH expression
//  |// expression MOD expression
//  | expression (PLUS | MINUS) expression
//  //| expression MINUS expression
//  | expression (LT | LE | GE | GT) expression
////  | expression LE expression
////  | expression GE expression
////  | expression GT expression
//  | expression (EQ | NE) expression
//  //| expression EQ expression
//  | expression LOGICAL_AND expression
//  | expression LOGICAL_OR expression
//  | expression IFF expression
//  | expression IMPLIES expression
//  | unaryExpression
//  | quantifiedExpression
//  | assignmentExpression
//
//  ;
//
//
//assignmentExpression
//  : IDENTIFIER
//    (ASSIGN | PLUS_ASSIGN | MINUS_ASSIGN | STAR_ASSIGN | DIV_ASSIGN | MOD_ASSIGN)
//    expression
//  ;
//
//logicalExpression
//  : expression (IMPLIES | IFF | LOGICAL_OR | LOGICAL_AND) expression
//  ;
//
//relationalExpression
//  : expression (NE | EQ | LT | LE | GE | GT) expression
//  ;
//
//arithmeticExpression
//  : expression (PLUS | MINUS | STAR | SLASH | MOD) expression
//  ;

unaryExpression
  : INCR unaryExpression
  | DECR unaryExpression
  | MINUS unaryExpression
  | PLUS unaryExpression
  | LOGICAL_NOT unaryExpression
  | primaryExpression
  ;

primaryExpression
  : idExpression
  | postIdExpression
  | setExpression
  | pathExpression
  | LPAREN assignmentExpression RPAREN
  ;

idExpression
  : methodCall
  | IDENTIFIER
  | INTEGER_LIT
  | FLOAT_LIT
  | STRING_LIT
  | CHAR_LIT
  | TRUE
  | FALSE
  | NULL
  ;

postIdExpression
  : IDENTIFIER SQUOTE
  ;

methodCall
  : IDENTIFIER LPAREN expressions? RPAREN
  ;

params
  : param (COMMA param)*
  ;

param
  : IDENTIFIER COLON dataType
  ;

quantifiedExpression
  : FORALL
    params
    IN (setExpression | idExpression)
    BAR expression
  | e=EXISTS (UNIQUE {((CommonToken )$e).setType(EXISTS_UNIQUE);})?
    params
    IN (setExpression | idExpression)
    BAR expression
  | LBRACE? SELECT params IN (setExpression | idExpression) BAR expression RBRACE?
  ;

nonLiteralIdExpression
  : methodCall
  | IDENTIFIER
  ;


setExpression
  : literalSet
  ;

pathExpression
  : SLASH nonLiteralIdExpression ( (COLON | COLON_BANG) IDENTIFIER)? (LBRACKET expression RBRACKET)?
    pathExpressionContinuation?
  ;

pathExpressionContinuation
  : SLASH IDENTIFIER ( (COLON | COLON_BANG) IDENTIFIER)? (LBRACKET expression RBRACKET)? pathExpressionContinuation?
  ;

literalSet
  : lb=LBRACE {((CommonToken )$lb).setType(SET);}
      (expression (COMMA expression)*)?
    RBRACE
  ;

dataType

  : OBJECT
  | INT
  | FLOAT
  | BOOLEAN
  | CHAR
  | STRING
  | SET (LBRACE dataType RBRACE)?
  | SEQUENCE (LBRACE dataType RBRACE)
  | RECORD (LBRACKET (IDENTIFIER (COMMA IDENTIFIER)* COLON dataType SEMICOLON)* RBRACKET)?
  | ENUM (LBRACE (IDENTIFIER (COMMA IDENTIFIER)*)? RBRACE)?
  | i=IDENTIFIER {((CommonToken)$i).setType (TYPE);}
  ;



MODULE: 'module';
IMPORT: 'import';
LIB: 'lib';
MODEL: 'model';
OP: 'op';
AS: 'as';
TACTIC: 'tactic';
CONDITION: 'condition';
ACTION: 'action';
EFFECT: 'effect';
ERROR: 'error';
STRATEGY: 'strategy';
DEFINE: 'define';
FUNCTION: 'function';
SUCCESS: 'success';
FAILURE: 'failure';
DEFAULT: 'default';
NULLTACTIC: 'TNULL';
DO: 'do';
DONE: 'done';

IF: 'if';
ELSE: 'else';
FOR: 'for';
WHILE: 'while';

OBJECT: 'object';
INT: 'int';
FLOAT: 'float';
BOOLEAN: 'boolean';
CHAR: 'char';
STRING: 'string';
SET: 'set';
SEQUENCE: 'sequence';
RECORD: 'record';
ENUM: 'enum';

FORALL: 'forall';
EXISTS: 'exists';
UNIQUE: 'unique';
SELECT: 'select';
AND: 'and';
OR: 'or';
IN: 'in';

TRUE: 'true';
FALSE: 'false';
NULL: 'null';

IDENTIFIER
  : (UNDERSCORE)* LETTER (UNDERSCORE | DOT | LETTER | DIGIT | MINUS)*
  ;

SL_COMMENT
  : '//' (~('\r'|'\n'))* NL
  {
    setChannel(HIDDEN);
  }
  ;

ML_COMMENT
  : '/*' .*? '*/' {setChannel(HIDDEN);}
  ;

INTEGER_LIT
  : DIGIT+
  ;

FLOAT_LIT
  : DIGIT+ DOT DIGIT+
  ;


STRING_LIT
  : DQUOTE (~'"')* DQUOTE;

CHAR_LIT: SQUOTE ~'\'' SQUOTE;

LPAREN: '(';
RPAREN: ')';
LBRACKET: '[';
RBRACKET: ']';
LBRACE: '{';
RBRACE: '}';

COLON: ':';
SEMICOLON: ';';
COMMA: ',';
DOT: '.';
DQUOTE: '"';
SQUOTE: '\'';
BSLASH: '\\';
BAR: '|';
HASH: '#';
AT: '@';
DOLLAR: '$';

ASSIGN: '=';
PLUS_ASSIGN: '+=';
MINUS_ASSIGN: '-=';
STAR_ASSIGN: '*=';
DIV_ASSIGN: '/=';
MOD_ASSIGN: '%=';
COLON_BANG: ':!';
LOGICAL_OR: '||' | 'or';
LOGICAL_AND: '&&' | 'and';
EQ: '==';
NE: '!=';
LT: '<';
LE: '<=';
GE: '>=';
GT: '>';
PLUS: '+';
MINUS: '-';
STAR: '*';
SLASH: '/';
MOD: '%';
INCR: '++';
DECR: '--';
LOGICAL_NOT: '!';
IMPLIES: '->';
IFF: '<->';

LETTER: 'A'..'Z'|'a'..'z';
DIGIT: '0'..'9';
UNDERSCORE: '_';

NL
  : (('\r''\n')
    |'\r'
    |'\n'
    )
    {setChannel(HIDDEN); setLine (getLine ()+1);}
  ;

WS
  : (' ' | '\t') {setChannel(HIDDEN); }
  ;

