package org.sa.rainbow.stitch.adaptation;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.CountDownLatch;

import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.DefaultAdaptationExecutorVisitor;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.core.StitchExecutionException;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Var;
import org.sa.rainbow.stitch.core.Strategy.Outcome;
import org.sa.rainbow.stitch.history.ExecutionHistoryCommandFactory;
import org.sa.rainbow.stitch.history.ExecutionHistoryModelInstance;
import org.sa.rainbow.stitch.util.ExecutionHistoryData;
import org.sa.rainbow.stitch.visitor.Stitch;

/**
 * This class specializes an adaptation visitor to evaluate Stitch-based
 * adaptations. This enables the parallel, sequential, until_fail, and
 * until_success execution semantics to work with strategies.
 *
 * @author Bradley Schmerl: schmerl
 */
public class StitchExecutionVisitor extends DefaultAdaptationExecutorVisitor<Strategy> {

	private StitchExecutor m_executor;
	private ModelReference m_modelRef;
	private ExecutionHistoryCommandFactory m_historyFactory;

	public StitchExecutionVisitor(StitchExecutor executor, ModelReference modelRef,
			ExecutionHistoryCommandFactory factory, AdaptationTree<Strategy> adt, ThreadGroup tg, CountDownLatch done) {
		super(adt, tg, modelRef + " Visitor", done, executor.getReportingPort());
		m_executor = executor;
		m_modelRef = modelRef;
		m_historyFactory = factory;
	}

	/**
	 * Evaluate the stitchState strSttategy. This evaluation should store the
	 * success or otherwise in a model of the strategy, and should mark it executed
	 * on the strategy model.
	 */
	@Override
	protected boolean evaluate(Strategy adaptation) {
		// Clone the adaptation strategy so that we can run them in parallel
		Strategy actualExecutedAdaptation = adaptation;// .clone ();
		try {
			synchronized (adaptation) {
				if (actualExecutedAdaptation.isExecuting()) {
					Stitch stitch = Ohana.instance()
							.findFreeStitch(actualExecutedAdaptation.stitchState()/*
																					 * .stitch ()
																					 */);
					for (Strategy t : stitch.script.strategies) {
						if (t.getName().equals(adaptation.getName())) {
							actualExecutedAdaptation = t;
							break;
						}
					}
					if (actualExecutedAdaptation == null || actualExecutedAdaptation.isExecuting()) {
						m_executor.getReportingPort().error(m_executor.getComponentType(), MessageFormat
								.format("[[{0}]]: Could not execute {1}", m_executor.id(), adaptation.getName()));
						return false;
					}
				}
				actualExecutedAdaptation.markExecuting(true);
				actualExecutedAdaptation.setExecutor(m_executor);
			}
			m_executor.log(MessageFormat.format("[[{0}]]: Executing Strategy {1}", m_executor.id(), actualExecutedAdaptation.getName()));
			Strategy.Outcome o = null;
			// provide var fo _dur_
			Var v = new Var();
			v.scope = actualExecutedAdaptation.stitchState().scope();
			v.setType("long");
			v.name = "_dur_";
			v.setValue(0L);
			actualExecutedAdaptation.stitchState()./* stitch(). */script.addVar(v.name, v);
			m_executor.getHistoryModelUSPort()
					.updateModel(m_historyFactory.strategyExecutionStateCommand(adaptation.getQualifiedName(),
							ExecutionHistoryModelInstance.STRATEGY, ExecutionHistoryData.ExecutionStateT.STARTED,
							null));
			o = (Strategy.Outcome) actualExecutedAdaptation.evaluate(null);
			actualExecutedAdaptation.stitchState()/* .stitch() */.script.vars().remove(v.name);

			m_executor.log(MessageFormat.format("[[{0}]]: Outcome({1}): {2}", m_executor.id(),actualExecutedAdaptation.getName(), o));
			m_executor.getHistoryModelUSPort()
					.updateModel(m_historyFactory.strategyExecutionStateCommand(adaptation.getQualifiedName(),
							ExecutionHistoryModelInstance.STRATEGY, ExecutionHistoryData.ExecutionStateT.FINISHED,
							o.toString()));
			adaptation.setOutcome(o);
			return o == Strategy.Outcome.SUCCESS;
		} catch (IOException e) {
			m_executor.getReportingPort().error(m_executor.getComponentType(),
					MessageFormat.format("[[{0}]]: Failed to parse the stitchState file",m_executor.id()), e);
			adaptation.setOutcome(Outcome.FAILURE);
		} catch (StitchExecutionException e) {
			m_executor.getReportingPort().error(m_executor.getComponentType(), MessageFormat.format("[[{0}]]: Failed to execute the strategy: {1}", m_executor.id(), adaptation.getName()), e);
			adaptation.setOutcome(Outcome.FAILURE);
		} finally {
			actualExecutedAdaptation.markExecuting(false);
		}

		return false;
	}

	/**
	 * Return a new instance to manage the execution of an adaptation in a new
	 * thread.
	 */
	@Override
	protected DefaultAdaptationExecutorVisitor spawnNewExecutorForTree(AdaptationTree adt, ThreadGroup g,
			CountDownLatch doneSignal) {
		return new StitchExecutionVisitor(m_executor, m_modelRef, m_historyFactory, adt, g, doneSignal);
	}

}
