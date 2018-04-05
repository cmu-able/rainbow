package org.sa.rainbow.brass.model.p2_cp3.mission;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class SetTargetWaypointCmd extends AbstractRainbowModelOperation<String, MissionState> {

    private String m_wp;

    public SetTargetWaypointCmd (MissionStateModelInstance model, String target, String wp) {
        super ("setTargetWaypoint", model, target, wp);
        m_wp = wp;
    }

    @Override
    public String getResult () throws IllegalStateException {
        return m_wp;
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, "setTargetWaypoint");
    }

    @Override
    protected void subExecute () throws RainbowException {
        getModelContext ().getModelInstance ().setTargetWaypoint (m_wp);
    }

    @Override
    protected void subRedo () throws RainbowException {
        subExecute ();
    }

    @Override
    protected void subUndo () throws RainbowException {
        // TODO Auto-generated method stub

    }

    @Override
    protected boolean checkModelValidForCommand (MissionState model) {
        return true;
    }

}
