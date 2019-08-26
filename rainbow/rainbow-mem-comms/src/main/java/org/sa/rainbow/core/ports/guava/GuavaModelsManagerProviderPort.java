package org.sa.rainbow.core.ports.guava;

import java.util.Collection;

import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelsManagerPort;

public class GuavaModelsManagerProviderPort implements IModelsManagerPort {

	private IModelsManager m_modelsManager;

	public GuavaModelsManagerProviderPort() {
	}
	
	@Override
	public Collection<? extends String> getRegisteredModelTypes() {
		error();
		return m_modelsManager.getRegisteredModelTypes();
	}

	private void error() {
		if (m_modelsManager == null) throw new NullPointerException("Connector does not connector to Models Manager");
	}

	@Override
	public <T> IModelInstance<T> getModelInstance(ModelReference modelRef) {
		error();
		return m_modelsManager.getModelInstance(modelRef);
	}

	public void setModelsManager(IModelsManager modelsManager) {
		m_modelsManager = modelsManager;
		
	}
	
	@Override
	public boolean isModelLocked(ModelReference modelRef) {
		return m_modelsManager.isModelLocked(modelRef);
	}

}
