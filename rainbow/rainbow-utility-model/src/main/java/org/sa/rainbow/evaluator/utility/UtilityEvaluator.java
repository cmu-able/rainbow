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
package org.sa.rainbow.evaluator.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.UtilityFunction;
import org.sa.rainbow.core.models.UtilityPreferenceDescription;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
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
import org.sa.rainbow.model.utility.AddUtilityMeasureCmd;
import org.sa.rainbow.model.utility.UtilityHistory;
import org.sa.rainbow.model.utility.UtilityHistoryModelInstance;
import org.sa.rainbow.model.utility.UtilityModelInstance;

/**
 * This evaluator listens for changes in an Acme model and updates the utility history with new utility values
 * calculated from the new state.
 * 
 * @author Bradley Schmerl: schmerl
 *
 */
public class UtilityEvaluator extends AbstractRainbowRunnable implements IRainbowAnalysis, IRainbowModelChangeCallback {

    private static final String                 OVERALL_UTILITY_KEY     = "globalUtility";
    private static final String                 NAME                    = "Rainbow Utility Evaluator";

    /** Used to get the current state of a model, e.g., the Acme model and the utility model **/
    private IModelsManagerPort                  m_modelsManagerPort;
    /** Subscribes to changes to the Acme model on this port **/
    private IModelChangeBusSubscriberPort       m_modelChangePort;
    /** Changes to the Utility History model are announced on this port **/
    private IModelUSBusPort                     m_modelUpstreamPort;

    /**
     * Used to store the models that the evaluator is interested in that has changed, to be picked up in the execution
     * thread
     **/
    private LinkedBlockingQueue<ModelReference> m_modelQ                = new LinkedBlockingQueue<> ();

    /** Matches the end of changes to an Acme model **/
    private IRainbowChangeBusSubscription       m_modelChangeSubscriber = new IRainbowChangeBusSubscription () {

        @Override
        public boolean
        matches (IRainbowMessage message) {
            String type = (String )message
                    .getProperty (IModelChangeBusPort.EVENT_TYPE_PROP);
            if (type != null) {
                try {
                    CommandEventT ct = CommandEventT
                            .valueOf (type);
                    if (ct.isEnd ()) {
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

    public UtilityEvaluator () {
        super (NAME);
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
        m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort ();
        m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort ();
        m_modelUpstreamPort = RainbowPortFactory.createModelsManagerClientUSPort (this);
    }

    @Override
    public void dispose () {
        m_modelChangePort.dispose ();
        m_reportingPort.dispose ();
    }

    @Override
    protected void log (String txt) {
        m_reportingPort.info (RainbowComponentT.ANALYSIS, txt);
    }

    @Override
    protected void runAction () {
        ModelReference ref = null;
        synchronized (m_modelQ) {
            ref = m_modelQ.poll ();
        }
        if (ref != null) {
            Collection<? extends String> forTracing = m_modelsManagerPort.getRegisteredModelTypes ();
            UtilityModelInstance utilityModel = (UtilityModelInstance )m_modelsManagerPort
                    .<UtilityPreferenceDescription> getModelInstance (new ModelReference (ref.getModelName (), UtilityModelInstance.UTILITY_MODEL_TYPE));
            AcmeModelInstance acmeModel = (AcmeModelInstance )m_modelsManagerPort.<IAcmeSystem> getModelInstance (ref);
            if (utilityModel != null && acmeModel != null) {
                Map<String, Double> utilities = computeSystemInstantUtility (utilityModel.getModelInstance (),
                        acmeModel,
                        m_reportingPort);
                UtilityHistoryModelInstance historyModel = (UtilityHistoryModelInstance )m_modelsManagerPort
                        .<UtilityHistory> getModelInstance (new ModelReference (ref.getModelName (),
                                UtilityHistoryModelInstance.UTILITY_HISTORY_TYPE));
                List<IRainbowOperation> cmds = new ArrayList<> (utilities.size ());

                AddUtilityMeasureCmd command = historyModel.getCommandFactory ().addUtilityMeasureCmd (
                        OVERALL_UTILITY_KEY, utilities.get (OVERALL_UTILITY_KEY));
                cmds.add (command);
                for (Entry<String, Double> e : utilities.entrySet ()) {
                    if (OVERALL_UTILITY_KEY.equals (e.getKey ())) {
                        continue;
                    }
                    cmds.add (historyModel.getCommandFactory ().addUtilityMeasureCmd (e.getKey (), e.getValue ()));
                }
                m_modelUpstreamPort.updateModel (cmds, true);
            }
        }
    }

    private Map<String, Double> computeSystemInstantUtility (UtilityPreferenceDescription utilityModel,
            AcmeModelInstance acmeModel,
            IRainbowReportingPort reportingPort) {
        Map<String, Double> weights = utilityModel.weights
                .get (Rainbow.instance().getProperty (RainbowConstants.PROPKEY_SCENARIO));
        Map<String, Double> utilities = new HashMap<> ();
        double[] conds = new double[utilityModel.getUtilities ().size ()];
        int i = 0;
        double score = 0.0;
        for (Entry<String, UtilityFunction> e : utilityModel.getUtilityFunctions ().entrySet ()) {
            double v = 0.0;
            UtilityFunction u = e.getValue ();
            // add attribute value from current condition to accumulated agg value
            Object condVal = acmeModel.getProperty (u.mapping ());
            if (condVal != null) {
                double val = 0.0;
                if (condVal instanceof Double) {
                    val = ((Double )condVal).doubleValue ();
                }
                else if (condVal instanceof Float) {
                    val = ((Float )condVal).doubleValue ();
                }
                else if (condVal instanceof Integer) {
                    val = ((Integer )condVal).doubleValue ();
                }
                conds[i] = val;
                v += conds[i];
            }
            // now compute the utility, apply weight, and accumulate to sum
            if (weights.containsKey (e.getKey ())) {
                double utility = u.f (v);
                utilities.put (e.getKey (), utility);
                score += weights.get (e.getKey ()) * utility;
            }
        }
        utilities.put (OVERALL_UTILITY_KEY, score);
        return utilities;
    }

    @Override
    public RainbowComponentT getComponentType () {
        return RainbowComponentT.ANALYSIS;
    }

    @Override
    public void onEvent (ModelReference reference, IRainbowMessage message) {
        synchronized (m_modelQ) {
            if (!m_modelQ.contains (reference)) {
                m_modelQ.offer (reference);
            }
        }
    }

    @Override
    public void setProperty (String key, String value) {
    }

    @Override
    public String getProperty (String key) {
        return null;
    }

}
