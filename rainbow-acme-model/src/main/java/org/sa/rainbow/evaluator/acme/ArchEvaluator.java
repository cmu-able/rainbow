package org.sa.rainbow.evaluator.acme;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.environment.IAcmeEnvironment;
import org.acmestudio.acme.environment.error.AcmeError;
import org.acmestudio.acme.type.IAcmeTypeChecker;
import org.acmestudio.acme.type.verification.SimpleModelTypeChecker;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.AcmeRainbowOperationEvent.CommandEventT;
import org.sa.rainbow.model.acme.AcmeTypecheckSetCmd;

/**
 * The Rainbow Architectural Evaluator, which performs change-triggered evaluation of the architectural model. When a
 * constraint fails, this is reported back to the model through the setTypecheckResult operation.
 * 
 * This is backward compatible with the old Rainbow: eventually, IArchEvaluations should be migrated as their own
 * Rainbow analysis
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 * @history * [2009.03.04] Removed beacon for model evaluation, set sleep period instead.
 */
public class ArchEvaluator extends AbstractRainbowRunnable implements IRainbowAnalysis,
IRainbowModelChangeCallback<IAcmeSystem> {

    private static final String                    SET_TYPECHECK_OPERATION_NAME = "setTypecheckResult";

    public static final String                     NAME                         = "Rainbow Acme Architecture Constraint Evaluator";

    /** Reference to the Rainbow model */
    private boolean                                m_adaptationNeeded           = false;
    private IModelChangeBusSubscriberPort          m_modelChangePort;

    /** Matches the end of changes to the model **/
    private IRainbowChangeBusSubscription          m_modelChangeSubscriber      = new IRainbowChangeBusSubscription () {

        @Override
        public
        boolean
        matches (IRainbowMessage message) {
            String type = (String )message
                    .getProperty (IModelChangeBusPort.EVENT_TYPE_PROP);
            if (type != null) {
                try {
                    CommandEventT ct = CommandEventT
                            .valueOf (type);
                    if (ct.isEnd ()
                            && !SET_TYPECHECK_OPERATION_NAME
                            .equals (message
                                    .getProperty (IModelChangeBusPort.COMMAND_PROP))) {
                        String modelType = (String )message
                                .getProperty (IModelChangeBusPort.MODEL_TYPE_PROP);
                        if ("Acme"
                                .equals (modelType))
                            return true;
                    }
                }
                catch (Exception e) {
                }
            }
            return false;
        }
    };

    /** The models to typecheck **/
    private LinkedBlockingQueue<AcmeModelInstance> m_modelCheckQ                = new LinkedBlockingQueue<> ();

    private Set<IArchEvaluation>                   m_evaluations;

    /**
     * Default Constructor.
     */
    public ArchEvaluator () {
        super (NAME);

        String per = Rainbow.getProperty (RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD);
        if (per != null) {
            setSleepTime (Long.parseLong (per));
        }
        else { // default to using the long sleep value
            setSleepTime (IRainbowRunnable.LONG_SLEEP_TIME);
        }

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
        m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort (Rainbow.instance ()
                .getRainbowMaster ().modelsManager ());
    }

    private void installEvaluations () {
        String evaluators = Rainbow.getProperty (RainbowConstants.PROPKEY_ARCH_EVALUATOR_EXTENSIONS);
        if (evaluators == null || evaluators.trim ().equals ("")) {
            m_evaluations = Collections.<IArchEvaluation> emptySet ();
        }
        else {
            m_evaluations = new HashSet<IArchEvaluation> ();
            String[] evaluationSet = evaluators.split (",");
            for (String evaluation : evaluationSet) {
                try {
                    IArchEvaluation evaluationInstance = (IArchEvaluation )Class.forName (evaluation.trim ())
                            .newInstance ();
                    m_evaluations.add (evaluationInstance);
                }
                catch (Throwable e) {
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
            if (typeChecker instanceof SimpleModelTypeChecker) {
                SimpleModelTypeChecker synchChecker = (SimpleModelTypeChecker )typeChecker;
                boolean constraintViolated = !synchChecker.typechecks (model.getModelInstance ());
                AcmeTypecheckSetCmd cmd = model.getCommandFactory ().acmeTypecheckSetCmd (!constraintViolated);
                try {
                    Rainbow.instance ().getRainbowMaster ().modelsManager ().requestModelUpdate (cmd);
                }
                catch (IllegalStateException | RainbowException e) {
                    m_reportingPort.error (RainbowComponentT.ANALYSIS,
                            "Could not execute set typecheck command on model", e);
                }
                if (constraintViolated) {
                    Set<? extends AcmeError> errors = env.getAllRegisteredErrors ();
                    m_reportingPort.info (RainbowComponentT.ANALYSIS,
                            "Model " + model.getModelName () + ":" + model.getModelType () + " constraints violated: "
                                    + errors.toString ());
                }
                else {
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
                            AcmeTypecheckSetCmd cmd = model.getCommandFactory ().acmeTypecheckSetCmd (false);
                            try {
                                Rainbow.instance ().getRainbowMaster ().modelsManager ().requestModelUpdate (cmd);
                            }
                            catch (IllegalStateException | RainbowException e) {
                                m_reportingPort.error (RainbowComponentT.ANALYSIS,
                                        "Could not execute set typecheck command on model", e);
                            }
                        }

                        @Override
                        public AcmeModelInstance getModel () {
                            return model;
                        }
                    });
                }
                catch (Throwable t) {
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
    public void onEvent (IModelInstance<IAcmeSystem> model, IRainbowMessage message) {
        if (model instanceof AcmeModelInstance) {
            m_modelCheckQ.offer ((AcmeModelInstance )model);
        }
    }

    @Override
    public String id () {
        return NAME;
    }

    @Override
    protected RainbowComponentT getComponentType () {
        return RainbowComponentT.ANALYSIS;
    }

}