/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.sa.rainbow.evaluator.acme;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.acmestudio.acme.core.IAcmeObject;
import org.acmestudio.acme.element.IAcmeElement;
import org.acmestudio.acme.environment.IAcmeEnvironment;
import org.acmestudio.acme.environment.error.AcmeError;
import org.acmestudio.acme.type.IAcmeTypeChecker;
import org.acmestudio.acme.type.verification.ErrorHelper;
import org.acmestudio.acme.type.verification.SynchronousTypeChecker;
import org.acmestudio.standalone.environment.StandaloneEnvironment;
import org.acmestudio.standalone.environment.StandaloneEnvironment.TypeCheckerType;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.AcmeRainbowOperationEvent.CommandEventT;
import org.sa.rainbow.model.acme.AcmeTypecheckSetCmd;

/**
 * The Rainbow Architectural Evaluator, which performs change-triggered
 * evaluation of the architectural model. When a constraint fails, this is
 * reported back to the model through the setTypecheckResult operation.
 * <p/>
 * This is backward compatible with the old Rainbow: eventually,
 * IArchEvaluations should be migrated as their own Rainbow analysis
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 * @history * [2009.03.04] Removed beacon for model evaluation, set sleep period
 *          instead.
 */
public class ArchEvaluator extends AbstractRainbowRunnable implements IRainbowAnalysis, IRainbowModelChangeCallback {

	private static final String SET_TYPECHECK_OPERATION_NAME = "setTypecheckResult";

	private static final String NAME = "Architecture Evaluator";

	private IModelChangeBusSubscriberPort m_modelChangePort;
	private IModelUSBusPort m_modelUSPort;

	private final Map<String, String> properties = new HashMap<>();

	/**
	 * Matches the end of changes to the model
	 **/

	private final IRainbowChangeBusSubscription m_modelChangeSubscriber = new IRainbowChangeBusSubscription() {

		@Override
		public boolean matches(IRainbowMessage message) {
			String type = (String) message.getProperty(IModelChangeBusPort.EVENT_TYPE_PROP);
			if (type != null) {
				try {
					CommandEventT ct = CommandEventT.valueOf(type);
					if (ct.isEnd() && !SET_TYPECHECK_OPERATION_NAME
							.equals(message.getProperty(IModelChangeBusPort.COMMAND_PROP))) {
						String modelType = (String) message.getProperty(IModelChangeBusPort.MODEL_TYPE_PROP);
						if ("Acme".equals(modelType))
							return true;
					}
				} catch (Exception e) {
				}
			}
			return false;
		}
	};

	/**
	 * The models to typecheck
	 **/
	private final LinkedBlockingQueue<AcmeModelInstance> m_modelCheckQ = new LinkedBlockingQueue<>();
	private final Map<ModelReference, Boolean> m_lastResult = new HashMap<>();

	private Set<IArchEvaluation> m_evaluations;

	private IModelsManagerPort m_modelsManagerPort;

