package org.sa.rainbow.stitch;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sa.rainbow.stitch.adaptation.IStitchExecutor;
import org.sa.rainbow.stitch.core.IScope;
import org.sa.rainbow.stitch.core.ScopedEntity;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Strategy.Outcome;
import org.sa.rainbow.stitch.error.DummyStitchProblemHandler;
import org.sa.rainbow.stitch.error.StitchProblem;
import org.sa.rainbow.stitch.error.StitchProblemHandler;
import org.sa.rainbow.stitch.parser.StitchLexer;
import org.sa.rainbow.stitch.parser.StitchParser;
import org.sa.rainbow.stitch.test.NullTestModelDSPublisherPort;
import org.sa.rainbow.stitch.test.NullTestReportingPort;
import org.sa.rainbow.stitch.test.NullTestModelUSPort;
import org.sa.rainbow.stitch.test.TestStitchExecutor;
import org.sa.rainbow.stitch.visitor.IStitchBehavior;
import org.sa.rainbow.stitch.visitor.Stitch;
import org.sa.rainbow.stitch.visitor.StitchBeginEndVisitor;

public class TestStitchParameterPassing {
	
	private static IStitchExecutor m_ex;

	@BeforeClass
	public static void setUp() throws Exception {
		File basePath = new File (System.getProperty ("user.dir"));
	    File testMasterDir = new File (basePath, "src/test/resources/");
	    System.setProperty ("user.dir", testMasterDir.getCanonicalPath ());
		
	    m_ex = new TestStitchExecutor(new NullTestModelUSPort(), new NullTestModelDSPublisherPort(), new NullTestReportingPort());

	}
	
	@Test
	public void test() throws Exception {
		
		IScope rootScope = new ScopedEntity(null, "Test Root Scope", Stitch.NULL_STITCH);
		File f = new File("src/test/resources/param-to-tactic.s");
		FileInputStream fis = new FileInputStream(f);

		StitchProblemHandler sph = new DummyStitchProblemHandler();
		final Stitch stitch = Stitch.newInstance(f.getCanonicalPath(), sph);
		CharStream i = new ANTLRInputStream(fis);

		StitchLexer lexer = new StitchLexer(i);
		lexer.setTokenFactory(new CommonTokenFactory());
		TokenStream tokens = new CommonTokenStream(lexer);

		StitchParser parser = new StitchParser(tokens);
		ANTLRErrorListener errReporter = new BaseErrorListener() {
			@Override
			public void syntaxError(@NotNull Recognizer<?, ?> recognizer, @Nullable Object offendingSymbol, int line,
					int charPositionInLine, @NotNull String msg, @Nullable RecognitionException e) {
				StitchProblem problem = new StitchProblem(
						new org.sa.rainbow.stitch.error.RecognitionException(msg, null, line, charPositionInLine),
						StitchProblem.ERROR);
				stitch.stitchProblemHandler.setProblem(problem);
			}
		};
		lexer.addErrorListener(errReporter);
		parser.addErrorListener(errReporter);
		StitchParser.ScriptContext script = parser.script();
		String tacticPath = "/script/tactic";
		stitch.parser = parser;

		IStitchBehavior sb = stitch.getBehavior(Stitch.SCOPER_PASS);
		sb.stitch().setScope(rootScope);
		StitchBeginEndVisitor walker = new StitchBeginEndVisitor(sb, rootScope);
		walker.visit(script);
		
		Strategy s = stitch.script.strategies.iterator().next();
		s.setExecutor(m_ex);
		s.evaluate(null);
		
		 assertEquals (s.outcome(), Outcome.SUCCESS);

	}

}
