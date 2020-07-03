package org.sa.rainbow.brass.model.instructions;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChargeInstruction implements IInstruction {
	public static final String COMMAND_NAME = "Charge";
	
	private String m_label;
	private String m_instruction; // Charge(t)
	private String m_nextLabel;
	
	private double m_chargingTime;

	public ChargeInstruction(String label, String instruction, String nextLabel) {
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
	public String toString() {
		return MessageFormat.format("{0} -> Charge({1})", m_instruction, m_chargingTime);
	}

	@Override
	public IInstruction copy() {
		String label = new String(m_label);
    	String instruction = new String(m_instruction);
    	String nextLabel = new String(m_nextLabel);
    	
    	ChargeInstruction i = new ChargeInstruction(label, instruction, nextLabel);
    	i.m_chargingTime = m_chargingTime;
        return i;
	}
	
	public double getChargingTime() {
		return m_chargingTime;
	}
	
	private void parseFidelityLevel() {
		Pattern chargePattern = Pattern.compile ("Charge\\s*\\(\\s*([0-9.]+)\\s*\\)");
        Matcher m = chargePattern.matcher (m_instruction);
        if (m.matches ()) {
            m_chargingTime = Double.parseDouble(m.group (1));
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
		
		ChargeInstruction inst = (ChargeInstruction) obj;
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
