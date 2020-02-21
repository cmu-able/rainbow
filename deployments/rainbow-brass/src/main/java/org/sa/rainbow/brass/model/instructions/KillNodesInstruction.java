package org.sa.rainbow.brass.model.instructions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KillNodesInstruction implements IInstruction {
	public static final String COMMAND_NAME = "KillNodes";
	private String m_label;
	private String m_instruction;
	private String m_nextLabel;
	private String m_node;

	public KillNodesInstruction(String label, String instruction, String nextLabel) {
		m_label = label;
		m_instruction = instruction;
		m_nextLabel = nextLabel;
		
		parseInformation();
	}
	
	private void parseInformation() {
		Pattern startPattern = Pattern.compile(COMMAND_NAME+"\\s*\\((.+)\\)");
		Matcher m = startPattern.matcher(m_instruction.trim());
		if (m.matches()) {
			m_node = extractContents(m.group(1));
		}
	}
	
	private String extractContents(String s) {
		if (s.startsWith("%") || s.startsWith("\""))
			s = s.substring(1);
		if (s.endsWith("%") || s.endsWith("\""))
			s = s.substring(0, s.length()-1);
		return s;
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
		return new KillNodesInstruction(m_label, m_instruction, m_nextLabel);
	}

	public String getNode() {
		return m_node;
	}

}
