package org.sa.rainbow.brass.model.instructions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForwardInstruction implements IInstruction {
    public static final String COMMAND_NAME = "Forward";

    private String m_label;
    private String m_instruction; // Forward(d, v)
    private String m_nextLabel;

    private double m_distance;
    private double m_speed;

    public ForwardInstruction(String label, String instruction, String nextLabel) {
        m_label = label;
        m_instruction = instruction;
        m_nextLabel = nextLabel;
        parseForwardDistanceSpeed();
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

        ForwardInstruction i = new ForwardInstruction(label, instruction, nextLabel);
        i.m_distance = m_distance;
        i.m_speed = m_speed;
        return i;
    }

    public double getDistance() {
        return m_distance;
    }

    public double getSpeed() {
        return m_speed;
    }

    private void parseForwardDistanceSpeed() {
        Pattern forwardPattern = Pattern.compile ("Forward\\(([0-9.]+), ?([0-9.]+)\\)");
        Matcher m = forwardPattern.matcher (m_instruction);
        if (m.matches ()) {
            m_distance = Double.parseDouble (m.group (1));
            m_speed = Double.parseDouble (m.group (2));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj == null || obj.getClass() != this.getClass()) return false;

        ForwardInstruction forward = (ForwardInstruction) obj;
        return m_label.equals(forward.m_label)
                && m_instruction.equals(forward.m_instruction)
                && m_nextLabel.equals(forward.m_nextLabel);
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
