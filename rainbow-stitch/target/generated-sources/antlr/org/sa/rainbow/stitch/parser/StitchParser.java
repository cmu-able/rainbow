// $ANTLR 2.7.6 (2005-12-22): "stitchP.g" -> "StitchParser.java"$
package org.sa.rainbow.stitch.parser;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.collections.AST;
import java.util.Hashtable;
import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;
import antlr.collections.impl.LList;
import java.util.ArrayList;
import org.sa.rainbow.stitch.error.StitchProblem;
import org.sa.rainbow.stitch.error.StitchProblemHandler;

public class StitchParser extends antlr.LLkParser       implements StitchParserTokenTypes
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

protected StitchParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public StitchParser(TokenBuffer tokenBuf) {
  this(tokenBuf,2);
}

protected StitchParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public StitchParser(TokenStream lexer) {
  this(lexer,2);
}

public StitchParser(ParserSharedInputState state) {
  super(state,2);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	public final void script() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST script_AST = null;
		
		try {      // for error handling
			AST tmp1_AST = null;
			tmp1_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp1_AST);
			match(MODULE);
			AST tmp2_AST = null;
			tmp2_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp2_AST);
			match(IDENTIFIER);
			match(SEMICOLON);
			imports();
			astFactory.addASTChild(currentAST, returnAST);
			functions();
			astFactory.addASTChild(currentAST, returnAST);
			tactics();
			astFactory.addASTChild(currentAST, returnAST);
			strategies();
			astFactory.addASTChild(currentAST, returnAST);
			AST tmp4_AST = null;
			tmp4_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp4_AST);
			match(Token.EOF_TYPE);
			script_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_0);
					
			} else {
				throw ex;
			}
		}
		returnAST = script_AST;
	}
	
	public final void imports() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST imports_AST = null;
		
		try {      // for error handling
			{
			_loop4:
			do {
				if ((LA(1)==IMPORT)) {
					importSt();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop4;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				imports_AST = (AST)currentAST.root;
				imports_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(IMPORTS,"IMPORTS")).add(imports_AST));
				currentAST.root = imports_AST;
				currentAST.child = imports_AST!=null &&imports_AST.getFirstChild()!=null ?
					imports_AST.getFirstChild() : imports_AST;
				currentAST.advanceChildToEnd();
			}
			imports_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_1);
			} else {
			  throw ex;
			}
		}
		returnAST = imports_AST;
	}
	
	public final void functions() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST functions_AST = null;
		
		try {      // for error handling
			{
			_loop14:
			do {
				if ((LA(1)==DEFINE)) {
					function();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop14;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				functions_AST = (AST)currentAST.root;
				functions_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FUNC_LIST,"FUNC_LIST")).add(functions_AST));
				currentAST.root = functions_AST;
				currentAST.child = functions_AST!=null &&functions_AST.getFirstChild()!=null ?
					functions_AST.getFirstChild() : functions_AST;
				currentAST.advanceChildToEnd();
			}
			functions_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_2);
			} else {
			  throw ex;
			}
		}
		returnAST = functions_AST;
	}
	
	public final void tactics() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST tactics_AST = null;
		
		try {      // for error handling
			{
			_loop18:
			do {
				if ((LA(1)==TACTIC)) {
					tactic();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop18;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				tactics_AST = (AST)currentAST.root;
				tactics_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(TACTICS,"TACTICS")).add(tactics_AST));
				currentAST.root = tactics_AST;
				currentAST.child = tactics_AST!=null &&tactics_AST.getFirstChild()!=null ?
					tactics_AST.getFirstChild() : tactics_AST;
				currentAST.advanceChildToEnd();
			}
			tactics_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_3);
			} else {
			  throw ex;
			}
		}
		returnAST = tactics_AST;
	}
	
	public final void strategies() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST strategies_AST = null;
		
		try {      // for error handling
			{
			_loop35:
			do {
				if ((LA(1)==STRATEGY)) {
					strategy();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop35;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				strategies_AST = (AST)currentAST.root;
				strategies_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(STRATEGIES,"STRATEGIES")).add(strategies_AST));
				currentAST.root = strategies_AST;
				currentAST.child = strategies_AST!=null &&strategies_AST.getFirstChild()!=null ?
					strategies_AST.getFirstChild() : strategies_AST;
				currentAST.advanceChildToEnd();
			}
			strategies_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = strategies_AST;
	}
	
	public final void importSt() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST importSt_AST = null;
		Token  i = null;
		AST i_AST = null;
		Token  tl = null;
		AST tl_AST = null;
		Token  tm = null;
		AST tm_AST = null;
		Token  to = null;
		AST to_AST = null;
		
		try {      // for error handling
			i = LT(1);
			i_AST = astFactory.create(i);
			astFactory.makeASTRoot(currentAST, i_AST);
			match(IMPORT);
			{
			switch ( LA(1)) {
			case LIB:
			{
				tl = LT(1);
				tl_AST = astFactory.create(tl);
				match(LIB);
				if ( inputState.guessing==0 ) {
					i_AST.setType(IMPORT_LIB); i_AST.setText(tl_AST.getText());
				}
				break;
			}
			case MODEL:
			{
				tm = LT(1);
				tm_AST = astFactory.create(tm);
				match(MODEL);
				if ( inputState.guessing==0 ) {
					i_AST.setType(IMPORT_MODEL); i_AST.setText(tm_AST.getText());
				}
				break;
			}
			case OP:
			{
				to = LT(1);
				to_AST = astFactory.create(to);
				match(OP);
				if ( inputState.guessing==0 ) {
					i_AST.setType(IMPORT_OP); i_AST.setText(to_AST.getText());
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			AST tmp5_AST = null;
			tmp5_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp5_AST);
			match(STRING_LIT);
			{
			switch ( LA(1)) {
			case LBRACE:
			{
				importRenameClause();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case SEMICOLON:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(SEMICOLON);
			importSt_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_4);
					
			} else {
				throw ex;
			}
		}
		returnAST = importSt_AST;
	}
	
	public final void importRenameClause() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST importRenameClause_AST = null;
		
		try {      // for error handling
			match(LBRACE);
			importRenamePhrase();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop10:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					importRenamePhrase();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop10;
				}
				
			} while (true);
			}
			match(RBRACE);
			importRenameClause_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_5);
					
			} else {
				throw ex;
			}
		}
		returnAST = importRenameClause_AST;
	}
	
	public final void importRenamePhrase() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST importRenamePhrase_AST = null;
		
		try {      // for error handling
			AST tmp10_AST = null;
			tmp10_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp10_AST);
			match(IDENTIFIER);
			AST tmp11_AST = null;
			tmp11_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp11_AST);
			match(AS);
			AST tmp12_AST = null;
			tmp12_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp12_AST);
			match(IDENTIFIER);
			importRenamePhrase_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_6);
					
			} else {
				throw ex;
			}
		}
		returnAST = importRenamePhrase_AST;
	}
	
	public final void function() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST function_AST = null;
		
		try {      // for error handling
			match(DEFINE);
			declaration();
			astFactory.addASTChild(currentAST, returnAST);
			match(SEMICOLON);
			function_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
				processError(ex, _tokenSet_7);
				
			} else {
				throw ex;
			}
		}
		returnAST = function_AST;
	}
	
	public final void declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST declaration_AST = null;
		AST t_AST = null;
		AST v_AST = null;
		
		try {      // for error handling
			dataType();
			t_AST = (AST)returnAST;
			varDefinition(t_AST);
			v_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				declaration_AST = (AST)currentAST.root;
				declaration_AST = v_AST;
				currentAST.root = declaration_AST;
				currentAST.child = declaration_AST!=null &&declaration_AST.getFirstChild()!=null ?
					declaration_AST.getFirstChild() : declaration_AST;
				currentAST.advanceChildToEnd();
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_5);
					
			} else {
				throw ex;
			}
		}
		returnAST = declaration_AST;
	}
	
	public final void tactic() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST tactic_AST = null;
		
		try {      // for error handling
			AST tmp15_AST = null;
			tmp15_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp15_AST);
			match(TACTIC);
			signature();
			astFactory.addASTChild(currentAST, returnAST);
			match(LBRACE);
			tacticBody();
			astFactory.addASTChild(currentAST, returnAST);
			match(RBRACE);
			if ( inputState.guessing==0 ) {
				tactic_AST = (AST)currentAST.root;
				
						//Ali: Added (trying to catch all tactics and throw them into a ds that I can then give to the content assist processor)
						definedTactics.add(tactic_AST.getFirstChild());
					
			}
			tactic_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_8);
					
			} else {
				throw ex;
			}
		}
		returnAST = tactic_AST;
	}
	
	public final void signature() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST signature_AST = null;
		
		try {      // for error handling
			AST tmp18_AST = null;
			tmp18_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp18_AST);
			match(IDENTIFIER);
			match(LPAREN);
			parameterList();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			signature_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_9);
					
			} else {
				throw ex;
			}
		}
		returnAST = signature_AST;
	}
	
	public final void tacticBody() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST tacticBody_AST = null;
		
		try {      // for error handling
			tacticVars();
			astFactory.addASTChild(currentAST, returnAST);
			tacticConditionBlock();
			astFactory.addASTChild(currentAST, returnAST);
			tacticActionBlock();
			astFactory.addASTChild(currentAST, returnAST);
			tacticEffectBlock();
			astFactory.addASTChild(currentAST, returnAST);
			tacticBody_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_10);
					
			} else {
				throw ex;
			}
		}
		returnAST = tacticBody_AST;
	}
	
	public final void tacticVars() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST tacticVars_AST = null;
		
		try {      // for error handling
			{
			_loop23:
			do {
				if ((_tokenSet_11.member(LA(1)))) {
					declaration();
					astFactory.addASTChild(currentAST, returnAST);
					match(SEMICOLON);
				}
				else {
					break _loop23;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				tacticVars_AST = (AST)currentAST.root;
				tacticVars_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(VAR_LIST,"VAR_LIST")).add(tacticVars_AST));
				currentAST.root = tacticVars_AST;
				currentAST.child = tacticVars_AST!=null &&tacticVars_AST.getFirstChild()!=null ?
					tacticVars_AST.getFirstChild() : tacticVars_AST;
				currentAST.advanceChildToEnd();
			}
			tacticVars_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_12);
					
			} else {
				throw ex;
			}
		}
		returnAST = tacticVars_AST;
	}
	
	public final void tacticConditionBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST tacticConditionBlock_AST = null;
		
		try {      // for error handling
			AST tmp22_AST = null;
			tmp22_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp22_AST);
			match(CONDITION);
			match(LBRACE);
			{
			_loop26:
			do {
				if ((_tokenSet_13.member(LA(1)))) {
					expression();
					astFactory.addASTChild(currentAST, returnAST);
					match(SEMICOLON);
				}
				else {
					break _loop26;
				}
				
			} while (true);
			}
			match(RBRACE);
			tacticConditionBlock_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_14);
					
			} else {
				throw ex;
			}
		}
		returnAST = tacticConditionBlock_AST;
	}
	
	public final void tacticActionBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST tacticActionBlock_AST = null;
		
		try {      // for error handling
			AST tmp26_AST = null;
			tmp26_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp26_AST);
			match(ACTION);
			match(LBRACE);
			{
			_loop29:
			do {
				if ((_tokenSet_15.member(LA(1)))) {
					statement();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop29;
				}
				
			} while (true);
			}
			match(RBRACE);
			tacticActionBlock_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_16);
					
			} else {
				throw ex;
			}
		}
		returnAST = tacticActionBlock_AST;
	}
	
	public final void tacticEffectBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST tacticEffectBlock_AST = null;
		
		try {      // for error handling
			AST tmp29_AST = null;
			tmp29_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp29_AST);
			match(EFFECT);
			match(LBRACE);
			{
			_loop32:
			do {
				if ((_tokenSet_13.member(LA(1)))) {
					expression();
					astFactory.addASTChild(currentAST, returnAST);
					match(SEMICOLON);
				}
				else {
					break _loop32;
				}
				
			} while (true);
			}
			match(RBRACE);
			tacticEffectBlock_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_10);
					
			} else {
				throw ex;
			}
		}
		returnAST = tacticEffectBlock_AST;
	}
	
	public final void expression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expression_AST = null;
		
		try {      // for error handling
			assignmentExpression();
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				expression_AST = (AST)currentAST.root;
				expression_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(EXPR,"EXPR")).add(expression_AST));
				currentAST.root = expression_AST;
				currentAST.child = expression_AST!=null &&expression_AST.getFirstChild()!=null ?
					expression_AST.getFirstChild() : expression_AST;
				currentAST.advanceChildToEnd();
			}
			expression_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_17);
					
			} else {
				throw ex;
			}
		}
		returnAST = expression_AST;
	}
	
	public final void statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST statement_AST = null;
		Token  s = null;
		AST s_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case FOR:
			{
				forStatement();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case WHILE:
			{
				whileStatement();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case SEMICOLON:
			{
				s = LT(1);
				s_AST = astFactory.create(s);
				astFactory.addASTChild(currentAST, s_AST);
				match(SEMICOLON);
				if ( inputState.guessing==0 ) {
					s_AST.setType(EMPTY_STMT); s_AST.setText("EMPTY_STMT");
				}
				break;
			}
			default:
				boolean synPredMatched61 = false;
				if (((LA(1)==LBRACE) && (_tokenSet_18.member(LA(2))))) {
					int _m61 = mark();
					synPredMatched61 = true;
					inputState.guessing++;
					try {
						{
						compoundStatement();
						}
					}
					catch (RecognitionException pe) {
						synPredMatched61 = false;
					}
					rewind(_m61);
inputState.guessing--;
				}
				if ( synPredMatched61 ) {
					compoundStatement();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					boolean synPredMatched63 = false;
					if (((_tokenSet_11.member(LA(1))) && (_tokenSet_19.member(LA(2))))) {
						int _m63 = mark();
						synPredMatched63 = true;
						inputState.guessing++;
						try {
							{
							declaration();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched63 = false;
						}
						rewind(_m63);
inputState.guessing--;
					}
					if ( synPredMatched63 ) {
						declaration();
						astFactory.addASTChild(currentAST, returnAST);
						match(SEMICOLON);
					}
					else if ((_tokenSet_13.member(LA(1))) && (_tokenSet_20.member(LA(2)))) {
						expression();
						astFactory.addASTChild(currentAST, returnAST);
						match(SEMICOLON);
					}
					else {
						boolean synPredMatched65 = false;
						if (((LA(1)==IF) && (LA(2)==LPAREN))) {
							int _m65 = mark();
							synPredMatched65 = true;
							inputState.guessing++;
							try {
								{
								ifThenElseStatement();
								}
							}
							catch (RecognitionException pe) {
								synPredMatched65 = false;
							}
							rewind(_m65);
inputState.guessing--;
						}
						if ( synPredMatched65 ) {
							ifThenElseStatement();
							astFactory.addASTChild(currentAST, returnAST);
						}
						else if ((LA(1)==IF) && (LA(2)==LPAREN)) {
							ifThenStatement();
							astFactory.addASTChild(currentAST, returnAST);
						}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					}}}
					}
					statement_AST = (AST)currentAST.root;
				}
				catch (RecognitionException ex) {
					if (inputState.guessing==0) {
						
								processError(ex, _tokenSet_21);
							
					} else {
						throw ex;
					}
				}
				returnAST = statement_AST;
			}
			
	public final void strategy() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST strategy_AST = null;
		
		try {      // for error handling
			AST tmp35_AST = null;
			tmp35_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp35_AST);
			match(STRATEGY);
			AST tmp36_AST = null;
			tmp36_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp36_AST);
			match(IDENTIFIER);
			match(LBRACKET);
			expression();
			astFactory.addASTChild(currentAST, returnAST);
			match(RBRACKET);
			match(LBRACE);
			strategyBody();
			astFactory.addASTChild(currentAST, returnAST);
			match(RBRACE);
			strategy_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_3);
					
			} else {
				throw ex;
			}
		}
		returnAST = strategy_AST;
	}
	
	public final void strategyBody() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST strategyBody_AST = null;
		
		try {      // for error handling
			functions();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop39:
			do {
				if ((LA(1)==IDENTIFIER)) {
					strategyExpr();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop39;
				}
				
			} while (true);
			}
			strategyBody_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_10);
					
			} else {
				throw ex;
			}
		}
		returnAST = strategyBody_AST;
	}
	
	public final void strategyExpr() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST strategyExpr_AST = null;
		
		try {      // for error handling
			AST tmp41_AST = null;
			tmp41_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp41_AST);
			match(IDENTIFIER);
			match(COLON);
			strategyCond();
			astFactory.addASTChild(currentAST, returnAST);
			match(IMPLIES);
			strategyOutcome();
			astFactory.addASTChild(currentAST, returnAST);
			strategyExpr_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_22);
					
			} else {
				throw ex;
			}
		}
		returnAST = strategyExpr_AST;
	}
	
	public final void strategyCond() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST strategyCond_AST = null;
		
		try {      // for error handling
			match(LPAREN);
			{
			switch ( LA(1)) {
			case HASH:
			{
				strategyProbExpr();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case SUCCESS:
			case FAILURE:
			case DEFAULT:
			case FORALL:
			case EXISTS:
			case TRUE:
			case FALSE:
			case NULL:
			case FLOAT_LIT:
			case IDENTIFIER:
			case INTEGER_LIT:
			case STRING_LIT:
			case CHAR_LIT:
			case LPAREN:
			case LBRACE:
			case PLUS:
			case MINUS:
			case INCR:
			case DECR:
			case LOGICAL_NOT:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case FORALL:
			case EXISTS:
			case TRUE:
			case FALSE:
			case NULL:
			case FLOAT_LIT:
			case IDENTIFIER:
			case INTEGER_LIT:
			case STRING_LIT:
			case CHAR_LIT:
			case LPAREN:
			case LBRACE:
			case PLUS:
			case MINUS:
			case INCR:
			case DECR:
			case LOGICAL_NOT:
			{
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case SUCCESS:
			{
				AST tmp45_AST = null;
				tmp45_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp45_AST);
				match(SUCCESS);
				break;
			}
			case FAILURE:
			{
				AST tmp46_AST = null;
				tmp46_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp46_AST);
				match(FAILURE);
				break;
			}
			case DEFAULT:
			{
				AST tmp47_AST = null;
				tmp47_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp47_AST);
				match(DEFAULT);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				strategyCond_AST = (AST)currentAST.root;
				strategyCond_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(STRATEGY_COND,"STRATEGY_COND")).add(strategyCond_AST));
				currentAST.root = strategyCond_AST;
				currentAST.child = strategyCond_AST!=null &&strategyCond_AST.getFirstChild()!=null ?
					strategyCond_AST.getFirstChild() : strategyCond_AST;
				currentAST.advanceChildToEnd();
			}
			strategyCond_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_23);
					
			} else {
				throw ex;
			}
		}
		returnAST = strategyCond_AST;
	}
	
	public final void strategyOutcome() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST strategyOutcome_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case NULLTACTIC:
			case DO:
			case DONE:
			{
				strategyClosedOutcome();
				astFactory.addASTChild(currentAST, returnAST);
				match(SEMICOLON);
				strategyOutcome_AST = (AST)currentAST.root;
				break;
			}
			case IDENTIFIER:
			{
				strategyOpenOutcome();
				astFactory.addASTChild(currentAST, returnAST);
				{
				switch ( LA(1)) {
				case AT:
				{
					strategyTimingExpr();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case LBRACE:
				case BAR:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				{
				switch ( LA(1)) {
				case BAR:
				{
					match(BAR);
					AST tmp51_AST = null;
					tmp51_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp51_AST);
					match(DONE);
					match(SEMICOLON);
					break;
				}
				case LBRACE:
				{
					strategyBranchOutcome();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				strategyOutcome_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_22);
					
			} else {
				throw ex;
			}
		}
		returnAST = strategyOutcome_AST;
	}
	
	public final void strategyProbExpr() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST strategyProbExpr_AST = null;
		
		try {      // for error handling
			AST tmp53_AST = null;
			tmp53_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp53_AST);
			match(HASH);
			match(LBRACKET);
			strategyProbValue();
			astFactory.addASTChild(currentAST, returnAST);
			match(RBRACKET);
			strategyProbExpr_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_24);
					
			} else {
				throw ex;
			}
		}
		returnAST = strategyProbExpr_AST;
	}
	
	public final void strategyClosedOutcome() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST strategyClosedOutcome_AST = null;
		Token  d = null;
		AST d_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case DONE:
			{
				AST tmp56_AST = null;
				tmp56_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp56_AST);
				match(DONE);
				strategyClosedOutcome_AST = (AST)currentAST.root;
				break;
			}
			case NULLTACTIC:
			{
				AST tmp57_AST = null;
				tmp57_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp57_AST);
				match(NULLTACTIC);
				strategyClosedOutcome_AST = (AST)currentAST.root;
				break;
			}
			case DO:
			{
				d = LT(1);
				d_AST = astFactory.create(d);
				astFactory.makeASTRoot(currentAST, d_AST);
				match(DO);
				match(LBRACKET);
				{
				switch ( LA(1)) {
				case IDENTIFIER:
				{
					AST tmp59_AST = null;
					tmp59_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp59_AST);
					match(IDENTIFIER);
					break;
				}
				case INTEGER_LIT:
				{
					AST tmp60_AST = null;
					tmp60_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp60_AST);
					match(INTEGER_LIT);
					break;
				}
				case RBRACKET:
				{
					if ( inputState.guessing==0 ) {
						d_AST.setType(DO_UNSPEC);
					}
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(RBRACKET);
				AST tmp62_AST = null;
				tmp62_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp62_AST);
				match(IDENTIFIER);
				strategyClosedOutcome_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_5);
					
			} else {
				throw ex;
			}
		}
		returnAST = strategyClosedOutcome_AST;
	}
	
	public final void strategyOpenOutcome() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST strategyOpenOutcome_AST = null;
		
		try {      // for error handling
			AST tmp63_AST = null;
			tmp63_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp63_AST);
			match(IDENTIFIER);
			match(LPAREN);
			argList();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			strategyOpenOutcome_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_25);
					
			} else {
				throw ex;
			}
		}
		returnAST = strategyOpenOutcome_AST;
	}
	
	public final void strategyTimingExpr() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST strategyTimingExpr_AST = null;
		
		try {      // for error handling
			AST tmp66_AST = null;
			tmp66_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp66_AST);
			match(AT);
			match(LBRACKET);
			strategyTimingValue();
			astFactory.addASTChild(currentAST, returnAST);
			match(RBRACKET);
			strategyTimingExpr_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_26);
					
			} else {
				throw ex;
			}
		}
		returnAST = strategyTimingExpr_AST;
	}
	
	public final void strategyBranchOutcome() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST strategyBranchOutcome_AST = null;
		Token  lb = null;
		AST lb_AST = null;
		
		try {      // for error handling
			lb = LT(1);
			lb_AST = astFactory.create(lb);
			astFactory.makeASTRoot(currentAST, lb_AST);
			match(LBRACE);
			if ( inputState.guessing==0 ) {
				lb_AST.setType(STRATEGY_BRANCH); lb_AST.setText("STRATEGY_BRANCH");
			}
			{
			int _cnt52=0;
			_loop52:
			do {
				if ((LA(1)==IDENTIFIER)) {
					strategyExpr();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					if ( _cnt52>=1 ) { break _loop52; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt52++;
			} while (true);
			}
			match(RBRACE);
			strategyBranchOutcome_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_22);
					
			} else {
				throw ex;
			}
		}
		returnAST = strategyBranchOutcome_AST;
	}
	
	public final void argList() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST argList_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case FORALL:
			case EXISTS:
			case TRUE:
			case FALSE:
			case NULL:
			case FLOAT_LIT:
			case IDENTIFIER:
			case INTEGER_LIT:
			case STRING_LIT:
			case CHAR_LIT:
			case LPAREN:
			case LBRACE:
			case PLUS:
			case MINUS:
			case INCR:
			case DECR:
			case LOGICAL_NOT:
			{
				expressionList();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case RPAREN:
			{
				if ( inputState.guessing==0 ) {
					argList_AST = (AST)currentAST.root;
					argList_AST = astFactory.create(EXPR_LIST,"EXPR_LIST");
					currentAST.root = argList_AST;
					currentAST.child = argList_AST!=null &&argList_AST.getFirstChild()!=null ?
						argList_AST.getFirstChild() : argList_AST;
					currentAST.advanceChildToEnd();
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			argList_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_27);
					
			} else {
				throw ex;
			}
		}
		returnAST = argList_AST;
	}
	
	public final void strategyProbValue() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST strategyProbValue_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case FLOAT_LIT:
			{
				AST tmp70_AST = null;
				tmp70_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp70_AST);
				match(FLOAT_LIT);
				strategyProbValue_AST = (AST)currentAST.root;
				break;
			}
			case IDENTIFIER:
			{
				AST tmp71_AST = null;
				tmp71_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp71_AST);
				match(IDENTIFIER);
				{
				switch ( LA(1)) {
				case LBRACE:
				{
					match(LBRACE);
					AST tmp73_AST = null;
					tmp73_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp73_AST);
					match(IDENTIFIER);
					match(RBRACE);
					break;
				}
				case RBRACKET:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				strategyProbValue_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_28);
					
			} else {
				throw ex;
			}
		}
		returnAST = strategyProbValue_AST;
	}
	
	public final void strategyTimingValue() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST strategyTimingValue_AST = null;
		
		try {      // for error handling
			expression();
			astFactory.addASTChild(currentAST, returnAST);
			strategyTimingValue_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_28);
					
			} else {
				throw ex;
			}
		}
		returnAST = strategyTimingValue_AST;
	}
	
	public final void compoundStatement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST compoundStatement_AST = null;
		Token  lb = null;
		AST lb_AST = null;
		
		try {      // for error handling
			lb = LT(1);
			lb_AST = astFactory.create(lb);
			astFactory.makeASTRoot(currentAST, lb_AST);
			match(LBRACE);
			if ( inputState.guessing==0 ) {
				lb_AST.setType(STMT_LIST); lb_AST.setText("STMT_LIST");
			}
			{
			_loop68:
			do {
				if ((_tokenSet_15.member(LA(1)))) {
					statement();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop68;
				}
				
			} while (true);
			}
			match(RBRACE);
			{
			switch ( LA(1)) {
			case ERROR:
			{
				errorBlock();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case IF:
			case ELSE:
			case FOR:
			case WHILE:
			case OBJECT:
			case INT:
			case FLOAT:
			case BOOLEAN:
			case CHAR:
			case STRING:
			case SET:
			case SEQUENCE:
			case RECORD:
			case ENUM:
			case FORALL:
			case EXISTS:
			case TRUE:
			case FALSE:
			case NULL:
			case FLOAT_LIT:
			case IDENTIFIER:
			case INTEGER_LIT:
			case STRING_LIT:
			case CHAR_LIT:
			case LPAREN:
			case LBRACE:
			case RBRACE:
			case SEMICOLON:
			case PLUS:
			case MINUS:
			case INCR:
			case DECR:
			case LOGICAL_NOT:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			compoundStatement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_21);
					
			} else {
				throw ex;
			}
		}
		returnAST = compoundStatement_AST;
	}
	
	public final void ifThenElseStatement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST ifThenElseStatement_AST = null;
		
		try {      // for error handling
			AST tmp76_AST = null;
			tmp76_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp76_AST);
			match(IF);
			match(LPAREN);
			expression();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			statement();
			astFactory.addASTChild(currentAST, returnAST);
			match(ELSE);
			statement();
			astFactory.addASTChild(currentAST, returnAST);
			ifThenElseStatement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_21);
					
			} else {
				throw ex;
			}
		}
		returnAST = ifThenElseStatement_AST;
	}
	
	public final void ifThenStatement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST ifThenStatement_AST = null;
		
		try {      // for error handling
			AST tmp80_AST = null;
			tmp80_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp80_AST);
			match(IF);
			match(LPAREN);
			expression();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			statement();
			astFactory.addASTChild(currentAST, returnAST);
			ifThenStatement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_21);
					
			} else {
				throw ex;
			}
		}
		returnAST = ifThenStatement_AST;
	}
	
	public final void forStatement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forStatement_AST = null;
		
		try {      // for error handling
			AST tmp83_AST = null;
			tmp83_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp83_AST);
			match(FOR);
			match(LPAREN);
			{
			boolean synPredMatched89 = false;
			if (((_tokenSet_29.member(LA(1))) && (_tokenSet_30.member(LA(2))))) {
				int _m89 = mark();
				synPredMatched89 = true;
				inputState.guessing++;
				try {
					{
					forInit();
					match(SEMICOLON);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched89 = false;
				}
				rewind(_m89);
inputState.guessing--;
			}
			if ( synPredMatched89 ) {
				traditionalForClause();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((_tokenSet_11.member(LA(1))) && (_tokenSet_19.member(LA(2)))) {
				forEachClause();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(RPAREN);
			statement();
			astFactory.addASTChild(currentAST, returnAST);
			forStatement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_21);
					
			} else {
				throw ex;
			}
		}
		returnAST = forStatement_AST;
	}
	
	public final void whileStatement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST whileStatement_AST = null;
		
		try {      // for error handling
			AST tmp86_AST = null;
			tmp86_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp86_AST);
			match(WHILE);
			match(LPAREN);
			expression();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			statement();
			astFactory.addASTChild(currentAST, returnAST);
			whileStatement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_21);
					
			} else {
				throw ex;
			}
		}
		returnAST = whileStatement_AST;
	}
	
	public final void errorBlock() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST errorBlock_AST = null;
		
		try {      // for error handling
			AST tmp89_AST = null;
			tmp89_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp89_AST);
			match(ERROR);
			match(LBRACE);
			{
			_loop74:
			do {
				if ((LA(1)==LPAREN)) {
					match(LPAREN);
					expression();
					astFactory.addASTChild(currentAST, returnAST);
					match(RPAREN);
					statement();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop74;
				}
				
			} while (true);
			}
			match(RBRACE);
			errorBlock_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_21);
					
			} else {
				throw ex;
			}
		}
		returnAST = errorBlock_AST;
	}
	
	public final void dataType() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST dataType_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case OBJECT:
			{
				AST tmp94_AST = null;
				tmp94_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp94_AST);
				match(OBJECT);
				break;
			}
			case INT:
			{
				AST tmp95_AST = null;
				tmp95_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp95_AST);
				match(INT);
				break;
			}
			case FLOAT:
			{
				AST tmp96_AST = null;
				tmp96_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp96_AST);
				match(FLOAT);
				break;
			}
			case BOOLEAN:
			{
				AST tmp97_AST = null;
				tmp97_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp97_AST);
				match(BOOLEAN);
				break;
			}
			case CHAR:
			{
				AST tmp98_AST = null;
				tmp98_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp98_AST);
				match(CHAR);
				break;
			}
			case STRING:
			{
				AST tmp99_AST = null;
				tmp99_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp99_AST);
				match(STRING);
				break;
			}
			case SET:
			{
				AST tmp100_AST = null;
				tmp100_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp100_AST);
				match(SET);
				{
				switch ( LA(1)) {
				case LBRACE:
				{
					match(LBRACE);
					dataType();
					astFactory.addASTChild(currentAST, returnAST);
					match(RBRACE);
					break;
				}
				case IN:
				case IDENTIFIER:
				case RBRACE:
				case SEMICOLON:
				case GT:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				break;
			}
			case SEQUENCE:
			{
				AST tmp103_AST = null;
				tmp103_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp103_AST);
				match(SEQUENCE);
				{
				switch ( LA(1)) {
				case LT:
				{
					match(LT);
					dataType();
					astFactory.addASTChild(currentAST, returnAST);
					match(GT);
					break;
				}
				case IN:
				case IDENTIFIER:
				case RBRACE:
				case SEMICOLON:
				case GT:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				break;
			}
			case RECORD:
			{
				AST tmp106_AST = null;
				tmp106_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp106_AST);
				match(RECORD);
				{
				switch ( LA(1)) {
				case LBRACKET:
				{
					match(LBRACKET);
					{
					_loop175:
					do {
						if ((LA(1)==IDENTIFIER)) {
							AST tmp108_AST = null;
							tmp108_AST = astFactory.create(LT(1));
							astFactory.addASTChild(currentAST, tmp108_AST);
							match(IDENTIFIER);
							{
							_loop173:
							do {
								if ((LA(1)==COMMA)) {
									match(COMMA);
									AST tmp110_AST = null;
									tmp110_AST = astFactory.create(LT(1));
									astFactory.addASTChild(currentAST, tmp110_AST);
									match(IDENTIFIER);
								}
								else {
									break _loop173;
								}
								
							} while (true);
							}
							{
							switch ( LA(1)) {
							case COLON:
							{
								AST tmp111_AST = null;
								tmp111_AST = astFactory.create(LT(1));
								astFactory.makeASTRoot(currentAST, tmp111_AST);
								match(COLON);
								dataType();
								astFactory.addASTChild(currentAST, returnAST);
								break;
							}
							case SEMICOLON:
							{
								break;
							}
							default:
							{
								throw new NoViableAltException(LT(1), getFilename());
							}
							}
							}
							match(SEMICOLON);
						}
						else {
							break _loop175;
						}
						
					} while (true);
					}
					match(RBRACKET);
					break;
				}
				case IN:
				case IDENTIFIER:
				case RBRACE:
				case SEMICOLON:
				case GT:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				break;
			}
			case ENUM:
			{
				AST tmp114_AST = null;
				tmp114_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp114_AST);
				match(ENUM);
				{
				switch ( LA(1)) {
				case LBRACE:
				{
					match(LBRACE);
					{
					switch ( LA(1)) {
					case IDENTIFIER:
					{
						AST tmp116_AST = null;
						tmp116_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp116_AST);
						match(IDENTIFIER);
						{
						_loop179:
						do {
							if ((LA(1)==COMMA)) {
								match(COMMA);
								AST tmp118_AST = null;
								tmp118_AST = astFactory.create(LT(1));
								astFactory.addASTChild(currentAST, tmp118_AST);
								match(IDENTIFIER);
							}
							else {
								break _loop179;
							}
							
						} while (true);
						}
						break;
					}
					case RBRACE:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					match(RBRACE);
					break;
				}
				case IN:
				case IDENTIFIER:
				case RBRACE:
				case SEMICOLON:
				case GT:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				break;
			}
			case IDENTIFIER:
			{
				AST tmp120_AST = null;
				tmp120_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp120_AST);
				match(IDENTIFIER);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				dataType_AST = (AST)currentAST.root;
				dataType_AST.setType(TYPE);
			}
			dataType_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_31);
					
			} else {
				throw ex;
			}
		}
		returnAST = dataType_AST;
	}
	
	public final void varDefinition(
		AST t
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST varDefinition_AST = null;
		
		try {      // for error handling
			varDeclarator(getASTFactory().dupList(t));
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop78:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					varDeclarator(getASTFactory().dupList(t));
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop78;
				}
				
			} while (true);
			}
			varDefinition_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_5);
					
			} else {
				throw ex;
			}
		}
		returnAST = varDefinition_AST;
	}
	
	public final void varDeclarator(
		AST t
	) throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST varDeclarator_AST = null;
		Token  id = null;
		AST id_AST = null;
		AST v_AST = null;
		
		try {      // for error handling
			id = LT(1);
			id_AST = astFactory.create(id);
			match(IDENTIFIER);
			varInitializer();
			v_AST = (AST)returnAST;
			if ( inputState.guessing==0 ) {
				varDeclarator_AST = (AST)currentAST.root;
				varDeclarator_AST = (AST)astFactory.make( (new ASTArray(4)).add(astFactory.create(VAR_DEF,"VAR_DEF")).add(t).add(id_AST).add(v_AST));
				currentAST.root = varDeclarator_AST;
				currentAST.child = varDeclarator_AST!=null &&varDeclarator_AST.getFirstChild()!=null ?
					varDeclarator_AST.getFirstChild() : varDeclarator_AST;
				currentAST.advanceChildToEnd();
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_32);
					
			} else {
				throw ex;
			}
		}
		returnAST = varDeclarator_AST;
	}
	
	public final void varInitializer() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST varInitializer_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case ASSIGN:
			{
				AST tmp122_AST = null;
				tmp122_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp122_AST);
				match(ASSIGN);
				initializer();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case SEMICOLON:
			case COMMA:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			varInitializer_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_32);
					
			} else {
				throw ex;
			}
		}
		returnAST = varInitializer_AST;
	}
	
	public final void initializer() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST initializer_AST = null;
		
		try {      // for error handling
			expression();
			astFactory.addASTChild(currentAST, returnAST);
			initializer_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_32);
					
			} else {
				throw ex;
			}
		}
		returnAST = initializer_AST;
	}
	
	public final void forInit() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forInit_AST = null;
		
		try {      // for error handling
			{
			boolean synPredMatched94 = false;
			if (((_tokenSet_11.member(LA(1))) && (_tokenSet_19.member(LA(2))))) {
				int _m94 = mark();
				synPredMatched94 = true;
				inputState.guessing++;
				try {
					{
					declaration();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched94 = false;
				}
				rewind(_m94);
inputState.guessing--;
			}
			if ( synPredMatched94 ) {
				declaration();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((_tokenSet_13.member(LA(1))) && (_tokenSet_33.member(LA(2)))) {
				expressionList();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else if ((LA(1)==SEMICOLON)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			if ( inputState.guessing==0 ) {
				forInit_AST = (AST)currentAST.root;
				forInit_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FOR_INIT,"FOR_INIT")).add(forInit_AST));
				currentAST.root = forInit_AST;
				currentAST.child = forInit_AST!=null &&forInit_AST.getFirstChild()!=null ?
					forInit_AST.getFirstChild() : forInit_AST;
				currentAST.advanceChildToEnd();
			}
			forInit_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_5);
					
			} else {
				throw ex;
			}
		}
		returnAST = forInit_AST;
	}
	
	public final void traditionalForClause() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST traditionalForClause_AST = null;
		
		try {      // for error handling
			forInit();
			astFactory.addASTChild(currentAST, returnAST);
			match(SEMICOLON);
			forCond();
			astFactory.addASTChild(currentAST, returnAST);
			match(SEMICOLON);
			forIter();
			astFactory.addASTChild(currentAST, returnAST);
			traditionalForClause_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_27);
					
			} else {
				throw ex;
			}
		}
		returnAST = traditionalForClause_AST;
	}
	
	public final void forEachClause() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forEachClause_AST = null;
		AST p_AST = null;
		
		try {      // for error handling
			parameterDeclaration();
			p_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
			match(COLON);
			expression();
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				forEachClause_AST = (AST)currentAST.root;
				forEachClause_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FOR_EACH,"FOR_EACH")).add(forEachClause_AST));
				currentAST.root = forEachClause_AST;
				currentAST.child = forEachClause_AST!=null &&forEachClause_AST.getFirstChild()!=null ?
					forEachClause_AST.getFirstChild() : forEachClause_AST;
				currentAST.advanceChildToEnd();
			}
			forEachClause_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_27);
					
			} else {
				throw ex;
			}
		}
		returnAST = forEachClause_AST;
	}
	
	public final void forCond() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forCond_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case FORALL:
			case EXISTS:
			case TRUE:
			case FALSE:
			case NULL:
			case FLOAT_LIT:
			case IDENTIFIER:
			case INTEGER_LIT:
			case STRING_LIT:
			case CHAR_LIT:
			case LPAREN:
			case LBRACE:
			case PLUS:
			case MINUS:
			case INCR:
			case DECR:
			case LOGICAL_NOT:
			{
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case SEMICOLON:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				forCond_AST = (AST)currentAST.root;
				forCond_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FOR_COND,"FOR_COND")).add(forCond_AST));
				currentAST.root = forCond_AST;
				currentAST.child = forCond_AST!=null &&forCond_AST.getFirstChild()!=null ?
					forCond_AST.getFirstChild() : forCond_AST;
				currentAST.advanceChildToEnd();
			}
			forCond_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_5);
					
			} else {
				throw ex;
			}
		}
		returnAST = forCond_AST;
	}
	
	public final void forIter() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forIter_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case FORALL:
			case EXISTS:
			case TRUE:
			case FALSE:
			case NULL:
			case FLOAT_LIT:
			case IDENTIFIER:
			case INTEGER_LIT:
			case STRING_LIT:
			case CHAR_LIT:
			case LPAREN:
			case LBRACE:
			case PLUS:
			case MINUS:
			case INCR:
			case DECR:
			case LOGICAL_NOT:
			{
				expressionList();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case RPAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				forIter_AST = (AST)currentAST.root;
				forIter_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(FOR_ITER,"FOR_ITER")).add(forIter_AST));
				currentAST.root = forIter_AST;
				currentAST.child = forIter_AST!=null &&forIter_AST.getFirstChild()!=null ?
					forIter_AST.getFirstChild() : forIter_AST;
				currentAST.advanceChildToEnd();
			}
			forIter_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_27);
					
			} else {
				throw ex;
			}
		}
		returnAST = forIter_AST;
	}
	
	public final void expressionList() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expressionList_AST = null;
		
		try {      // for error handling
			expression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop103:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					expression();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop103;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				expressionList_AST = (AST)currentAST.root;
				expressionList_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(EXPR_LIST,"EXPR_LIST")).add(expressionList_AST));
				currentAST.root = expressionList_AST;
				currentAST.child = expressionList_AST!=null &&expressionList_AST.getFirstChild()!=null ?
					expressionList_AST.getFirstChild() : expressionList_AST;
				currentAST.advanceChildToEnd();
			}
			expressionList_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_34);
					
			} else {
				throw ex;
			}
		}
		returnAST = expressionList_AST;
	}
	
	public final void parameterDeclaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST parameterDeclaration_AST = null;
		
		try {      // for error handling
			dataType();
			astFactory.addASTChild(currentAST, returnAST);
			AST tmp127_AST = null;
			tmp127_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp127_AST);
			match(IDENTIFIER);
			if ( inputState.guessing==0 ) {
				parameterDeclaration_AST = (AST)currentAST.root;
				parameterDeclaration_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(PARAM,"PARAM")).add(parameterDeclaration_AST));
				currentAST.root = parameterDeclaration_AST;
				currentAST.child = parameterDeclaration_AST!=null &&parameterDeclaration_AST.getFirstChild()!=null ?
					parameterDeclaration_AST.getFirstChild() : parameterDeclaration_AST;
				currentAST.advanceChildToEnd();
			}
			parameterDeclaration_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_35);
					
			} else {
				throw ex;
			}
		}
		returnAST = parameterDeclaration_AST;
	}
	
	public final void assignmentExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST assignmentExpression_AST = null;
		
		try {      // for error handling
			booleanExpression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case ASSIGN:
			case PLUS_ASSIGN:
			case MINUS_ASSIGN:
			case STAR_ASSIGN:
			case DIV_ASSIGN:
			case MOD_ASSIGN:
			{
				{
				switch ( LA(1)) {
				case ASSIGN:
				{
					AST tmp128_AST = null;
					tmp128_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp128_AST);
					match(ASSIGN);
					break;
				}
				case PLUS_ASSIGN:
				{
					AST tmp129_AST = null;
					tmp129_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp129_AST);
					match(PLUS_ASSIGN);
					break;
				}
				case MINUS_ASSIGN:
				{
					AST tmp130_AST = null;
					tmp130_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp130_AST);
					match(MINUS_ASSIGN);
					break;
				}
				case STAR_ASSIGN:
				{
					AST tmp131_AST = null;
					tmp131_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp131_AST);
					match(STAR_ASSIGN);
					break;
				}
				case DIV_ASSIGN:
				{
					AST tmp132_AST = null;
					tmp132_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp132_AST);
					match(DIV_ASSIGN);
					break;
				}
				case MOD_ASSIGN:
				{
					AST tmp133_AST = null;
					tmp133_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp133_AST);
					match(MOD_ASSIGN);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				assignmentExpression();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case RPAREN:
			case RBRACKET:
			case SEMICOLON:
			case COMMA:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			assignmentExpression_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_17);
					
			} else {
				throw ex;
			}
		}
		returnAST = assignmentExpression_AST;
	}
	
	public final void booleanExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST booleanExpression_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case TRUE:
			case FALSE:
			case NULL:
			case FLOAT_LIT:
			case IDENTIFIER:
			case INTEGER_LIT:
			case STRING_LIT:
			case CHAR_LIT:
			case LPAREN:
			case LBRACE:
			case PLUS:
			case MINUS:
			case INCR:
			case DECR:
			case LOGICAL_NOT:
			{
				impliesExpression();
				astFactory.addASTChild(currentAST, returnAST);
				booleanExpression_AST = (AST)currentAST.root;
				break;
			}
			case FORALL:
			case EXISTS:
			{
				quantifiedExpression();
				astFactory.addASTChild(currentAST, returnAST);
				booleanExpression_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_36);
					
			} else {
				throw ex;
			}
		}
		returnAST = booleanExpression_AST;
	}
	
	public final void impliesExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST impliesExpression_AST = null;
		
		try {      // for error handling
			iffExpression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case IMPLIES:
			{
				AST tmp134_AST = null;
				tmp134_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp134_AST);
				match(IMPLIES);
				impliesExpression();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case RPAREN:
			case RBRACKET:
			case RBRACE:
			case SEMICOLON:
			case COMMA:
			case ASSIGN:
			case PLUS_ASSIGN:
			case MINUS_ASSIGN:
			case STAR_ASSIGN:
			case DIV_ASSIGN:
			case MOD_ASSIGN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			impliesExpression_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_36);
					
			} else {
				throw ex;
			}
		}
		returnAST = impliesExpression_AST;
	}
	
	public final void quantifiedExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST quantifiedExpression_AST = null;
		Token  e = null;
		AST e_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case FORALL:
			{
				AST tmp135_AST = null;
				tmp135_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp135_AST);
				match(FORALL);
				break;
			}
			case EXISTS:
			{
				e = LT(1);
				e_AST = astFactory.create(e);
				astFactory.makeASTRoot(currentAST, e_AST);
				match(EXISTS);
				{
				switch ( LA(1)) {
				case UNIQUE:
				{
					match(UNIQUE);
					if ( inputState.guessing==0 ) {
						e_AST.setType(EXISTS_UNIQUE); e_AST.setText("exists unique");
					}
					break;
				}
				case IDENTIFIER:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			quantifierDeclaration();
			astFactory.addASTChild(currentAST, returnAST);
			match(IN);
			{
			switch ( LA(1)) {
			case LBRACE:
			{
				setExpression();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case IDENTIFIER:
			{
				identifierPrimary();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(BAR);
			booleanExpression();
			astFactory.addASTChild(currentAST, returnAST);
			quantifiedExpression_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_36);
					
			} else {
				throw ex;
			}
		}
		returnAST = quantifiedExpression_AST;
	}
	
	public final void quantifierDeclaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST quantifierDeclaration_AST = null;
		Token  id = null;
		AST id_AST = null;
		Token  id2 = null;
		AST id2_AST = null;
		AST t_AST = null;
		
		LList idList = new LList();  // to track identifiers
		
		
		try {      // for error handling
			id = LT(1);
			id_AST = astFactory.create(id);
			astFactory.addASTChild(currentAST, id_AST);
			match(IDENTIFIER);
			if ( inputState.guessing==0 ) {
				idList.append(id_AST);
			}
			{
			_loop114:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					id2 = LT(1);
					id2_AST = astFactory.create(id2);
					astFactory.addASTChild(currentAST, id2_AST);
					match(IDENTIFIER);
					if ( inputState.guessing==0 ) {
						idList.append(id2_AST);
					}
				}
				else {
					break _loop114;
				}
				
			} while (true);
			}
			match(COLON);
			dataType();
			t_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
			if ( inputState.guessing==0 ) {
				quantifierDeclaration_AST = (AST)currentAST.root;
				
				ASTArray params = new ASTArray(idList.length()+1);  //+1 for root
				params.add(getASTFactory().create(PARAM_LIST,"PARAM_LIST"));
				while (idList.length() > 0) {
				AST type = getASTFactory().dup(t_AST);
				AST param = getASTFactory().dup((AST )idList.pop());
				params.add((AST)astFactory.make( (new ASTArray(3)).add(astFactory.create(PARAM,"PARAM")).add(type).add(param)));
				}
				quantifierDeclaration_AST = getASTFactory().make(params);
				
				currentAST.root = quantifierDeclaration_AST;
				currentAST.child = quantifierDeclaration_AST!=null &&quantifierDeclaration_AST.getFirstChild()!=null ?
					quantifierDeclaration_AST.getFirstChild() : quantifierDeclaration_AST;
				currentAST.advanceChildToEnd();
			}
			quantifierDeclaration_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_37);
					
			} else {
				throw ex;
			}
		}
		returnAST = quantifierDeclaration_AST;
	}
	
	public final void setExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST setExpression_AST = null;
		
		try {      // for error handling
			if ((LA(1)==LBRACE) && (LA(2)==SELECT)) {
				setConstructor();
				astFactory.addASTChild(currentAST, returnAST);
				setExpression_AST = (AST)currentAST.root;
			}
			else if ((LA(1)==LBRACE) && (_tokenSet_38.member(LA(2)))) {
				literalSet();
				astFactory.addASTChild(currentAST, returnAST);
				setExpression_AST = (AST)currentAST.root;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_39);
					
			} else {
				throw ex;
			}
		}
		returnAST = setExpression_AST;
	}
	
	public final void identifierPrimary() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST identifierPrimary_AST = null;
		Token  lp = null;
		AST lp_AST = null;
		
		try {      // for error handling
			AST tmp141_AST = null;
			tmp141_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp141_AST);
			match(IDENTIFIER);
			{
			switch ( LA(1)) {
			case LPAREN:
			{
				lp = LT(1);
				lp_AST = astFactory.create(lp);
				astFactory.makeASTRoot(currentAST, lp_AST);
				match(LPAREN);
				if ( inputState.guessing==0 ) {
					lp_AST.setType(METHOD_CALL); lp_AST.setText("METHOD_CALL");
				}
				argList();
				astFactory.addASTChild(currentAST, returnAST);
				match(RPAREN);
				break;
			}
			case RPAREN:
			case RBRACKET:
			case RBRACE:
			case SEMICOLON:
			case COMMA:
			case BAR:
			case ASSIGN:
			case PLUS_ASSIGN:
			case MINUS_ASSIGN:
			case STAR_ASSIGN:
			case DIV_ASSIGN:
			case MOD_ASSIGN:
			case LOGICAL_OR:
			case LOGICAL_AND:
			case EQ:
			case NE:
			case LT:
			case LE:
			case GE:
			case GT:
			case PLUS:
			case MINUS:
			case STAR:
			case SLASH:
			case MOD:
			case INCR:
			case DECR:
			case IMPLIES:
			case IFF:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			identifierPrimary_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_39);
					
			} else {
				throw ex;
			}
		}
		returnAST = identifierPrimary_AST;
	}
	
	public final void iffExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST iffExpression_AST = null;
		
		try {      // for error handling
			logicalOrExpression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case IFF:
			{
				AST tmp143_AST = null;
				tmp143_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp143_AST);
				match(IFF);
				iffExpression();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case RPAREN:
			case RBRACKET:
			case RBRACE:
			case SEMICOLON:
			case COMMA:
			case ASSIGN:
			case PLUS_ASSIGN:
			case MINUS_ASSIGN:
			case STAR_ASSIGN:
			case DIV_ASSIGN:
			case MOD_ASSIGN:
			case IMPLIES:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			iffExpression_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_40);
					
			} else {
				throw ex;
			}
		}
		returnAST = iffExpression_AST;
	}
	
	public final void logicalOrExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logicalOrExpression_AST = null;
		
		try {      // for error handling
			logicalAndExpression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop121:
			do {
				if ((LA(1)==LOGICAL_OR)) {
					AST tmp144_AST = null;
					tmp144_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp144_AST);
					match(LOGICAL_OR);
					logicalAndExpression();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop121;
				}
				
			} while (true);
			}
			logicalOrExpression_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_41);
					
			} else {
				throw ex;
			}
		}
		returnAST = logicalOrExpression_AST;
	}
	
	public final void logicalAndExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logicalAndExpression_AST = null;
		
		try {      // for error handling
			equalityExpression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop124:
			do {
				if ((LA(1)==LOGICAL_AND)) {
					AST tmp145_AST = null;
					tmp145_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp145_AST);
					match(LOGICAL_AND);
					equalityExpression();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop124;
				}
				
			} while (true);
			}
			logicalAndExpression_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_42);
					
			} else {
				throw ex;
			}
		}
		returnAST = logicalAndExpression_AST;
	}
	
	public final void equalityExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST equalityExpression_AST = null;
		
		try {      // for error handling
			relationalExpression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop128:
			do {
				if ((LA(1)==EQ||LA(1)==NE)) {
					{
					switch ( LA(1)) {
					case NE:
					{
						AST tmp146_AST = null;
						tmp146_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp146_AST);
						match(NE);
						break;
					}
					case EQ:
					{
						AST tmp147_AST = null;
						tmp147_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp147_AST);
						match(EQ);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					relationalExpression();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop128;
				}
				
			} while (true);
			}
			equalityExpression_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_43);
					
			} else {
				throw ex;
			}
		}
		returnAST = equalityExpression_AST;
	}
	
	public final void relationalExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST relationalExpression_AST = null;
		
		try {      // for error handling
			additiveExpression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop132:
			do {
				if (((LA(1) >= LT && LA(1) <= GT))) {
					{
					switch ( LA(1)) {
					case LT:
					{
						AST tmp148_AST = null;
						tmp148_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp148_AST);
						match(LT);
						break;
					}
					case LE:
					{
						AST tmp149_AST = null;
						tmp149_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp149_AST);
						match(LE);
						break;
					}
					case GE:
					{
						AST tmp150_AST = null;
						tmp150_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp150_AST);
						match(GE);
						break;
					}
					case GT:
					{
						AST tmp151_AST = null;
						tmp151_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp151_AST);
						match(GT);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					additiveExpression();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop132;
				}
				
			} while (true);
			}
			relationalExpression_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_44);
					
			} else {
				throw ex;
			}
		}
		returnAST = relationalExpression_AST;
	}
	
	public final void additiveExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST additiveExpression_AST = null;
		
		try {      // for error handling
			multiplicativeExpression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop136:
			do {
				if ((LA(1)==PLUS||LA(1)==MINUS)) {
					{
					switch ( LA(1)) {
					case PLUS:
					{
						AST tmp152_AST = null;
						tmp152_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp152_AST);
						match(PLUS);
						break;
					}
					case MINUS:
					{
						AST tmp153_AST = null;
						tmp153_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp153_AST);
						match(MINUS);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					multiplicativeExpression();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop136;
				}
				
			} while (true);
			}
			additiveExpression_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_45);
					
			} else {
				throw ex;
			}
		}
		returnAST = additiveExpression_AST;
	}
	
	public final void multiplicativeExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST multiplicativeExpression_AST = null;
		
		try {      // for error handling
			unaryExpression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop140:
			do {
				if (((LA(1) >= STAR && LA(1) <= MOD))) {
					{
					switch ( LA(1)) {
					case STAR:
					{
						AST tmp154_AST = null;
						tmp154_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp154_AST);
						match(STAR);
						break;
					}
					case SLASH:
					{
						AST tmp155_AST = null;
						tmp155_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp155_AST);
						match(SLASH);
						break;
					}
					case MOD:
					{
						AST tmp156_AST = null;
						tmp156_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp156_AST);
						match(MOD);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					unaryExpression();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop140;
				}
				
			} while (true);
			}
			multiplicativeExpression_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_46);
					
			} else {
				throw ex;
			}
		}
		returnAST = multiplicativeExpression_AST;
	}
	
	public final void unaryExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST unaryExpression_AST = null;
		Token  m = null;
		AST m_AST = null;
		Token  p = null;
		AST p_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case INCR:
			{
				AST tmp157_AST = null;
				tmp157_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp157_AST);
				match(INCR);
				unaryExpression();
				astFactory.addASTChild(currentAST, returnAST);
				unaryExpression_AST = (AST)currentAST.root;
				break;
			}
			case DECR:
			{
				AST tmp158_AST = null;
				tmp158_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp158_AST);
				match(DECR);
				unaryExpression();
				astFactory.addASTChild(currentAST, returnAST);
				unaryExpression_AST = (AST)currentAST.root;
				break;
			}
			case MINUS:
			{
				m = LT(1);
				m_AST = astFactory.create(m);
				astFactory.makeASTRoot(currentAST, m_AST);
				match(MINUS);
				if ( inputState.guessing==0 ) {
					m_AST.setType(UNARY_MINUS);
				}
				unaryExpression();
				astFactory.addASTChild(currentAST, returnAST);
				unaryExpression_AST = (AST)currentAST.root;
				break;
			}
			case PLUS:
			{
				p = LT(1);
				p_AST = astFactory.create(p);
				astFactory.makeASTRoot(currentAST, p_AST);
				match(PLUS);
				if ( inputState.guessing==0 ) {
					p_AST.setType(UNARY_PLUS);
				}
				unaryExpression();
				astFactory.addASTChild(currentAST, returnAST);
				unaryExpression_AST = (AST)currentAST.root;
				break;
			}
			case TRUE:
			case FALSE:
			case NULL:
			case FLOAT_LIT:
			case IDENTIFIER:
			case INTEGER_LIT:
			case STRING_LIT:
			case CHAR_LIT:
			case LPAREN:
			case LBRACE:
			case LOGICAL_NOT:
			{
				unaryExpressionNotPlusMinus();
				astFactory.addASTChild(currentAST, returnAST);
				unaryExpression_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_47);
					
			} else {
				throw ex;
			}
		}
		returnAST = unaryExpression_AST;
	}
	
	public final void unaryExpressionNotPlusMinus() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST unaryExpressionNotPlusMinus_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LOGICAL_NOT:
			{
				AST tmp159_AST = null;
				tmp159_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp159_AST);
				match(LOGICAL_NOT);
				unaryExpression();
				astFactory.addASTChild(currentAST, returnAST);
				unaryExpressionNotPlusMinus_AST = (AST)currentAST.root;
				break;
			}
			case TRUE:
			case FALSE:
			case NULL:
			case FLOAT_LIT:
			case IDENTIFIER:
			case INTEGER_LIT:
			case STRING_LIT:
			case CHAR_LIT:
			case LPAREN:
			case LBRACE:
			{
				postfixExpression();
				astFactory.addASTChild(currentAST, returnAST);
				unaryExpressionNotPlusMinus_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_47);
					
			} else {
				throw ex;
			}
		}
		returnAST = unaryExpressionNotPlusMinus_AST;
	}
	
	public final void postfixExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST postfixExpression_AST = null;
		Token  in = null;
		AST in_AST = null;
		Token  de = null;
		AST de_AST = null;
		
		try {      // for error handling
			primaryExpression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case INCR:
			{
				in = LT(1);
				in_AST = astFactory.create(in);
				astFactory.makeASTRoot(currentAST, in_AST);
				match(INCR);
				if ( inputState.guessing==0 ) {
					in_AST.setType(POST_INCR);
				}
				break;
			}
			case DECR:
			{
				de = LT(1);
				de_AST = astFactory.create(de);
				astFactory.makeASTRoot(currentAST, de_AST);
				match(DECR);
				if ( inputState.guessing==0 ) {
					de_AST.setType(POST_DECR);
				}
				break;
			}
			case RPAREN:
			case RBRACKET:
			case RBRACE:
			case SEMICOLON:
			case COMMA:
			case ASSIGN:
			case PLUS_ASSIGN:
			case MINUS_ASSIGN:
			case STAR_ASSIGN:
			case DIV_ASSIGN:
			case MOD_ASSIGN:
			case LOGICAL_OR:
			case LOGICAL_AND:
			case EQ:
			case NE:
			case LT:
			case LE:
			case GE:
			case GT:
			case PLUS:
			case MINUS:
			case STAR:
			case SLASH:
			case MOD:
			case IMPLIES:
			case IFF:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			postfixExpression_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_47);
					
			} else {
				throw ex;
			}
		}
		returnAST = postfixExpression_AST;
	}
	
	public final void primaryExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST primaryExpression_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case IDENTIFIER:
			{
				identifierPrimary();
				astFactory.addASTChild(currentAST, returnAST);
				primaryExpression_AST = (AST)currentAST.root;
				break;
			}
			case LBRACE:
			{
				setExpression();
				astFactory.addASTChild(currentAST, returnAST);
				primaryExpression_AST = (AST)currentAST.root;
				break;
			}
			case TRUE:
			case FALSE:
			case NULL:
			case FLOAT_LIT:
			case INTEGER_LIT:
			case STRING_LIT:
			case CHAR_LIT:
			{
				constant();
				astFactory.addASTChild(currentAST, returnAST);
				primaryExpression_AST = (AST)currentAST.root;
				break;
			}
			case LPAREN:
			{
				match(LPAREN);
				assignmentExpression();
				astFactory.addASTChild(currentAST, returnAST);
				match(RPAREN);
				primaryExpression_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_48);
					
			} else {
				throw ex;
			}
		}
		returnAST = primaryExpression_AST;
	}
	
	public final void constant() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST constant_AST = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case INTEGER_LIT:
			{
				AST tmp162_AST = null;
				tmp162_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp162_AST);
				match(INTEGER_LIT);
				constant_AST = (AST)currentAST.root;
				break;
			}
			case FLOAT_LIT:
			{
				AST tmp163_AST = null;
				tmp163_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp163_AST);
				match(FLOAT_LIT);
				constant_AST = (AST)currentAST.root;
				break;
			}
			case STRING_LIT:
			{
				AST tmp164_AST = null;
				tmp164_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp164_AST);
				match(STRING_LIT);
				constant_AST = (AST)currentAST.root;
				break;
			}
			case CHAR_LIT:
			{
				AST tmp165_AST = null;
				tmp165_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp165_AST);
				match(CHAR_LIT);
				constant_AST = (AST)currentAST.root;
				break;
			}
			case TRUE:
			{
				AST tmp166_AST = null;
				tmp166_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp166_AST);
				match(TRUE);
				constant_AST = (AST)currentAST.root;
				break;
			}
			case FALSE:
			{
				AST tmp167_AST = null;
				tmp167_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp167_AST);
				match(FALSE);
				constant_AST = (AST)currentAST.root;
				break;
			}
			case NULL:
			{
				AST tmp168_AST = null;
				tmp168_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp168_AST);
				match(NULL);
				constant_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_48);
					
			} else {
				throw ex;
			}
		}
		returnAST = constant_AST;
	}
	
	public final void setConstructor() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST setConstructor_AST = null;
		
		try {      // for error handling
			match(LBRACE);
			AST tmp170_AST = null;
			tmp170_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp170_AST);
			match(SELECT);
			quantifierDeclaration();
			astFactory.addASTChild(currentAST, returnAST);
			match(IN);
			{
			switch ( LA(1)) {
			case LBRACE:
			{
				setExpression();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case IDENTIFIER:
			{
				identifierPrimary();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(BAR);
			booleanExpression();
			astFactory.addASTChild(currentAST, returnAST);
			match(RBRACE);
			setConstructor_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_39);
					
			} else {
				throw ex;
			}
		}
		returnAST = setConstructor_AST;
	}
	
	public final void literalSet() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST literalSet_AST = null;
		Token  lb = null;
		AST lb_AST = null;
		
		try {      // for error handling
			lb = LT(1);
			lb_AST = astFactory.create(lb);
			astFactory.makeASTRoot(currentAST, lb_AST);
			match(LBRACE);
			if ( inputState.guessing==0 ) {
				lb_AST.setType(SET); lb_AST.setText("SET");
			}
			{
			switch ( LA(1)) {
			case TRUE:
			case FALSE:
			case NULL:
			case FLOAT_LIT:
			case IDENTIFIER:
			case INTEGER_LIT:
			case STRING_LIT:
			case CHAR_LIT:
			{
				{
				switch ( LA(1)) {
				case IDENTIFIER:
				{
					identifierPrimary();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case TRUE:
				case FALSE:
				case NULL:
				case FLOAT_LIT:
				case INTEGER_LIT:
				case STRING_LIT:
				case CHAR_LIT:
				{
					constant();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				{
				_loop158:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						{
						switch ( LA(1)) {
						case IDENTIFIER:
						{
							identifierPrimary();
							astFactory.addASTChild(currentAST, returnAST);
							break;
						}
						case TRUE:
						case FALSE:
						case NULL:
						case FLOAT_LIT:
						case INTEGER_LIT:
						case STRING_LIT:
						case CHAR_LIT:
						{
							constant();
							astFactory.addASTChild(currentAST, returnAST);
							break;
						}
						default:
						{
							throw new NoViableAltException(LT(1), getFilename());
						}
						}
						}
					}
					else {
						break _loop158;
					}
					
				} while (true);
				}
				break;
			}
			case RBRACE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RBRACE);
			literalSet_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_39);
					
			} else {
				throw ex;
			}
		}
		returnAST = literalSet_AST;
	}
	
	public final void parameterList() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST parameterList_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case OBJECT:
			case INT:
			case FLOAT:
			case BOOLEAN:
			case CHAR:
			case STRING:
			case SET:
			case SEQUENCE:
			case RECORD:
			case ENUM:
			case IDENTIFIER:
			{
				parameterDeclaration();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop164:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						parameterDeclaration();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop164;
					}
					
				} while (true);
				}
				break;
			}
			case RPAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				parameterList_AST = (AST)currentAST.root;
				parameterList_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(PARAM_LIST,"PARAM_LIST")).add(parameterList_AST));
				currentAST.root = parameterList_AST;
				currentAST.child = parameterList_AST!=null &&parameterList_AST.getFirstChild()!=null ?
					parameterList_AST.getFirstChild() : parameterList_AST;
				currentAST.advanceChildToEnd();
			}
			parameterList_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				
						processError(ex, _tokenSet_27);
					
			} else {
				throw ex;
			}
		}
		returnAST = parameterList_AST;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"module\"",
		"\"import\"",
		"\"lib\"",
		"\"model\"",
		"\"op\"",
		"\"as\"",
		"\"tactic\"",
		"\"condition\"",
		"\"action\"",
		"\"effect\"",
		"\"error\"",
		"\"strategy\"",
		"\"define\"",
		"\"success\"",
		"\"failure\"",
		"\"default\"",
		"\"TNULL\"",
		"\"do\"",
		"\"done\"",
		"\"if\"",
		"\"else\"",
		"\"for\"",
		"\"while\"",
		"TYPE",
		"\"object\"",
		"\"int\"",
		"\"float\"",
		"\"boolean\"",
		"\"char\"",
		"\"string\"",
		"\"set\"",
		"\"sequence\"",
		"\"record\"",
		"\"enum\"",
		"\"forall\"",
		"\"exists\"",
		"\"unique\"",
		"EXISTS_UNIQUE",
		"\"select\"",
		"\"and\"",
		"\"or\"",
		"\"in\"",
		"\"true\"",
		"\"false\"",
		"\"null\"",
		"UNARY_MINUS",
		"UNARY_PLUS",
		"POST_INCR",
		"POST_DECR",
		"FLOAT_LIT",
		"an identifier",
		"SL_COMMENT",
		"ML_COMMENT",
		"INTEGER_LIT",
		"STRING_LIT",
		"CHAR_LIT",
		"LPAREN",
		"RPAREN",
		"LBRACKET",
		"RBRACKET",
		"LBRACE",
		"RBRACE",
		"COLON",
		"SEMICOLON",
		"COMMA",
		"DOT",
		"DQUOTE",
		"SQUOTE",
		"BSLASH",
		"BAR",
		"HASH",
		"AT",
		"DOLLAR",
		"ASSIGN",
		"PLUS_ASSIGN",
		"MINUS_ASSIGN",
		"STAR_ASSIGN",
		"DIV_ASSIGN",
		"MOD_ASSIGN",
		"COLON_BANG",
		"LOGICAL_OR",
		"LOGICAL_AND",
		"EQ",
		"NE",
		"LT",
		"LE",
		"GE",
		"GT",
		"PLUS",
		"MINUS",
		"STAR",
		"SLASH",
		"MOD",
		"INCR",
		"DECR",
		"LOGICAL_NOT",
		"IMPLIES",
		"IFF",
		"LETTER",
		"DIGIT",
		"UNDERSCORE",
		"a newline",
		"WS",
		"IMPORTS",
		"IMPORT_LIB",
		"IMPORT_MODEL",
		"IMPORT_OP",
		"TACTICS",
		"FUNC_LIST",
		"VAR_LIST",
		"STRATEGIES",
		"STRATEGY_COND",
		"STRATEGY_BRANCH",
		"DO_UNSPEC",
		"STMT_LIST",
		"EMPTY_STMT",
		"VAR_DEF",
		"EXPR_LIST",
		"EXPR",
		"METHOD_CALL",
		"FOR_INIT",
		"FOR_COND",
		"FOR_ITER",
		"FOR_EACH",
		"PARAM_LIST",
		"PARAM"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 99330L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 18014398509515778L, 2L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 32770L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 99362L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 0L, 8L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 0L, 18L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 18014398509581314L, 2L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 33794L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 0L, 1L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = { 0L, 2L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { 18014673118953472L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = { 2048L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { 2189242824745025536L, 60934848513L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = { 4096L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { 2189243099463548928L, 60934848521L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = { 8192L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = { -6917529027641081856L, 24L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = { 2189243099463548928L, 60934848523L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	private static final long[] mk_tokenSet_19() {
		long[] data = { 4629700416936869888L, 16777217L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
	private static final long[] mk_tokenSet_20() {
		long[] data = { 2189248322303164416L, 274877374475L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
	private static final long[] mk_tokenSet_21() {
		long[] data = { 2189243099480326144L, 60934848523L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
	private static final long[] mk_tokenSet_22() {
		long[] data = { 18014398509481984L, 2L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
	private static final long[] mk_tokenSet_23() {
		long[] data = { 0L, 68719476736L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
	private static final long[] mk_tokenSet_24() {
		long[] data = { 2189242824745943040L, 60934848513L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_24 = new BitSet(mk_tokenSet_24());
	private static final long[] mk_tokenSet_25() {
		long[] data = { 0L, 2561L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_25 = new BitSet(mk_tokenSet_25());
	private static final long[] mk_tokenSet_26() {
		long[] data = { 0L, 513L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_26 = new BitSet(mk_tokenSet_26());
	private static final long[] mk_tokenSet_27() {
		long[] data = { 2305843009213693952L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_27 = new BitSet(mk_tokenSet_27());
	private static final long[] mk_tokenSet_28() {
		long[] data = { -9223372036854775808L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_28 = new BitSet(mk_tokenSet_28());
	private static final long[] mk_tokenSet_29() {
		long[] data = { 2189243099354497024L, 60934848521L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_29 = new BitSet(mk_tokenSet_29());
	private static final long[] mk_tokenSet_30() {
		long[] data = { 6800934340730552320L, 274877374491L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_30 = new BitSet(mk_tokenSet_30());
	private static final long[] mk_tokenSet_31() {
		long[] data = { 18049582881570816L, 134217738L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_31 = new BitSet(mk_tokenSet_31());
	private static final long[] mk_tokenSet_32() {
		long[] data = { 0L, 24L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_32 = new BitSet(mk_tokenSet_32());
	private static final long[] mk_tokenSet_33() {
		long[] data = { 2189248322303164416L, 274877374491L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_33 = new BitSet(mk_tokenSet_33());
	private static final long[] mk_tokenSet_34() {
		long[] data = { 2305843009213693952L, 8L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_34 = new BitSet(mk_tokenSet_34());
	private static final long[] mk_tokenSet_35() {
		long[] data = { 2305843009213693952L, 20L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_35 = new BitSet(mk_tokenSet_35());
	private static final long[] mk_tokenSet_36() {
		long[] data = { -6917529027641081856L, 516122L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_36 = new BitSet(mk_tokenSet_36());
	private static final long[] mk_tokenSet_37() {
		long[] data = { 35184372088832L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_37 = new BitSet(mk_tokenSet_37());
	private static final long[] mk_tokenSet_38() {
		long[] data = { 1036320495504457728L, 2L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_38 = new BitSet(mk_tokenSet_38());
	private static final long[] mk_tokenSet_39() {
		long[] data = { -6917529027641081856L, 240517636634L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_39 = new BitSet(mk_tokenSet_39());
	private static final long[] mk_tokenSet_40() {
		long[] data = { -6917529027641081856L, 68719992858L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_40 = new BitSet(mk_tokenSet_40());
	private static final long[] mk_tokenSet_41() {
		long[] data = { -6917529027641081856L, 206158946330L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_41 = new BitSet(mk_tokenSet_41());
	private static final long[] mk_tokenSet_42() {
		long[] data = { -6917529027641081856L, 206159994906L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_42 = new BitSet(mk_tokenSet_42());
	private static final long[] mk_tokenSet_43() {
		long[] data = { -6917529027641081856L, 206162092058L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_43 = new BitSet(mk_tokenSet_43());
	private static final long[] mk_tokenSet_44() {
		long[] data = { -6917529027641081856L, 206174674970L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_44 = new BitSet(mk_tokenSet_44());
	private static final long[] mk_tokenSet_45() {
		long[] data = { -6917529027641081856L, 206426333210L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_45 = new BitSet(mk_tokenSet_45());
	private static final long[] mk_tokenSet_46() {
		long[] data = { -6917529027641081856L, 207231639578L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_46 = new BitSet(mk_tokenSet_46());
	private static final long[] mk_tokenSet_47() {
		long[] data = { -6917529027641081856L, 214747832346L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_47 = new BitSet(mk_tokenSet_47());
	private static final long[] mk_tokenSet_48() {
		long[] data = { -6917529027641081856L, 240517636122L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_48 = new BitSet(mk_tokenSet_48());
	
	}
