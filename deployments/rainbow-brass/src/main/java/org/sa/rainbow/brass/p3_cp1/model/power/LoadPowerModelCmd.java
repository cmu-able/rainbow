package org.sa.rainbow.brass.p3_cp1.model.power;

import java.io.InputStream;

import org.sa.rainbow.brass.confsynthesis.SimpleConfigurationStore;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.AbstractLoadModelCmd;

public class LoadPowerModelCmd extends AbstractLoadModelCmd<SimpleConfigurationStore> {

	private String m_modelName;
	private InputStream m_stream;
	private CP1PowerModelInstance m_result;

	public LoadPowerModelCmd(ModelsManager mm, String modelName, InputStream stream, String source) {
		super("loadPowerModel", mm, modelName, stream, source);
		m_modelName = modelName;
		m_stream = stream;
	}

	@Override
	public CP1PowerModelInstance getResult() throws IllegalStateException {
		return m_result;
	}

	@Override
	public ModelReference getModelReference() {
		return new ModelReference(m_modelName, CP1PowerModelInstance.POWER_MODEL_TYPE);
	}

	@Override
	protected void subExecute() throws RainbowException {
//		if (m_stream == null) {
			SimpleConfigurationStore store = new SimpleConfigurationStore(Rainbow.instance().allProperties());
			m_result = new CP1PowerModelInstance(store, getOriginalSource());
			m_result.setModelReference(getModelReference());
			doPostExecute();
//		}
	}

	@Override
	protected void subRedo() throws RainbowException {
		doPostExecute();
	}

	@Override
	protected void subUndo() throws RainbowException {
		doPostUndo();
	}

	@Override
	protected boolean checkModelValidForCommand(Object model) {
		return true;
	}

}
