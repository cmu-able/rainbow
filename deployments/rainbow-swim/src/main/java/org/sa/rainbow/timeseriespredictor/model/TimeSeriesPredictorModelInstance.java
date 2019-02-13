package org.sa.rainbow.timeseriespredictor.model;

import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public class TimeSeriesPredictorModelInstance implements IModelInstance<TimeSeriesPredictorModel> {

    public static final String MODEL_TYPE = "TSP";
    private TimeSeriesPredictorModel      m_model;
    private String              m_attachedToModel;
    private String              m_source;
    private TimeSeriesPredictorModelCommandFactory m_commandFactory;

    public TimeSeriesPredictorModelInstance (TimeSeriesPredictorModel pModel, String attachedToModel, String source) {
        setModelInstance (pModel);
        setOriginalSource (source);
        m_attachedToModel = attachedToModel;
    }

    @Override
    public TimeSeriesPredictorModel getModelInstance () {
        return m_model;
    }

    @Override
    public void setModelInstance (TimeSeriesPredictorModel model) {
        m_model = model;
    }

    @Override
    public IModelInstance<TimeSeriesPredictorModel> copyModelInstance (String newName) throws RainbowCopyException {
        try {
            return new TimeSeriesPredictorModelInstance ((TimeSeriesPredictorModel )m_model.clone (), m_attachedToModel,
                    getOriginalSource ());
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public String getModelType () {
        return MODEL_TYPE;
    }

    @Override
    public String getModelName () {
        return m_attachedToModel;
    }

    @Override
    public ModelCommandFactory<TimeSeriesPredictorModel> getCommandFactory () {
        if (m_commandFactory == null) {
            m_commandFactory = new TimeSeriesPredictorModelCommandFactory (this);
        }
        return m_commandFactory;
    }

    @Override
    public void setOriginalSource (String source) {
        m_source = source;
    }

    @Override
    public String getOriginalSource () {
        return m_source;
    }

    @Override
    public void dispose () throws RainbowException {
        m_source = null;
        m_model = null;
        m_attachedToModel = null;
    }

}
