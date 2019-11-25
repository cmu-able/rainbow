package org.sa.rainbow.brass.model.p2_cp3.rainbowState;

import java.io.InputStream;

import org.sa.rainbow.brass.model.p2_cp3.mission.SetUtilityPreferenceCmd;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

public class RainbowStateCommandFactory extends ModelCommandFactory<RainbowState>{
	private static final String CLEAR_MODEL_PROBLEMS_CMD = "clearModelProblems";
	private static final String REMOVE_MODEL_PROBLEM_CMD = "removeModelProblem";
	private static final String SET_MODEL_PROBLEM_CMD = "setModelProblem";
	private static final String SET_PLAN_ISSUED_CMD = "setPlanIssued";

	public RainbowStateCommandFactory(
			RainbowStateModelInstance model) throws RainbowException {
		super(RainbowStateModelInstance.class, model);
	}

	@LoadOperation
	public static RainbowStateLoadCmd loadCommand(ModelsManager mm, String modelName, InputStream
            stream, String source) {
		return new RainbowStateLoadCmd(mm, modelName, stream, source);
	}

	@Override
	public AbstractSaveModelCmd<RainbowState> saveCommand(String location) throws RainbowModelException {
		return null;
	}
	
	@Operation(name=SET_PLAN_ISSUED_CMD)
	public SetPlanIssuedCmd setPlanIssued(boolean issued) {
		return new SetPlanIssuedCmd(SET_PLAN_ISSUED_CMD, (RainbowStateModelInstance )m_modelInstance, "", Boolean.toString(issued));
	}
	
	@Operation(name=SET_MODEL_PROBLEM_CMD)
	public SetModelProblemCmd setModelProblem(RainbowState.CP3ModelState problem) {
		return new SetModelProblemCmd(SET_MODEL_PROBLEM_CMD, (RainbowStateModelInstance )m_modelInstance, "", problem.name());
	}
	
	@Operation(name=REMOVE_MODEL_PROBLEM_CMD)
	public RemoveModelProblemCmd removeModelProblem(RainbowState.CP3ModelState problem) {
		return new RemoveModelProblemCmd(REMOVE_MODEL_PROBLEM_CMD, (RainbowStateModelInstance )m_modelInstance, "", problem.name());
	}
	
	@Operation(name=CLEAR_MODEL_PROBLEMS_CMD)
	public ClearModelProblemsCmd clearModelProblems() {
		return new ClearModelProblemsCmd(CLEAR_MODEL_PROBLEMS_CMD, (RainbowStateModelInstance )m_modelInstance, "", "");
	}
	

	
}
