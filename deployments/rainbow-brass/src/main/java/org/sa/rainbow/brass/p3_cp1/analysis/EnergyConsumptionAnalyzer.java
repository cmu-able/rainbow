package org.sa.rainbow.brass.p3_cp1.analysis;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.sa.rainbow.brass.confsynthesis.Configuration;
import org.sa.rainbow.brass.confsynthesis.SimpleConfigurationStore;
import org.sa.rainbow.brass.model.instructions.ChargeInstruction;
import org.sa.rainbow.brass.model.instructions.ForwardInstruction;
import org.sa.rainbow.brass.model.instructions.IInstruction;
import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.model.instructions.MoveAbsHInstruction;
import org.sa.rainbow.brass.model.instructions.SetConfigInstruction;
import org.sa.rainbow.brass.model.instructions.SetExecutionFailedCmd;
import org.sa.rainbow.brass.model.map.BatteryPredictor;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapModelInstance;
import org.sa.rainbow.brass.model.map.MapTranslator;
import org.sa.rainbow.brass.model.mission.MissionStateModelInstance;
import org.sa.rainbow.brass.model.mission.MissionState.Heading;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState.LocationRecording;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState.CP3ModelState;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.util.Pair;

public class EnergyConsumptionAnalyzer extends P2CP1Analyzer {
	public static final String NAME = "BRASS Accuracy Evaluator";
	private IInstruction m_prevAnalyzedAndPassedInstruction;
	private SimpleConfigurationStore m_powerModel;

	public EnergyConsumptionAnalyzer() {
		super(NAME);
	}

	@Override
	public void initialize(IRainbowReportingPort port) throws RainbowConnectionException {
		super.initialize(port);
//		m_powerModel = new SimpleConfigurationStore(Rainbow.instance().allProperties());
		m_powerModel = getModels().getPowerModel().getModelInstance();
		m_powerModel.populate();
	}

	@Override
	protected void runAction() {
		if (getModels().getInstructionGraphModel() == null)
			return;
		// Need to work out how to wait for planner in CP1 -- is it
		if (getModels().getRainbowStateModel().getModelInstance().waitForIG())
			return;
		InstructionGraphProgress igModel = getModels().getInstructionGraphModel().getModelInstance();
		IInstruction currentInstruction = igModel.getCurrentInstruction();
		if (currentInstruction == m_prevAnalyzedAndPassedInstruction)
			return;

		if (!(currentInstruction instanceof ChargeInstruction)) {
			log("Analyzing energy consumption");
			List<? extends IInstruction> remainingInstructions = igModel.getRemainingInstructions();
			double batteryCharge = -1;
			try {
				try {
					batteryCharge = getModels().getRobotStateModel().getModelInstance().getCharge();
					if (batteryCharge <= 0) {
						IRainbowOperation op = getModels().getRainbowStateModel().getCommandFactory()
								.setModelProblem(CP3ModelState.OUT_OF_BATTERY);
						m_modelUSPort.updateModel(op);
						return;
					}
				} catch (RainbowException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IllegalStateException e) {
				// Don't have battery information yet
				return;
			}

			// We need to break the instructions into chunks that are segmented by charge
			// and then ask
			// is there enough energy to get to each charge instruction or the target?
			String tgtWP = getModels().getMissionStateModel().getModelInstance().getTargetWaypoint();
			List<List<? extends IInstruction>> segments = InstructionGraphProgress
					.segmentByInstructionType(remainingInstructions, ChargeInstruction.class);
			Iterator<List<? extends IInstruction>> it = segments.iterator();
			boolean first = true;
			boolean hasEnoughEnergy = true;
			EnergyConsumptionPredictor predictor = new EnergyConsumptionPredictor(
					getModels().getEnvMapModel().getModelInstance(),
					getModels().getMissionStateModel().getModelInstance(), m_powerModel);
			predictor.setConfig(getModels().getRobotStateModel().getModelInstance().getConfigId());
			try {
				while (it.hasNext()) {
					if (first) {
						first = false;
						hasEnoughEnergy &= predictor.getPlanEnergyConsumption(currentInstruction, it.next(),
								tgtWP) < batteryCharge;
					} else {
						hasEnoughEnergy &= predictor.getPlanEnergyConsumption(null, it.next(),
								tgtWP) < MapTranslator.ROBOT_BATTERY_RANGE_MAX;
					}
				}
			} catch (NullPointerException e) {
				log("Could not yet analyze the instruction graph -- could be waypoints have not been set up");
				m_reportingPort.error(RainbowComponentT.ANALYSIS, e.getMessage(), e, LOGGER);
				return;
			}
			// double planEnergyConsumption = hasEnoughEnergy(currentInstruction,
			// remainingInstructions);
			// boolean hasEnoughEnergy = batteryCharge >= planEnergyConsumption;
			// log("Current charge = " + batteryCharge + ", needed charge = " +
			// planEnergyConsumption);
			if (hasEnoughEnergy) {
				// Keep track of the latest instruction that we have analyzed the accuracy
				// property,
				// and it passed
				m_prevAnalyzedAndPassedInstruction = currentInstruction;
			}
			boolean knowAboutLowBattery = getModels().getRainbowStateModel().getModelInstance().getProblems()
					.contains(CP3ModelState.LOW_ON_BATTERY);
			if (hasEnoughEnergy && knowAboutLowBattery) {
				try {
					IRainbowOperation op = getModels().getRainbowStateModel().getCommandFactory()
							.removeModelProblem(CP3ModelState.LOW_ON_BATTERY);
					m_modelUSPort.updateModel(op);
				} catch (RainbowException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (!hasEnoughEnergy && !knowAboutLowBattery) {
				try {
					log("Do not have enough battery.");
					LocationRecording pose = getModels().getMissionStateModel().getModelInstance().getCurrentPose();
					insertNodeIntoMap(pose, currentInstruction);
					IRainbowOperation op = getModels().getRainbowStateModel().getCommandFactory()
							.setModelProblem(CP3ModelState.LOW_ON_BATTERY);
					m_modelUSPort.updateModel(op);
				} catch (RainbowException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		} else if (getModels().getRainbowStateModel().getModelInstance().getProblems()
				.contains(CP3ModelState.LOW_ON_BATTERY)) {
			try {
				IRainbowOperation op = getModels().getRainbowStateModel().getCommandFactory()
						.removeModelProblem(CP3ModelState.LOW_ON_BATTERY);
				m_modelUSPort.updateModel(op);
			} catch (RainbowException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
