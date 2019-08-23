package org.sa.rainbow.brass.model.p2_cp3.clock;

import java.io.InputStream;

import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public class ClockCommandFactory extends ModelCommandFactory<Clock> {

	public static ClockLoadCmd loadCommand(ModelsManager mm, String modelName, InputStream
            stream, String source) {
		return new ClockLoadCmd(mm, modelName, stream, source);
	}
	
	public ClockCommandFactory(IModelInstance<Clock> model) {
		super (ClockModelInstance.class, model);
	}
	
	@Override
	protected void fillInCommandMap() {
		m_commandMap.put("setCurrentTime".toLowerCase(), SetCurrentTimeCmd.class);
	}

	@Override
	public AbstractSaveModelCmd<Clock> saveCommand(String location) throws RainbowModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public SetCurrentTimeCmd setCurrentTimeCmd(double t) {
		return new SetCurrentTimeCmd((ClockModelInstance )m_modelInstance, "", Double.toString(t));
	}
}
