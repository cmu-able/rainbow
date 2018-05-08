package org.sa.rainbow.brass.model.instructions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartNodesInstruction implements IInstruction {
	public static final String COMMAND_NAME = "StartNodes";
	private String m_label;
	private String m_instruction;
	private String m_nextLabel;
	private String m_node;

	public StartNodesInstruction(String label, String instruction, String nextLabel) {
		m_label = label;
		m_instruction = instruction;
		m_nextLabel = nextLabel;
		
		parseInformation ();
	}

	private void parseInformation() {
		Pattern startPattern = Pattern.compile(COMMAND_NAME+"\\s*\\((.+)\\)");
		Matcher m = startPattern.matcher(m_instruction);
		if (m.matches()) {
			m_node = m.group(1);
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
		return new StartNodesInstruction(m_label, m_instruction, m_nextLabel);
	}

	public String getNode() {
		return m_node;
	}

}
