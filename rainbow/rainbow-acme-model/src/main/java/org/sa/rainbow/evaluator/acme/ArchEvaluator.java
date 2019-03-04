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

import org.acmestudio.acme.environment.IAcmeEnvironment;
import org.acmestudio.acme.environment.error.AcmeError;
import org.acmestudio.acme.type.IAcmeTypeChecker;
import org.acmestudio.acme.type.verification.SimpleModelTypeChecker;
import org.acmestudio.acme.type.verification.SynchronousTypeChecker;
import org.acmestudio.standalone.environment.StandaloneEnvironment;
import org.acmestudio.standalone.environment.StandaloneEnvironment.TypeCheckerType;
import org.sa.rainbow.core.*;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.*;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.AcmeRainbowOperationEvent.CommandEventT;
import org.sa.rainbow.model.acme.AcmeTypecheckSetCmd;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The Rainbow Architectural Evaluator, which performs change-triggered evaluation of the architectural model. When a
 * constraint fails, this is reported back to the model through the setTypecheckResult operation.
 * <p/>
 * This is backward compatible with the old Rainbow: eventually, IArchEvaluations should be migrated as their own
 * Rainbow analysis
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 * @history * [2009.03.04] Removed beacon for model evaluation, set sleep period instead.
 */
public class ArchEvaluator extends AbstractRainbowRunnable implements IRainbowAnalysis,
                                                                      IRainbowModelChangeCallback {

    private static final String SET_TYPECHECK_OPERATION_NAME = "setTypecheckResult";

    private static final String NAME = "Rainbow Acme Architecture Constraint Evaluator";

    private IModelChangeBusSubscriberPort m_modelChangePort;
    private IModelUSBusPort               m_modelUSPort;

    private final Map<String, String> properties = new HashMap<> ();

    /**
     * Matches the end of changes to the model
     **/

    private final IRainbowChangeBusSubscription m_modelChangeSubscriber = new IRainbowChangeBusSubscription () {

        @Override
        public boolean
        matches (IRainbowMessage message) {
            String type = (String) message
                    .getProperty (IModelChangeBusPort.EVENT_TYPE_PROP);
            if (type != null) {
                try {
                    CommandEventT ct = CommandEventT
                            .valueOf (type);
                    if (ct.isEnd ()
                            && !SET_TYPECHECK_OPERATION_NAME
                            .equals (message
                                             .getProperty (IModelChangeBusPort.COMMAND_PROP))) {
                        String modelType = (String) message
                                .getProperty (IModelChangeBusPort.MODEL_TYPE_PROP);
                        if ("Acme"
                                .equals (modelType))
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
    private final LinkedBlockingQueue<AcmeModelInstance> m_modelCheckQ = new LinkedBlockingQueue<> ();
    private final Map<ModelReference, Boolean>           m_lastResult  = new HashMap<> ();

    private Set<IArchEvaluation> m_evaluations;

    private IModelsManagerPort m_modelsManagerPort;

    /**
     * Default Constructor.
     */
    public ArchEvaluator () {
        super (NAME);

        String per = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD);
        if (per != null) {
            setSleepTime (Long.parseLong (per));
        } else { // default to using the long sleep value
            setSleepTime (IRainbowRunnable.LONG_SLEEP_TIME);
        }
        StandaloneEnvironment.instance().useTypeChecker(TypeCheckerType.SYNCHRONOUS);
        installEvaluations ();
    }

    @Override
    public void initialize (IRainbowReportingPort port) throws RainbowConnectionException {
        super.initialize (port);
        initializeConnections ();
        initializeSubscriptions ();
    }

    private void initializeSubscriptions () {
        m_modelChangePort.subscribe (m_modelChangeSubscriber, this);
    }

    private void initializeConnections () throws RainbowConnectionException {
        m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort ();
        m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort ();
        m_modelUSPort = RainbowPortFactory.createModelsManagerClientUSPort (this);
    }

    private void installEvaluations () {
        String evaluators = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_ARCH_EVALUATOR_EXTENSIONS);
        if (evaluators == null || evaluators.trim ().equals ("")) {
            m_evaluations = Collections.emptySet ();
        } else {
            m_evaluations = new HashSet<> ();
            String[] evaluationSet = evaluators.split (",");
            for (String evaluation : evaluationSet) {
                try {
                    IArchEvaluation evaluationInstance = (IArchEvaluation) Class.forName (evaluation.trim ())
                            .newInstance ();
                    m_evaluations.add (evaluationInstance);
                } catch (Throwable e) {
                    m_reportingPort.error (RainbowComponentT.ANALYSIS, MessageFormat.format (
                            "Failed to instantiate {0} as an IArchEvaluation", evaluation.trim ()), e);
                }
            }
        }

    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.IDisposable#dispose()
     */
    @Override
    public void dispose () {
        m_modelChangePort.dispose ();
        m_reportingPort.dispose ();
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.AbstractRainbowRunnable#log(java.lang.String)
     */
    @Override
    protected void log (String txt) {
        m_reportingPort.info (RainbowComponentT.ANALYSIS, txt);
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.AbstractRainbowRunnable#runAction()
     */
    @Override
    protected void runAction () {
        final AcmeModelInstance model = m_modelCheckQ.poll ();
        if (model != null) {
            // For each Acme model that changed, check to see if it typechecks
            IAcmeEnvironment env = model.getModelInstance ().getContext ().getEnvironment ();
            IAcmeTypeChecker typeChecker = env.getTypeChecker ();
            if (typeChecker instanceof SynchronousTypeChecker) {
            	SynchronousTypeChecker synchChecker = (SynchronousTypeChecker) typeChecker;
            	// This is probably thread unsafe -- changes may be being made while 
            	// the model is being typechecked
            	synchChecker.typecheckAllModelsNow();
                boolean constraintViolated = !synchChecker.typechecks (model.getModelInstance ());
                ModelReference ref = new ModelReference (model.getModelName (), model.getModelType ());
                Boolean last = m_lastResult.get (ref);
                if (last == null || last != constraintViolated) {
                    m_lastResult.put (ref, constraintViolated);
                    AcmeTypecheckSetCmd cmd = model.getCommandFactory ().setTypecheckResultCmd
                            (model.getModelInstance (), !constraintViolated);

                    try {
                        m_modelUSPort.updateModel (cmd);
                    } catch (IllegalStateException e) {
                        m_reportingPort.error (RainbowComponentT.ANALYSIS,
                                               "Could not execute set typecheck command on model", e);
                    }
                }
                if (constraintViolated) {
                    try {
                        Set<? extends AcmeError> errors = env.getAllRegisteredErrors ();
                        m_reportingPort.info (RainbowComponentT.ANALYSIS,
                                              "Model " + model.getModelName () + ":" + model.getModelType () + " " +
                                                      "constraints violated: "
                                                      + errors.toString ());
                    } catch (Exception e) {
                        m_reportingPort.error (RainbowComponentT.ANALYSIS,
                                               "There's an error reporting the constraint violation", e);
                        m_reportingPort.info (RainbowComponentT.ANALYSIS, "Model " + model.getModelName () + ":"
                                + model.getModelType () + " constraints violated: <error in reporting>");
                    }
                } else {
                    m_reportingPort.info (RainbowComponentT.ANALYSIS,
                                          "Model " + model.getModelName () + ":" + model.getModelType () + " ok");
                }

            }

            // This is here for backwards compatibility of sorts; these should be factored out into
            // separate analyses
            for (IArchEvaluation evaluation : m_evaluations) {
                try {
                    evaluation.modelChanged (new IArchEvaluator () {

                        @Override
                        public void requestAdaptation () {
                            AcmeTypecheckSetCmd cmd = model.getCommandFactory ().setTypecheckResultCmd (getModel
                                                                                                                ()
                                                                                                                .getModelInstance (),
                                                                                                        false);
                            try {
                                m_modelUSPort.updateModel (cmd);
                            } catch (IllegalStateException e) {
                                m_reportingPort.error (RainbowComponentT.ANALYSIS,
                                                       "Could not execute set typecheck command on model", e);
                            }
                        }

                        @Override
                        public AcmeModelInstance getModel () {
                            return model;
                        }
                    });
                } catch (Throwable t) {
                    m_reportingPort.error (RainbowComponentT.ANALYSIS, "Evaluator " + evaluation.getClass ().getName ()
                            + " threw an exception: " + t.getMessage ());
                }
            }
        }
    }

/* (non-Javadoc)
 * @see org.sa.rainbow.model.evaluator.IArchEvaluator#getModel()
 */

/* (non-Javadoc)
 * @see org.sa.rainbow.model.evaluator.IArchEvaluator#requestAdaptation()
 */

    @Override
    public void onEvent (ModelReference ref, IRainbowMessage message) {
        // Assuming that the model manager is local, otherwise this call will be slow when done this often
        @SuppressWarnings("rawtypes")
        IModelInstance model = m_modelsManagerPort.getModelInstance (ref); //ref.getModelType (), ref.getModelName ());
        if (model instanceof AcmeModelInstance) {
            m_modelCheckQ.offer ((AcmeModelInstance) model);
        }
    }

    @Override
    public String id () {
        return NAME;
    }


    @Override
    public RainbowComponentT getComponentType () {
        return RainbowComponentT.ANALYSIS;
    }

    @Override
    public void setProperty (String key, String value) {
        properties.put (key, value);
    }

    @Override
    public String getProperty (String key) {
        return properties.get (key);
    }

}
