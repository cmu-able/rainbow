package org.sa.rainbow.brass.analyses.p2_cp1;

import org.sa.rainbow.brass.analyses.P2Analyzer;
import org.sa.rainbow.brass.model.instructions.IInstruction;
import org.sa.rainbow.brass.model.instructions.MoveAbsHInstruction;
import org.sa.rainbow.brass.model.instructions.SetExecutionFailedCmd;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.InsertNodeCmd;
import org.sa.rainbow.brass.model.p2_cp1.ModelAccessor;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState.LocationRecording;
import org.sa.rainbow.core.error.RainbowConnectionException;

public abstract class P2CP1Analyzer extends P2Analyzer {
	public P2CP1Analyzer(String name) {
		super(name);
	}

	private ModelAccessor m_modelAccessor;
	
	@Override
	public void initializeConnections() throws RainbowConnectionException {
		super.initializeConnections();
		m_modelAccessor = new ModelAccessor(m_modelsManagerPort);
	}

	protected ModelAccessor getModels () {
		return m_modelAccessor;
	}

	private MoveAbsHInstruction getPreviousMoveAbsH(MoveAbsHInstruction currentMoveAbsH) {
	    int j = Integer.valueOf (currentMoveAbsH.getInstructionLabel ()) - 1;
	    for (int i = j; i > 0; i--) {
	        String label = String.valueOf (i);
	        IInstruction instruction = getModels().getInstructionGraphModel().getModelInstance().getInstruction (label);
	
	        if (instruction instanceof MoveAbsHInstruction) return (MoveAbsHInstruction )instruction;
	    }
	
	    // No previous MoveAbsH instruction
	    return null;
	}

	protected void insertNodeIntoMap(LocationRecording pose, IInstruction currentInst) {
		        if (currentInst instanceof MoveAbsHInstruction) {
		            MoveAbsHInstruction currentMoveAbsH = (MoveAbsHInstruction )currentInst;
		            MoveAbsHInstruction prevMoveAbsH = getPreviousMoveAbsH (currentMoveAbsH);
	
		            double sourceX;
		            double sourceY;
		            double targetX = currentMoveAbsH.getTargetX ();
		            double targetY = currentMoveAbsH.getTargetY ();
	
		            if (prevMoveAbsH != null) {
		                sourceX = prevMoveAbsH.getTargetX ();
		                sourceY = prevMoveAbsH.getTargetY ();
		            }
		            else {
		                // The current instruction is the first MoveAbsH instruction in IG
		                // Use the initial pose as the source pose
		                LocationRecording initialPose = getModels().getMissionStateModel().getModelInstance().getInitialPose ();
						sourceX = initialPose.getX ();
		                sourceY = initialPose.getY ();
		            }
	
		            // Find the corresponding environment map nodes of the source and target positions
		            // Node naming assumption: node's label is lX where X is the order in which the node is added
		            EnvMap envMap = getModels().getEnvMapModel().getModelInstance();
					int numNodes = envMap.getNodeCount () + 1;
		            String n = "l" + numNodes;
		            String na = envMap.getNode (sourceX, sourceY).getLabel ();
		            String nb = envMap.getNode (targetX, targetY).getLabel ();
	
		            // Update the environment map
		            String rx = Double.toString (pose.getX ());
		            String ry = Double.toString (pose.getY ());
		            
		            InsertNodeCmd insertNodeCmd = getModels().getEnvMapModel().getCommandFactory ().insertNodeCmd (n, na, nb,
		                    rx, ry, "false");
		            log ("Inserting node '" + n + "' at (" + rx + ", " + ry + ") between " + na + " and "
		                    + nb);
	
		            SetExecutionFailedCmd resetFailedCmd = getModels().getInstructionGraphModel().getCommandFactory ()
		                    .setExecutionFailedCmd ("false");
		            
	
		            // Send the commands -- different models, so can't bundle them
		            m_modelUSPort.updateModel (resetFailedCmd);
		            m_modelUSPort.updateModel (insertNodeCmd);
		        }
		    }

}