	/**
	 * Default Constructor.
	 */
	public ArchEvaluator() {
		super(NAME);

		String per = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD);
		if (per != null) {
			setSleepTime(Long.parseLong(per));
		} else { // default to using the long sleep value
			setSleepTime(IRainbowRunnable.LONG_SLEEP_TIME);
		}
		StandaloneEnvironment.instance().useTypeChecker(TypeCheckerType.SYNCHRONOUS);
		installEvaluations();
	}

	@Override
	public void initialize(IRainbowReportingPort port) throws RainbowConnectionException {
		super.initialize(port);
		initializeConnections();
		initializeSubscriptions();
	}

	private void initializeSubscriptions() {
		m_modelChangePort.subscribe(m_modelChangeSubscriber, this);
	}

	private void initializeConnections() throws RainbowConnectionException {
		m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort();
		m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort();
		m_modelUSPort = RainbowPortFactory.createModelsManagerClientUSPort(this);
	}

	private void installEvaluations() {
		String evaluators = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_ARCH_EVALUATOR_EXTENSIONS);
		if (evaluators == null || evaluators.trim().equals("")) {
			m_evaluations = Collections.emptySet();
		} else {
			m_evaluations = new HashSet<>();
			String[] evaluationSet = evaluators.split(",");
			for (String evaluation : evaluationSet) {
				try {
					IArchEvaluation evaluationInstance = (IArchEvaluation) Class.forName(evaluation.trim())
							.newInstance();
					m_evaluations.add(evaluationInstance);
				} catch (Throwable e) {
					m_reportingPort.error(RainbowComponentT.ANALYSIS, MessageFormat.format(
							"[[{2}]]: Failed to instantiate {0} as an IArchEvaluation", evaluation.trim(), id()), e);
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.core.IDisposable#dispose()
	 */
	@Override
	public void dispose() {
		m_modelChangePort.dispose();
		m_reportingPort.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.core.AbstractRainbowRunnable#log(java.lang.String)
	 */
	@Override
	protected void log(String txt) {
		m_reportingPort.info(RainbowComponentT.ANALYSIS, txt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.core.AbstractRainbowRunnable#runAction()
	 */
	@Override
	protected void runAction() {
		final AcmeModelInstance model = m_modelCheckQ.poll();
		if (model != null) {
			// For each Acme model that changed, check to see if it typechecks
			m_reportingPort.info(getComponentType(), MessageFormat.format("[[{0}]]: Checking constraints", id()));
			IAcmeEnvironment env = model.getModelInstance().getContext().getEnvironment();
			IAcmeTypeChecker typeChecker = env.getTypeChecker();
			if (typeChecker instanceof SynchronousTypeChecker) {
				SynchronousTypeChecker synchChecker = (SynchronousTypeChecker) typeChecker;
				// This is probably thread unsafe -- changes may be being made while
				// the model is being typechecked
				synchChecker.registerModel(model.getModelInstance().getContext().getModel());
				synchChecker.typecheckAllModelsNow();
				boolean constraintViolated = !synchChecker.typechecks(model.getModelInstance());
				if (constraintViolated) {
					String msg = getConstraintMessage(model, env);
					msg += "\n";
				}
				synchChecker.deregisterModel(model.getModelInstance().getContext().getModel());
				ModelReference ref = new ModelReference(model.getModelName(), model.getModelType());

				Boolean last = m_lastResult.get(ref);
				processNewOldAndFixedErrors(model, env);

				if (!m_newErrors.isEmpty()) {
					AcmeTypecheckSetCmd cmd = model.getCommandFactory().setTypecheckResultCmd(model.getModelInstance(),
							false);

					try {
						m_modelUSPort.updateModel(cmd);
					} catch (IllegalStateException e) {
						m_reportingPort.error(RainbowComponentT.ANALYSIS,
								MessageFormat.format("[[{0}]]: Could not execute set typecheck command on model", id()),
								e);
					}
				} else if (!m_fixedErrors.isEmpty()) {
					AcmeTypecheckSetCmd cmd = model.getCommandFactory().setTypecheckResultCmd(model.getModelInstance(),
							true);

					try {
						m_modelUSPort.updateModel(cmd);
					} catch (IllegalStateException e) {
						m_reportingPort.error(RainbowComponentT.ANALYSIS,
								MessageFormat.format("[[{0}]]: Could not execute set typecheck command on model", id()),
								e);
					}
				}

//				if (last == null || last != constraintViolated) {
//					AcmeTypecheckSetCmd cmd = model.getCommandFactory().setTypecheckResultCmd(model.getModelInstance(),
//							!constraintViolated);
//
//					try {
//						m_modelUSPort.updateModel(cmd);
//					} catch (IllegalStateException e) {
//						m_reportingPort.error(RainbowComponentT.ANALYSIS,
//								MessageFormat.format("[[{0}]]: Could not execute set typecheck command on model", id()),
//								e);
//					}
//				}

				if (m_fixedErrors.isEmpty() && m_oldErrors.isEmpty() && m_newErrors.isEmpty()) {
					m_reportingPort.info(RainbowComponentT.ANALYSIS, MessageFormat.format("[[{0}]]: Model {1}:{2} ok",
							id(), model.getModelName(), model.getModelType()));
				} else {
					StringBuffer newErrorS = new StringBuffer();
					StringBuffer oldErrorS = new StringBuffer();
					StringBuffer fixedErrorS = new StringBuffer();

					for (AcmeError e : m_newErrors) {
						IAcmeObject source = e.getSource();
						if (source instanceof IAcmeElement) {
							newErrorS.append(((IAcmeElement) source).getQualifiedName());
							newErrorS.append(", ");
						}
					}

					for (AcmeError e : m_fixedErrors) {
						IAcmeObject source = e.getSource();
						if (source instanceof IAcmeElement) {
							fixedErrorS.append(((IAcmeElement) source).getQualifiedName());
							fixedErrorS.append(", ");
						}
					}
					for (AcmeError e : m_oldErrors) {
						IAcmeObject source = e.getSource();
						if (source instanceof IAcmeElement) {
							oldErrorS.append(((IAcmeElement) source).getQualifiedName());
							oldErrorS.append(", ");
						}
					}
					StringBuffer errorStrs = new StringBuffer();
					if (!m_newErrors.isEmpty())
						errorStrs.append("Error in: ").append(newErrorS);
					if (!m_fixedErrors.isEmpty())
						errorStrs.append("Fixed errors in ").append(fixedErrorS);
					if (!m_oldErrors.isEmpty())
						errorStrs.append("Still errors in: ").append(oldErrorS);
					String msg = MessageFormat.format("[[{3}]]: Model {0}:{1} Errors: {2}", model.getModelName(),
							model.getModelType(), errorStrs, id());
					m_reportingPort.info(RainbowComponentT.ANALYSIS, msg);

				}

//				if (constraintViolated) {
//					try {
//						String msg = getConstraintMessage(model, env);
//						m_reportingPort.info(RainbowComponentT.ANALYSIS, msg);
//					} catch (Exception e) {
//						m_reportingPort.error(RainbowComponentT.ANALYSIS, MessageFormat
//								.format("[[{0}]]: There's an error reporting the constraint violation", id()), e);
//						m_reportingPort.info(RainbowComponentT.ANALYSIS,
//								MessageFormat.format("[[{3}]]: Model {0}:{1} Evaluation Error: {2}",
//										model.getModelName(), model.getModelType(), e.getMessage(), id()));
//					}
//				} else {
//					m_reportingPort.info(RainbowComponentT.ANALYSIS, MessageFormat.format("[[{0}]]: Model {1}:{2} ok",
//							id(), model.getModelName(), model.getModelType()));
//				}

			}

			// This is here for backwards compatibility of sorts; these should be factored
			// out into
			// separate analyses
			for (IArchEvaluation evaluation : m_evaluations) {
				try {
					evaluation.modelChanged(new IArchEvaluator() {

						@Override
						public void requestAdaptation() {
							AcmeTypecheckSetCmd cmd = model.getCommandFactory()
									.setTypecheckResultCmd(getModel().getModelInstance(), false);
							try {
								m_modelUSPort.updateModel(cmd);
							} catch (IllegalStateException e) {
								m_reportingPort.error(RainbowComponentT.ANALYSIS,
										"Could not execute set typecheck command on model", e);
							}
						}

						@Override
						public AcmeModelInstance getModel() {
							return model;
						}
					});
				} catch (Throwable t) {
					m_reportingPort.error(RainbowComponentT.ANALYSIS,
							"Evaluator " + evaluation.getClass().getName() + " threw an exception: " + t.getMessage());
				}
			}
		}
	}

	Set<AcmeError> m_oldErrors = new HashSet<>();
	Set<AcmeError> m_newErrors = new HashSet<>();
	Set<AcmeError> m_fixedErrors = new HashSet<>();

	protected void processNewOldAndFixedErrors(final AcmeModelInstance model, IAcmeEnvironment env) {
		Set<? extends AcmeError> errors = env.getAllRegisteredErrors();
		Set<AcmeError> rootCauses = new HashSet<AcmeError>();
		for (AcmeError error : errors) {
			ErrorHelper.populateRootCauseSet(error, rootCauses);
		}

		m_oldErrors.addAll(m_newErrors);
		m_fixedErrors = new HashSet<>(m_oldErrors);
		m_fixedErrors.removeAll(rootCauses);
		m_newErrors = new HashSet<>(rootCauses);
		m_newErrors.removeAll(m_oldErrors);
		m_oldErrors.retainAll(rootCauses);
	}

	protected String getConstraintMessage(final AcmeModelInstance model, IAcmeEnvironment env) {
		Set<? extends AcmeError> errors = env.getAllRegisteredErrors();
		Set<String> errorStrs = new HashSet<>();
		for (AcmeError error : errors) {
			String[] causes = error.getMessageText().split("->");
			errorStrs.add(causes[causes.length - 1]);
		}

		String msg = MessageFormat.format("[[{3}]]: Model {0}:{1} constraints violated: {2}", model.getModelName(),
				model.getModelType(), errorStrs, id());
		return msg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.model.evaluator.IArchEvaluator#getModel()
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.model.evaluator.IArchEvaluator#requestAdaptation()
	 */

	@Override
	public void onEvent(ModelReference ref, IRainbowMessage message) {
		// Assuming that the model manager is local, otherwise this call will be slow
		// when done this often
		@SuppressWarnings("rawtypes")
		IModelInstance model = m_modelsManagerPort.getModelInstance(ref); // ref.getModelType (), ref.getModelName ());
		if (model instanceof AcmeModelInstance && !m_modelCheckQ.contains(model)) {
			m_modelCheckQ.offer((AcmeModelInstance) model);
		}
	}

	@Override
	public String id() {
		return NAME;
	}

	@Override
	public RainbowComponentT getComponentType() {
		return RainbowComponentT.ANALYSIS;
	}

	@Override
	public void setProperty(String key, String value) {
		properties.put(key, value);
	}

	@Override
	public String getProperty(String key) {
		return properties.get(key);
	}

}
