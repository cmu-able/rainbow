package org.sa.raibow.gui.stitch;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

public class StitchTokenMaker extends AbstractTokenMaker {

	static class TokenInfo {
		String keyword;
		int tokenType;

		public TokenInfo(String kw, int tt) {
			keyword = kw;
			tokenType = tt;

		}
	}

	public static final TokenInfo[] KEYWORDS = new TokenInfo[] { new TokenInfo("module", Token.RESERVED_WORD),
			new TokenInfo("import", Token.RESERVED_WORD), new TokenInfo("lib", Token.RESERVED_WORD_2),
			new TokenInfo("model", Token.RESERVED_WORD_2), new TokenInfo("op", Token.RESERVED_WORD_2),
			new TokenInfo("acme", Token.RESERVED_WORD_2), new TokenInfo("as", Token.RESERVED_WORD),
			new TokenInfo("tactic", Token.RESERVED_WORD), new TokenInfo("condition", Token.RESERVED_WORD),
			new TokenInfo("action", Token.RESERVED_WORD), new TokenInfo("effect", Token.RESERVED_WORD),
			new TokenInfo("error", Token.RESERVED_WORD_2), new TokenInfo("strategy", Token.RESERVED_WORD),
			new TokenInfo("define", Token.RESERVED_WORD), new TokenInfo("function", Token.RESERVED_WORD),
			new TokenInfo("success", Token.RESERVED_WORD_2), new TokenInfo("failure", Token.RESERVED_WORD_2),
			new TokenInfo("default", Token.RESERVED_WORD_2), new TokenInfo("TNULL", Token.RESERVED_WORD_2),
			new TokenInfo("do", Token.RESERVED_WORD), new TokenInfo("done", Token.RESERVED_WORD),
			new TokenInfo("if", Token.RESERVED_WORD), new TokenInfo("else", Token.RESERVED_WORD),
			new TokenInfo("for", Token.RESERVED_WORD), new TokenInfo("while", Token.RESERVED_WORD),
			new TokenInfo("forall", Token.RESERVED_WORD), new TokenInfo("exists", Token.RESERVED_WORD),
			new TokenInfo("unique", Token.RESERVED_WORD), new TokenInfo("select", Token.RESERVED_WORD),
			new TokenInfo("and", Token.RESERVED_WORD), new TokenInfo("or", Token.RESERVED_WORD),
			new TokenInfo("in", Token.RESERVED_WORD),

			/* Data types. */
			new TokenInfo("object", Token.DATA_TYPE), new TokenInfo("int", Token.DATA_TYPE),
			new TokenInfo("float", Token.DATA_TYPE), new TokenInfo("boolean", Token.DATA_TYPE),
			new TokenInfo("char", Token.DATA_TYPE), new TokenInfo("string", Token.DATA_TYPE),
			new TokenInfo("set", Token.DATA_TYPE), new TokenInfo("sequence", Token.DATA_TYPE),
			new TokenInfo("record", Token.DATA_TYPE), new TokenInfo("enum", Token.DATA_TYPE),
			new TokenInfo(";", Token.SEPARATOR), new TokenInfo("@", Token.OPERATOR),
			new TokenInfo("<=", Token.OPERATOR), new TokenInfo(">=", Token.OPERATOR),
			new TokenInfo("<", Token.OPERATOR), new TokenInfo(">", Token.OPERATOR), new TokenInfo("+", Token.OPERATOR),
			new TokenInfo("-", Token.OPERATOR), new TokenInfo("*", Token.OPERATOR), new TokenInfo("/", Token.OPERATOR),
			new TokenInfo("'", Token.OPERATOR), new TokenInfo("...", Token.OPERATOR),
			new TokenInfo("==", Token.OPERATOR), new TokenInfo("|", Token.OPERATOR), new TokenInfo("#", Token.OPERATOR),
			new TokenInfo("=", Token.OPERATOR), new TokenInfo("+=", Token.OPERATOR),
			new TokenInfo("-=", Token.OPERATOR), new TokenInfo("*=", Token.OPERATOR),
			new TokenInfo("/=", Token.OPERATOR), new TokenInfo("%=", Token.OPERATOR),
			new TokenInfo("%", Token.OPERATOR), new TokenInfo("++", Token.OPERATOR),
			new TokenInfo("--", Token.OPERATOR), new TokenInfo("!", Token.OPERATOR),
			new TokenInfo("->", Token.OPERATOR), new TokenInfo("<->", Token.OPERATOR),
			new TokenInfo(",", Token.SEPARATOR), new TokenInfo("{", Token.SEPARATOR),
			new TokenInfo("}", Token.SEPARATOR), new TokenInfo("(", Token.SEPARATOR),
			new TokenInfo(")", Token.SEPARATOR), new TokenInfo("[", Token.SEPARATOR),
			new TokenInfo("]", Token.SEPARATOR), new TokenInfo(".", Token.SEPARATOR),
			new TokenInfo(":", Token.SEPARATOR) };
	private int currentTokenStart;
	private int currentTokenType;

