package org.sa.rainbow.brass.model.clock;

import java.util.Collections;
import java.util.List;

import org.sa.rainbow.brass.model.mission.MissionState;
import org.sa.rainbow.brass.model.mission.MissionStateModelInstance;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class SetCurrentTimeCmd extends AbstractRainbowModelOperation<Double, Clock> {

    private double m_time;

    public SetCurrentTimeCmd (ClockModelInstance model, String target, String wp) {
        super ("setCurrentTime", model, target, wp);
        m_time = Double.parseDouble (wp);
    }

    @Override
    public Double getResult () throws IllegalStateException {
        return m_time;
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return Collections.<IRainbowMessage> emptyList (); // Let's not send events for this
    }

    @Override
    protected void subExecute () throws RainbowException {
        getModelContext ().getModelInstance ().setCurrentTime (m_time);

    }

    @Override
    protected void subRedo () throws RainbowException {


    }

    @Override
    protected void subUndo () throws RainbowException {
        // TODO Auto-generated method stub

    }

    @Override
    protected boolean checkModelValidForCommand (Clock model) {
        return true;
    }

}
