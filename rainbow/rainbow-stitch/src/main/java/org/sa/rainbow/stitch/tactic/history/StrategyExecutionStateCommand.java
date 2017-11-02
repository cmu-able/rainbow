package org.sa.rainbow.stitch.tactic.history;

import java.util.List;
import java.util.Map;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.stitch.util.ExecutionHistoryData;
import org.sa.rainbow.stitch.util.ExecutionHistoryData.ExecutionStateT;

public class StrategyExecutionStateCommand
extends
AbstractRainbowModelOperation<ExecutionHistoryData.ExecutionPoint, Map<String, ExecutionHistoryData>> {

    private ExecutionHistoryData m_data;
    private ExecutionHistoryData m_oldData;

    public StrategyExecutionStateCommand (String commandName, IModelInstance<Map<String, ExecutionHistoryData>> model,
            String target, String type, String newState, String data) {
        super (commandName, model, target, type, newState, data);
    }

    @Override
    public ExecutionHistoryData.ExecutionPoint getResult () throws IllegalStateException {
        return m_data.getLastExecutionPoint ();
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, getName ());
    }

    @Override
    protected void subExecute () throws RainbowException {
        ExecutionHistoryData datum = getModelContext ().getModelInstance ().get (getTarget ());
        if (datum == null) {
            datum = new ExecutionHistoryData ();
            datum.setIdentifier (getTarget ());
            getModelContext ().getModelInstance ().put (getTarget (), datum);
        }
        else {
            m_oldData = new ExecutionHistoryData (datum);
        }
        datum.addExecutionTransition (getParameters ()[0], ExecutionStateT.valueOf (getParameters ()[1]),
                getParameters ()[2]);
        m_data = datum;
    }

    @Override
    protected void subRedo () throws RainbowException {
        subExecute ();
    }

    @Override
    protected void subUndo () throws RainbowException {
        if (m_oldData == null) {
            getModelContext ().getModelInstance ().remove (getTarget ());
        }
        else {
            getModelContext ().getModelInstance ().put (getTarget (), m_oldData);
        }
    }

    @Override
    protected boolean checkModelValidForCommand (Map<String, ExecutionHistoryData> model) {
        return true;
    }

}
