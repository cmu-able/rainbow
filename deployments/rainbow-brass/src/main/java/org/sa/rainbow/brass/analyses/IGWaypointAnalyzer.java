package org.sa.rainbow.brass.analyses;

import java.util.Collection;

import org.sa.rainbow.brass.model.P2ModelAccessor;
import org.sa.rainbow.brass.model.instructions.IInstruction;
import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.MoveAbsHInstruction;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapNode;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState.LocationRecording;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

public class IGWaypointAnalyzer extends P2Analyzer implements IRainbowModelChangeCallback {

	public IGWaypointAnalyzer() {
		super("IG to Waypoint");
	}

	private P2ModelAccessor m_modelAccessor;
	private IRainbowChangeBusSubscription m_newIGSubscription = new IRainbowChangeBusSubscription() {

		@Override
		public boolean matches(IRainbowMessage message) {
			String modelName = (String) message.getProperty(IModelChangeBusPort.MODEL_NAME_PROP);
			String modelType = (String) message.getProperty(IModelChangeBusPort.MODEL_TYPE_PROP);
			String commandName = (String) message.getProperty(IModelChangeBusPort.COMMAND_PROP);

			// New IG event
			boolean isNewIGEvent = InstructionGraphModelInstance.INSTRUCTION_GRAPH_TYPE.equals(modelType)
					&& "setInstructions".equals(commandName);

			return isNewIGEvent;
		}
	};

	@Override
	public void initializeConnections() throws RainbowConnectionException {
		super.initializeConnections();
		m_modelAccessor = new P2ModelAccessor(m_modelsManagerPort);
		m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort();
		m_modelChangePort.subscribe(m_newIGSubscription, this);
	}

	protected P2ModelAccessor getModels() {
		return m_modelAccessor;
	}

	@Override
	protected void runAction() {

	}

	@Override
	public void onEvent(ModelReference reference, IRainbowMessage message) {
		try {
			log("Notified of a new IG");
			LocationRecording currentPose = getModels().getMissionStateModel().getModelInstance().getCurrentPose();
			EnvMap envMap = getModels().getEnvMapModel().getModelInstance();
			String currentSrc = envMap.getNode(currentPose.getX(), currentPose.getY()).getLabel();
			Collection<? extends IInstruction> instructions = getModels().getInstructionGraphModel().getModelInstance()
					.getInstructions();
			for (IInstruction i : instructions) {
				if (i instanceof MoveAbsHInstruction) {
					MoveAbsHInstruction mai = (MoveAbsHInstruction) i;
					mai.setSourceWaypoint(currentSrc);
					EnvMapNode node = envMap.getNode(mai.getTargetX(), mai.getTargetY());
					if (node != null) {
						String tgtWp = node.getLabel();
						mai.setTargetWaypoint(tgtWp);
						currentSrc = tgtWp;
					} else {
						throw new NullPointerException("Node from " + mai.getTargetX() + ", " + mai.getTargetY()
								+ " does not exist in envmap");
					}
				}
			}

			log("Received and processed a new IG");
		} catch (Throwable e) {
			e.printStackTrace();
			log("IG processor encountered an error");
		} finally {
			m_modelUSPort.updateModel(getModels().getRainbowStateModel().getCommandFactory().clearModelProblems());
			getModels().getRainbowStateModel().getModelInstance().m_waitForIG = false;
		}
	}

}
