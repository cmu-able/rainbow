package org.sa.rainbow.brass.model.instructions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetLocalizationFidelityInstruction implements IInstruction {
	public static final String COMMAND_NAME = "SetLocalizationFidelity";
	
	private String m_label;
	private String m_instruction; // SetLocalizationFidelity(f)
	private String m_nextLabel;
	
	public static enum LocalizationFidelity {
		LOW, MEDIUM, HIGH;
	}
	
	private LocalizationFidelity m_fidelity;

	public SetLocalizationFidelityInstruction(String label, String instruction, String nextLabel) {
		m_label = label;
		m_instruction = instruction;
		m_nextLabel = nextLabel;
		parseFidelityLevel();
	}
	
	@Override
	public String getInstructionLabel() {
		return m_label;
	}

	@Override
	public String getInstruction() {
		return m_instruction;
	}

	@Override
	public String getNextInstructionLabel() {
		return m_nextLabel;
	}

	@Override
	public IInstruction copy() {
		String label = new String(m_label);
    	String instruction = new String(m_instruction);
    	String nextLabel = new String(m_nextLabel);
    	
    	SetLocalizationFidelityInstruction i = new SetLocalizationFidelityInstruction(label, instruction, nextLabel);
    	i.m_fidelity = m_fidelity;
        return i;
	}
	
	public LocalizationFidelity getLocalizationFidelity() {
		return m_fidelity;
	}
	
	private void parseFidelityLevel() {
		Pattern setLocFidelityPattern = Pattern.compile ("SetLocalizationFidelity\\(([0-9]+)\\)");
        Matcher m = setLocFidelityPattern.matcher (m_instruction);
        if (m.matches ()) {
            int fidelityLevel = Integer.parseInt (m.group (1));
            m_fidelity = LocalizationFidelity.values()[fidelityLevel];
        }
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		
		SetLocalizationFidelityInstruction inst = (SetLocalizationFidelityInstruction) obj;
		return m_label.equals(inst.m_label)
				&& m_instruction.equals(inst.m_instruction)
				&& m_nextLabel.equals(inst.m_nextLabel);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + m_label.hashCode();
		result = prime * result + m_instruction.hashCode();
		result = prime * result + m_nextLabel.hashCode();
		return result;
	}

}
