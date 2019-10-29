package org.sa.rainbow.configuration.parser.antlr.lexer;

import java.io.InputStream;

import org.eclipse.xtext.parser.antlr.IAntlrTokenFileProvider;

public class ConfigModelCustomAntlrTokenFileProvider implements IAntlrTokenFileProvider {

	@Override
	public InputStream getAntlrTokenFile() {
		ClassLoader classLoader = getClass().getClassLoader();
		return classLoader.getResourceAsStream("org/sa/rainbow/configuration/parser/antlr/lexer/InternalConfigModelLexer.tokens");
	}

}
