package org.sa.rainbow.timeseriespredictor.model;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class ObserveCmd extends AbstractRainbowModelOperation<Object, TimeSeriesPredictorModel> {

    private Double m_value;

    public ObserveCmd (TimeSeriesPredictorModelInstance model, String bogusTarget, String value) {
        super ("observe", model, bogusTarget, value);
        m_value = Double.valueOf (value);
    }

    @Override
    public Object getResult () throws IllegalStateException {
        return null;
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, "TimeSeriesPredictorModelOperation");
    }

    @Override
    protected void subExecute () throws RainbowException {
        m_modelContext.getModelInstance ().observe (m_value);
    }

    @Override
    protected void subRedo () throws RainbowException {
        m_modelContext.getModelInstance ().observe (m_value);
    }

    @Override
    protected void subUndo () throws RainbowException {
        throw new RainbowException("Undo not supported for " + super.getName());
    }

    @Override
    protected boolean checkModelValidForCommand (TimeSeriesPredictorModel model) {
        return true;
    }

}
