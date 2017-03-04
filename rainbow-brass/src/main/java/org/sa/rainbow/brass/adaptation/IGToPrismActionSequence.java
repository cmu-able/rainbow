package org.sa.rainbow.brass.adaptation;

import java.util.ArrayList;
import java.util.List;

import org.sa.rainbow.brass.model.instructions.ChargeInstruction;
import org.sa.rainbow.brass.model.instructions.ForwardInstruction;
import org.sa.rainbow.brass.model.instructions.IInstruction;
import org.sa.rainbow.brass.model.instructions.MoveAbsHInstruction;
import org.sa.rainbow.brass.model.instructions.SetLocalizationFidelityInstruction;
import org.sa.rainbow.brass.model.instructions.SetLocalizationFidelityInstruction.LocalizationFidelity;
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
		EnvMapNode prev = null;
		double prevSpeed = 0;
		boolean hasPrevSpeed = false;
		
		double targetX = source.getX();
		double targetY = source.getY();
		
		for (IInstruction instruction : m_instructions) {
			
			if (instruction instanceof MoveAbsHInstruction) {
				// MoveAbsH(x, y, v, w)
				MoveAbsHInstruction moveAbsH = (MoveAbsHInstruction) instruction;
				targetX = moveAbsH.getTargetX();
				targetY = moveAbsH.getTargetY();
				
				EnvMapNode target = m_envMap.getNode(targetX, targetY);
				String moveAction = createMoveAction(source, target);
				
				// Check if speed is changed
				// If so, add set-speed action
				if (hasPrevSpeed && prevSpeed > moveAbsH.getSpeed()) {
					String lowerSpeedAction = "t_set_half_speed";
					actions.add(lowerSpeedAction);
				} else if (hasPrevSpeed && prevSpeed < moveAbsH.getSpeed()) {
					String higherSpeedAction = "t_set_full_speed";
					actions.add(higherSpeedAction);
				}
				
				actions.add(moveAction);
				
				// For next instruction
				prev = source;
				source = target;
				prevSpeed = moveAbsH.getSpeed();
				hasPrevSpeed = true;
			} else if (instruction instanceof ForwardInstruction) {
				// Forward(d, v)
				ForwardInstruction forward = (ForwardInstruction) instruction;
				
				// Assume that any 2 nodes on the map share an axis
				if (prev.getX() < source.getX()) {
					targetX = source.getX() + forward.getDistance();
					targetY = source.getY();
				} else if (prev.getX() > source.getX()) {
					targetX = source.getX() + forward.getDistance();
					targetY = source.getY();
				} else if (prev.getY() < source.getY()) {
					targetX = source.getX();
					targetY = source.getY() + forward.getDistance();
				} else if (prev.getY() > source.getY()) {
					targetX = source.getX();
					targetY = source.getY() - forward.getDistance();
				}
				
				EnvMapNode target = m_envMap.getNode(targetX, targetY);
				String moveAction = createMoveAction(source, target);
				
				// Check if speed is changed
				// If so, add set-speed action
				if (hasPrevSpeed && prevSpeed > forward.getSpeed()) {
					String lowerSpeedAction = "t_set_half_speed";
					actions.add(lowerSpeedAction);
				} else if (hasPrevSpeed && prevSpeed < forward.getSpeed()) {
					String higherSpeedAction = "t_set_full_speed";
					actions.add(higherSpeedAction);
				}
				
				actions.add(moveAction);
				
				// For next instruction
				prev = source;
				source = target;
				prevSpeed = forward.getSpeed();
				hasPrevSpeed = true;
			} else if (instruction instanceof SetLocalizationFidelityInstruction) {
				// SetLocalizationFidelity(f)
				SetLocalizationFidelityInstruction inst = (SetLocalizationFidelityInstruction) instruction;
				LocalizationFidelity fidelity = inst.getLocalizationFidelity();
				
				if (fidelity == LocalizationFidelity.LOW) {
					actions.add("t_set_loc_lo");
				} else if (fidelity == LocalizationFidelity.MEDIUM) {
					actions.add("t_set_loc_med");
				} else if (fidelity == LocalizationFidelity.HIGH) {
					actions.add("t_set_loc_hi");
				} else {
					actions.add("t_set_loc_lo");
				}
			} else if (instruction instanceof ChargeInstruction) {
				// Charge(t)
				actions.add("t_recharge");
			} else {
				actions.add("unknown");
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
	
	private String createMoveAction(EnvMapNode source, EnvMapNode target) {
		String sourceLabel = source.getLabel();
		String targetLabel = target.getLabel();
		String action = sourceLabel + "_to_" + targetLabel;
		return action;
	}
}
