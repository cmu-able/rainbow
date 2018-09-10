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
package org.sa.rainbow.swim.adaptation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.acmestudio.acme.element.IAcmeSystem;
import org.apache.commons.lang.time.StopWatch;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.DefaultAdaptationTreeWalker;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.health.IRainbowHealthProtocol;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.UtilityFunction;
import org.sa.rainbow.core.models.UtilityPreferenceDescription;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowAdaptationEnqueuePort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.AcmeRainbowOperationEvent.CommandEventT;
import org.sa.rainbow.model.acme.swim.SwimModelHelper;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Tactic;
import org.sa.rainbow.stitch.error.DummyStitchProblemHandler;
import org.sa.rainbow.stitch.error.IStitchProblem;
import org.sa.rainbow.stitch.error.StitchProblem;
import org.sa.rainbow.stitch.visitor.Stitch;
import org.sa.rainbow.timeseriespredictor.model.TimeSeriesPredictorModel;
import org.sa.rainbow.timeseriespredictor.model.TimeSeriesPredictorModelInstance;
import org.sa.rainbow.util.Beacon;
import org.sa.rainbow.util.Util;

import pladapt.EnvironmentDTMCPartitioned;

/**
 * PLA Adaptation Manager Base
 *
 * @author gmoreno
 */