	@Override
	public void addToken(Segment segment, int start, int end, int tokenType, int startOffset) {
		// This assumes all keywords, etc. were parsed as "identifiers."
		if (tokenType == Token.IDENTIFIER) {
			int value = wordsToHighlight.get(segment, start, end);
			if (value != -1) {
				tokenType = value;
			}
		}
		super.addToken(segment, start, end, tokenType, startOffset);
	}
	
	

	@Override
	public Token getTokenList(Segment text, int startTokenType, int startOffset) {
		resetTokenList();

		char[] array = text.array;
		int offset = text.offset;
		int count = text.count;
		int end = offset + count;

		// Token starting offsets are always of the form:
		// 'startOffset + (currentTokenStart-offset)', but since startOffset and
		// offset are constant, tokens' starting positions become:
		// 'newStartOffset+currentTokenStart'.
		int newStartOffset = startOffset - offset;

		currentTokenStart = offset;
		currentTokenType = startTokenType;

		for (int i = offset; i < end; i++) {
			if (currentTokenType == Token.LITERAL_STRING_DOUBLE_QUOTE && array[i] != '"') {
				continue;
			}
			else if (currentTokenType == Token.COMMENT_MULTILINE) {
				if (!isEOC(array,i)) {
					continue;
				} 
				else {
					i++;i++;
				} 
			}
			if (currentTokenType == Token.COMMENT_EOL) {
				continue;
			}

			int newTokenType = wordsToHighlight.get(array, currentTokenStart, i);
			boolean skip = false;
			if (newTokenType == Token.RESERVED_WORD || newTokenType == Token.RESERVED_WORD_2) {
				// Lookahead to make sure it isn't an identifier
				if (i + 1 < end && (Character.isLetter(array[i + 1]) || '_' == array[i + 1])) {
					newTokenType = Token.IDENTIFIER;
				} else {
					currentTokenType = newTokenType;
				}
			} else {
				char ch = array[i];
				if (ch == ' ' || ch == '\t') {
					newTokenType = Token.WHITESPACE;
				} else if (ch == '"') {
					if (currentTokenType != Token.LITERAL_STRING_DOUBLE_QUOTE) {
						newTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
					} else {
						addToken(text, currentTokenStart, i, Token.LITERAL_STRING_DOUBLE_QUOTE,
								newStartOffset + currentTokenStart);
						skip = true;
						currentTokenStart = i + 1;
						currentTokenType = Token.NULL;
					}
				} else if (Character.isDigit(ch)) {
					if (currentTokenType == Token.IDENTIFIER)
						newTokenType = Token.IDENTIFIER;
					else
						newTokenType = (currentTokenType == Token.LITERAL_NUMBER_FLOAT) ? Token.LITERAL_NUMBER_FLOAT
								: Token.LITERAL_NUMBER_DECIMAL_INT;
				} else if ('.' == ch && currentTokenType == Token.LITERAL_NUMBER_DECIMAL_INT) {
					currentTokenType = newTokenType = Token.LITERAL_NUMBER_FLOAT;
				} else if (Character.isLetter(ch)) {
					newTokenType = Token.IDENTIFIER;
				} else if ('_' == ch) {
					if (currentTokenType == Token.IDENTIFIER)
						newTokenType = Token.IDENTIFIER;
					else
						newTokenType = Token.NULL;
				} else if ('/' == ch) {
					if (i+1 < end) {
						if ('*' == array[i+1]) newTokenType = Token.COMMENT_MULTILINE;
						else if ('/' == array[i+1]) newTokenType = Token.COMMENT_EOL;
					}
				} else if ((newTokenType = wordsToHighlight.get(array, i, i)) != -1) {

				}
			}
			if (newTokenType == Token.NULL || newTokenType == -1) {
				if (currentTokenType == Token.NULL || currentTokenType == -1) {
					skip = true;
					currentTokenStart = i;
				}
			}
			if (!skip && newTokenType != currentTokenType) {
				switch (currentTokenType) {
				case Token.NULL:
				case -1:
					break;
				default:
					addToken(text, currentTokenStart, i - 1, currentTokenType, newStartOffset + currentTokenStart);
				}
				currentTokenStart = i;
				currentTokenType = newTokenType;

			}
//			char c = array[i];
//
//			switch (currentTokenType) {
//
//			case Token.NULL:
//
//				currentTokenStart = i; // Starting a new token here.
//				
//				
//
//				switch (c) {
//
//				case ' ':
//				case '\t':
//					currentTokenType = Token.WHITESPACE;
//					break;
//
//				case '"':
//					currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
//					break;
//
//				
//
//				default:
//					if (RSyntaxUtilities.isDigit(c)) {
//						currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
//						break;
//					} else if (RSyntaxUtilities.isLetter(c) || c == '/' || c == '_') {
//						currentTokenType = Token.IDENTIFIER;
//						break;
//					}
//
//					// Anything not currently handled - mark as an identifier
//					currentTokenType = Token.IDENTIFIER;
//					break;
//
//				} // End of switch (c).
//
//				break;
//
//			case Token.WHITESPACE:
//
//				switch (c) {
//
//				case ' ':
//				case '\t':
//					break; // Still whitespace.
//
//				case '"':
//					addToken(text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart);
//					currentTokenStart = i;
//					currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
//					break;
//
//				case '#':
//					addToken(text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart);
//					currentTokenStart = i;
//					currentTokenType = Token.COMMENT_EOL;
//					break;
//
//				default: // Add the whitespace token and start anew.
//
//					addToken(text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart);
//					currentTokenStart = i;
//
//					if (RSyntaxUtilities.isDigit(c)) {
//						currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
//						break;
//					} else if (RSyntaxUtilities.isLetter(c) || c == '/' || c == '_') {
//						currentTokenType = Token.IDENTIFIER;
//						break;
//					}
//
//					// Anything not currently handled - mark as identifier
//					currentTokenType = Token.IDENTIFIER;
//
//				} // End of switch (c).
//
//				break;
//
//			default: // Should never happen
//			case Token.IDENTIFIER:
//
//				switch (c) {
//
//				case ' ':
//				case '\t':
//					addToken(text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart);
//					currentTokenStart = i;
//					currentTokenType = Token.WHITESPACE;
//					break;
//
//				case '"':
//					addToken(text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart);
//					currentTokenStart = i;
//					currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
//					break;
//
//				default:
//					if (RSyntaxUtilities.isLetterOrDigit(c) || c == '/' || c == '_') {
//						break; // Still an identifier of some type.
//					}
//					// Otherwise, we're still an identifier (?).
//
//				} // End of switch (c).
//
//				break;
//
//			case Token.LITERAL_NUMBER_DECIMAL_INT:
//
//				switch (c) {
//
//				case ' ':
//				case '\t':
//					addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT,
//							newStartOffset + currentTokenStart);
//					currentTokenStart = i;
//					currentTokenType = Token.WHITESPACE;
//					break;
//
//				case '"':
//					addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT,
//							newStartOffset + currentTokenStart);
//					currentTokenStart = i;
//					currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
//					break;
//
//				default:
//
//					if (RSyntaxUtilities.isDigit(c)) {
//						break; // Still a literal number.
//					}
//
//					// Otherwise, remember this was a number and start over.
//					addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT,
//							newStartOffset + currentTokenStart);
//					i--;
//					currentTokenType = Token.NULL;
//
//				} // End of switch (c).
//
//				break;
//
//			case Token.COMMENT_EOL:
//				i = end - 1;
//				addToken(text, currentTokenStart, i, currentTokenType, newStartOffset + currentTokenStart);
//				// We need to set token type to null so at the bottom we don't add one more
//				// token.
//				currentTokenType = Token.NULL;
//				break;
//
//			case Token.LITERAL_STRING_DOUBLE_QUOTE:
//				if (c == '"') {
//					addToken(text, currentTokenStart, i, Token.LITERAL_STRING_DOUBLE_QUOTE,
//							newStartOffset + currentTokenStart);
//					currentTokenType = Token.NULL;
//				}
//				break;
//
//			} // End of switch (currentTokenType).

		} // End of for (int i=offset; i<end; i++).

		switch (currentTokenType) {

		// Remember what token type to begin the next line with.
		case Token.LITERAL_STRING_DOUBLE_QUOTE:
		case Token.COMMENT_MULTILINE:
			addToken(text, currentTokenStart, end - 1, currentTokenType, newStartOffset + currentTokenStart);
			break;
			

		// Do nothing if everything was okay.
		case Token.NULL:
			addNullToken();
			break;

		// All other token types don't continue to the next line...
		default:
			addToken(text, currentTokenStart, end - 1, currentTokenType, newStartOffset + currentTokenStart);
			addNullToken();

		}

		// Return the first token in our linked list.
		return firstToken;

	}

	private boolean isEOC(char[] array, int i) {
		if (i+2 < array.length) {
			return array[i] == '*' && array[i+1]=='/';
		}
		return false;
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
