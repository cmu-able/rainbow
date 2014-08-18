package org.sa.rainbow.stitch.tactic.history;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

public class SaveTacticExecutionHistoryMonitor implements IRainbowAnalysis,
IRainbowModelChangeCallback<Map<String, ExecutionHistoryData>> {

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
            IModelInstance model = m_modelsManagerPort.getModelInstance (mr.getModelType (), mr.getModelName ());
            saveExecutionHistoryToFile (model);
        }
    }

    private void saveExecutionHistoryToFile (IModelInstance<Map<String, ExecutionHistoryData>> model) {
        try {
            BufferedWriter bw = new BufferedWriter (new FileWriter (new File (m_filename)));
            for (ExecutionHistoryData ed : model.getModelInstance ().values ()) {
                bw.write (ed.toString ());
                bw.newLine ();
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
