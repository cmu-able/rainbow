package org.sa.rainbow.brass.model.instructions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetReconfiguringInstruction implements IInstruction {
	public static final String COMMAND_NAME = "SetReconfiguring";
	private String m_label;
	private String m_instruction;
	private String m_nextLabel;
	private boolean m_reconfiguring;

	public SetReconfiguringInstruction(String label, String instruction, String nextLabel) {
		m_label = label;
		m_instruction = instruction;
		m_nextLabel = nextLabel;
		
		praseInformation();
	}

	public boolean isReconfiguring() {
		return m_reconfiguring;
	}

	private void praseInformation() {
		Pattern p = Pattern.compile(COMMAND_NAME+"\\s*\\((.+)\\s*\\)");
		Matcher m = p.matcher(m_instruction);
		if (m.matches()) {
			m_reconfiguring = Double.parseDouble(m.group(1).trim()) != 0;
		}
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
		return new SetReconfiguringInstruction(m_label, m_instruction, m_nextLabel);
	}

}
