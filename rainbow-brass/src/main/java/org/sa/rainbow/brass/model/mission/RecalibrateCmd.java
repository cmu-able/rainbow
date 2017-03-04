package org.sa.rainbow.brass.model.mission;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class RecalibrateCmd extends AbstractRainbowModelOperation<Boolean, MissionState> {

    private boolean m_bad;

    public RecalibrateCmd (MissionStateModelInstance modelInstance, String target, String bad) {
        super ("recalibrate", modelInstance, target, bad);
        m_bad = Boolean.parseBoolean (bad);
    }

    @Override
    public Boolean getResult () throws IllegalStateException {
        return m_bad;
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, "recalibrate");
    }

    @Override
    protected void subExecute () throws RainbowException {
        getModelContext ().getModelInstance ().setBadlyCalibrated (m_bad);
    }

    @Override
    protected void subRedo () throws RainbowException {
        subExecute ();
    }

    @Override
    protected void subUndo () throws RainbowException {

    }

    @Override
    protected boolean checkModelValidForCommand (MissionState model) {
        return true;
    }

}
