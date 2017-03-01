package org.sa.rainbow.brass.adaptation;

import java.util.ArrayList;
import java.util.List;

import org.sa.rainbow.brass.model.instructions.IInstruction;
import org.sa.rainbow.brass.model.instructions.MoveAbsHInstruction;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapNode;

/**
 * Translates InstructionGraph to a PRISM action sequence.
 * @author rsukkerd
 *
 */
public class IGToPrismActionSequence {
	
	private EnvMap m_envMap;
	private List<IInstruction> m_instructions;
	private EnvMapNode m_sourceNode;
	private EnvMapNode m_targetNode;

	public IGToPrismActionSequence(EnvMap envMap, List<IInstruction> instructions, double sourceX, double sourceY) {
		m_envMap = envMap;
		m_instructions = instructions;
		m_sourceNode = m_envMap.getNode(sourceX, sourceY);
	}
	
	public List<String> translate() {
		List<String> actions = new ArrayList<String>();
		EnvMapNode source = m_sourceNode;
		
		double targetX = source.getX();
		double targetY = source.getY();
		
		for (IInstruction instruction : m_instructions) {
			if (instruction instanceof MoveAbsHInstruction) {
				// MoveAbsH instruction
				MoveAbsHInstruction moveAbsH = (MoveAbsHInstruction) instruction;
				targetX = moveAbsH.getTargetX();
				targetY = moveAbsH.getTargetY();
				EnvMapNode target = m_envMap.getNode(targetX, targetY);
				String sourceLabel = source.getLabel();
				String targetLabel = target.getLabel();
				String action = sourceLabel + "_to_" + targetLabel;
				actions.add(action);
				
				// For next instruction
				source = target;
			} else {
				//TODO
			}
		}
		
		m_targetNode = m_envMap.getNode(targetX, targetY);
		
		return actions;
	}
	
	public EnvMapNode getSourceNode() {
		return m_sourceNode;
	}
	
	public EnvMapNode getTargetNode() {
		return m_targetNode;
	}
}
