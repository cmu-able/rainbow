package org.sa.rainbow.stitch.tactic.history;

import java.util.List;
import java.util.Map;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;
import org.sa.rainbow.stitch.util.ExecutionHistoryData;

public class TacticDurationCommand
extends
AbstractRainbowModelOperation<org.sa.rainbow.stitch.util.ExecutionHistoryData, Map<String, ExecutionHistoryData>> {

    private ExecutionHistoryData m_oldDatum;
    private ExecutionHistoryData m_newDatum;

    public TacticDurationCommand (String commandName, IModelInstance<Map<String, ExecutionHistoryData>> model,
            String target, String duration) {
        super (commandName, model, target, duration);
    }


    @Override
    public ExecutionHistoryData getResult () throws IllegalStateException {
        return m_newDatum;
    }

    @Override
    public String getModelName () {
        return m_modelContext.getModelName ();
    }

    @Override
    public String getModelType () {
        return m_modelContext.getModelType ();
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, "TacticHistoryOperation");

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
            m_oldDatum = new ExecutionHistoryData (datum);
        }
        datum.addDurationSample (Long.parseLong (getParameters ()[0]));
        m_newDatum = datum;
    }

    @Override
    protected void subRedo () throws RainbowException {
        subExecute ();
    }

    @Override
    protected void subUndo () throws RainbowException {
        if (m_oldDatum == null) {
            getModelContext ().getModelInstance ().remove (getTarget ());
        }
        else {
            getModelContext ().getModelInstance ().put (getTarget (), m_oldDatum);
        }
    }

    @Override
    protected boolean checkModelValidForCommand (Map<String, ExecutionHistoryData> model) {
        return true;
    }

}