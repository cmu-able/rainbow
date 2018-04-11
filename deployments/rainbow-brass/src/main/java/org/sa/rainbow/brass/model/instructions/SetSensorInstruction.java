package org.sa.rainbow.brass.model.instructions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState.Sensors;

public class SetSensorInstruction implements IInstruction {
	public static final String COMMAND_NAME = "SetSensor";

	
	private String m_label;
	private String m_instruction;
	private String m_nextLabel;
	
	private Sensors m_sensor;
	private boolean m_enablement;

	public SetSensorInstruction(String label, String instruction, String nextLabel) {
		m_label = label;
		m_instruction = instruction;
		m_nextLabel = nextLabel;
		
		parseSensorInformation();
	}
	
	private void parseSensorInformation() {
		Pattern sensorPattern = Pattern.compile("SetSensor\\((.+),(.+)\\)");
		Matcher m = sensorPattern.matcher(m_instruction);
		if (m.matches()) {
			m_sensor = Sensors.valueOf(m.group(1));
			m_enablement = Boolean.getBoolean(m.group(2));
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
		SetSensorInstruction ss = new SetSensorInstruction(m_label, m_instruction, m_nextLabel);
		ss.m_sensor = m_sensor;
		ss.m_enablement = m_enablement;
		return ss;
	}
	
	public Sensors getSensor() {return m_sensor;}
	
	public boolean getEnablement() {return m_enablement;}

}
