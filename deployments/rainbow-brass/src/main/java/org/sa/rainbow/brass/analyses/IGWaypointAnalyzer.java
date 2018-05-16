package org.sa.rainbow.brass.analyses;

import java.util.Collection;
import java.util.Date;

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
	private boolean m_newIG = false;
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

	private Long m_time;

	private void handleProblem() {
		if (m_time == null)
			m_time = new Date().getTime() / 1000;
		if (new Date().getTime() / 1000 - 15 > m_time)
			throw new NullPointerException(
					"IGWaypointAnalyzer waited too long for the current pose or finding the corresponding map position");
	}

	@Override
	protected void runAction() {
		boolean go = false;
		synchronized (this) {
			if (m_newIG)
				go = true;
		}
		if (go) {
			try {
				LocationRecording currentPose = getModels().getMissionStateModel().getModelInstance().getCurrentPose();
				EnvMap envMap = getModels().getEnvMapModel().getModelInstance();
				if (currentPose == null || envMap == null) {
					log("Still waiting for pose information");
					handleProblem();
					return;
				}
				EnvMapNode srcNode = envMap.getNode(currentPose.getX(), currentPose.getY());
				if (srcNode == null) {
					log("Robot is not at a location in the map: (" + currentPose.getX() + ", " + currentPose.getY()
							+ ")");
					log("Robot is not a a location in the map");
					log("The source node is not set, using current pose to guess where robot is coming from");
					String closestNode = "none";
					double distance = Double.MAX_VALUE;
					for (EnvMapNode node : envMap.getNodes().values()) {
						double d = envMap.distanceBetweenCoords(currentPose.getX(), currentPose.getY(), node.getX(),
								node.getY());
						if (d < distance) {
							distance = d;
							closestNode = node.getLabel();
							srcNode = node;
						}
					}
					log("Guessing that current source is " + closestNode);

				}

				String currentSrc = srcNode == null ? null : srcNode.getLabel();
				Collection<? extends IInstruction> instructions = getModels().getInstructionGraphModel()
						.getModelInstance().getInstructions();
				for (IInstruction i : instructions) {
					if (i instanceof MoveAbsHInstruction) {
						MoveAbsHInstruction mai = (MoveAbsHInstruction) i;
						if (currentSrc == null) {
							log("Current source is null");
						}
						mai.setSourceWaypoint(currentSrc);
						EnvMapNode node = envMap.getNode(mai.getTargetX(), mai.getTargetY());
						if (node != null) {
							String tgtWp = node.getLabel();
							mai.setTargetWaypoint(tgtWp);
							currentSrc = tgtWp;
						} else {
							throw new NullPointerException("Node from " + mai.getTargetX() + ", " + mai.getTargetY()
									+ " does not exist in envmap in instruction " + mai.getInstruction());
						}
					}
				}

				log("Received and processed a new IG");
				synchronized (this) {
					m_newIG = false;
				}
			} catch (Throwable e) {
				e.printStackTrace();
				log("IG processor encountered an error");
			} finally {
				m_modelUSPort.updateModel(getModels().getRainbowStateModel().getCommandFactory().clearModelProblems());
				getModels().getRainbowStateModel().getModelInstance().m_waitForIG = false;
			}

		}
	}

	@Override
	public void onEvent(ModelReference reference, IRainbowMessage message) {
		log("Notified of a new IG");
		synchronized (this) {
			m_newIG = true;
		}

	}

}
