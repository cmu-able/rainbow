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
package org.sa.rainbow.stitch.tactic.history;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.stitch.util.ExecutionHistoryData;
import org.sa.rainbow.stitch.util.ExecutionHistoryData.ExecutionPoint;

public class SaveTacticExecutionHistoryMonitor implements IRainbowAnalysis,
IRainbowModelChangeCallback {

    public static final String            NAME                       = "Tactic History Model Saver";

    private IRainbowChangeBusSubscription m_modelChangeBusSubscriber = new IRainbowChangeBusSubscription () {

        @Override
        public boolean
        matches (IRainbowMessage message) {
            String type = (String )message
                    .getProperty (IModelChangeBusPort.EVENT_TYPE_PROP);
            return ("TacticHistoryOperation"
                    .equals (type)
                    && m_modelDesc
                    .getType ()
                    .equals (
                            message.getProperty (IModelChangeBusPort.MODEL_TYPE_PROP)) && m_modelDesc
                    .getName ()
                    .equals (
                            message.getProperty (IModelChangeBusPort.MODEL_NAME_PROP)));
        }
    };

    IModelChangeBusSubscriberPort         m_modelChangePort;

    private IRainbowReportingPort         m_reportingPort;

    private State                         m_state                    = State.RAW;
    private State                         m_nextState                = State.RAW;

    private TypedAttribute                m_modelDesc;

    private String                        m_filename;

    private int                           m_updateCnt                = 0;

    private IModelsManagerPort            m_modelsManagerPort;

    public SaveTacticExecutionHistoryMonitor (TypedAttribute modelDesc, String filename) {
        m_modelDesc = modelDesc;
        m_filename = filename;
    }

    @Override
    public void dispose () {
        if (m_modelChangePort != null) {
            m_modelChangePort.unsubscribe (this);
        }
        m_modelChangePort = null;
    }

    @Override
    public String id () {
        return NAME;
    }

    @Override
    public void start () {
        if (m_state == State.TERMINATED) return;
        if (m_modelChangePort != null) {
            m_modelChangePort.subscribe (m_modelChangeBusSubscriber, this);
        }
        switch (m_state) {
        case RAW: // note yet started
            m_state = State.STARTED;
            break;
        case STOPPED:
            m_state = State.STARTED;
            break;
        }
    }

    @Override
    public void stop () {
        transition (State.STOPPED);
        if (m_modelChangePort != null) {
            m_modelChangePort.unsubscribe (this);
        }
    }

    @Override
    public void restart () {
        if (m_modelChangePort != null) {
            m_modelChangePort.subscribe (m_modelChangeBusSubscriber, this);
        }
    }

    @Override
    public void terminate () {
        transition (State.TERMINATED);
        dispose ();
    }

    @Override
    public State state () {
        return m_state;
    }

    @Override
    public boolean isTerminated () {
        return m_state == State.TERMINATED;
    }

    @Override
    public boolean isDisposed () {
        return isTerminated ();
    }

    @Override
    public void run () {

    }

    private void transition (State nextState) {
        // rule out prohibited transitions, basically TERMINATED->* and *->RAW
        if (m_state == State.TERMINATED || nextState == State.RAW) return;
        if (m_state != State.STOPPED) return;

        m_nextState = nextState;
    }

    @Override
    public void initialize (IRainbowReportingPort port) throws RainbowConnectionException {
        m_reportingPort = port;
        m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort ();
        m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort ();
    }

    @Override
    public void onEvent (ModelReference mr, IRainbowMessage message) {
        if (++m_updateCnt % 10 == 0) {
            IModelInstance<Map<String, ExecutionHistoryData>> model = m_modelsManagerPort.getModelInstance (mr);
            saveExecutionHistoryToFile (model);
        }
    }

    private void saveExecutionHistoryToFile (IModelInstance<Map<String, ExecutionHistoryData>> model) {
        try {
            File file = new File (m_filename);
            BufferedWriter bw = new BufferedWriter (new FileWriter (file));
            for (ExecutionHistoryData ed : model.getModelInstance ().values ()) {
                bw.write (ed.toString ());
                bw.newLine ();
            }
            bw.flush ();
            bw.close ();

            File history = new File (file.getParentFile (), file.getName () + "-states.csv");
            bw = new BufferedWriter (new FileWriter (history));
            for (ExecutionHistoryData ed : model.getModelInstance ().values ()) {
                List<ExecutionPoint> list = ed.getExecutionHistory ();
                for (ExecutionPoint p : list) {
                    bw.write (ed.getIdentifier ());
                    bw.write (",");
                    bw.write (p.toString ());
                }
            }
            bw.flush ();
            bw.close ();
        }
        catch (IOException e) {
            m_reportingPort.error (RainbowComponentT.ANALYSIS, "Could not save tactic execution history file");
        }
    }

    @Override
    public void setProperty (String key, String value) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getProperty (String key) {
        // TODO Auto-generated method stub
        return null;
    }
}
