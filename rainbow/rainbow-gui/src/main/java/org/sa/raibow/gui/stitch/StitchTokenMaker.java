package org.sa.raibow.gui.stitch;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

public class StitchTokenMaker extends AbstractTokenMaker {
	
	class TokenInfo {
		String keyword;
		int tokenType;
		
		public TokenInfo(String kw, int tt) {
			keyword = kw;
			tokenType = tt;
			
		}
	}
	
	
	public static final TokenInfo[] KEYWORDS = new TokenInfo[] {
			 new TokenInfo("module",Token.RESERVED_WORD),
			 new TokenInfo("import",Token.RESERVED_WORD),
			 new TokenInfo("lib",Token.RESERVED_WORD_2),
			 new TokenInfo("model",Token.RESERVED_WORD_2),
			 new TokenInfo("op",Token.RESERVED_WORD_2),
			 new TokenInfo("acme",Token.RESERVED_WORD_2),
			 new TokenInfo("as",Token.RESERVED_WORD),
			 new TokenInfo("tactic",Token.RESERVED_WORD),
			 new TokenInfo("condition",Token.RESERVED_WORD),
			 new TokenInfo("action",Token.RESERVED_WORD),
			 new TokenInfo("effect",Token.RESERVED_WORD),
			 new TokenInfo("error",Token.RESERVED_WORD_2),
			 new TokenInfo("strategy",Token.RESERVED_WORD),
			 new TokenInfo("define",Token.RESERVED_WORD),
			 new TokenInfo("function",Token.RESERVED_WORD),
			 new TokenInfo("success",Token.RESERVED_WORD_2),
			 new TokenInfo("failure",Token.RESERVED_WORD_2),
			 new TokenInfo("default",Token.RESERVED_WORD_2),
			 new TokenInfo("TNULL",Token.RESERVED_WORD_2),
			 new TokenInfo("do",Token.RESERVED_WORD),
			 new TokenInfo("done",Token.RESERVED_WORD),
			 new TokenInfo("if",Token.RESERVED_WORD),
			 new TokenInfo("else",Token.RESERVED_WORD),
			 new TokenInfo("for",Token.RESERVED_WORD),
			 new TokenInfo("while",Token.RESERVED_WORD),
			 new TokenInfo("forall",Token.RESERVED_WORD),
			 new TokenInfo("exists",Token.RESERVED_WORD),
			 new TokenInfo("unique",Token.RESERVED_WORD),
			 new TokenInfo("select",Token.RESERVED_WORD),
			 new TokenInfo("and",Token.RESERVED_WORD),
			 new TokenInfo("or",Token.RESERVED_WORD),
			 new TokenInfo("in",Token.RESERVED_WORD),

			/* Data types. */
			 new TokenInfo("object",Token.DATA_TYPE),
			 new TokenInfo("int",Token.DATA_TYPE),
			 new TokenInfo("float",Token.DATA_TYPE),
			 new TokenInfo("boolean",Token.DATA_TYPE),
			 new TokenInfo("char",Token.DATA_TYPE),
			 new TokenInfo("string",Token.DATA_TYPE),
			 new TokenInfo("set",Token.DATA_TYPE),
			 new TokenInfo("sequence",Token.DATA_TYPE),
			 new TokenInfo("record",Token.DATA_TYPE),
			 new TokenInfo("enum",Token.DATA_TYPE)
	};

	@Override
	public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TokenMap getWordsToHighlight() {
		TokenMap tm = new TokenMap();
		
		for (TokenInfo ti : KEYWORDS) {
			tm.put(ti.keyword, ti.tokenType);
		}
		return tm;
		
	}

}