public abstract class AdaptationManagerBase extends AbstractRainbowRunnable
        implements IAdaptationManager<Strategy>, IRainbowModelChangeCallback {

    private static final String PLA_OPERA_CONFIG = "rainbow.adaptation.pla.operaConfig";
    private static final String TSP_MODEL = "ArrivalRate";
    
	public enum Mode {
        SERIAL, MULTI_PRONE
    }

    public static final  String NAME                     = "PLA Adaptation Manager";
    public static final  double FAILURE_RATE_THRESHOLD   = 0.95;
    public static final  double MIN_UTILITY_THRESHOLD    = 0.40;
    public static final  long   FAILURE_EFFECTIVE_WINDOW = 2000 /* ms */;
    public static final  long   FAILURE_WINDOW_CHUNK     = 1000 /* ms */;
    
    private Mode                           m_mode              = Mode.SERIAL;
    protected AcmeModelInstance              m_model             = null;
    private boolean                        m_adaptNeeded       = false;      // treat as synonymous with
    // constraint being violated
    private boolean                        m_adaptEnabled      = true;       // by default, we adapt
    protected List<Stitch>                   m_repertoire        = null;
    private List<AdaptationTree<Strategy>> m_pendingStrategies = null;

    // track history
    private String                                  m_historyTrackUtilName = null;
    private Map<String, int[]>                      m_historyCnt           = null;
    private Map<String, Beacon>                     m_failTimer            = null;
    private IRainbowAdaptationEnqueuePort<Strategy> m_enqueuePort          = null;
    private IModelChangeBusSubscriberPort           m_modelChangePort      = null;
    private IModelsManagerPort                      m_modelsManagerPort    = null;
    private String m_modelRef;
    private FileChannel                   m_strategyLog              = null;
    private IRainbowChangeBusSubscription m_modelTypecheckingChanged = new IRainbowChangeBusSubscription () {

        @Override
        public boolean matches (IRainbowMessage message) {
            String type = (String) message.getProperty (IModelChangeBusPort.EVENT_TYPE_PROP);
            String modelName = (String) message.getProperty (IModelChangeBusPort.MODEL_NAME_PROP);
            String modelType = (String) message.getProperty (IModelChangeBusPort.MODEL_TYPE_PROP);
            try {
                CommandEventT ct = CommandEventT.valueOf (type);
                return (ct.isEnd ()
                        && "setTypecheckResult".equals (message.getProperty (IModelChangeBusPort.COMMAND_PROP))
                        && m_modelRef.equals (Util.genModelRef (modelName, modelType)));
            } catch (Exception e) {
                return false;
            }
        }
    };
    //private UtilityPreferenceDescription m_utilityModel;
    
    protected int m_horizon;
    private TimeSeriesPredictorModelInstance m_tspModel;
    private OperaWrapper m_opera;
    private double m_lastBasicResponseTime;
    private double m_lastOptResponseTime;
    protected boolean m_isInitialized = false;

    /**
     * Default constructor.
     */
    public AdaptationManagerBase () {
        super (NAME);

        m_repertoire = new ArrayList<Stitch> ();
        m_pendingStrategies = new ArrayList<AdaptationTree<Strategy>> ();
        m_historyTrackUtilName = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_TRACK_STRATEGY);
        if (m_historyTrackUtilName != null) {
            m_historyCnt = new HashMap<String, int[]> ();
            m_failTimer = new HashMap<String, Beacon> ();
        }

        //setSleepTime (SLEEP_TIME);
        String per = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD);
        if (per != null) {
            setSleepTime (Long.parseLong (per));
        } else { // default to using the long sleep value
            setSleepTime (IRainbowRunnable.LONG_SLEEP_TIME);
        }
    }

    @Override
    public void initialize (IRainbowReportingPort port) throws RainbowConnectionException {
        super.initialize (port);
        initConnectors ();

    }

    private void initConnectors () throws RainbowConnectionException {
        m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort ();
        m_modelChangePort.subscribe (m_modelTypecheckingChanged, this);
        m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort ();
    }

    @Override
    public void setModelToManage (ModelReference model) {
        m_modelRef = model.getModelName () + ":" + model.getModelType ();
        try {
            m_strategyLog = new FileOutputStream (new File (new File (Rainbow.instance ().getTargetPath (), "log"),
                                                            model.getModelName () + "-adaptation.log")).getChannel ();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
        m_model = (AcmeModelInstance) m_modelsManagerPort.<IAcmeSystem>getModelInstance (model);
        if (m_model == null) {
            m_reportingPort.error (RainbowComponentT.ADAPTATION_MANAGER,
                                   MessageFormat.format ("Could not find reference to {0}", model.toString ()));
        }
        m_enqueuePort = RainbowPortFactory.createAdaptationEnqueuePort (model);
//        ModelReference utilityModelRef = new ModelReference (model.getModelName (), "UtilityModel");
//        IModelInstance<UtilityPreferenceDescription> modelInstance = m_modelsManagerPort
//                .getModelInstance (utilityModelRef);
//        if (modelInstance == null) {
//            m_reportingPort.error (RainbowComponentT.ADAPTATION_MANAGER,
//                                   MessageFormat.format (
//                                           "There is no utility model associated with this model. Expecting to find " +
//                                                   "''{0}''. Perhaps it is not specified in the rainbow.properties " +
//                                                   "file?",
//                                           utilityModelRef.toString ()));
//
//        } else {
//            m_utilityModel = modelInstance.getModelInstance ();
//        }

        initAdaptationRepertoire ();

        SwimModelHelper swimModel = new SwimModelHelper(m_model);
        if (!m_isInitialized) {
        	initializeAdaptationMgr(swimModel);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.core.IDisposable#dispose()
     */
    @Override
    public void dispose () {
        for (Stitch stitch : m_repertoire) {
            stitch.dispose ();
        }
        Ohana.instance ().dispose ();
        m_repertoire.clear ();
        m_pendingStrategies.clear ();
        if (m_historyTrackUtilName != null) {
            m_historyCnt.clear ();
            m_failTimer.clear ();
            m_historyCnt = null;
            m_failTimer = null;
        }

        if (m_enqueuePort != null) {
            m_enqueuePort.dispose ();
        }
        m_modelChangePort.dispose ();

        // null-out data members
        m_repertoire = null;
        m_pendingStrategies = null;
        m_historyTrackUtilName = null;
        m_model = null;
        if (m_strategyLog != null) {
            try {
                m_strategyLog.close ();
            } catch (IOException ignored) {
            }
            m_strategyLog = null;
        }
    }

    @Override
    protected void doTerminate () {
        if (m_strategyLog != null) {
            try {
                m_strategyLog.close ();
            } catch (IOException ignore) {
            }
            m_strategyLog = null;
        }
        super.doTerminate ();
    }
    
    protected void finalize() {
        if (m_strategyLog != null) {
            try {
                m_strategyLog.close ();
            } catch (IOException ignore) {
            }
            m_strategyLog = null;
        }
    }

    /*
         * (non-Javadoc)
         *
         * @see org.sa.rainbow.core.AbstractRainbowRunnable#log(java.lang.String)
         */
    @Override
    protected void log (String txt) {
        m_reportingPort.info (RainbowComponentT.ADAPTATION_MANAGER, txt);
    }

	protected void error (String txt) {
	    m_reportingPort.error (RainbowComponentT.ADAPTATION_MANAGER, txt);
	}

    @Override
    public boolean isEnabled () {
        return m_adaptEnabled;
    }

    public void setAdaptationEnabled (boolean b) {
        m_adaptEnabled = b;
    }

    public boolean adaptationInProgress () {
        return m_adaptNeeded;
    }

    /**
     * Removes a Strategy from the list of pending strategies, marking it as being completed (doesn't incorporate
     * outcome).
     *
     * @param strategy the strategy to mark as being executed.
     */
    @Override
    public void markStrategyExecuted (AdaptationTree<Strategy> strategy) {
    	//log("****marking strategy " + strategy);
        if (m_pendingStrategies.contains (strategy)) {
            m_pendingStrategies.remove (strategy);
            final List<Strategy> strategiesExecuted = new LinkedList<> ();
            
            StrategyAdaptationResultsVisitor resultCollector = new StrategyAdaptationResultsVisitor
                    (strategy, strategiesExecuted);
            strategy.visit (resultCollector);
            
            for (Strategy str : strategiesExecuted) {
                String s = str.getName () + ";" + str.outcome ();
                log ("*S* outcome: " + s);
                Util.dataLogger ().info (IRainbowHealthProtocol.DATA_ADAPTATION_STRATEGY + s);
                tallyStrategyOutcome (str);
            }

        }
        if (m_pendingStrategies.size () == 0) {
            Util.dataLogger ().info (IRainbowHealthProtocol.DATA_ADAPTATION_END);
            // reset adaptation flags
            m_adaptNeeded = false;
//            m_model.clearConstraintViolated ();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sa.rainbow.core.AbstractRainbowRunnable#runAction()
     */
    @Override
    protected void runAction () {
        if (m_adaptEnabled) {
            if (m_mode == Mode.SERIAL && m_pendingStrategies.size () > 0) {
                // Only go if there are no pending strategies
            	
            	log("Pending strategies. Skipping adaptation decision");
                return;
            }
            Util.dataLogger ().info (IRainbowHealthProtocol.DATA_ADAPTATION_SELECTION_BEGIN);
            AdaptationTree<Strategy> at = checkAdaptation ();
            Util.dataLogger ().info (IRainbowHealthProtocol.DATA_ADAPTATION_SELECTION_END);
            if (at != null) {
                //log (">> do strategy: " + at);
                m_pendingStrategies.add (at);
                m_enqueuePort.offerAdaptation (at, null);
                String logMessage = at.toString ();
                strategyLog(logMessage);
            }
        }
    }

    private void strategyLog (String logMessage) {
        if (m_strategyLog != null) {
            Date d = new Date ();
            String log = MessageFormat.format ("{0,number,#},queuing,{1}\n", d.getTime (),
                                               logMessage);
            try {
                m_strategyLog.write (java.nio.ByteBuffer.wrap (log.getBytes ()));
            } catch (IOException e) {
                reportingPort ().error (getComponentType (), "Failed to write " + log + " to log file");
            }
        }
    }

    /**
     * For JUnit testing, used to set a stopwatch object used to time duration.
     */
    StopWatch _stopWatchForTesting = null;

    /**
     * For JUnit testing, allows fetching the strategy repertoire. NOT for public use!
     *
     * @return list of Stitch objects loaded at initialization from stitch file.
     */
    List<Stitch> _retrieveRepertoireForTesting () {
        return m_repertoire;
    }


    /**
     * For JUnit testing, allows re-invoking defineAttributes to artificially increase the number of quality dimensions
     * in tactic attribute vectors.
     */
    void _defineAttributesFromTester (Stitch stitch, Map<String, Map<String, Object>> attrVectorMap) {
        defineAttributes (stitch, attrVectorMap);
    }

    
    private void computeDecisionHorizon(SwimModelHelper swimModel) {
		m_horizon = (int) Math.max(5.0, 
				swimModel.getAddServerLatencyPeriods() * (swimModel.getMaxServers() - 1) + 1);
		
		// set value in tsp model
		ModelReference modelRef = new ModelReference(TSP_MODEL, TimeSeriesPredictorModelInstance.MODEL_TYPE);
		m_tspModel = (TimeSeriesPredictorModelInstance) m_modelsManagerPort.<TimeSeriesPredictorModel>getModelInstance (modelRef);
		try {
			m_tspModel.getModelInstance().setHorizon(m_horizon);
		} catch (RainbowException e) {
            m_reportingPort.error (RainbowComponentT.ADAPTATION_MANAGER, e.toString());
		}
    }
    
    /**
     * load PLADAPT wrapper
     */
    static {
        System.loadLibrary("pladapt_wrap");
      }
    
    private void initOpera() {
        log("Initializing Opera");
        File operaConfigFile = Util.getRelativeToPath (Rainbow.instance ().getTargetPath (),
                Rainbow.instance ().getProperty (PLA_OPERA_CONFIG));
        m_opera = new OperaWrapper();
        m_opera.configure(operaConfigFile);
        log("Opera initialized");
    }
    
    /**
     * update KF using Opera model
     */
    private void updateOperaKF(SwimModelHelper swimModel) {
    	
        // update KF
        // utilization and arrival rate have to be normalized to a single server (we assumed all are identical)
    	double obsBasicThroughput = swimModel.getBasicThroughput();
    	double obsOptThroughput = swimModel.getOptThroughput();
    	double obsBasicResponseTime = swimModel.getBasicResponseTime();
    	double obsOptResponseTime = swimModel.getOptResponseTime();
    	
        int activeServers = swimModel.getNumActiveServers();
        double avgUtilization = swimModel.getAvgUtilization();
        double basicResponseTime = 0;
        if (obsBasicThroughput > 0.0) {
            basicResponseTime = obsBasicResponseTime;
            m_lastBasicResponseTime = basicResponseTime;
        } else {
            basicResponseTime = m_lastBasicResponseTime;
        }
        double optResponseTime = 0;
        if (obsOptThroughput > 0.0) {
            optResponseTime = obsOptResponseTime;
            m_lastOptResponseTime = optResponseTime;
        } else {
            optResponseTime = m_lastOptResponseTime;
        }

        double arrivalRate = swimModel.getArrivalRate();
        double dimmer = swimModel.getCurrentDimmer();
        double normalArrivalRate = arrivalRate * dimmer;
        double lowArrivalRate = arrivalRate * (1 - dimmer);


        log("KF update : servers=" + activeServers
                + " avgutil=" + avgUtilization
                + " normalAR=" + normalArrivalRate
                + " normalRT=" + optResponseTime
                + " normalTP=" + obsOptThroughput
                + " low: AR=" + lowArrivalRate
                + " RT=" + basicResponseTime
                + " TP=" + obsBasicThroughput);
        m_opera.updateModel(activeServers, avgUtilization,
                normalArrivalRate, optResponseTime, obsOptThroughput,
                lowArrivalRate, basicResponseTime, obsBasicThroughput);

        double[] results  = m_opera.getParamEstimates();
        double estBasicServiceTime = results[1];
        double estOptServiceTime = results[0];

        log("KF output X low(" + estBasicServiceTime + ')'
                + ", normal(" + estOptServiceTime +  ')');
        swimModel.setEstimatedBasicServiceTime(estBasicServiceTime, Math.pow(estBasicServiceTime, 2));
        swimModel.setEstimatedOptServiceTime(estOptServiceTime, Math.pow(estOptServiceTime, 2));
    }
    

    private EnvironmentDTMCPartitioned generateEnvironmentModel() {
    	return m_tspModel.getModelInstance().generateEnvironmentDTMC(2, m_horizon);
    }
    
    protected Strategy getStrategy(String name) {
    	Strategy strategy = null;
        for (Stitch stitch : m_repertoire) {
            if (!stitch.script.isApplicableForSystem (m_model)) {
                m_reportingPort.trace (getComponentType (), "x. skipping " + stitch.script.getName ());
                continue; // skip checking this script
            }
            for (Strategy t : stitch.script.strategies) {
            	if (t.getName().equals(name)) {
            		strategy = t;
            		break;
            	}
            }
        }
        
        if (strategy == null) {
			m_reportingPort.error(RainbowComponentT.ADAPTATION_MANAGER,
					"Script for strategy ''" + name + "'' not found");
        }
        return strategy;
    }
  
    /**
     * Subclasses overriding this for their own initialization must invoke this method
     */
    protected void initializeAdaptationMgr(SwimModelHelper swimModel) {
        computeDecisionHorizon(swimModel);
        initOpera();
    	m_isInitialized = true;
    }

    /**
     * Select (or generate) strategy for adaptation
     * @return strategy to execute or null for no adaptation
     */
    protected AdaptationTree<Strategy> checkAdaptation () {
    	boolean decide = true;
    	AdaptationTree<Strategy> at = null;
        log ("Checking if adaptation is required.");
        if (_stopWatchForTesting != null) {
            _stopWatchForTesting.start ();
        }

        SwimModelHelper swimModel = new SwimModelHelper(m_model);
        if (!m_isInitialized) {
        	initializeAdaptationMgr(swimModel);
        }

        updateOperaKF(swimModel);
        
        // the env generation is part of the decision as in OMNeT
    	long start = System.currentTimeMillis();
        EnvironmentDTMCPartitioned env = generateEnvironmentModel();
        if (env == null) {
        	log("No environment observations available. Can't make adaptation decision");
        	decide = false;
        }
        
        if (decide) {
        	at = checkAdaptationImpl (swimModel, env);
        	long durationMsec = System.currentTimeMillis() - start;
        	log("Adaptation decision time (ms) = " + durationMsec);
        }
        
        if (_stopWatchForTesting != null) {
            _stopWatchForTesting.stop ();
        }
        
        return at;
    }
    
	protected abstract AdaptationTree<Strategy> checkAdaptationImpl(SwimModelHelper swimModel,
			EnvironmentDTMCPartitioned env);

    @SuppressWarnings("unused")
	private AdaptationTree<Strategy> checkAdaptationTest () {
    	AdaptationTree<Strategy> at = null;
        log ("Checking if adaptation is required.");
        if (_stopWatchForTesting != null) {
            _stopWatchForTesting.start ();
        }

        at = new AdaptationTree<Strategy>(getStrategy("AddServer"));
//        StringVector tactics = new StringVector();
//        tactics.add("IncDimmer");
//        tactics.add("AddServer");
//    	at = new AdaptationTree<Strategy>(AdaptationExecutionOperatorT.PARALLEL);
//        for (int t = 0; t < tactics.size(); t++) {
//        	log(tactics.get(t));
//        	Strategy strategy = getStrategy(tactics.get(t)); // Strategy has tactic name
//        	at.addLeaf(strategy);
//        }

        if (_stopWatchForTesting != null) {
            _stopWatchForTesting.stop ();
        }
        return at;
    }

    /**
     * Retrieves the adaptation repertoire; for each tactic, store the respective tactic attribute vectors.
     */
    private void initAdaptationRepertoire () {
        File stitchPath = Util.getRelativeToPath (Rainbow.instance ().getTargetPath (),
                                                  Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_SCRIPT_PATH));
        if (stitchPath == null) {
            m_reportingPort.error (RainbowComponentT.ADAPTATION_MANAGER, "The stitch path is not set!");
        } else if (stitchPath.exists () && stitchPath.isDirectory ()) {
            FilenameFilter ff = new FilenameFilter () { // find only ".s" files
                @Override
                public boolean accept (File dir, String name) {
                    return name.endsWith (".s");
                }
            };
            for (File f : stitchPath.listFiles (ff)) {
                try {
                	
                	// don't load .t.s files: these are tactics imported from .s files
                	if (f.getName().endsWith(".t.s")) {
                		continue;
                	}
                    // don't duplicate loading of script files
                    Stitch stitch = Ohana.instance ().findStitch (f.getCanonicalPath ());
                    if (stitch == null) {
                        DummyStitchProblemHandler stitchProblemHandler = new DummyStitchProblemHandler ();
                        stitch = Stitch.newInstance (f.getCanonicalPath (), stitchProblemHandler);
                        Ohana.instance ().parseFile (stitch);
//                        StitchTypechecker behavior = (StitchTypechecker )stitch.getBehavior (Stitch.TYPECHECKER_PASS);

                        reportProblems (f, stitchProblemHandler);

                        // apply attribute vectors to tactics, if available
//                        defineAttributes (stitch, m_utilityModel.attributeVectors);
                        m_repertoire.add (stitch);
                        log ("Parsed script " + stitch.path);
                    } else {
                        log ("Previously known script " + stitch.path);
                    }
                } catch (IOException e) {
                    m_reportingPort.error (RainbowComponentT.ADAPTATION_MANAGER,
                                           "Obtaining file canonical path failed! " + f.getName (), e);
                }
            }
        }
    }

    private void reportProblems (File f, DummyStitchProblemHandler sph) {

        Collection<IStitchProblem> problem = sph.getProblems ();
        if (!problem.isEmpty ()) {
            log ("Errors exist in strategy: " + f.getName () + ", or one of its included files");
        }
        for (IStitchProblem p : problem) {
            StringBuilder out = new StringBuilder ();
            switch (p.getSeverity ()) {
                case StitchProblem.ERROR:
                    out.append ("ERROR: ");
                    break;
                case StitchProblem.WARNING:
                    out.append ("WARNING: ");
                    break;
                case StitchProblem.FATAL:
                    out.append ("FATAL ERROR: ");
                    break;
                case StitchProblem.UNKNOWN:
                    out.append ("UNKNOWN PROBLEM: ");
                    break;
            }
            out.append ("Line: ").append (p.getLine ());
            out.append (", ");
            out.append (" Column: ").append (p.getColumn ());
            out.append (": ").append (p.getMessage ());
            log (out.toString ());
        }
        sph.clearProblems ();
    }

    private void defineAttributes (Stitch stitch, Map<String, Map<String, Object>> attrVectorMap) {
        for (Tactic t : stitch.script.tactics) {
            Map<String, Object> attributes = attrVectorMap.get (t.getName ());
            if (attributes != null) {
                // found attribute def for tactic, save all key-value pairs
                m_reportingPort.trace (getComponentType (),
                                       "Found attributes for tactic " + t.getName () + ", saving pairs...");
                for (Map.Entry<String, Object> e : attributes.entrySet ()) {
                    t.putAttribute (e.getKey (), e.getValue ());
                    m_reportingPort.trace (getComponentType (), " - (" + e.getKey () + ", " + e.getValue () + ")");
                }
            }
        }
    }

    private static final int I_RUN     = 0;
    private static final int I_SUCCESS = 1;
    private static final int I_FAIL    = 2;
    private static final int I_OTHER   = 3;
    private static final int CNT_I     = 4;

    private void tallyStrategyOutcome (Strategy s) {
        if (m_historyTrackUtilName == null) return;

        String name = s.getName ();
        // mark timer of failure, if applicable
        Beacon timer = m_failTimer.get (name);
        if (timer == null) {
            timer = new Beacon ();
            m_failTimer.put (name, timer);
        }
        // get the stats array for this strategy
        int[] stat = m_historyCnt.get (name);
        if (stat == null) {
            stat = new int[CNT_I];
            stat[I_RUN] = 0;
            stat[I_SUCCESS] = 0;
            stat[I_FAIL] = 0;
            stat[I_OTHER] = 0;
            m_historyCnt.put (name, stat);
        }
        // tally outcome counts
        ++stat[I_RUN];
        switch (s.outcome ()) {
            case SUCCESS:
                ++stat[I_SUCCESS];
                break;
            case FAILURE:
                ++stat[I_FAIL];
                timer.mark ();
                break;
            default:
                ++stat[I_OTHER];
                break;
        }
        String str = name + Arrays.toString (stat);
        log ("History: " + str);
        Util.dataLogger ().info (IRainbowHealthProtocol.DATA_ADAPTATION_STAT + str);
    }


    @Override
    public void onEvent (ModelReference mr, IRainbowMessage message) {
        // Because of the subscription, the model should be the model ref so no need to check
        String typecheckSt = (String) message.getProperty (IModelChangeBusPort.PARAMETER_PROP + "0");
        Boolean typechecks = Boolean.valueOf (typecheckSt);
        // Cause the thread to wake up if it is sleeping
        if (!typechecks) {
            activeThread ().interrupt ();
        }
    }

    @Override
    public RainbowComponentT getComponentType () {
        return RainbowComponentT.ADAPTATION_MANAGER;
    }

    @Override
    public void setEnabled (boolean enabled) {
        m_reportingPort.info (getComponentType (),
                              MessageFormat.format ("Turning adaptation {0}.", (enabled ? "on" : "off")));
        if (!enabled && !m_pendingStrategies.isEmpty ()) {
            m_reportingPort.info (getComponentType (), "There is an adaptation in progress. This will finish.");
        }
        m_adaptEnabled = enabled;
    }

    private class StrategyAdaptationResultsVisitor extends DefaultAdaptationTreeWalker<Strategy> {
        private final List<Strategy> m_strategiesExecuted;

        public StrategyAdaptationResultsVisitor (AdaptationTree<Strategy> strategy,
                                                 List<Strategy> strategiesExecuted) {
            super (strategy);
            m_strategiesExecuted = strategiesExecuted;
        }

        @Override
        protected void evaluate (Strategy adaptation) {
            if (adaptation.outcome () != Strategy.Outcome.UNKNOWN) {
                synchronized (m_strategiesExecuted) {
                    m_strategiesExecuted.add (adaptation);
                }
            }
        }


    }
    

    /**
     * This wraps Rainbow.instance().getProperty() so that environment variables can be used
     * in property values.
     * 
     * For example: reachPath = ${PLADAPT}/reach/reach.sh
     * where PLADAPT is an environment variable
     * 
     * @param key property name
     * @return property value with environment variables replaced with their values
     */
    protected String getRainbowPropertyWithEnv(String key) {
    	String value = Rainbow.instance().getProperty(key);
        if (null == key) {
            return null;
         }

        Pattern p = Pattern.compile("\\$\\{(\\w+)\\}|\\$(\\w+)");
        Matcher m = p.matcher(value);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String envVarName = null == m.group(1) ? m.group(2) : m.group(1);
            String envVarValue = System.getenv(envVarName);
            if (null == envVarValue) {
            	error("Environment variable " + envVarName + " is not defined");
            }
            m.appendReplacement(sb,
                null == envVarValue ? "" : Matcher.quoteReplacement(envVarValue));
         }
        m.appendTail(sb);
        return sb.toString();
    }
}
