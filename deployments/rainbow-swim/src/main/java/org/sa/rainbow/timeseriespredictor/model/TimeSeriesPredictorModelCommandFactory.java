package org.sa.rainbow.timeseriespredictor.model;

import java.io.InputStream;

import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public class TimeSeriesPredictorModelCommandFactory extends ModelCommandFactory<TimeSeriesPredictorModel> {

    public static TimeSeriesPredictorModelLoadModelCommand loadCommand (ModelsManager mm,
            String modelName,
            InputStream stream,
            String source) {
        return new TimeSeriesPredictorModelLoadModelCommand (mm, modelName, stream, source);
    }

    public TimeSeriesPredictorModelCommandFactory (TimeSeriesPredictorModelInstance model) {
        super (model.getClass (), model);

    }

    @Override
    protected void fillInCommandMap () {
        m_commandMap.put("Observe".toLowerCase (), ObserveCmd.class);
        m_commandMap.put("SetHorizon".toLowerCase (), SetHorizonCmd.class);
    }

    @Override
    public AbstractSaveModelCmd<TimeSeriesPredictorModel> saveCommand (String location) throws RainbowModelException {
        // TODO Auto-generated method stub
        return null;
    }

    public ObserveCmd observeCmd (double value) {
        return new ObserveCmd ((TimeSeriesPredictorModelInstance) m_modelInstance, "", Double.toString (value));
    }

    public SetHorizonCmd setHorizonCmd (int value) {
        return new SetHorizonCmd ((TimeSeriesPredictorModelInstance) m_modelInstance, "", Integer.toString (value));
    }


}
