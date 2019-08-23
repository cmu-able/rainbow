package org.sa.rainbow.brass.model.instructions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoveAbsHInstruction implements IInstruction {
    public static final String COMMAND_NAME = "MoveAbsH";

    private String m_label;
    private String m_instruction; // MoveAbsH(x, y, v, w)
    private String m_nextLabel;

    private double m_targetX;
    private double m_targetY;
    private double m_targetW;
    private double m_speed;
    private String m_srcWaypoint;
    private String m_tgtWaypoint;
    
    public MoveAbsHInstruction (String label, String instruction, String nextLabel) {
        m_label = label;
        m_instruction = instruction;
        m_nextLabel = nextLabel;
        parseMoveAbsHTargetPose ();
    }

    @Override
    public String getInstructionLabel () {
        return m_label;
    }

    @Override
    public String getInstruction () {
        return m_instruction;
    }

    @Override
    public String getNextInstructionLabel () {
        return m_nextLabel;
    }

    public double getTargetX () {
        return m_targetX;
    }

    public double getTargetY () {
        return m_targetY;
    }
    
    public String getTargetWaypoint() {
    	return m_tgtWaypoint;
    }
    
    public String getSourceWaypoint() {
    	return m_srcWaypoint;
    }
    
    public void setTargetWaypoint(String wp) {
    	m_tgtWaypoint = wp;
    }
    
    public void setSourceWaypoint(String wp) {
    	m_srcWaypoint = wp;
    }

    public double getSpeed () {
        return m_speed;
    }

    public double getTargetW () {
        return m_targetW;
    }

    @Override
    public MoveAbsHInstruction copy () {
        String label = new String (m_label);
        String instruction = new String (m_instruction);
        String nextLabel = new String (m_nextLabel);

        MoveAbsHInstruction i = new MoveAbsHInstruction (label, instruction, nextLabel);
        i.m_targetX = m_targetX;
        i.m_targetY = m_targetY;
        i.m_targetW = m_targetW;
        i.m_speed = m_speed;
        return i;
    }

    private void parseMoveAbsHTargetPose () {
        Pattern moveAbsHPattern = Pattern
                .compile ("MoveAbsH\\(([\\-0-9.]+),\\s*([\\-0-9.]+),\\s*([\\-0-9.]+),\\s*([\\-0-9.]+)\\)");
        Matcher m = moveAbsHPattern.matcher (m_instruction.trim());
        if (m.matches ()) {
            m_targetX = Double.parseDouble (m.group (1));
            m_targetY = Double.parseDouble (m.group (2));
            m_speed = Double.parseDouble (m.group (3));
            m_targetW = Double.parseDouble (m.group (4));
        }
        else {
            System.out.println ("Error matching instruction: " + m_instruction);
        }
    }

    @Override
    public boolean equals (Object obj) {
        if (obj == this) return true;

        if (obj == null || obj.getClass () != this.getClass ()) return false;

        MoveAbsHInstruction moveAbsH = (MoveAbsHInstruction )obj;
        return m_label.equals (moveAbsH.m_label) && m_instruction.equals (moveAbsH.m_instruction)
                && m_nextLabel.equals (moveAbsH.m_nextLabel);
    }

    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + m_label.hashCode ();
        result = prime * result + m_instruction.hashCode ();
        result = prime * result + m_nextLabel.hashCode ();
        return result;
    }

}
