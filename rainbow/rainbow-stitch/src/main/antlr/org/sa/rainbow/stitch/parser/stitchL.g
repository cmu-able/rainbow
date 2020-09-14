/*
 * Author:   Shang-Wen Cheng (zensoul@cs.cmu.edu)
 * Created:  March 10, 2006
 * Purpose:  Define the grammar tree walker for Stitch, Rainbow's Qos-driven,
 *   Utility-informed, Architecture-based, System Adaptation Representation
 *   language (otherwise named the Rainbow QUASAR Language).
 * History:
 *   February 05, 2008 - added "define" keyword for strategy function;
 *       should generalize this to Functions later!
 */

header {package org.sa.rainbow.stitch.parser; 
}
options {
    language = "Java";
}

class StitchLexer extends Lexer;

options {
	k = 3;  // lookahead to disambiguate operators, handle newline & ML comment
	filter = true;
	charVocabulary = '\3'..'\377' | '\u1000'..'\u1fff';  // ascii + extended Unicode
	exportVocab = StitchLexer;
	testLiterals = false;
}

tokens {
	/* Language keywords */
	MODULE     = "module";
	IMPORT     = "import";
	LIB        = "lib";
	MODEL      = "model";
	OP         = "op";
	AS         = "as";
	TACTIC     = "tactic";
	CONDITION  = "condition";
	ACTION     = "action";
	EFFECT     = "effect";
    ERROR      = "error";
	STRATEGY   = "strategy";
	DEFINE     = "define";
	SUCCESS    = "success";
    FAILURE    = "failure";
	DEFAULT    = "default";
	NULLTACTIC = "TNULL";
	DO         = "do";
	DONE       = "done";

	/* Flow control keywords */
	IF         = "if";
	ELSE       = "else";
	FOR        = "for";
	WHILE      = "while";

	/* Data types keywords */
	TYPE;  // imaginary token
    OBJECT     = "object";
	INT        = "int";
	FLOAT      = "float";
	BOOLEAN    = "boolean";
	CHAR       = "char";
	STRING     = "string";
	SET        = "set";
	SEQUENCE   = "sequence";
	RECORD     = "record";
	ENUM       = "enum";

	/* First-order predicate constructs keywords */
	FORALL     = "forall";
	EXISTS     = "exists";
	UNIQUE     = "unique";
	EXISTS_UNIQUE;  // imaginary token
	SELECT     = "select";
	AND        = "and";
	OR         = "or";
	IN         = "in";

	/* Value literals */
	TRUE       = "true";
	FALSE      = "false";
	NULL       = "null";
	
	/* Operator imaginary tokens */
	UNARY_MINUS;
	UNARY_PLUS;
	POST_INCR;
	POST_DECR;
	FLOAT_LIT;
}

{
	boolean debug = false;

	private void debugln(String s) {
		if (debug) System.out.println(s);
	}

}

/* Identifer */
IDENTIFIER
options {
    paraphrase = "an identifier";
    testLiterals = true;
}
    : (UNDERSCORE)* LETTER (UNDERSCORE | DOT | LETTER | DIGIT | MINUS)*
    ;

/* Comments */
SL_COMMENT
    : "//" (~('\r'|'\n'))* NL
    {
        $setType(Token.SKIP);
        debugln("TODO:  handle single-line comment {{" + getText() + "}}");
    }
    ;
ML_COMMENT
    :    "/*"
         (  { LA(2)!='/' }? '*'
         | NL
         | ~('*'|'\r'|'\n')
         )*
         "*/"
    {
        $setType(Token.SKIP);
        debugln("TODO:  handle multi-line comment...{{" + getText() + "}}");
    }
    ;

/* Literals */
INTEGER_LIT
    :   (DIGIT)+
        ( DOT { $setType(FLOAT_LIT); }
          (DIGIT)+
        |
        )
    ;
STRING_LIT : DQUOTE (~'"')* DQUOTE ;
CHAR_LIT   : SQUOTE ~'\'' SQUOTE ;

/* Separators */
LPAREN  : '(' ;
RPAREN  : ')' ;
LBRACKET: '[' ;
RBRACKET: ']' ;
LBRACE  : '{' ;
RBRACE  : '}' ;

/* Punctuations */
COLON     : ':' ;
SEMICOLON : ';' ;
COMMA     : ',' ;
DOT       : '.' ;
DQUOTE    : '"' ;
SQUOTE    : '\'' ;
BSLASH    : '\\' ;
BAR       : '|' ;
HASH      : '#' ;
AT        : '@' ;
DOLLAR    : '$' ;

/* Operators */
ASSIGN      : '=' ;
PLUS_ASSIGN : "+=" ;
MINUS_ASSIGN: "-=" ;
STAR_ASSIGN : "*=" ;
DIV_ASSIGN  : "/=" ;
MOD_ASSIGN  : "%=" ;
COLON_BANG  : ":!" ;
LOGICAL_OR  : "||" ;
LOGICAL_AND : "&&" ;
EQ          : "==" ;
NE          : "!=" ;
LT          : "<" ;
LE          : "<=" ;
GE          : ">=" ;
GT          : ">" ;
PLUS        : '+' ;
MINUS       : '-' ;
STAR        : '*' ;
SLASH       : '/' ;
MOD         : '%' ;
INCR        : "++" ;
DECR        : "--" ;
LOGICAL_NOT : '!' ;
IMPLIES     : "->" ;
IFF         : "<->" ;

/* Invisible Literals */
protected LETTER    : 'A'..'Z'|'a'..'z' ;
protected DIGIT     : '0'..'9' ;
protected UNDERSCORE: '_' ;

/* White spaces */
NL
options { paraphrase = "a newline"; }
    : ( ('\r''\n')=> '\r''\n'  //DOS
    | '\r'                     //MAC
    | '\n'                     //UNIX
    )
    { 
        $setType(Token.SKIP);
        newline();
    }
    ;
WS  : ( ' ' | '\t')
    { $setType(Token.SKIP); }
    ;
