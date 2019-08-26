package org.sa.rainbow.stitch.visitor;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ParserUtils {

	public static String formatTokens(Token start, Token stop) {
		TokenSource source = start.getTokenSource();
		CharStream inputStream = source.getInputStream();
		String text = inputStream.getText(new Interval(start.getStartIndex(), stop.getStopIndex()));
		return text;
	}

	public static String formatTree(ParseTree ast) {
		if (ast == null) return "<empty tree>";
		if (ast instanceof ParserRuleContext) {
			ParserRuleContext prc = (ParserRuleContext) ast;
			return formatTokens(prc.getStart(), prc.getStop());
		}
		if (ast instanceof TerminalNode) {
			return ((TerminalNode )ast).getSymbol().getText();
		}
		return "";
	}

}
