package org.sa.rainbow.brass.p3_cp1.model.power;

import java.io.InputStream;

import org.sa.rainbow.brass.confsynthesis.SimpleConfigurationStore;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public class PowerModelCommandFactory extends ModelCommandFactory<SimpleConfigurationStore> {

	public static LoadPowerModelCmd loadCommand(ModelsManager mm,
			String modelName,
			InputStream stream,
			String source) {
		return new LoadPowerModelCmd(mm, modelName, stream, source);
	}
	
	public PowerModelCommandFactory(
			CP1PowerModelInstance model) {
		super(CP1PowerModelInstance.class, model);
	}

	@Override
	protected void fillInCommandMap() {
		m_commandMap.put("updatePowerModel".toLowerCase(), UpdatePowerModelCmd.class);
	}

	@Override
	public AbstractSaveModelCmd<SimpleConfigurationStore> saveCommand(String location) throws RainbowModelException {
		return null;
	}
	
	public UpdatePowerModelCmd updatePowerModelCmd(String newFileName) {
		return new UpdatePowerModelCmd((CP1PowerModelInstance )m_modelInstance,"",newFileName);
	}

}
