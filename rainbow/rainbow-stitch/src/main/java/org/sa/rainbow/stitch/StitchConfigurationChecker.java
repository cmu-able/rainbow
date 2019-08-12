package org.sa.rainbow.stitch;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.antlr.v4.runtime.tree.ParseTree;
import org.sa.rainbow.core.IRainbowMaster;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.stitch.error.DummyStitchProblemHandler;
import org.sa.rainbow.stitch.error.IStitchProblem;
import org.sa.rainbow.stitch.visitor.IStitchBehavior;
import org.sa.rainbow.stitch.visitor.Stitch;
import org.sa.rainbow.stitch.visitor.StitchBeginEndVisitor;
import org.sa.rainbow.stitch.visitor.StitchScopeEstablisher;
import org.sa.rainbow.util.IRainbowConfigurationChecker;
import org.sa.rainbow.util.RainbowConfigurationChecker.Problem;
import org.sa.rainbow.util.RainbowConfigurationChecker.ProblemT;
import org.sa.rainbow.util.Util;

public class StitchConfigurationChecker implements IRainbowConfigurationChecker {

	private LinkedList<Problem> m_problems;
	private IRainbowMaster m_master;

	public StitchConfigurationChecker() {
		m_problems = new LinkedList<Problem>();
	}

	@Override
	public void checkRainbowConfiguration() {
		String message = "Checking stitch directory and file existence...";
		Problem p = new Problem(ProblemT.INFO, message);
		m_problems.add(p);
		int num = m_problems.size();
		// Check file existence
		File stitchPath = Util.getRelativeToPath(Rainbow.instance().getTargetPath(),
				Rainbow.instance().getProperty(RainbowConstants.PROPKEY_SCRIPT_PATH));
		if (stitchPath == null) {
			m_problems.add(new Problem(ProblemT.WARNING,
					"There is no stitch path and yet the configuaration of stitch strategies is being checked."));
		} else if (!stitchPath.exists() || !stitchPath.isDirectory()) {
			m_problems.add(new Problem(ProblemT.ERROR,
					MessageFormat.format("''{0}'' is not a directory", stitchPath.getAbsolutePath())));
		} else {
			FilenameFilter ff = new FilenameFilter() { // find only ".s" files
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".s");
				}
			};
			File[] stitchFiles = stitchPath.listFiles(ff);
			if (stitchFiles == null || stitchFiles.length == 0) {
				m_problems.add(new Problem(ProblemT.WARNING,
						MessageFormat.format("''{0}'' does not seem to contain any Stitch files (with extension '.s')",
								stitchPath.getAbsolutePath())));
			} else {
				if (num == m_problems.size())
					p.setMessage(message + "ok");
				for (File f : stitchFiles) {
					message = MessageFormat.format("Checking stitch file ''{0}''...", f.getAbsolutePath());
					p = new Problem(ProblemT.INFO, message);
					m_problems.add(p);
					num = m_problems.size();
					checkStitchFile(f);
					if (num == m_problems.size()) {
						p.msg = message + "ok";
					}
				}
			}
		}
		Ohana.instance().dispose();
	}

	protected void checkStitchFile(File f) {
		DummyStitchProblemHandler sph = new DummyStitchProblemHandler();
		try {
			Stitch stitch = Stitch.newInstance(f.getCanonicalPath(), sph);
			ArrayList<ArrayList<ParseTree>> parsedFile = Ohana.instance().parseFile(stitch);
			if (sph.getProblems().isEmpty()) {
				// File parsed ok
				IStitchBehavior tc = stitch.getBehavior(Stitch.TYPECHECKER_PASS);
				StitchBeginEndVisitor walker = new StitchBeginEndVisitor(tc,
						Ohana.instance().getRootScope());
				walker.visit(parsedFile.get(0).get(0));
			}
		} catch (IOException e) {
			m_problems.add(new Problem(ProblemT.ERROR,
					MessageFormat.format("There was an error opening ''{0}''", f.getAbsolutePath())));
		}
		for (IStitchProblem problem : sph.getProblems()) {
			m_problems.add(new Problem(stitchProblemToProblem(problem), problem.getMessage()));
		}
	}

	protected ProblemT stitchProblemToProblem(IStitchProblem problem) {
		switch (problem.getSeverity()) {
		case IStitchProblem.ERROR:
		case IStitchProblem.FATAL:
			return ProblemT.ERROR;
		case IStitchProblem.WARNING:
			return ProblemT.WARNING;
		default:
			return ProblemT.INFO;

		}

	}

	@Override
	public void setRainbowMaster(IRainbowMaster master) {
		m_master = master;
	}

	@Override
	public Collection<Problem> getProblems() {
		return m_problems;
	}

}
