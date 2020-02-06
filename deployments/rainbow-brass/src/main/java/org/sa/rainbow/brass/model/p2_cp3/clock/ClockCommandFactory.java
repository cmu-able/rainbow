package org.sa.rainbow.brass.model.p2_cp3.clock;

import java.io.InputStream;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public class ClockCommandFactory extends ModelCommandFactory<Clock> {

	private static final String SET_CURRENT_TIME_CMD = "setCurrentTime";

	@LoadOperation
	public static ClockLoadCmd loadCommand(ModelsManager mm, String modelName, InputStream
            stream, String source) {
		return new ClockLoadCmd(mm, modelName, stream, source);
	}
	
	public ClockCommandFactory(IModelInstance<Clock> model) throws RainbowException {
		super (ClockModelInstance.class, model);
	}
	
	@Override
	public AbstractSaveModelCmd<Clock> saveCommand(String location) throws RainbowModelException {
		// TODO Auto-generated method stub
		return null;
	}

	@Operation(name=SET_CURRENT_TIME_CMD)
	public SetCurrentTimeCmd setCurrentTimeCmd(double t) {
		return new SetCurrentTimeCmd(SET_CURRENT_TIME_CMD, (ClockModelInstance )m_modelInstance, "", Double.toString(t));
	}
}
