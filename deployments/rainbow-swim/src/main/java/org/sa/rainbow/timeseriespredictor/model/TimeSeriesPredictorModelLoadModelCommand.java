package org.sa.rainbow.timeseriespredictor.model;

import java.io.InputStream;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.AbstractLoadModelCmd;

public class TimeSeriesPredictorModelLoadModelCommand extends AbstractLoadModelCmd<TimeSeriesPredictorModel> {

    private static final String PROP_ARGS = "customize.model.timeseriespredictor.args";
	public static final String PROP_TRAININGLENGTH = "customize.model.timeseriespredictor.traininglength";
	private String         m_name;
    private TimeSeriesPredictorModelInstance m_result;

    public TimeSeriesPredictorModelLoadModelCommand (IModelsManager mm, String modelName, InputStream is, String source) {
        super ("loadTimeSeriesPredictorModel", mm, modelName, is, source);
        m_name = modelName;
    }

    @Override
    public IModelInstance<TimeSeriesPredictorModel> getResult () throws IllegalStateException {
        return m_result;
    }

    @Override
    public ModelReference getModelReference () {
        return new ModelReference (m_name, TimeSeriesPredictorModelInstance.MODEL_TYPE);
    }

    @Override
    protected void subExecute () throws RainbowException {
    	String modelArgs = Rainbow.instance ().getProperty (PROP_ARGS);
    	int trainingLength = Integer.parseInt(Rainbow.instance ().getProperty (PROP_TRAININGLENGTH));
    	
        TimeSeriesPredictorModel pm = new TimeSeriesPredictorModel (modelArgs, trainingLength);
		m_result = new TimeSeriesPredictorModelInstance(pm, getModelReference().getModelName(), "");
        doPostExecute ();
    }

    @Override
    protected void subRedo () throws RainbowException {
        doPostExecute ();
    }

    @Override
    protected void subUndo () throws RainbowException {
        doPostUndo ();
    }

    @Override
    protected boolean checkModelValidForCommand (Object model) {
        return true;
    }

}
