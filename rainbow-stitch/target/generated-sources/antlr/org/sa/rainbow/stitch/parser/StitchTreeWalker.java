// $ANTLR 2.7.6 (2005-12-22): "stitchT.g" -> "StitchTreeWalker.java"$
package org.sa.rainbow.stitch.parser;

import antlr.TreeParser;
import antlr.Token;
import antlr.collections.AST;
import antlr.RecognitionException;
import antlr.ANTLRException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.collections.impl.BitSet;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;
import org.sa.rainbow.stitch.visitor.ILiloBehavior;
import org.sa.rainbow.stitch.core.IScope;
import org.sa.rainbow.stitch.core.Import;
import org.sa.rainbow.stitch.core.Expression;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.error.StitchProblem;
import org.sa.rainbow.stitch.error.StitchProblemHandler;


public class StitchTreeWalker extends antlr.TreeParser       implements StitchTreeWalkerTokenTypes
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
public StitchTreeWalker() {
	tokenNames = _tokenNames;
}

	public final void script(AST _t,
		IScope parentScope
	) throws RecognitionException {
		
		AST script_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST script_AST = null;
		AST id = null;
		AST id_AST = null;
		beh.beginScript(parentScope);
		
		try {      // for error handling
			AST __t2 = _t;
			AST tmp1_AST = null;
			AST tmp1_AST_in = null;
			tmp1_AST = astFactory.create((AST)_t);
			tmp1_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp1_AST);
			ASTPair __currentAST2 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MODULE);
			_t = _t.getFirstChild();
			id = (AST)_t;
			AST id_AST_in = null;
			id_AST = astFactory.create(id);
			astFactory.addASTChild(currentAST, id_AST);
			match(_t,IDENTIFIER);
			_t = _t.getNextSibling();
			beh.createModule(id_AST);
			AST __t3 = _t;
			AST tmp2_AST = null;
			AST tmp2_AST_in = null;
			tmp2_AST = astFactory.create((AST)_t);
			tmp2_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp2_AST);
			ASTPair __currentAST3 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,IMPORTS);
			_t = _t.getFirstChild();
			{
			_loop5:
			do {
				if (_t==null) _t=ASTNULL;
				if (((_t.getType() >= IMPORT_LIB && _t.getType() <= IMPORT_OP))) {
					importSt(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop5;
				}
				
			} while (true);
			}
			currentAST = __currentAST3;
			_t = __t3;
			_t = _t.getNextSibling();
			beh.doImports();
			functions(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			AST __t6 = _t;
			AST tmp3_AST = null;
			AST tmp3_AST_in = null;
			tmp3_AST = astFactory.create((AST)_t);
			tmp3_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp3_AST);
			ASTPair __currentAST6 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,TACTICS);
			_t = _t.getFirstChild();
			{
			_loop8:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==TACTIC)) {
					tactic(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop8;
				}
				
			} while (true);
			}
			currentAST = __currentAST6;
			_t = __t6;
			_t = _t.getNextSibling();
			AST __t9 = _t;
			AST tmp4_AST = null;
			AST tmp4_AST_in = null;
			tmp4_AST = astFactory.create((AST)_t);
			tmp4_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp4_AST);
			ASTPair __currentAST9 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,STRATEGIES);
			_t = _t.getFirstChild();
			{
			_loop11:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==STRATEGY)) {
					strategy(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop11;
				}
				
			} while (true);
			}
			currentAST = __currentAST9;
			_t = __t9;
			_t = _t.getNextSibling();
			currentAST = __currentAST2;
			_t = __t2;
			_t = _t.getNextSibling();
			beh.endScript();
			script_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = script_AST;
		_retTree = _t;
	}
	
	public final void importSt(AST _t) throws RecognitionException {
		
		AST importSt_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST importSt_AST = null;
		AST i1 = null;
		AST i1_AST = null;
		AST s1 = null;
		AST s1_AST = null;
		AST i2 = null;
		AST i2_AST = null;
		AST s2 = null;
		AST s2_AST = null;
		AST i3 = null;
		AST i3_AST = null;
		AST s3 = null;
		AST s3_AST = null;
		Import imp = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case IMPORT_LIB:
			{
				AST __t13 = _t;
				i1 = _t==ASTNULL ? null :(AST)_t;
				AST i1_AST_in = null;
				i1_AST = astFactory.create(i1);
				astFactory.addASTChild(currentAST, i1_AST);
				ASTPair __currentAST13 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,IMPORT_LIB);
				_t = _t.getFirstChild();
				s1 = (AST)_t;
				AST s1_AST_in = null;
				s1_AST = astFactory.create(s1);
				astFactory.addASTChild(currentAST, s1_AST);
				match(_t,STRING_LIT);
				_t = _t.getNextSibling();
				imp = beh.createImport(i1_AST, s1_AST);
				{
				_loop15:
				do {
					if (_t==null) _t=ASTNULL;
					if ((_t.getType()==AS)) {
						importRenames(_t);
						_t = _retTree;
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop15;
					}
					
				} while (true);
				}
				currentAST = __currentAST13;
				_t = __t13;
				_t = _t.getNextSibling();
				importSt_AST = (AST)currentAST.root;
				break;
			}
			case IMPORT_MODEL:
			{
				AST __t16 = _t;
				i2 = _t==ASTNULL ? null :(AST)_t;
				AST i2_AST_in = null;
				i2_AST = astFactory.create(i2);
				astFactory.addASTChild(currentAST, i2_AST);
				ASTPair __currentAST16 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,IMPORT_MODEL);
				_t = _t.getFirstChild();
				s2 = (AST)_t;
				AST s2_AST_in = null;
				s2_AST = astFactory.create(s2);
				astFactory.addASTChild(currentAST, s2_AST);
				match(_t,STRING_LIT);
				_t = _t.getNextSibling();
				imp = beh.createImport(i2_AST, s2_AST);
				{
				_loop18:
				do {
					if (_t==null) _t=ASTNULL;
					if ((_t.getType()==AS)) {
						importRenames(_t);
						_t = _retTree;
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop18;
					}
					
				} while (true);
				}
				currentAST = __currentAST16;
				_t = __t16;
				_t = _t.getNextSibling();
				importSt_AST = (AST)currentAST.root;
				break;
			}
			case IMPORT_OP:
			{
				AST __t19 = _t;
				i3 = _t==ASTNULL ? null :(AST)_t;
				AST i3_AST_in = null;
				i3_AST = astFactory.create(i3);
				astFactory.addASTChild(currentAST, i3_AST);
				ASTPair __currentAST19 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,IMPORT_OP);
				_t = _t.getFirstChild();
				s3 = (AST)_t;
				AST s3_AST_in = null;
				s3_AST = astFactory.create(s3);
				astFactory.addASTChild(currentAST, s3_AST);
				match(_t,STRING_LIT);
				_t = _t.getNextSibling();
				imp = beh.createImport(i3_AST, s3_AST);
				{
				_loop21:
				do {
					if (_t==null) _t=ASTNULL;
					if ((_t.getType()==AS)) {
						importRenames(_t);
						_t = _retTree;
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop21;
					}
					
				} while (true);
				}
				currentAST = __currentAST19;
				_t = __t19;
				_t = _t.getNextSibling();
				importSt_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = importSt_AST;
		_retTree = _t;
	}
	
	public final void functions(AST _t) throws RecognitionException {
		
		AST functions_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST functions_AST = null;
		beh.beginVarList();
		
		try {      // for error handling
			AST __t25 = _t;
			AST tmp5_AST = null;
			AST tmp5_AST_in = null;
			tmp5_AST = astFactory.create((AST)_t);
			tmp5_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp5_AST);
			ASTPair __currentAST25 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,FUNC_LIST);
			_t = _t.getFirstChild();
			{
			_loop27:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==VAR_DEF)) {
					var(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop27;
				}
				
			} while (true);
			}
			currentAST = __currentAST25;
			_t = __t25;
			_t = _t.getNextSibling();
			beh.endVarList();
			functions_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
			processError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
			
		}
		returnAST = functions_AST;
		_retTree = _t;
	}
	
	public final void tactic(AST _t) throws RecognitionException {
		
		AST tactic_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST tactic_AST = null;
		AST id = null;
		AST id_AST = null;
		
		try {      // for error handling
			AST __t29 = _t;
			AST tmp6_AST = null;
			AST tmp6_AST_in = null;
			tmp6_AST = astFactory.create((AST)_t);
			tmp6_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp6_AST);
			ASTPair __currentAST29 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,TACTIC);
			_t = _t.getFirstChild();
			id = (AST)_t;
			AST id_AST_in = null;
			id_AST = astFactory.create(id);
			astFactory.addASTChild(currentAST, id_AST);
			match(_t,IDENTIFIER);
			_t = _t.getNextSibling();
			beh.beginTactic(id_AST);
			params(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			vars(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			condition(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			action(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			effect(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST29;
			_t = __t29;
			_t = _t.getNextSibling();
			beh.endTactic();
			tactic_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = tactic_AST;
		_retTree = _t;
	}
	
	public final void strategy(AST _t) throws RecognitionException {
		
		AST strategy_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST strategy_AST = null;
		AST id = null;
		AST id_AST = null;
		
		try {      // for error handling
			AST __t51 = _t;
			AST tmp7_AST = null;
			AST tmp7_AST_in = null;
			tmp7_AST = astFactory.create((AST)_t);
			tmp7_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp7_AST);
			ASTPair __currentAST51 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,STRATEGY);
			_t = _t.getFirstChild();
			id = (AST)_t;
			AST id_AST_in = null;
			id_AST = astFactory.create(id);
			astFactory.addASTChild(currentAST, id_AST);
			match(_t,IDENTIFIER);
			_t = _t.getNextSibling();
			beh.beginStrategy(id_AST);
			expr(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			beh.doStrategyCondition(Strategy.ConditionKind.APPLICABILITY);
			functions(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop53:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==IDENTIFIER)) {
					strategyNode(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop53;
				}
				
			} while (true);
			}
			currentAST = __currentAST51;
			_t = __t51;
			_t = _t.getNextSibling();
			beh.endStrategy();
			strategy_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = strategy_AST;
		_retTree = _t;
	}
	
	public final void importRenames(AST _t) throws RecognitionException {
		
		AST importRenames_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST importRenames_AST = null;
		AST id1 = null;
		AST id1_AST = null;
		AST id2 = null;
		AST id2_AST = null;
		
		try {      // for error handling
			AST __t23 = _t;
			AST tmp8_AST = null;
			AST tmp8_AST_in = null;
			tmp8_AST = astFactory.create((AST)_t);
			tmp8_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp8_AST);
			ASTPair __currentAST23 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,AS);
			_t = _t.getFirstChild();
			id1 = (AST)_t;
			AST id1_AST_in = null;
			id1_AST = astFactory.create(id1);
			astFactory.addASTChild(currentAST, id1_AST);
			match(_t,IDENTIFIER);
			_t = _t.getNextSibling();
			id2 = (AST)_t;
			AST id2_AST_in = null;
			id2_AST = astFactory.create(id2);
			astFactory.addASTChild(currentAST, id2_AST);
			match(_t,IDENTIFIER);
			_t = _t.getNextSibling();
			currentAST = __currentAST23;
			_t = __t23;
			_t = _t.getNextSibling();
			beh.addImportRename(id1_AST, id2_AST);
			importRenames_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = importRenames_AST;
		_retTree = _t;
	}
	
	public final void var(AST _t) throws RecognitionException {
		
		AST var_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST var_AST = null;
		AST v = null;
		AST v_AST = null;
		AST t = null;
		AST t_AST = null;
		AST id = null;
		AST id_AST = null;
		
		try {      // for error handling
			AST __t35 = _t;
			v = _t==ASTNULL ? null :(AST)_t;
			AST v_AST_in = null;
			v_AST = astFactory.create(v);
			astFactory.addASTChild(currentAST, v_AST);
			ASTPair __currentAST35 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,VAR_DEF);
			_t = _t.getFirstChild();
			beh.beginStatement(v_AST);
			t = (AST)_t;
			AST t_AST_in = null;
			t_AST = astFactory.create(t);
			astFactory.addASTChild(currentAST, t_AST);
			match(_t,TYPE);
			_t = _t.getNextSibling();
			id = (AST)_t;
			AST id_AST_in = null;
			id_AST = astFactory.create(id);
			astFactory.addASTChild(currentAST, id_AST);
			match(_t,IDENTIFIER);
			_t = _t.getNextSibling();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ASSIGN:
			{
				AST __t37 = _t;
				AST tmp9_AST = null;
				AST tmp9_AST_in = null;
				tmp9_AST = astFactory.create((AST)_t);
				tmp9_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp9_AST);
				ASTPair __currentAST37 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,ASSIGN);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST37;
				_t = __t37;
				_t = _t.getNextSibling();
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST35;
			_t = __t35;
			_t = _t.getNextSibling();
			
			beh.createVar(t_AST, id_AST);
				beh.endStatement();
			
			var_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = var_AST;
		_retTree = _t;
	}
	
	public final void params(AST _t) throws RecognitionException {
		
		AST params_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST params_AST = null;
		beh.beginParamList();
		
		try {      // for error handling
			AST __t156 = _t;
			AST tmp10_AST = null;
			AST tmp10_AST_in = null;
			tmp10_AST = astFactory.create((AST)_t);
			tmp10_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp10_AST);
			ASTPair __currentAST156 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PARAM_LIST);
			_t = _t.getFirstChild();
			{
			_loop158:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==PARAM)) {
					param(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop158;
				}
				
			} while (true);
			}
			currentAST = __currentAST156;
			_t = __t156;
			_t = _t.getNextSibling();
			beh.endParamList();
			params_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = params_AST;
		_retTree = _t;
	}
	
	public final void vars(AST _t) throws RecognitionException {
		
		AST vars_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST vars_AST = null;
		beh.beginVarList();
		
		try {      // for error handling
			AST __t31 = _t;
			AST tmp11_AST = null;
			AST tmp11_AST_in = null;
			tmp11_AST = astFactory.create((AST)_t);
			tmp11_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp11_AST);
			ASTPair __currentAST31 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,VAR_LIST);
			_t = _t.getFirstChild();
			{
			_loop33:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==VAR_DEF)) {
					var(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop33;
				}
				
			} while (true);
			}
			currentAST = __currentAST31;
			_t = __t31;
			_t = _t.getNextSibling();
			beh.endVarList();
			vars_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = vars_AST;
		_retTree = _t;
	}
	
	public final void condition(AST _t) throws RecognitionException {
		
		AST condition_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST condition_AST = null;
		AST cn = null;
		AST cn_AST = null;
		
		try {      // for error handling
			AST __t39 = _t;
			cn = _t==ASTNULL ? null :(AST)_t;
			AST cn_AST_in = null;
			cn_AST = astFactory.create(cn);
			astFactory.addASTChild(currentAST, cn_AST);
			ASTPair __currentAST39 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,CONDITION);
			_t = _t.getFirstChild();
			beh.beginConditionBlock(cn_AST);
			{
			_loop41:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_0.member(_t.getType()))) {
					expr(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop41;
				}
				
			} while (true);
			}
			currentAST = __currentAST39;
			_t = __t39;
			_t = _t.getNextSibling();
			beh.endConditionBlock();
			condition_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = condition_AST;
		_retTree = _t;
	}
	
	public final void action(AST _t) throws RecognitionException {
		
		AST action_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST action_AST = null;
		AST ac = null;
		AST ac_AST = null;
		
		try {      // for error handling
			AST __t43 = _t;
			ac = _t==ASTNULL ? null :(AST)_t;
			AST ac_AST_in = null;
			ac_AST = astFactory.create(ac);
			astFactory.addASTChild(currentAST, ac_AST);
			ASTPair __currentAST43 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,ACTION);
			_t = _t.getFirstChild();
			beh.beginActionBlock(ac_AST);
			{
			_loop45:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_1.member(_t.getType()))) {
					statement(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop45;
				}
				
			} while (true);
			}
			currentAST = __currentAST43;
			_t = __t43;
			_t = _t.getNextSibling();
			beh.endActionBlock();
			action_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = action_AST;
		_retTree = _t;
	}
	
	public final void effect(AST _t) throws RecognitionException {
		
		AST effect_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST effect_AST = null;
		AST ef = null;
		AST ef_AST = null;
		
		try {      // for error handling
			AST __t47 = _t;
			ef = _t==ASTNULL ? null :(AST)_t;
			AST ef_AST_in = null;
			ef_AST = astFactory.create(ef);
			astFactory.addASTChild(currentAST, ef_AST);
			ASTPair __currentAST47 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,EFFECT);
			_t = _t.getFirstChild();
			beh.beginEffectBlock(ef_AST);
			{
			_loop49:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_0.member(_t.getType()))) {
					expr(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop49;
				}
				
			} while (true);
			}
			currentAST = __currentAST47;
			_t = __t47;
			_t = _t.getNextSibling();
			beh.endEffectBlock();
			effect_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = effect_AST;
		_retTree = _t;
	}
	
	public final void expr(AST _t) throws RecognitionException {
		
		AST expr_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expr_AST = null;
		AST e_AST = null;
		AST e = null;
		beh.beginExpression();
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EXPR:
			{
				AST __t114 = _t;
				AST tmp12_AST = null;
				AST tmp12_AST_in = null;
				tmp12_AST = astFactory.create((AST)_t);
				tmp12_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp12_AST);
				ASTPair __currentAST114 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,EXPR);
				_t = _t.getFirstChild();
				e = _t==ASTNULL ? null : (AST)_t;
				expr(_t);
				_t = _retTree;
				e_AST = (AST)returnAST;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST114;
				_t = __t114;
				_t = _t.getNextSibling();
				beh.doExpression(e_AST);
				break;
			}
			case FORALL:
			case EXISTS:
			case EXISTS_UNIQUE:
			case SELECT:
			{
				quanExpr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case SET:
			{
				setExpr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case ASSIGN:
			{
				assignExpr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case LOGICAL_OR:
			case LOGICAL_AND:
			case IMPLIES:
			case IFF:
			{
				logicalExpr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EQ:
			case NE:
			case LT:
			case LE:
			case GE:
			case GT:
			{
				relationalExpr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case PLUS:
			case MINUS:
			case STAR:
			case SLASH:
			case MOD:
			{
				arithmeticExpr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case UNARY_MINUS:
			case UNARY_PLUS:
			case INCR:
			case DECR:
			case LOGICAL_NOT:
			{
				unaryExpr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
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
			case METHOD_CALL:
			{
				idExpr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			beh.endExpression();
			expr_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = expr_AST;
		_retTree = _t;
	}
	
	public final void statement(AST _t) throws RecognitionException {
		
		AST statement_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST statement_AST = null;
		AST sl = null;
		AST sl_AST = null;
		AST x = null;
		AST x_AST = null;
		AST es = null;
		AST es_AST = null;
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case STMT_LIST:
			{
				AST __t78 = _t;
				sl = _t==ASTNULL ? null :(AST)_t;
				AST sl_AST_in = null;
				sl_AST = astFactory.create(sl);
				astFactory.addASTChild(currentAST, sl_AST);
				ASTPair __currentAST78 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,STMT_LIST);
				_t = _t.getFirstChild();
				beh.beginStatement(sl_AST);
				{
				_loop80:
				do {
					if (_t==null) _t=ASTNULL;
					if ((_tokenSet_1.member(_t.getType()))) {
						statement(_t);
						_t = _retTree;
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop80;
					}
					
				} while (true);
				}
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case ERROR:
				{
					errorHandler(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				currentAST = __currentAST78;
				_t = __t78;
				_t = _t.getNextSibling();
				beh.endStatement();
				break;
			}
			case VAR_DEF:
			{
				var(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EXPR:
			{
				AST __t82 = _t;
				x = _t==ASTNULL ? null :(AST)_t;
				AST x_AST_in = null;
				x_AST = astFactory.create(x);
				astFactory.addASTChild(currentAST, x_AST);
				ASTPair __currentAST82 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,EXPR);
				_t = _t.getFirstChild();
				beh.beginStatement(x_AST);
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.endStatement();
				currentAST = __currentAST82;
				_t = __t82;
				_t = _t.getNextSibling();
				break;
			}
			case IF:
			{
				ifStmt(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case WHILE:
			{
				whileStmt(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case FOR:
			{
				forStmt(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case EMPTY_STMT:
			{
				es = (AST)_t;
				AST es_AST_in = null;
				es_AST = astFactory.create(es);
				astFactory.addASTChild(currentAST, es_AST);
				match(_t,EMPTY_STMT);
				_t = _t.getNextSibling();
				beh.beginStatement(es_AST); beh.endStatement();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			statement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = statement_AST;
		_retTree = _t;
	}
	
	public final void strategyNode(AST _t) throws RecognitionException {
		
		AST strategyNode_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST strategyNode_AST = null;
		AST l = null;
		AST l_AST = null;
		
		try {      // for error handling
			AST __t55 = _t;
			l = _t==ASTNULL ? null :(AST)_t;
			AST l_AST_in = null;
			l_AST = astFactory.create(l);
			astFactory.addASTChild(currentAST, l_AST);
			ASTPair __currentAST55 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,IDENTIFIER);
			_t = _t.getFirstChild();
			beh.beginStrategyNode(l_AST);
			strategyCond(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			tacticRef(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST55;
			_t = __t55;
			_t = _t.getNextSibling();
			beh.endStrategyNode();
			strategyNode_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = strategyNode_AST;
		_retTree = _t;
	}
	
	public final void strategyCond(AST _t) throws RecognitionException {
		
		AST strategyCond_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST strategyCond_AST = null;
		AST pid1 = null;
		AST pid1_AST = null;
		AST pid2 = null;
		AST pid2_AST = null;
		AST pl = null;
		AST pl_AST = null;
		
		try {      // for error handling
			AST __t57 = _t;
			AST tmp13_AST = null;
			AST tmp13_AST_in = null;
			tmp13_AST = astFactory.create((AST)_t);
			tmp13_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp13_AST);
			ASTPair __currentAST57 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,STRATEGY_COND);
			_t = _t.getFirstChild();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case HASH:
			{
				AST __t59 = _t;
				AST tmp14_AST = null;
				AST tmp14_AST_in = null;
				tmp14_AST = astFactory.create((AST)_t);
				tmp14_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp14_AST);
				ASTPair __currentAST59 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,HASH);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case IDENTIFIER:
				{
					AST __t61 = _t;
					pid1 = _t==ASTNULL ? null :(AST)_t;
					AST pid1_AST_in = null;
					pid1_AST = astFactory.create(pid1);
					astFactory.addASTChild(currentAST, pid1_AST);
					ASTPair __currentAST61 = currentAST.copy();
					currentAST.root = currentAST.child;
					currentAST.child = null;
					match(_t,IDENTIFIER);
					_t = _t.getFirstChild();
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case IDENTIFIER:
					{
						pid2 = (AST)_t;
						AST pid2_AST_in = null;
						pid2_AST = astFactory.create(pid2);
						astFactory.addASTChild(currentAST, pid2_AST);
						match(_t,IDENTIFIER);
						_t = _t.getNextSibling();
						break;
					}
					case 3:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(_t);
					}
					}
					}
					currentAST = __currentAST61;
					_t = __t61;
					_t = _t.getNextSibling();
					break;
				}
				case FLOAT_LIT:
				{
					pl = (AST)_t;
					AST pl_AST_in = null;
					pl_AST = astFactory.create(pl);
					astFactory.addASTChild(currentAST, pl_AST);
					match(_t,FLOAT_LIT);
					_t = _t.getNextSibling();
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				currentAST = __currentAST59;
				_t = __t59;
				_t = _t.getNextSibling();
				beh.doStrategyProbability(pid1_AST, pid2_AST, pl_AST);
				break;
			}
			case SUCCESS:
			case FAILURE:
			case DEFAULT:
			case SET:
			case FORALL:
			case EXISTS:
			case EXISTS_UNIQUE:
			case SELECT:
			case TRUE:
			case FALSE:
			case NULL:
			case UNARY_MINUS:
			case UNARY_PLUS:
			case FLOAT_LIT:
			case IDENTIFIER:
			case INTEGER_LIT:
			case STRING_LIT:
			case CHAR_LIT:
			case ASSIGN:
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
			case LOGICAL_NOT:
			case IMPLIES:
			case IFF:
			case EXPR:
			case METHOD_CALL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case SET:
			case FORALL:
			case EXISTS:
			case EXISTS_UNIQUE:
			case SELECT:
			case TRUE:
			case FALSE:
			case NULL:
			case UNARY_MINUS:
			case UNARY_PLUS:
			case FLOAT_LIT:
			case IDENTIFIER:
			case INTEGER_LIT:
			case STRING_LIT:
			case CHAR_LIT:
			case ASSIGN:
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
			case LOGICAL_NOT:
			case IMPLIES:
			case IFF:
			case EXPR:
			case METHOD_CALL:
			{
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.doStrategyCondition(Strategy.ConditionKind.EXPRESSION);
				break;
			}
			case SUCCESS:
			{
				AST tmp15_AST = null;
				AST tmp15_AST_in = null;
				tmp15_AST = astFactory.create((AST)_t);
				tmp15_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp15_AST);
				match(_t,SUCCESS);
				_t = _t.getNextSibling();
				beh.doStrategyCondition(Strategy.ConditionKind.SUCCESS);
				break;
			}
			case FAILURE:
			{
				AST tmp16_AST = null;
				AST tmp16_AST_in = null;
				tmp16_AST = astFactory.create((AST)_t);
				tmp16_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp16_AST);
				match(_t,FAILURE);
				_t = _t.getNextSibling();
				beh.doStrategyCondition(Strategy.ConditionKind.FAILURE);
				break;
			}
			case DEFAULT:
			{
				AST tmp17_AST = null;
				AST tmp17_AST_in = null;
				tmp17_AST = astFactory.create((AST)_t);
				tmp17_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp17_AST);
				match(_t,DEFAULT);
				_t = _t.getNextSibling();
				beh.doStrategyCondition(Strategy.ConditionKind.DEFAULT);
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST57;
			_t = __t57;
			_t = _t.getNextSibling();
			strategyCond_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = strategyCond_AST;
		_retTree = _t;
	}
	
	public final void tacticRef(AST _t) throws RecognitionException {
		
		AST tacticRef_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST tacticRef_AST = null;
		AST t1 = null;
		AST t1_AST = null;
		AST v = null;
		AST v_AST = null;
		AST i = null;
		AST i_AST = null;
		AST t2 = null;
		AST t2_AST = null;
		AST t3 = null;
		AST t3_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case IDENTIFIER:
			{
				AST __t65 = _t;
				t1 = _t==ASTNULL ? null :(AST)_t;
				AST t1_AST_in = null;
				t1_AST = astFactory.create(t1);
				astFactory.addASTChild(currentAST, t1_AST);
				ASTPair __currentAST65 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,IDENTIFIER);
				_t = _t.getFirstChild();
				beh.beginReferencedTactic(t1_AST);
				exprs(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST65;
				_t = __t65;
				_t = _t.getNextSibling();
				beh.endReferencedTactic();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case AT:
				{
					AST __t67 = _t;
					AST tmp18_AST = null;
					AST tmp18_AST_in = null;
					tmp18_AST = astFactory.create((AST)_t);
					tmp18_AST_in = (AST)_t;
					astFactory.addASTChild(currentAST, tmp18_AST);
					ASTPair __currentAST67 = currentAST.copy();
					currentAST.root = currentAST.child;
					currentAST.child = null;
					match(_t,AT);
					_t = _t.getFirstChild();
					expr(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
					currentAST = __currentAST67;
					_t = __t67;
					_t = _t.getNextSibling();
					beh.doStrategyDuration(/*expression is implicit*/);
					break;
				}
				case DONE:
				case STRATEGY_BRANCH:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case STRATEGY_BRANCH:
				{
					strategyBranch(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case DONE:
				{
					AST tmp19_AST = null;
					AST tmp19_AST_in = null;
					tmp19_AST = astFactory.create((AST)_t);
					tmp19_AST_in = (AST)_t;
					astFactory.addASTChild(currentAST, tmp19_AST);
					match(_t,DONE);
					_t = _t.getNextSibling();
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				tacticRef_AST = (AST)currentAST.root;
				break;
			}
			case DONE:
			{
				AST tmp20_AST = null;
				AST tmp20_AST_in = null;
				tmp20_AST = astFactory.create((AST)_t);
				tmp20_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp20_AST);
				match(_t,DONE);
				_t = _t.getNextSibling();
				beh.doStrategyAction(Strategy.ActionKind.DONE);
				tacticRef_AST = (AST)currentAST.root;
				break;
			}
			case NULLTACTIC:
			{
				AST tmp21_AST = null;
				AST tmp21_AST_in = null;
				tmp21_AST = astFactory.create((AST)_t);
				tmp21_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp21_AST);
				match(_t,NULLTACTIC);
				_t = _t.getNextSibling();
				beh.doStrategyAction(Strategy.ActionKind.NULL);
				tacticRef_AST = (AST)currentAST.root;
				break;
			}
			case DO:
			{
				AST __t69 = _t;
				AST tmp22_AST = null;
				AST tmp22_AST_in = null;
				tmp22_AST = astFactory.create((AST)_t);
				tmp22_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp22_AST);
				ASTPair __currentAST69 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,DO);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case IDENTIFIER:
				{
					v = (AST)_t;
					AST v_AST_in = null;
					v_AST = astFactory.create(v);
					astFactory.addASTChild(currentAST, v_AST);
					match(_t,IDENTIFIER);
					_t = _t.getNextSibling();
					break;
				}
				case INTEGER_LIT:
				{
					i = (AST)_t;
					AST i_AST_in = null;
					i_AST = astFactory.create(i);
					astFactory.addASTChild(currentAST, i_AST);
					match(_t,INTEGER_LIT);
					_t = _t.getNextSibling();
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				t2 = (AST)_t;
				AST t2_AST_in = null;
				t2_AST = astFactory.create(t2);
				astFactory.addASTChild(currentAST, t2_AST);
				match(_t,IDENTIFIER);
				_t = _t.getNextSibling();
				beh.doStrategyLoop(v_AST, i_AST, t2_AST);
				currentAST = __currentAST69;
				_t = __t69;
				_t = _t.getNextSibling();
				tacticRef_AST = (AST)currentAST.root;
				break;
			}
			case DO_UNSPEC:
			{
				AST __t71 = _t;
				AST tmp23_AST = null;
				AST tmp23_AST_in = null;
				tmp23_AST = astFactory.create((AST)_t);
				tmp23_AST_in = (AST)_t;
				astFactory.addASTChild(currentAST, tmp23_AST);
				ASTPair __currentAST71 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,DO_UNSPEC);
				_t = _t.getFirstChild();
				t3 = (AST)_t;
				AST t3_AST_in = null;
				t3_AST = astFactory.create(t3);
				astFactory.addASTChild(currentAST, t3_AST);
				match(_t,IDENTIFIER);
				_t = _t.getNextSibling();
				beh.doStrategyLoop(null, null, t3_AST);
				currentAST = __currentAST71;
				_t = __t71;
				_t = _t.getNextSibling();
				tacticRef_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = tacticRef_AST;
		_retTree = _t;
	}
	
	public final void exprs(AST _t) throws RecognitionException {
		
		AST exprs_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST exprs_AST = null;
		
		try {      // for error handling
			AST __t109 = _t;
			AST tmp24_AST = null;
			AST tmp24_AST_in = null;
			tmp24_AST = astFactory.create((AST)_t);
			tmp24_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp24_AST);
			ASTPair __currentAST109 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,EXPR_LIST);
			_t = _t.getFirstChild();
			{
			_loop111:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_0.member(_t.getType()))) {
					expr(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop111;
				}
				
			} while (true);
			}
			currentAST = __currentAST109;
			_t = __t109;
			_t = _t.getNextSibling();
			exprs_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		returnAST = exprs_AST;
		_retTree = _t;
	}
	
	public final void strategyBranch(AST _t) throws RecognitionException {
		
		AST strategyBranch_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST strategyBranch_AST = null;
		
		try {      // for error handling
			AST __t73 = _t;
			AST tmp25_AST = null;
			AST tmp25_AST_in = null;
			tmp25_AST = astFactory.create((AST)_t);
			tmp25_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp25_AST);
			ASTPair __currentAST73 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,STRATEGY_BRANCH);
			_t = _t.getFirstChild();
			beh.beginBranching();
			{
			int _cnt75=0;
			_loop75:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==IDENTIFIER)) {
					strategyNode(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					if ( _cnt75>=1 ) { break _loop75; } else {throw new NoViableAltException(_t);}
				}
				
				_cnt75++;
			} while (true);
			}
			currentAST = __currentAST73;
			_t = __t73;
			_t = _t.getNextSibling();
			beh.endBranching();
			strategyBranch_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = strategyBranch_AST;
		_retTree = _t;
	}
	
	public final void errorHandler(AST _t) throws RecognitionException {
		
		AST errorHandler_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST errorHandler_AST = null;
		AST e = null;
		AST e_AST = null;
		
		try {      // for error handling
			AST __t84 = _t;
			e = _t==ASTNULL ? null :(AST)_t;
			AST e_AST_in = null;
			e_AST = astFactory.create(e);
			astFactory.addASTChild(currentAST, e_AST);
			ASTPair __currentAST84 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,ERROR);
			_t = _t.getFirstChild();
			beh.beginStatement(e_AST);
			{
			_loop86:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_0.member(_t.getType()))) {
					expr(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
					statement(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop86;
				}
				
			} while (true);
			}
			currentAST = __currentAST84;
			_t = __t84;
			_t = _t.getNextSibling();
			beh.endStatement();
			errorHandler_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = errorHandler_AST;
		_retTree = _t;
	}
	
	public final void ifStmt(AST _t) throws RecognitionException {
		
		AST ifStmt_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST ifStmt_AST = null;
		AST si = null;
		AST si_AST = null;
		
		try {      // for error handling
			AST __t88 = _t;
			si = _t==ASTNULL ? null :(AST)_t;
			AST si_AST_in = null;
			si_AST = astFactory.create(si);
			astFactory.addASTChild(currentAST, si_AST);
			ASTPair __currentAST88 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,IF);
			_t = _t.getFirstChild();
			beh.beginStatement(si_AST);
			expr(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			statement(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case IF:
			case FOR:
			case WHILE:
			case STMT_LIST:
			case EMPTY_STMT:
			case VAR_DEF:
			case EXPR:
			{
				statement(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST88;
			_t = __t88;
			_t = _t.getNextSibling();
			beh.endStatement();
			ifStmt_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = ifStmt_AST;
		_retTree = _t;
	}
	
	public final void whileStmt(AST _t) throws RecognitionException {
		
		AST whileStmt_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST whileStmt_AST = null;
		AST w = null;
		AST w_AST = null;
		
		try {      // for error handling
			AST __t91 = _t;
			w = _t==ASTNULL ? null :(AST)_t;
			AST w_AST_in = null;
			w_AST = astFactory.create(w);
			astFactory.addASTChild(currentAST, w_AST);
			ASTPair __currentAST91 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,WHILE);
			_t = _t.getFirstChild();
			beh.beginStatement(w_AST);
			expr(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			statement(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST91;
			_t = __t91;
			_t = _t.getNextSibling();
			beh.endStatement();
			whileStmt_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = whileStmt_AST;
		_retTree = _t;
	}
	
	public final void forStmt(AST _t) throws RecognitionException {
		
		AST forStmt_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forStmt_AST = null;
		AST f = null;
		AST f_AST = null;
		
		try {      // for error handling
			AST __t93 = _t;
			f = _t==ASTNULL ? null :(AST)_t;
			AST f_AST_in = null;
			f_AST = astFactory.create(f);
			astFactory.addASTChild(currentAST, f_AST);
			ASTPair __currentAST93 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,FOR);
			_t = _t.getFirstChild();
			beh.beginStatement(f_AST);
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case FOR_INIT:
			{
				forInit(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.markForCondition();
				forCond(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				forIter(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				statement(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case FOR_EACH:
			{
				forEach(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.markForEach();
				statement(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST93;
			_t = __t93;
			_t = _t.getNextSibling();
			beh.endStatement();
			forStmt_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = forStmt_AST;
		_retTree = _t;
	}
	
	public final void forInit(AST _t) throws RecognitionException {
		
		AST forInit_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forInit_AST = null;
		
		try {      // for error handling
			AST __t96 = _t;
			AST tmp26_AST = null;
			AST tmp26_AST_in = null;
			tmp26_AST = astFactory.create((AST)_t);
			tmp26_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp26_AST);
			ASTPair __currentAST96 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,FOR_INIT);
			_t = _t.getFirstChild();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case VAR_DEF:
			{
				{
				int _cnt99=0;
				_loop99:
				do {
					if (_t==null) _t=ASTNULL;
					if ((_t.getType()==VAR_DEF)) {
						var(_t);
						_t = _retTree;
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						if ( _cnt99>=1 ) { break _loop99; } else {throw new NoViableAltException(_t);}
					}
					
					_cnt99++;
				} while (true);
				}
				break;
			}
			case EXPR_LIST:
			{
				exprs(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST96;
			_t = __t96;
			_t = _t.getNextSibling();
			forInit_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = forInit_AST;
		_retTree = _t;
	}
	
	public final void forCond(AST _t) throws RecognitionException {
		
		AST forCond_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forCond_AST = null;
		
		try {      // for error handling
			AST __t101 = _t;
			AST tmp27_AST = null;
			AST tmp27_AST_in = null;
			tmp27_AST = astFactory.create((AST)_t);
			tmp27_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp27_AST);
			ASTPair __currentAST101 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,FOR_COND);
			_t = _t.getFirstChild();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case SET:
			case FORALL:
			case EXISTS:
			case EXISTS_UNIQUE:
			case SELECT:
			case TRUE:
			case FALSE:
			case NULL:
			case UNARY_MINUS:
			case UNARY_PLUS:
			case FLOAT_LIT:
			case IDENTIFIER:
			case INTEGER_LIT:
			case STRING_LIT:
			case CHAR_LIT:
			case ASSIGN:
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
			case LOGICAL_NOT:
			case IMPLIES:
			case IFF:
			case EXPR:
			case METHOD_CALL:
			{
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST101;
			_t = __t101;
			_t = _t.getNextSibling();
			forCond_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = forCond_AST;
		_retTree = _t;
	}
	
	public final void forIter(AST _t) throws RecognitionException {
		
		AST forIter_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forIter_AST = null;
		
		try {      // for error handling
			AST __t104 = _t;
			AST tmp28_AST = null;
			AST tmp28_AST_in = null;
			tmp28_AST = astFactory.create((AST)_t);
			tmp28_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp28_AST);
			ASTPair __currentAST104 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,FOR_ITER);
			_t = _t.getFirstChild();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EXPR_LIST:
			{
				exprs(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST104;
			_t = __t104;
			_t = _t.getNextSibling();
			forIter_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = forIter_AST;
		_retTree = _t;
	}
	
	public final void forEach(AST _t) throws RecognitionException {
		
		AST forEach_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST forEach_AST = null;
		
		try {      // for error handling
			AST __t107 = _t;
			AST tmp29_AST = null;
			AST tmp29_AST_in = null;
			tmp29_AST = astFactory.create((AST)_t);
			tmp29_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp29_AST);
			ASTPair __currentAST107 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,FOR_EACH);
			_t = _t.getFirstChild();
			param(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			expr(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST107;
			_t = __t107;
			_t = _t.getNextSibling();
			forEach_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = forEach_AST;
		_retTree = _t;
	}
	
	public final void param(AST _t) throws RecognitionException {
		
		AST param_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST param_AST = null;
		AST t = null;
		AST t_AST = null;
		AST id = null;
		AST id_AST = null;
		
		try {      // for error handling
			AST __t160 = _t;
			AST tmp30_AST = null;
			AST tmp30_AST_in = null;
			tmp30_AST = astFactory.create((AST)_t);
			tmp30_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp30_AST);
			ASTPair __currentAST160 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PARAM);
			_t = _t.getFirstChild();
			t = (AST)_t;
			AST t_AST_in = null;
			t_AST = astFactory.create(t);
			astFactory.addASTChild(currentAST, t_AST);
			match(_t,TYPE);
			_t = _t.getNextSibling();
			id = (AST)_t;
			AST id_AST_in = null;
			id_AST = astFactory.create(id);
			astFactory.addASTChild(currentAST, id_AST);
			match(_t,IDENTIFIER);
			_t = _t.getNextSibling();
			currentAST = __currentAST160;
			_t = __t160;
			_t = _t.getNextSibling();
			beh.createVar(t_AST, id_AST);
			param_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = param_AST;
		_retTree = _t;
	}
	
	public final void quanExpr(AST _t) throws RecognitionException {
		
		AST quanExpr_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST quanExpr_AST = null;
		AST fa = null;
		AST fa_AST = null;
		AST e = null;
		AST e_AST = null;
		AST eu = null;
		AST eu_AST = null;
		AST s = null;
		AST s_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case FORALL:
			{
				AST __t116 = _t;
				fa = _t==ASTNULL ? null :(AST)_t;
				AST fa_AST_in = null;
				fa_AST = astFactory.create(fa);
				astFactory.addASTChild(currentAST, fa_AST);
				ASTPair __currentAST116 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,FORALL);
				_t = _t.getFirstChild();
				beh.beginQuantifiedExpression();
				params(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.doQuantifiedExpression();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST116;
				_t = __t116;
				_t = _t.getNextSibling();
				beh.endQuantifiedExpression(fa_AST);
				quanExpr_AST = (AST)currentAST.root;
				break;
			}
			case EXISTS:
			{
				AST __t117 = _t;
				e = _t==ASTNULL ? null :(AST)_t;
				AST e_AST_in = null;
				e_AST = astFactory.create(e);
				astFactory.addASTChild(currentAST, e_AST);
				ASTPair __currentAST117 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,EXISTS);
				_t = _t.getFirstChild();
				beh.beginQuantifiedExpression();
				params(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.doQuantifiedExpression();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST117;
				_t = __t117;
				_t = _t.getNextSibling();
				beh.endQuantifiedExpression(e_AST);
				quanExpr_AST = (AST)currentAST.root;
				break;
			}
			case EXISTS_UNIQUE:
			{
				AST __t118 = _t;
				eu = _t==ASTNULL ? null :(AST)_t;
				AST eu_AST_in = null;
				eu_AST = astFactory.create(eu);
				astFactory.addASTChild(currentAST, eu_AST);
				ASTPair __currentAST118 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,EXISTS_UNIQUE);
				_t = _t.getFirstChild();
				beh.beginQuantifiedExpression();
				params(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.doQuantifiedExpression();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST118;
				_t = __t118;
				_t = _t.getNextSibling();
				beh.endQuantifiedExpression(eu_AST);
				quanExpr_AST = (AST)currentAST.root;
				break;
			}
			case SELECT:
			{
				AST __t119 = _t;
				s = _t==ASTNULL ? null :(AST)_t;
				AST s_AST_in = null;
				s_AST = astFactory.create(s);
				astFactory.addASTChild(currentAST, s_AST);
				ASTPair __currentAST119 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,SELECT);
				_t = _t.getFirstChild();
				beh.beginQuantifiedExpression();
				params(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.doQuantifiedExpression();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST119;
				_t = __t119;
				_t = _t.getNextSibling();
				beh.endQuantifiedExpression(s_AST);
				quanExpr_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = quanExpr_AST;
		_retTree = _t;
	}
	
	public final void setExpr(AST _t) throws RecognitionException {
		
		AST setExpr_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST setExpr_AST = null;
		AST s = null;
		AST s_AST = null;
		beh.beginSetExpression();
		
		try {      // for error handling
			AST __t121 = _t;
			s = _t==ASTNULL ? null :(AST)_t;
			AST s_AST_in = null;
			s_AST = astFactory.create(s);
			astFactory.addASTChild(currentAST, s_AST);
			ASTPair __currentAST121 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,SET);
			_t = _t.getFirstChild();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case SET:
			case FORALL:
			case EXISTS:
			case EXISTS_UNIQUE:
			case SELECT:
			case TRUE:
			case FALSE:
			case NULL:
			case UNARY_MINUS:
			case UNARY_PLUS:
			case FLOAT_LIT:
			case IDENTIFIER:
			case INTEGER_LIT:
			case STRING_LIT:
			case CHAR_LIT:
			case ASSIGN:
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
			case LOGICAL_NOT:
			case IMPLIES:
			case IFF:
			case EXPR:
			case METHOD_CALL:
			{
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop124:
				do {
					if (_t==null) _t=ASTNULL;
					if ((_tokenSet_0.member(_t.getType()))) {
						expr(_t);
						_t = _retTree;
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop124;
					}
					
				} while (true);
				}
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST121;
			_t = __t121;
			_t = _t.getNextSibling();
			beh.endSetExpression(s_AST);
			setExpr_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = setExpr_AST;
		_retTree = _t;
	}
	
	public final void assignExpr(AST _t) throws RecognitionException {
		
		AST assignExpr_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST assignExpr_AST = null;
		AST aa = null;
		AST aa_AST = null;
		AST lv1_AST = null;
		AST lv1 = null;
		
		try {      // for error handling
			AST __t126 = _t;
			aa = _t==ASTNULL ? null :(AST)_t;
			AST aa_AST_in = null;
			aa_AST = astFactory.create(aa);
			astFactory.addASTChild(currentAST, aa_AST);
			ASTPair __currentAST126 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,ASSIGN);
			_t = _t.getFirstChild();
			beh.lOp();
			lv1 = _t==ASTNULL ? null : (AST)_t;
			expr(_t);
			_t = _retTree;
			lv1_AST = (AST)returnAST;
			astFactory.addASTChild(currentAST, returnAST);
			beh.rOp();
			expr(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST126;
			_t = __t126;
			_t = _t.getNextSibling();
			beh.doAssignExpression(aa_AST, lv1_AST);
			assignExpr_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = assignExpr_AST;
		_retTree = _t;
	}
	
	public final void logicalExpr(AST _t) throws RecognitionException {
		
		AST logicalExpr_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logicalExpr_AST = null;
		AST imp = null;
		AST imp_AST = null;
		AST iff = null;
		AST iff_AST = null;
		AST lor = null;
		AST lor_AST = null;
		AST lnd = null;
		AST lnd_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case IMPLIES:
			{
				AST __t128 = _t;
				imp = _t==ASTNULL ? null :(AST)_t;
				AST imp_AST_in = null;
				imp_AST = astFactory.create(imp);
				astFactory.addASTChild(currentAST, imp_AST);
				ASTPair __currentAST128 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,IMPLIES);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.rOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST128;
				_t = __t128;
				_t = _t.getNextSibling();
				beh.doLogicalExpression(imp_AST);
				logicalExpr_AST = (AST)currentAST.root;
				break;
			}
			case IFF:
			{
				AST __t129 = _t;
				iff = _t==ASTNULL ? null :(AST)_t;
				AST iff_AST_in = null;
				iff_AST = astFactory.create(iff);
				astFactory.addASTChild(currentAST, iff_AST);
				ASTPair __currentAST129 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,IFF);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.rOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST129;
				_t = __t129;
				_t = _t.getNextSibling();
				beh.doLogicalExpression(iff_AST);
				logicalExpr_AST = (AST)currentAST.root;
				break;
			}
			case LOGICAL_OR:
			{
				AST __t130 = _t;
				lor = _t==ASTNULL ? null :(AST)_t;
				AST lor_AST_in = null;
				lor_AST = astFactory.create(lor);
				astFactory.addASTChild(currentAST, lor_AST);
				ASTPair __currentAST130 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,LOGICAL_OR);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.rOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST130;
				_t = __t130;
				_t = _t.getNextSibling();
				beh.doLogicalExpression(lor_AST);
				logicalExpr_AST = (AST)currentAST.root;
				break;
			}
			case LOGICAL_AND:
			{
				AST __t131 = _t;
				lnd = _t==ASTNULL ? null :(AST)_t;
				AST lnd_AST_in = null;
				lnd_AST = astFactory.create(lnd);
				astFactory.addASTChild(currentAST, lnd_AST);
				ASTPair __currentAST131 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,LOGICAL_AND);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.rOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST131;
				_t = __t131;
				_t = _t.getNextSibling();
				beh.doLogicalExpression(lnd_AST);
				logicalExpr_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = logicalExpr_AST;
		_retTree = _t;
	}
	
	public final void relationalExpr(AST _t) throws RecognitionException {
		
		AST relationalExpr_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST relationalExpr_AST = null;
		AST ne = null;
		AST ne_AST = null;
		AST eq = null;
		AST eq_AST = null;
		AST lt = null;
		AST lt_AST = null;
		AST le = null;
		AST le_AST = null;
		AST ge = null;
		AST ge_AST = null;
		AST gt = null;
		AST gt_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case NE:
			{
				AST __t133 = _t;
				ne = _t==ASTNULL ? null :(AST)_t;
				AST ne_AST_in = null;
				ne_AST = astFactory.create(ne);
				astFactory.addASTChild(currentAST, ne_AST);
				ASTPair __currentAST133 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,NE);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.rOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST133;
				_t = __t133;
				_t = _t.getNextSibling();
				beh.doRelationalExpression(ne_AST);
				relationalExpr_AST = (AST)currentAST.root;
				break;
			}
			case EQ:
			{
				AST __t134 = _t;
				eq = _t==ASTNULL ? null :(AST)_t;
				AST eq_AST_in = null;
				eq_AST = astFactory.create(eq);
				astFactory.addASTChild(currentAST, eq_AST);
				ASTPair __currentAST134 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,EQ);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.rOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST134;
				_t = __t134;
				_t = _t.getNextSibling();
				beh.doRelationalExpression(eq_AST);
				relationalExpr_AST = (AST)currentAST.root;
				break;
			}
			case LT:
			{
				AST __t135 = _t;
				lt = _t==ASTNULL ? null :(AST)_t;
				AST lt_AST_in = null;
				lt_AST = astFactory.create(lt);
				astFactory.addASTChild(currentAST, lt_AST);
				ASTPair __currentAST135 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,LT);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.rOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST135;
				_t = __t135;
				_t = _t.getNextSibling();
				beh.doRelationalExpression(lt_AST);
				relationalExpr_AST = (AST)currentAST.root;
				break;
			}
			case LE:
			{
				AST __t136 = _t;
				le = _t==ASTNULL ? null :(AST)_t;
				AST le_AST_in = null;
				le_AST = astFactory.create(le);
				astFactory.addASTChild(currentAST, le_AST);
				ASTPair __currentAST136 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,LE);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.rOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST136;
				_t = __t136;
				_t = _t.getNextSibling();
				beh.doRelationalExpression(le_AST);
				relationalExpr_AST = (AST)currentAST.root;
				break;
			}
			case GE:
			{
				AST __t137 = _t;
				ge = _t==ASTNULL ? null :(AST)_t;
				AST ge_AST_in = null;
				ge_AST = astFactory.create(ge);
				astFactory.addASTChild(currentAST, ge_AST);
				ASTPair __currentAST137 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,GE);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.rOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST137;
				_t = __t137;
				_t = _t.getNextSibling();
				beh.doRelationalExpression(ge_AST);
				relationalExpr_AST = (AST)currentAST.root;
				break;
			}
			case GT:
			{
				AST __t138 = _t;
				gt = _t==ASTNULL ? null :(AST)_t;
				AST gt_AST_in = null;
				gt_AST = astFactory.create(gt);
				astFactory.addASTChild(currentAST, gt_AST);
				ASTPair __currentAST138 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,GT);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.rOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST138;
				_t = __t138;
				_t = _t.getNextSibling();
				beh.doRelationalExpression(gt_AST);
				relationalExpr_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = relationalExpr_AST;
		_retTree = _t;
	}
	
	public final void arithmeticExpr(AST _t) throws RecognitionException {
		
		AST arithmeticExpr_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST arithmeticExpr_AST = null;
		AST p = null;
		AST p_AST = null;
		AST m = null;
		AST m_AST = null;
		AST t = null;
		AST t_AST = null;
		AST d = null;
		AST d_AST = null;
		AST r = null;
		AST r_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PLUS:
			{
				AST __t140 = _t;
				p = _t==ASTNULL ? null :(AST)_t;
				AST p_AST_in = null;
				p_AST = astFactory.create(p);
				astFactory.addASTChild(currentAST, p_AST);
				ASTPair __currentAST140 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,PLUS);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.rOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST140;
				_t = __t140;
				_t = _t.getNextSibling();
				beh.doArithmeticExpression(p_AST);
				arithmeticExpr_AST = (AST)currentAST.root;
				break;
			}
			case MINUS:
			{
				AST __t141 = _t;
				m = _t==ASTNULL ? null :(AST)_t;
				AST m_AST_in = null;
				m_AST = astFactory.create(m);
				astFactory.addASTChild(currentAST, m_AST);
				ASTPair __currentAST141 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,MINUS);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.rOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST141;
				_t = __t141;
				_t = _t.getNextSibling();
				beh.doArithmeticExpression(m_AST);
				arithmeticExpr_AST = (AST)currentAST.root;
				break;
			}
			case STAR:
			{
				AST __t142 = _t;
				t = _t==ASTNULL ? null :(AST)_t;
				AST t_AST_in = null;
				t_AST = astFactory.create(t);
				astFactory.addASTChild(currentAST, t_AST);
				ASTPair __currentAST142 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,STAR);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.rOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST142;
				_t = __t142;
				_t = _t.getNextSibling();
				beh.doArithmeticExpression(t_AST);
				arithmeticExpr_AST = (AST)currentAST.root;
				break;
			}
			case SLASH:
			{
				AST __t143 = _t;
				d = _t==ASTNULL ? null :(AST)_t;
				AST d_AST_in = null;
				d_AST = astFactory.create(d);
				astFactory.addASTChild(currentAST, d_AST);
				ASTPair __currentAST143 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,SLASH);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.rOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST143;
				_t = __t143;
				_t = _t.getNextSibling();
				beh.doArithmeticExpression(d_AST);
				arithmeticExpr_AST = (AST)currentAST.root;
				break;
			}
			case MOD:
			{
				AST __t144 = _t;
				r = _t==ASTNULL ? null :(AST)_t;
				AST r_AST_in = null;
				r_AST = astFactory.create(r);
				astFactory.addASTChild(currentAST, r_AST);
				ASTPair __currentAST144 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,MOD);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				beh.rOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST144;
				_t = __t144;
				_t = _t.getNextSibling();
				beh.doArithmeticExpression(r_AST);
				arithmeticExpr_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = arithmeticExpr_AST;
		_retTree = _t;
	}
	
	public final void unaryExpr(AST _t) throws RecognitionException {
		
		AST unaryExpr_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST unaryExpr_AST = null;
		AST ic = null;
		AST ic_AST = null;
		AST dc = null;
		AST dc_AST = null;
		AST um = null;
		AST um_AST = null;
		AST up = null;
		AST up_AST = null;
		AST ln = null;
		AST ln_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case INCR:
			{
				AST __t146 = _t;
				ic = _t==ASTNULL ? null :(AST)_t;
				AST ic_AST_in = null;
				ic_AST = astFactory.create(ic);
				astFactory.addASTChild(currentAST, ic_AST);
				ASTPair __currentAST146 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,INCR);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST146;
				_t = __t146;
				_t = _t.getNextSibling();
				beh.doUnaryExpression(ic_AST);
				unaryExpr_AST = (AST)currentAST.root;
				break;
			}
			case DECR:
			{
				AST __t147 = _t;
				dc = _t==ASTNULL ? null :(AST)_t;
				AST dc_AST_in = null;
				dc_AST = astFactory.create(dc);
				astFactory.addASTChild(currentAST, dc_AST);
				ASTPair __currentAST147 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,DECR);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST147;
				_t = __t147;
				_t = _t.getNextSibling();
				beh.doUnaryExpression(dc_AST);
				unaryExpr_AST = (AST)currentAST.root;
				break;
			}
			case UNARY_MINUS:
			{
				AST __t148 = _t;
				um = _t==ASTNULL ? null :(AST)_t;
				AST um_AST_in = null;
				um_AST = astFactory.create(um);
				astFactory.addASTChild(currentAST, um_AST);
				ASTPair __currentAST148 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,UNARY_MINUS);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST148;
				_t = __t148;
				_t = _t.getNextSibling();
				beh.doUnaryExpression(um_AST);
				unaryExpr_AST = (AST)currentAST.root;
				break;
			}
			case UNARY_PLUS:
			{
				AST __t149 = _t;
				up = _t==ASTNULL ? null :(AST)_t;
				AST up_AST_in = null;
				up_AST = astFactory.create(up);
				astFactory.addASTChild(currentAST, up_AST);
				ASTPair __currentAST149 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,UNARY_PLUS);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST149;
				_t = __t149;
				_t = _t.getNextSibling();
				beh.doUnaryExpression(up_AST);
				unaryExpr_AST = (AST)currentAST.root;
				break;
			}
			case LOGICAL_NOT:
			{
				AST __t150 = _t;
				ln = _t==ASTNULL ? null :(AST)_t;
				AST ln_AST_in = null;
				ln_AST = astFactory.create(ln);
				astFactory.addASTChild(currentAST, ln_AST);
				ASTPair __currentAST150 = currentAST.copy();
				currentAST.root = currentAST.child;
				currentAST.child = null;
				match(_t,LOGICAL_NOT);
				_t = _t.getFirstChild();
				beh.lOp();
				expr(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				currentAST = __currentAST150;
				_t = __t150;
				_t = _t.getNextSibling();
				beh.doUnaryExpression(ln_AST);
				unaryExpr_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = unaryExpr_AST;
		_retTree = _t;
	}
	
	public final void idExpr(AST _t) throws RecognitionException {
		
		AST idExpr_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST idExpr_AST = null;
		AST id = null;
		AST id_AST = null;
		AST i = null;
		AST i_AST = null;
		AST d = null;
		AST d_AST = null;
		AST s = null;
		AST s_AST = null;
		AST c = null;
		AST c_AST = null;
		AST t = null;
		AST t_AST = null;
		AST f = null;
		AST f_AST = null;
		AST n = null;
		AST n_AST = null;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case IDENTIFIER:
			{
				id = (AST)_t;
				AST id_AST_in = null;
				id_AST = astFactory.create(id);
				astFactory.addASTChild(currentAST, id_AST);
				match(_t,IDENTIFIER);
				_t = _t.getNextSibling();
				beh.doIdentifierExpression(id_AST, Expression.Kind.IDENTIFIER);
				idExpr_AST = (AST)currentAST.root;
				break;
			}
			case METHOD_CALL:
			{
				methodCall(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				idExpr_AST = (AST)currentAST.root;
				break;
			}
			case INTEGER_LIT:
			{
				i = (AST)_t;
				AST i_AST_in = null;
				i_AST = astFactory.create(i);
				astFactory.addASTChild(currentAST, i_AST);
				match(_t,INTEGER_LIT);
				_t = _t.getNextSibling();
				beh.doIdentifierExpression(i_AST, Expression.Kind.INTEGER);
				idExpr_AST = (AST)currentAST.root;
				break;
			}
			case FLOAT_LIT:
			{
				d = (AST)_t;
				AST d_AST_in = null;
				d_AST = astFactory.create(d);
				astFactory.addASTChild(currentAST, d_AST);
				match(_t,FLOAT_LIT);
				_t = _t.getNextSibling();
				beh.doIdentifierExpression(d_AST, Expression.Kind.FLOAT);
				idExpr_AST = (AST)currentAST.root;
				break;
			}
			case STRING_LIT:
			{
				s = (AST)_t;
				AST s_AST_in = null;
				s_AST = astFactory.create(s);
				astFactory.addASTChild(currentAST, s_AST);
				match(_t,STRING_LIT);
				_t = _t.getNextSibling();
				beh.doIdentifierExpression(s_AST, Expression.Kind.STRING);
				idExpr_AST = (AST)currentAST.root;
				break;
			}
			case CHAR_LIT:
			{
				c = (AST)_t;
				AST c_AST_in = null;
				c_AST = astFactory.create(c);
				astFactory.addASTChild(currentAST, c_AST);
				match(_t,CHAR_LIT);
				_t = _t.getNextSibling();
				beh.doIdentifierExpression(c_AST, Expression.Kind.CHAR);
				idExpr_AST = (AST)currentAST.root;
				break;
			}
			case TRUE:
			{
				t = (AST)_t;
				AST t_AST_in = null;
				t_AST = astFactory.create(t);
				astFactory.addASTChild(currentAST, t_AST);
				match(_t,TRUE);
				_t = _t.getNextSibling();
				beh.doIdentifierExpression(t_AST, Expression.Kind.BOOLEAN);
				idExpr_AST = (AST)currentAST.root;
				break;
			}
			case FALSE:
			{
				f = (AST)_t;
				AST f_AST_in = null;
				f_AST = astFactory.create(f);
				astFactory.addASTChild(currentAST, f_AST);
				match(_t,FALSE);
				_t = _t.getNextSibling();
				beh.doIdentifierExpression(f_AST, Expression.Kind.BOOLEAN);
				idExpr_AST = (AST)currentAST.root;
				break;
			}
			case NULL:
			{
				n = (AST)_t;
				AST n_AST_in = null;
				n_AST = astFactory.create(n);
				astFactory.addASTChild(currentAST, n_AST);
				match(_t,NULL);
				_t = _t.getNextSibling();
				beh.doIdentifierExpression(n_AST, Expression.Kind.NULL);
				idExpr_AST = (AST)currentAST.root;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = idExpr_AST;
		_retTree = _t;
	}
	
	public final void methodCall(AST _t) throws RecognitionException {
		
		AST methodCall_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST methodCall_AST = null;
		AST mc = null;
		AST mc_AST = null;
		AST id = null;
		AST id_AST = null;
		beh.beginMethodCallExpression();
		
		try {      // for error handling
			AST __t153 = _t;
			mc = _t==ASTNULL ? null :(AST)_t;
			AST mc_AST_in = null;
			mc_AST = astFactory.create(mc);
			astFactory.addASTChild(currentAST, mc_AST);
			ASTPair __currentAST153 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,METHOD_CALL);
			_t = _t.getFirstChild();
			id = (AST)_t;
			AST id_AST_in = null;
			id_AST = astFactory.create(id);
			astFactory.addASTChild(currentAST, id_AST);
			match(_t,IDENTIFIER);
			_t = _t.getNextSibling();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EXPR_LIST:
			{
				exprs(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST153;
			_t = __t153;
			_t = _t.getNextSibling();
			beh.endMethodCallExpression(mc_AST, id_AST);
			methodCall_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			
					processError(ex);
					if (_t!=null) {_t = _t.getNextSibling();}
				
		}
		returnAST = methodCall_AST;
		_retTree = _t;
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
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 1038016784248078336L, 864691403332001792L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 109051904L, 414331165718085632L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	}
	
