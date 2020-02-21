package org.sa.rainbow.brass.model.p2_cp3.clock;

import java.io.InputStream;

import org.sa.rainbow.brass.model.mission.MissionState;
import org.sa.rainbow.brass.model.mission.MissionStateModelInstance;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.AbstractLoadModelCmd;

public class ClockLoadCmd extends AbstractLoadModelCmd<Clock> {

	private String m_modelName;
	private InputStream m_stream;
	private ClockModelInstance m_result;

	public ClockLoadCmd(IModelsManager mm, String resource, InputStream is, String source) {
		super("loadClock", mm, resource, is, source);
		m_modelName = resource;
		m_stream = is;
	}

	@Override
	public IModelInstance<Clock> getResult() throws IllegalStateException {
		return m_result;
	}

	@Override
	public ModelReference getModelReference() {
		return new ModelReference(m_modelName, ClockModelInstance.CLOCK_TYPE);
	}

	@Override
	protected void subExecute() throws RainbowException {
        if (m_stream == null) {
        	Clock c = new Clock(getModelReference());
            m_result = new ClockModelInstance(c, getOriginalSource ());
            doPostExecute ();
        }
	}

	@Override
	protected void subRedo() throws RainbowException {
        doPostExecute ();
		
	}

	@Override
	protected void subUndo() throws RainbowException {
        doPostUndo ();
		
	}

	@Override
	protected boolean checkModelValidForCommand(Object model) {
        return true;
	}

}
