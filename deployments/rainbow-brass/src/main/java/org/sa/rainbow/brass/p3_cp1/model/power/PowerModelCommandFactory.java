package org.sa.rainbow.brass.p3_cp1.model.power;

import java.io.InputStream;

import org.sa.rainbow.brass.confsynthesis.SimpleConfigurationStore;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public class PowerModelCommandFactory extends ModelCommandFactory<SimpleConfigurationStore> {

	private static final String UPDATE_POWER_MODEL_CMD = "updatePowerModel";

	@LoadOperation
	public static LoadPowerModelCmd loadCommand(ModelsManager mm,
			String modelName,
			InputStream stream,
			String source) {
		return new LoadPowerModelCmd(mm, modelName, stream, source);
	}
	
	public PowerModelCommandFactory(
			CP1PowerModelInstance model) throws RainbowException {
		super(CP1PowerModelInstance.class, model);
	}


	@Override
	public AbstractSaveModelCmd<SimpleConfigurationStore> saveCommand(String location) throws RainbowModelException {
		return null;
	}
	
	@Operation(name=UPDATE_POWER_MODEL_CMD)
	public UpdatePowerModelCmd updatePowerModelCmd(String newFileName) {
		return new UpdatePowerModelCmd(UPDATE_POWER_MODEL_CMD, (CP1PowerModelInstance )m_modelInstance,"",newFileName);
	}

}
