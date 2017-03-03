package org.sa.rainbow.brass.model.mission;

import java.util.List;

import org.sa.rainbow.brass.model.mission.MissionState.GroundPlaneError;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class SetGroundPlaneErrorCmd extends AbstractRainbowModelOperation<GroundPlaneError, MissionState> {

    private GroundPlaneError m_error;

    public SetGroundPlaneErrorCmd (MissionStateModelInstance model, String target, String t, String r) {
        super ("setGroundPlaneError", model, target, t, r);
        m_error = new GroundPlaneError ();
        m_error.translational_error = Double.parseDouble (t);
        m_error.rotational_error = Double.parseDouble (r);
    }

    @Override
    public GroundPlaneError getResult () throws IllegalStateException {
        return m_error;
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, "setGroundPlaneError");
    }

    @Override
    protected void subExecute () throws RainbowException {
        getModelContext ().getModelInstance ().addGroundPlaneSample (m_error);
    }

    @Override
    protected void subRedo () throws RainbowException {
        subExecute ();
    }

    @Override
    protected void subUndo () throws RainbowException {
        getModelContext ().getModelInstance ().m_groundPlaneErrorHistory.pop ();
    }

    @Override
    protected boolean checkModelValidForCommand (MissionState model) {
        return true;
    }

}
