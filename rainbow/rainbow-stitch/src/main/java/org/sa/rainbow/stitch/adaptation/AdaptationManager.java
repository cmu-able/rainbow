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
package org.sa.rainbow.stitch.adaptation;

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
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.acmestudio.acme.element.IAcmeSystem;
import org.apache.commons.lang.time.StopWatch;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.DefaultAdaptationExecutorVisitor;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.error.RainbowConnectionException;
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
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Tactic;
import org.sa.rainbow.stitch.error.DummyStitchProblemHandler;
import org.sa.rainbow.stitch.error.IStitchProblem;
import org.sa.rainbow.stitch.visitor.Stitch;
import org.sa.rainbow.stitch.visitor.StitchTypechecker;
import org.sa.rainbow.util.Beacon;
import org.sa.rainbow.util.Util;

/**
 * The Rainbow Adaptation Engine... Currently implements a learner interface to
 * interact with Nick Lynn's learner.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public final class AdaptationManager extends AbstractRainbowRunnable
		implements IAdaptationManager<Strategy>/*
												 * implements IRainbowLearner
												 */, IRainbowModelChangeCallback {

	public enum Mode {
		SERIAL, MULTI_PRONE
	}

	public static final String NAME = "Rainbow Adaptation Manager";
	public static final double FAILURE_RATE_THRESHOLD = 0.95;
	public static final double MIN_UTILITY_THRESHOLD = 0.40;
	private static double m_minUtilityThreshold = 0.0;
	public static final long FAILURE_EFFECTIVE_WINDOW = 2000 /* ms */;
	public static final long FAILURE_WINDOW_CHUNK = 1000 /* ms */;
	private static final int SLEEP_TIME = 10000;
	/**
	 * The prefix to be used by the strategy which takes a "leap" by achieving a
	 * similar adaptation that would have taken multiple increments of the non-leap
	 * version, but at a potential "cost" in non-dire scenarios.
	 */
	public static final String LEAP_STRATEGY_PREFIX = "Leap-";
	/**
	 * The prefix to represent the corresponding multi-step strategy of the
	 * leap-version strategy.
	 */
	public static final String MULTI_STRATEGY_PREFIX = "Multi-";

	private Mode m_mode = Mode.SERIAL;
	private AcmeModelInstance m_model = null;
	private boolean m_adaptNeeded = false; // treat as synonymous with
	// constraint being violated
	private boolean m_adaptEnabled = true; // by default, we adapt
	private List<Stitch> m_repertoire = null;
	private List<AdaptationTree<Strategy>> m_pendingStrategies = null;

	// track history
	private String m_historyTrackUtilName = null;
	private Map<String, int[]> m_historyCnt = null;
	private Map<String, Beacon> m_failTimer = null;
	private IRainbowAdaptationEnqueuePort<Strategy> m_enqueuePort = null;
	private IModelChangeBusSubscriberPort m_modelChangePort = null;
	private IModelsManagerPort m_modelsManagerPort = null;
	private String m_modelRef;
	private FileChannel m_strategyLog = null;
	private IRainbowChangeBusSubscription m_modelTypecheckingChanged = new IRainbowChangeBusSubscription() {

		@Override
		public boolean matches(IRainbowMessage message) {
			String type = (String) message.getProperty(IModelChangeBusPort.EVENT_TYPE_PROP);
			String modelName = (String) message.getProperty(IModelChangeBusPort.MODEL_NAME_PROP);
			String modelType = (String) message.getProperty(IModelChangeBusPort.MODEL_TYPE_PROP);
			try {
				CommandEventT ct = CommandEventT.valueOf(type);
				return (ct.isEnd() && "setTypecheckResult".equals(message.getProperty(IModelChangeBusPort.COMMAND_PROP))
						&& m_modelRef.equals(Util.genModelRef(modelName, modelType)));
			} catch (Exception e) {
				return false;
			}
		}
	};
	private UtilityPreferenceDescription m_utilityModel;

	/**
	 * Default constructor.
	 */
	public AdaptationManager() {
		super(NAME);

		m_repertoire = new ArrayList<Stitch>();
		m_pendingStrategies = new ArrayList<AdaptationTree<Strategy>>();
		m_historyTrackUtilName = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_TRACK_STRATEGY);
		if (m_historyTrackUtilName != null) {
			m_historyCnt = new HashMap<String, int[]>();
			m_failTimer = new HashMap<String, Beacon>();
		}
		String thresholdStr = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_UTILITY_MINSCORE_THRESHOLD);
		if (thresholdStr == null) {
			m_minUtilityThreshold = MIN_UTILITY_THRESHOLD;
		} else {
			m_minUtilityThreshold = Double.valueOf(thresholdStr);
		}
		setSleepTime(SLEEP_TIME);

	}

	@Override
	public void initialize(IRainbowReportingPort port) throws RainbowConnectionException {
		super.initialize(port);
		initConnectors();

	}

	private void initConnectors() throws RainbowConnectionException {
		m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort();
		m_modelChangePort.subscribe(m_modelTypecheckingChanged, this);
		m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort();
	}

	@Override
	public void setModelToManage(ModelReference model) {
		m_modelRef = model.getModelName() + ":" + model.getModelType();
		try {
			m_strategyLog = new FileOutputStream(new File(new File(Rainbow.instance().getTargetPath(), "log"),
					model.getModelName() + "-adaptation.log")).getChannel();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_model = (AcmeModelInstance) m_modelsManagerPort.<IAcmeSystem>getModelInstance(model);
		if (m_model == null) {
			m_reportingPort.error(RainbowComponentT.ADAPTATION_MANAGER,
					MessageFormat.format("Could not find reference to {0}", model.toString()));
		}
		m_enqueuePort = RainbowPortFactory.createAdaptationEnqueuePort(model);
		ModelReference utilityModelRef = new ModelReference(model.getModelName(), "UtilityModel");
		IModelInstance<UtilityPreferenceDescription> modelInstance = m_modelsManagerPort
				.getModelInstance(utilityModelRef);
		if (modelInstance == null) {
			m_reportingPort.error(RainbowComponentT.ADAPTATION_MANAGER,
					MessageFormat.format(
							"There is no utility model associated with this model. Expecting to find "
									+ "''{0}''. Perhaps it is not specified in the rainbow.properties " + "file?",
							utilityModelRef.toString()));

		} else {
			m_utilityModel = modelInstance.getModelInstance();
		}
//        for (String k : m_utilityModel.utilities.keySet ()) {
//            UtilityAttributes ua = m_utilityModel.utilities.get (k);
//            UtilityFunction uf = new UtilityFunction (k, ua.label, ua.mapping, ua.desc, ua.values);
//            m_utils.put (k, uf);
//        }
		initAdaptationRepertoire();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.core.IDisposable#dispose()
	 */
	@Override
	public void dispose() {
		for (Stitch stitch : m_repertoire) {
			stitch.dispose();
		}
		Ohana.instance().dispose();
		m_repertoire.clear();
		m_pendingStrategies.clear();
		if (m_historyTrackUtilName != null) {
			m_historyCnt.clear();
			m_failTimer.clear();
			m_historyCnt = null;
			m_failTimer = null;
		}

		if (m_enqueuePort != null) {
			m_enqueuePort.dispose();
		}
		m_modelChangePort.dispose();

		// null-out data members
		m_repertoire = null;
		m_pendingStrategies = null;
		m_historyTrackUtilName = null;
		m_model = null;
		if (m_strategyLog != null) {
			try {
				m_strategyLog.close();
			} catch (IOException ignored) {
			}
		}
	}

	@Override
	protected void doTerminate() {
		if (m_strategyLog != null) {
			try {
				m_strategyLog.close();
			} catch (IOException ignore) {
			}
			m_strategyLog = null;
		}
		super.doTerminate();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.sa.rainbow.core.AbstractRainbowRunnable#log(java.lang.String)
	 */
	@Override
	protected void log(String txt) {
		m_reportingPort.info(RainbowComponentT.ADAPTATION_MANAGER, txt);
	}

	@Override
	public boolean isEnabled() {
		return m_adaptEnabled;
	}

	public void setAdaptationEnabled(boolean b) {
		m_adaptEnabled = b;
	}

	public boolean adaptationInProgress() {
		return m_adaptNeeded;
	}

	/**
	 * Removes a Strategy from the list of pending strategies, marking it as being
	 * completed (doesn't incorporate outcome).
	 *
	 * @param strategy the strategy to mark as being executed.
	 */
	@Override
	public void markStrategyExecuted(AdaptationTree<Strategy> strategy) {
		if (m_pendingStrategies.contains(strategy)) {
			m_pendingStrategies.remove(strategy);
			final List<Strategy> strategiesExecuted = new LinkedList<>();
			final CountDownLatch countdownLatch = new CountDownLatch(1);
			DefaultAdaptationExecutorVisitor<Strategy> resultCollector = new StrategyAdaptationResultsVisitor(strategy,
					countdownLatch, strategiesExecuted);
			resultCollector.start();
			try {
				countdownLatch.await(2, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
			}

			for (Strategy str : strategiesExecuted) {
				String s = str.getName() + ";" + str.outcome();
				log("*S* outcome: " + s);
				Util.dataLogger().info(IRainbowHealthProtocol.DATA_ADAPTATION_STRATEGY + s);
				tallyStrategyOutcome(str);
			}

		}
		if (m_pendingStrategies.size() == 0) {
			Util.dataLogger().info(IRainbowHealthProtocol.DATA_ADAPTATION_END);
			// reset adaptation flags
			m_adaptNeeded = false;
//            m_model.clearConstraintViolated ();
		}
	}

	/**
	 * Computes instantaneous utility of target system given current conditions.
	 *
	 * @return double the instantaneous utility of current conditions
	 */
	public double computeSystemInstantUtility() {
		Map<String, Double> weights = m_utilityModel.weights
				.get(Rainbow.instance().getProperty(RainbowConstants.PROPKEY_SCENARIO));
		double[] conds = new double[m_utilityModel.getUtilityFunctions().size()];
		int i = 0;
		double score = 0.0;
		for (String k : new ArrayList<String>(m_utilityModel.getUtilityFunctions().keySet())) {
			double v = 0.0;
			// find the applicable utility function
			UtilityFunction u = m_utilityModel.getUtilityFunctions().get(k);
			// add attribute value from current condition to accumulated agg
			// value
			Object condVal = m_model.getProperty(u.mapping());
			if (condVal != null) {
				double val = 0.0;
				if (condVal instanceof Double) {
					val = (Double) condVal;
				} else if (condVal instanceof Float) {
					val = ((Float) condVal).doubleValue();
				} else if (condVal instanceof Integer) {
					val = ((Integer) condVal).doubleValue();
				}
				m_reportingPort.trace(getComponentType(), "Avg value of prop: " + u.mapping() + " == " + condVal);
				conds[i] = val;
				v += conds[i];
			}
			// now compute the utility, apply weight, and accumulate to sum
			if (weights.containsKey(k)) { // but only if weight is defined
				score += weights.get(k) * u.f(v);
			}
		}
		return score;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sa.rainbow.core.AbstractRainbowRunnable#runAction()
	 */
	@Override
	protected void runAction() {
		if (m_adaptEnabled) {
//            RainbowModelTypecheckExtension ext = (RainbowModelTypecheckExtension )m_model.getModelInstance ()
//                    .getUserData ("TYPECHECKS");
//            if (ext == null || ext.typechecks ()) return;
			if (m_mode == Mode.SERIAL && m_pendingStrategies.size() > 0)
				// Only go if there are no pending strategies
				return;
			if (Rainbow.instance().getProperty(RainbowConstants.PROPKEY_ADAPTATION_HOMEOSTATIC, false)
					|| m_modelError) {

				Util.dataLogger().info(IRainbowHealthProtocol.DATA_ADAPTATION_SELECTION_BEGIN);
				Strategy selectedStrategy = checkAdaptation();
				Util.dataLogger().info(IRainbowHealthProtocol.DATA_ADAPTATION_SELECTION_END);
				if (selectedStrategy != null) {
					log(">> do strategy: " + selectedStrategy.getName());
					// strategy args removed...
					Object[] args = new Object[0];
					AdaptationTree<Strategy> at = new AdaptationTree<Strategy>(selectedStrategy);
					m_pendingStrategies.add(at);
					m_enqueuePort.offerAdaptation(at, null);
					String logMessage = selectedStrategy.getName();
					strategyLog(logMessage);
				}
			}

		}

//        if (m_adaptEnabled && m_adaptNeeded) {
//            if ((m_mode == Mode.SERIAL && m_pendingStrategies.size () == 0) || m_mode == Mode.MULTI_PRONE) {
//                // in serial mode, only do adaptation if no strategy is pending
//                // in multi-prone mode, just do adaptation
//                Util.dataLogger ().info (IRainbowHealthProtocol.DATA_ADAPTATION_BEGIN);
//                doAdaptation ();
//            }
//        }
	}

	private void strategyLog(String logMessage) {
		if (m_strategyLog != null) {
			Date d = new Date();
			String log = MessageFormat.format("{0,number,#},queuing,{1}\n", d.getTime(), logMessage);
			try {
				m_strategyLog.write(java.nio.ByteBuffer.wrap(log.getBytes()));
			} catch (IOException e) {
				reportingPort().error(getComponentType(), "Failed to write " + log + " to log file");
			}
		}
	}

	/**
	 * For JUnit testing, used to set a stopwatch object used to time duration.
	 */
	StopWatch _stopWatchForTesting = null;
	private boolean m_modelError = false;

	/**
	 * For JUnit testing, allows fetching the strategy repertoire. NOT for public
	 * use!
	 *
	 * @return list of Stitch objects loaded at initialization from stitchState
	 *         file.
	 */
	List<Stitch> _retrieveRepertoireForTesting() {
		return m_repertoire;
	}

	/**
	 * For JUnit testing, allows fetching the utility objects. NOT for public use!
	 *
	 * @return map of utility identifiers to functions.
	 */
	Map<String, UtilityFunction> _retrieveUtilityProfilesForTesting() {
		return m_utilityModel.getUtilityFunctions();
	}

	/**
	 * For JUnit testing, allows re-invoking defineAttributes to artificially
	 * increase the number of quality dimensions in tactic attribute vectors.
	 */
	void _defineAttributesFromTester(Stitch stitch, Map<String, Map<String, Object>> attrVectorMap) {
		defineAttributes(stitch, attrVectorMap);
	}

	/*
	 * Algorithm: - Iterate through repertoire searching for enabled strategies,
	 * where "enabled" means applicable to current system condition NOTE: A Strategy
	 * is "applicable" iff the conditions of applicability of the root tactic is
	 * true TODO: Need to check if above is good assumption - Calculate scores of
	 * the enabled strategies = this involves evaluating the meta-information of the
	 * tactics in each strategy - Select and execute the highest scoring strategy
	 */
	private Strategy checkAdaptation() {
		log("Checking if adaptation is required.");
		if (_stopWatchForTesting != null) {
			_stopWatchForTesting.start();
		}

		int availCnt = 0;
		Map<String, Strategy> appSubsetByName = new HashMap<String, Strategy>();
		for (Stitch stitch : m_repertoire) {
			if (!stitch.script.isApplicableForSystem(m_model)) {
				m_reportingPort.trace(getComponentType(), "x. skipping " + stitch.script.getName());
				continue; // skip checking this script
			}
			for (Strategy strategy : stitch.script.strategies) {
				++availCnt;
				// check first for prior failures
				if (getFailureRate(strategy) > FAILURE_RATE_THRESHOLD) {
					continue; // don't consider this Strategy
				}
				// get estimated time cost for predicted property
				long dur = 0L;
//                if (Rainbow.predictionEnabled ()) { // provide future duration
//                    dur = strategy.estimateAvgTimeCost ();
//                }
				Map<String, Object> moreVars = new HashMap<String, Object>();
				moreVars.put("_dur_", dur);
				// check condition of Strategy applicability
				strategy.stitchState().stitchProblemHandler.clearProblems();
				if (strategy.isApplicable(moreVars)) {
					appSubsetByName.put(strategy.getName(), strategy);
				}
				Collection<IStitchProblem> problems = strategy.stitchState().stitchProblemHandler.unreportedProblems();
				if (!problems.isEmpty()) {
					for (IStitchProblem p : problems) {
						log(p.getMessage());
					}
				}
			}
		}
		if (appSubsetByName.size() == 0) { // can't do adaptation
			log("No applicable Strategies to do an adaptation!");
			m_adaptNeeded = false;
//            m_model.clearConstraintViolated ();
			return null;
		}

		// check for leap-version strategy to see whether to "chain" util
		// computation
		Set<String> applicableSubsetNames = appSubsetByName.keySet();
		for (String name : applicableSubsetNames.toArray(new String[applicableSubsetNames.size()])) {
			Strategy strategy = appSubsetByName.get(name);
			Strategy leap = appSubsetByName.get(LEAP_STRATEGY_PREFIX + name);
			if (leap != null) { // Leap-version exists
				/*
				 * To chain: Determine the integer multiple N of Leap over this, then compute
				 * aggregate attributes using previous attributes as the starting point,
				 * repeating N-1 times.
				 */
				// HACK: use the first argument of the tactic closest to root
				int factor = 1;
				double stratArgVal = strategy.getFirstTacticArgumentValue();
				double leapArgVal = leap.getFirstTacticArgumentValue();
				if (stratArgVal != Double.NaN) {
					// compute multiple now
					factor = (int) (leapArgVal / stratArgVal);
				}
				Strategy multi = strategy.clone(strategy.parent());
				multi.setName(MULTI_STRATEGY_PREFIX + strategy.getName());
				multi.multiples = factor;
				appSubsetByName.put(multi.getName(), multi);
				++availCnt;
			}
		}
		log(">> repertoire: " + appSubsetByName.size() + " / " + availCnt + " strategy" + (availCnt > 1 ? "ies" : "y"));
		SortedMap<Double, Strategy> scoredStrategies = scoreStrategies(appSubsetByName);
		if (Util.dataLogger().isInfoEnabled()) {
			StringBuffer buf = new StringBuffer();
			buf.append("  [\n");
			for (Map.Entry<Double, Strategy> entry : scoredStrategies.entrySet()) {
				buf.append("   ").append(entry.getValue().getName()).append(":");
				buf.append(entry.getKey()).append("\n");
			}
			buf.append("  ]\n");
			log(buf.toString());
			Util.dataLogger().info(IRainbowHealthProtocol.DATA_ADAPTATION_SCORE + buf.toString());
		}

		if (_stopWatchForTesting != null) {
			_stopWatchForTesting.stop();
		}
		if (scoredStrategies.size() > 0) {
			Strategy selectedStrategy = scoredStrategies.get(scoredStrategies.lastKey());
			return selectedStrategy;
		} else {
			Util.dataLogger().info(IRainbowHealthProtocol.DATA_ADAPTATION_END);
			log("<< NO applicable strategy, adaptation cycle ended.");
			return null;
//            m_model.clearConstraintViolated ();
		}
	}

	/**
	 * Iterate through the supplied set of strategies, compute aggregate attributes,
	 * and use the aggregate values plus stakeholder utility preferences to compute
	 * an integer score for each Strategy, between 0 and 100.
	 *
	 * @param subset the subset of condition-applicable Strategies to score, in the
	 *               form of a name-strategy map
	 * @return a map of score-strategy pairs, sorted in increasing order by score.
	 */
	private SortedMap<Double, Strategy> scoreStrategies(Map<String, Strategy> subset) {
		String scenario = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_SCENARIO);
//        Set<String> scenarios = Rainbow.instance ().getRainbowMaster ().preferenceDesc ().weights.keySet ();
//        for (String s : scenarios) {
//            if (scenarios.equals (s)) {
//                continue;
//            }
//            log ("--------- hypothetical scoring for " + s);
//            scoreForScenario (s, subset);
//            log ("--------- done hypothetical");
//        }
		return scoreForScenario(scenario, subset);
	}

	SortedMap<Double, Strategy> scoreForScenario(String scenario, Map<String, Strategy> subset) {
		Map<String, Double> weights = m_utilityModel.weights.get(scenario);
		SortedMap<Double, Strategy> scored = new TreeMap<Double, Strategy>();
		boolean predictionEnabled = false; // Rainbow.predictionEnabled () && Rainbow.utilityPredictionDuration () > 0;
		double[] conds = null; // store the conditions to output for diagnosis
		double[] condsPred = null; // store predicted conditions
		// find the weights of the applicable scenario
		log("Scoring for " + scenario);
		for (Strategy strategy : subset.values()) {
			SortedMap<String, Double> aggAtt = strategy.computeAggregateAttributes();
			// add the strategy failure history as another attribute
			accountForStrategyHistory(aggAtt, strategy);
			String s = strategy.getName() + aggAtt;
			Util.dataLogger().info(IRainbowHealthProtocol.DATA_ADAPTATION_STRATEGY_ATTR + s);
			log("aggAttr: " + s);
			/*
			 * compute utility values from attributes that combines values representing
			 * current condition, then accumulate the weighted utility sum
			 */
			double[] items = new double[aggAtt.size()];
			double[] itemsPred = new double[aggAtt.size()];
			double[] utilityOfItem = new double[aggAtt.size()];
			double[] currentUtility = new double[aggAtt.size()];
			if (conds == null) {
				conds = new double[aggAtt.size()];
			}
			if (condsPred == null) {
				condsPred = new double[aggAtt.size()];
			}
			int i = 0;
			double score = 0.0;
			double scorePred = 0.0; // score based on predictions
			for (String k : aggAtt.keySet()) {
				double v = aggAtt.get(k);
				// find the applicable utility function
				UtilityFunction u = m_utilityModel.getUtilityFunctions().get(k);
				if (u == null) {
					log("Error: attempting to calculate for not existent function: " + k);
					continue;
				}
				Object condVal = null;
				Object condValPred = null;
				// add attribute value from CURRENT condition to accumulated agg
				// value
				condVal = m_model.getProperty(u.mapping());
				items[i] = v;
				if (condVal != null) {
					double val = 0.0;
					if (condVal instanceof Double) {
						val = (Double) condVal;
					} else if (condVal instanceof Float) {
						val = ((Float) condVal).doubleValue();
					} else if (condVal instanceof Integer) {
						val = ((Integer) condVal).doubleValue();
					}

					m_reportingPort.trace(getComponentType(), "Avg value of prop: " + u.mapping() + " == " + condVal);
					conds[i] = val;
					items[i] += conds[i];
				}
				// now compute the utility, apply weight, and accumulate to sum
				utilityOfItem[i] = u.f(items[i]);
				currentUtility[i] = u.f(conds[i]);
				score += weights.get(k) * utilityOfItem[i];

				// if applicable, process the same set of info using predicted
				// values
				if (predictionEnabled) {
					// add attribute value from FUTURE condition to accumulated
					// agg value
					condValPred = m_model.predictProperty(u.mapping(), 0L/* Rainbow.utilityPredictionDuration () */);
					itemsPred[i] = v;
					if (condValPred != null && condValPred instanceof Double) {
						// if (m_logger.isTraceEnabled())
						log("Avg value of predicted prop: " + u.mapping() + " == " + condValPred);
						condsPred[i] = (Double) condValPred;
						itemsPred[i] += condsPred[i];
					}
					// now compute the utility, apply weight, and accumulate to
					// sum
					scorePred += weights.get(k) * u.f(itemsPred[i]);
				}
				++i;
			}

			if (predictionEnabled) {
				// compare and pick higher score
				if (scorePred > .9 * score) { // score based on prediction
					// prevails
					log("cur-cond score " + score + " was lower, discarding: " + Arrays.toString(items));
					score = scorePred;
					items = itemsPred;
				}
			}

			// log this
			s = Arrays.toString(items);
			if (score < m_minUtilityThreshold) {
				// utility score too low, don't consider for adaptation
				log("score " + score + " below threshold, discarding: " + s);
			} else {
				scored.put(score, strategy);
			}
			log("current model properties: " + Arrays.toString(conds));
			log("current model utilities: " + Arrays.toString(currentUtility));
			log(strategy.getName() + ": predicted utilities: " + Arrays.toString(utilityOfItem));
			log(strategy.getName() + ": score = " + score);
			Util.dataLogger().info(IRainbowHealthProtocol.DATA_ADAPTATION_STRATEGY_ATTR2 + s);
			log("aggAtt': " + s);
		}
		log("cond   : " + Arrays.toString(conds));
		if (predictionEnabled) {
			log("condP! : " + Arrays.toString(condsPred));
		}
		return scored;
	}

	/**
	 * Retrieves the adaptation repertoire; for each tactic, store the respective
	 * tactic attribute vectors.
	 */
	private void initAdaptationRepertoire() {
		File stitchPath = Util.getRelativeToPath(Rainbow.instance().getTargetPath(),
				Rainbow.instance().getProperty(RainbowConstants.PROPKEY_SCRIPT_PATH));
		if (stitchPath == null) {
			m_reportingPort.error(RainbowComponentT.ADAPTATION_MANAGER, "The stitchState path is not set!");
		} else if (stitchPath.exists() && stitchPath.isDirectory()) {
			FilenameFilter ff = new FilenameFilter() { // find only ".s" files
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".s");
				}
			};
			for (File f : stitchPath.listFiles(ff)) {
				try {
					// don't duplicate loading of script files
					Stitch stitch = Ohana.instance().findStitch(f.getCanonicalPath());
					if (stitch == null) {
						DummyStitchProblemHandler stitchProblemHandler = new DummyStitchProblemHandler();
						stitch = Stitch.newInstance(f.getCanonicalPath(), stitchProblemHandler);
						Ohana.instance().parseFile(stitch);
//						StitchTypechecker behavior = (StitchTypechecker) stitch
//								.getBehavior(Stitch .TYPECHECKER_PASS);
//						stitch.script.
//						reportProblems(f, stitchProblemHandler);

						// apply attribute vectors to tactics, if available
						defineAttributes(stitch, m_utilityModel.attributeVectors);
						m_repertoire.add(stitch);
						log("Parsed script " + stitch.path);
					} else {
						log("Previously known script " + stitch.path);
					}
				} catch (IOException e) {
					m_reportingPort.error(RainbowComponentT.ADAPTATION_MANAGER,
							"Obtaining file canonical path failed! " + f.getName(), e);
				}
			}
		}
	}

	private void reportProblems(File f, DummyStitchProblemHandler sph) {

		Collection<IStitchProblem> problem = sph.getProblems();
		boolean reported = !problem.isEmpty();
		if (!problem.isEmpty()) {
			log("Errors exist in strategy: " + f.getName() + ", or one of its included files");
		}
		for (IStitchProblem p : problem) {
			StringBuilder out = new StringBuilder();
			switch (p.getSeverity()) {
			case IStitchProblem.ERROR:
				out.append("ERROR: ");
				break;
			case IStitchProblem.WARNING:
				out.append("WARNING: ");
				break;
			case IStitchProblem.FATAL:
				out.append("FATAL ERROR: ");
				break;
			case IStitchProblem.UNKNOWN:
				out.append("UNKNOWN PROBLEM: ");
				break;
			}
			out.append("Line: ").append(p.getLine());
			out.append(", ");
			out.append(" Column: ").append(p.getColumn());
			out.append(": ").append(p.getMessage());
			log(out.toString());
		}
		sph.clearProblems();
	}

	private void defineAttributes(Stitch stitch, Map<String, Map<String, Object>> attrVectorMap) {
		for (Tactic t : stitch.script.tactics) {
			Map<String, Object> attributes = attrVectorMap.get(t.getName());
			if (attributes != null) {
				// found attribute def for tactic, save all key-value pairs
				m_reportingPort.trace(getComponentType(),
						"Found attributes for tactic " + t.getName() + ", saving pairs...");
				for (Map.Entry<String, Object> e : attributes.entrySet()) {
					t.putAttribute(e.getKey(), e.getValue());
					m_reportingPort.trace(getComponentType(), " - (" + e.getKey() + ", " + e.getValue() + ")");
				}
			}
		}
	}

	private static final int I_RUN = 0;
	private static final int I_SUCCESS = 1;
	private static final int I_FAIL = 2;
	private static final int I_OTHER = 3;
	private static final int CNT_I = 4;

	private void tallyStrategyOutcome(Strategy s) {
		if (m_historyTrackUtilName == null)
			return;

		String name = s.getName();
		// mark timer of failure, if applicable
		Beacon timer = m_failTimer.get(name);
		if (timer == null) {
			timer = new Beacon();
			m_failTimer.put(name, timer);
		}
		// get the stats array for this strategy
		int[] stat = m_historyCnt.get(name);
		if (stat == null) {
			stat = new int[CNT_I];
			stat[I_RUN] = 0;
			stat[I_SUCCESS] = 0;
			stat[I_FAIL] = 0;
			stat[I_OTHER] = 0;
			m_historyCnt.put(name, stat);
		}
		// tally outcome counts
		++stat[I_RUN];
		switch (s.outcome()) {
		case SUCCESS:
			++stat[I_SUCCESS];
			break;
		case FAILURE:
			++stat[I_FAIL];
			timer.mark();
			break;
		default:
			++stat[I_OTHER];
			break;
		}
		String str = name + Arrays.toString(stat);
		log("History: " + str);
		Util.dataLogger().info(IRainbowHealthProtocol.DATA_ADAPTATION_STAT + str);
	}

	private void accountForStrategyHistory(Map<String, Double> aggAtt, Strategy s) {
		if (m_historyTrackUtilName == null)
			return;

		if (m_historyCnt.containsKey(s.getName())) {
			// consider failure only
			aggAtt.put(m_historyTrackUtilName, getFailureRate(s));
		} else {
			// consider no failure
			aggAtt.put(m_historyTrackUtilName, 0.0);
		}
	}

	private double getFailureRate(Strategy s) {
		double rv = 0.0;
		if (m_historyTrackUtilName == null)
			return rv;

		int[] stat = m_historyCnt.get(s.getName());
		if (stat != null) {
			Beacon timer = m_failTimer.get(s.getName());
			double factor = 1.0;
			long failTimeDelta = timer.elapsedTime() - FAILURE_EFFECTIVE_WINDOW;
			if (failTimeDelta > 0) {
				factor = FAILURE_WINDOW_CHUNK * 1.0 / failTimeDelta;
			}
			rv = factor * stat[I_FAIL] / stat[I_RUN];
		}
		return rv;
	}

	@Override
	public void onEvent(ModelReference mr, IRainbowMessage message) {
		// Because of the subscription, the model should be the model ref so no need to
		// check
		String typecheckSt = (String) message.getProperty(IModelChangeBusPort.PARAMETER_PROP + "0");
		Boolean typechecks = Boolean.valueOf(typecheckSt);
		// Cause the thread to wake up if it is sleeping
		if (!typechecks) {
			m_modelError = !typechecks;
			activeThread().interrupt();
		}
	}

	@Override
	public RainbowComponentT getComponentType() {
		return RainbowComponentT.ADAPTATION_MANAGER;
	}

	@Override
	public void setEnabled(boolean enabled) {
		m_reportingPort.info(getComponentType(),
				MessageFormat.format("Turning adaptation {0}.", (enabled ? "on" : "off")));
		if (!enabled && !m_pendingStrategies.isEmpty()) {
			m_reportingPort.info(getComponentType(), "There is an adaptation in progress. This will finish.");
		}
		m_adaptEnabled = enabled;
	}

	private class StrategyAdaptationResultsVisitor extends DefaultAdaptationExecutorVisitor<Strategy> {
		private final List<Strategy> m_strategiesExecuted;

		public StrategyAdaptationResultsVisitor(AdaptationTree<Strategy> strategy, CountDownLatch countdownLatch,
				List<Strategy> strategiesExecuted) {
			super(strategy, AdaptationManager.this.activeThread().getThreadGroup(), "", countdownLatch,
					AdaptationManager.this.m_reportingPort);
			m_strategiesExecuted = strategiesExecuted;
		}

		@Override
		protected boolean evaluate(Strategy adaptation) {
			if (adaptation.outcome() != Strategy.Outcome.UNKNOWN) {
				synchronized (m_strategiesExecuted) {
					m_strategiesExecuted.add(adaptation);
				}
			}
			return true;
		}

		@Override
		protected DefaultAdaptationExecutorVisitor<Strategy> spawnNewExecutorForTree(AdaptationTree<Strategy> adt,
				ThreadGroup g, CountDownLatch doneSignal) {
			return new StrategyAdaptationResultsVisitor(adt, doneSignal, m_strategiesExecuted);
		}
	}
}
