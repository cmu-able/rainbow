package org.sa.rainbow.brass.model.map;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.AbstractLoadModelCmd;

import java.io.InputStream;

/**
 * @author jcamara
 *
 */
public class EnvMapLoadCmd extends AbstractLoadModelCmd<EnvMap> {
	private final String m_modelName;
	private final InputStream m_stream;
	private EnvMapModelInstance m_result;

	public EnvMapLoadCmd (IModelsManager mm, String resource, InputStream is, String source){
		super("loadEnvMap", mm, resource, is, source);
		m_modelName = resource;
		m_stream=is;
	}
	
	@Override
	public IModelInstance<EnvMap> getResult () throws IllegalStateException {
		return m_result;
	}
	
	public ModelReference getModelReference () {
		return new ModelReference (m_modelName, EnvMapModelInstance.ENV_MAP_TYPE);
	}
	
	@Override
	protected void subExecute () throws RainbowException {
		if (m_stream == null){
			EnvMap m = new EnvMap (getModelReference());
			m_result = new EnvMapModelInstance (m, getOriginalSource ());
			doPostExecute();
		}
	}
	
	@Override
	protected void subRedo () throws RainbowException {
		doPostExecute();
	}
	
	@Override
	protected void subUndo () throws RainbowException {
		doPostUndo ();
	}
	
	@Override
	protected boolean checkModelValidForCommand (Object o) {
		return true;
	}
	
}
