package org.sa.rainbow.stitch.history;

import java.util.List;
import java.util.Map;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.stitch.util.ExecutionHistoryData;
import org.sa.rainbow.stitch.util.ExecutionHistoryData.ExecutionStateT;

public class StrategyExecutionStateCommand
extends
AbstractRainbowModelOperation<ExecutionHistoryData.ExecutionPoint, Map<String, ExecutionHistoryData>> {

    private ExecutionHistoryData m_data;
    private ExecutionHistoryData m_oldData;
	private ModelReference m_modelReference;

    public StrategyExecutionStateCommand (String commandName, IModelInstance<Map<String, ExecutionHistoryData>> model,
            String target, String modelref, String type, String newState, String data) {
        super (commandName, model, target, modelref, type, newState, data);
        m_modelReference = ModelReference.fromString(modelref);
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
            datum.setModel(m_modelReference);
            getModelContext ().getModelInstance ().put (getTarget (), datum);
        }
        else {
            m_oldData = new ExecutionHistoryData (datum);
        }
        ExecutionStateT valueOf = ExecutionStateT.valueOf (getParameters ()[2]);
		datum.addExecutionTransition (getParameters ()[1], valueOf,
                getParameters ()[3]);
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
