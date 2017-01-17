package org.sa.rainbow.brass.model.map;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;

import java.io.OutputStream;

public class SaveEnvMapCmd extends AbstractSaveModelCmd<EnvMap> {
	public SaveEnvMapCmd (IModelsManager mm, String resource, OutputStream os, String source) {
		super ("saveEnvMapState", mm, resource, os, source);
	}
	
	@Override
	public Object getResult () throws IllegalStateException {
		return null;
	}
	
	@Override
	public ModelReference getModelReference (){
		return new ModelReference ("", "EnvMap");
	}

	@Override
	protected void subExecute () throws RainbowException {
		
	}
	
	@Override 
	protected void subRedo () throws RainbowException {
		
	}
	
	@Override
	protected void subUndo () throws RainbowException {
		
	}
	
	@Override
	protected boolean checkModelValidForCommand (EnvMap envMap) {
		return true;
	}
	
}
