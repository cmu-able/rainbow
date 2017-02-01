package org.sa.rainbow.brass.missiongui;

import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;

public interface IBRASSOperations {

    public void reportFromDAS (String string);

    public void setRobotLocation (Double x, Double y);

    public void setRobotObstructed (Boolean obstructed);

    public void newInstructionGraph (InstructionGraphProgress igModel);

    public void setExecutingInstruction (String label);

    public void setInstructionFailed (Boolean result);

    public void insertMapNode (String n, String na, String nb, Double x, Double y);

}
