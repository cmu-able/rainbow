package org.sa.rainbow.brass.p3_cp1.model.power;

import org.sa.rainbow.brass.confsynthesis.SimpleConfigurationStore;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public class PowerModelCommandFactory extends ModelCommandFactory<SimpleConfigurationStore> {

	public PowerModelCommandFactory(
			CP1PowerModelInstance model) {
		super(CP1PowerModelInstance.class, model);
	}

	@Override
	protected void fillInCommandMap() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AbstractSaveModelCmd<SimpleConfigurationStore> saveCommand(String location) throws RainbowModelException {
		// TODO Auto-generated method stub
		return null;
	}

}
