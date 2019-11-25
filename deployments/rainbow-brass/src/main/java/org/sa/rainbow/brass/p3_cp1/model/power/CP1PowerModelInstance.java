package org.sa.rainbow.brass.p3_cp1.model.power;

import org.sa.rainbow.brass.confsynthesis.SimpleConfigurationStore;
import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public class CP1PowerModelInstance implements IModelInstance<SimpleConfigurationStore> {

	public static final String POWER_MODEL_TYPE = "PowerModel";
	private SimpleConfigurationStore m_powerModel;
	private ModelReference m_modelReference;
	private PowerModelCommandFactory m_commandFactory;
	private String m_source;

	public CP1PowerModelInstance(SimpleConfigurationStore pm, String source) {
		setModelInstance(pm);
		setOriginalSource(source);
	}

	@Override
	public SimpleConfigurationStore getModelInstance() {
		return m_powerModel;
	}

	@Override
	public void setModelInstance(SimpleConfigurationStore model) {
		m_powerModel = model;
	}

	@Override
	public IModelInstance<SimpleConfigurationStore> copyModelInstance(String newName) throws RainbowCopyException {
		return new CP1PowerModelInstance(m_powerModel.copy(), getOriginalSource());
	}

	@Override
	public String getModelType() {
		return POWER_MODEL_TYPE;
	}

	@Override
	public String getModelName() {
		return getModelReference().getModelName();
	}

	@Override
	public ModelCommandFactory<SimpleConfigurationStore> getCommandFactory() throws RainbowException {
		if (m_commandFactory == null) 
			m_commandFactory = new PowerModelCommandFactory(this);
		return m_commandFactory;
	}

	@Override
	public void setOriginalSource(String source) {
		m_source = source;
	}

	@Override
	public String getOriginalSource() {
		return m_source;
	}

	@Override
	public void dispose() throws RainbowException {

	}

	public ModelReference getModelReference() {
		return m_modelReference;
	}

	public void setModelReference(ModelReference modelReference) {
		m_modelReference = modelReference;
	}

}
