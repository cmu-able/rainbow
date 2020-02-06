package org.sa.rainbow.brass.model.mission;

import java.util.List;

import org.sa.rainbow.brass.model.mission.MissionState.Heading;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

/**
 * Created by schmerl on 12/27/2016.
 */
public class SetCurrentLocationCmd extends AbstractRainbowModelOperation<MissionState.LocationRecording, MissionState> {
    private final double m_x;
    private final double m_y;
    private double       m_w;
    private Heading      m_heading;

    public SetCurrentLocationCmd (String commandName, MissionStateModelInstance model, String target, String x,
            String y, String w) {
        super (commandName, model, target, x, y, w);
        m_x = Double.parseDouble (x);
        m_y = Double.parseDouble (y);
        m_w = Double.parseDouble (w);
    }

    @Override
    public MissionState.LocationRecording getResult () throws IllegalStateException {
        return getModelContext ().getModelInstance ().getCurrentPose ();
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, getName());
    }

    @Override
    protected void subExecute () throws RainbowException {
        getModelContext ().getModelInstance ().setCurrentPose (m_x, m_y, m_w);
    }

    @Override
    protected void subRedo () throws RainbowException {
        getModelContext ().getModelInstance ().setCurrentPose (m_x, m_y, m_w);
    }

    @Override
    protected void subUndo () throws RainbowException {
        getModelContext ().getModelInstance ().m_locationHistory.pop ();
    }

    @Override
    protected boolean checkModelValidForCommand (MissionState missionState) {
        return missionState == getModelContext
                ().getModelInstance ();
    }
}
