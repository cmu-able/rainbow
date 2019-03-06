package org.sa.rainbow.stitch;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.acmestudio.acme.core.exception.AcmeException;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;
import org.sa.rainbow.stitch.core.ScopedEntity;
import org.sa.rainbow.stitch.error.DummyStitchProblemHandler;
import org.sa.rainbow.stitch.error.IStitchProblem;
import org.sa.rainbow.stitch.error.StitchProblem;
import org.sa.rainbow.stitch.error.StitchProblemHandler;
import org.sa.rainbow.stitch.parser.StitchLexer;
import org.sa.rainbow.stitch.parser.StitchParser;
import org.sa.rainbow.stitch.parser.StitchParser.ScriptContext;
import org.sa.rainbow.stitch.visitor.IStitchBehavior;
import org.sa.rainbow.stitch.visitor.Stitch;
import org.sa.rainbow.stitch.visitor.StitchBeginEndVisitor;

import junit.framework.AssertionFailedError;


public class StitchTest  {

	
	
	public static class ProblemStorer {
		List<StitchProblem> problems = new ArrayList<>();
	}

	public static class TestProblemHandler implements StitchProblemHandler {

		@Override
		public void setProblem(IStitchProblem problem) {
			if (problem.getSeverity() == IStitchProblem.WARNING) {
				System.out.println(problem.getMessage());
			}
			else {
				throw new AssertionFailedError(problem.getMessage());
			}
		}

		@Override
		public void addAll(Collection<IStitchProblem> problems) {
			boolean notWarning = false;
			for (IStitchProblem p : problems) {
				System.out.println(p.getMessage());
				if (p.getSeverity() != IStitchProblem.WARNING) {
					notWarning = true;
				}
			}
			if (notWarning) throw new AssertionFailedError("Mulltiple problems reported");
		}

		@Override
		public Collection<IStitchProblem> unreportedProblems() {
			return null;
		}

		@Override
		public StitchProblemHandler clone() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void clearProblems() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	protected static String s_executed;

	protected Stitch loadScript(String pathname) throws IOException, FileNotFoundException {
		File f = new File (pathname);
	    ScopedEntity rootScope = new ScopedEntity (null, "Ohana2 Stitch Root Scope", Stitch.NULL_STITCH);
	
		DummyStitchProblemHandler stitchProblemHandler = new DummyStitchProblemHandler ();
	    Stitch stitch = Stitch.newInstance (f.getCanonicalPath (), stitchProblemHandler);
		FileInputStream is = new FileInputStream(f);
		StitchLexer lexer = new StitchLexer(new ANTLRInputStream(is));
		lexer.setTokenFactory(new CommonTokenFactory());
		TokenStream tokens = new CommonTokenStream(lexer);
		StitchParser parser = new StitchParser(tokens);
		final ProblemStorer problems = new ProblemStorer();
		ANTLRErrorListener errReporter = new BaseErrorListener() {
			@Override
			public void syntaxError(@NotNull Recognizer<?, ?> recognizer, @Nullable Object offendingSymbol, int line,
					int charPositionInLine, @NotNull String msg, @Nullable RecognitionException e) {
				StitchProblem problem = new StitchProblem(
						new org.sa.rainbow.stitch.error.RecognitionException(msg, null, line, charPositionInLine),
						StitchProblem.ERROR);
				problems.problems.add(problem);
			}
		};
		lexer.addErrorListener(errReporter);
		parser.addErrorListener(errReporter);
		ScriptContext script = parser.script();
		for (StitchProblem err : problems.problems) {
			System.out.println(err.getMessage());
		}
		assertTrue("Could not parse the script", problems.problems.isEmpty());
		String tacticPath = "/script/tactic";
		final Collection<ParseTree> definedTactics = XPath.findAll(script, tacticPath, parser);
		IStitchBehavior sb = stitch.getBehavior(Stitch.SCOPER_PASS);
		sb.stitch().setScope(rootScope);
		StitchBeginEndVisitor walker = new StitchBeginEndVisitor(sb, rootScope);
		walker.visit(script);

		stitch.stitchProblemHandler = new TestProblemHandler();
		return stitch;
	}
	
	public static void markExecuted(String s) {
		s_executed = s;
	}
	
	protected static IAcmeSystem s_system;
	
	public static void removeComponent(IAcmeSystem system, String componentName) {
		s_system = system;
		IAcmeComponent component = system.getComponent(componentName);
		assertTrue(component != null);
		try {
			system.getCommandFactory().componentDeleteCommand(component).execute();
		} catch (IllegalStateException | AcmeException e) {
			e.printStackTrace();
		}
		assertTrue(system.getComponent(componentName) == null);
	}
	
}
