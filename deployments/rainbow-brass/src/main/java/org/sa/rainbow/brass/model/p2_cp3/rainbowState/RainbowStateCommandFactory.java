package org.sa.rainbow.brass.model.p2_cp3.rainbowState;

import java.io.InputStream;

import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public class RainbowStateCommandFactory extends ModelCommandFactory<RainbowState>{
	public RainbowStateCommandFactory(
			RainbowStateModelInstance model) {
		super(RainbowStateModelInstance.class, model);
	}

	public static RainbowStateLoadCmd loadCommand(ModelsManager mm, String modelName, InputStream
            stream, String source) {
		return new RainbowStateLoadCmd(mm, modelName, stream, source);
	}

	@Override
	protected void fillInCommandMap() {
		m_commandMap.put("setPlanIssued".toLowerCase(), SetPlanIssuedCmd.class);
		m_commandMap.put("setModelProblem".toLowerCase(), SetModelProblemCmd.class);
		m_commandMap.put("removeModelProblem".toLowerCase(), RemoveModelProblemCmd.class);
		m_commandMap.put("clearModelProblems".toLowerCase(), ClearModelProblemsCmd.class);
	}

	@Override
	public AbstractSaveModelCmd<RainbowState> saveCommand(String location) throws RainbowModelException {
		return null;
	}
	
	public SetPlanIssuedCmd setPlanIssued(boolean issued) {
		return new SetPlanIssuedCmd((RainbowStateModelInstance )m_modelInstance, "", Boolean.toString(issued));
	}
	
	public SetModelProblemCmd setModelProblem(RainbowState.CP3ModelState problem) {
		return new SetModelProblemCmd((RainbowStateModelInstance )m_modelInstance, "", problem.name());
	}
	
	public RemoveModelProblemCmd removeModelProblem(RainbowState.CP3ModelState problem) {
		return new RemoveModelProblemCmd((RainbowStateModelInstance )m_modelInstance, "", problem.name());
	}
	
	public ClearModelProblemsCmd clearModelProblems() {
		return new ClearModelProblemsCmd((RainbowStateModelInstance )m_modelInstance, "", "");
	}
	
}
