package org.sa.rainbow.brass.model.mission;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

import java.util.List;

/**
 * Created by schmerl on 12/27/2016.
 */
public class SetCurrentLocationCmd extends AbstractRainbowModelOperation<MissionState.LocationRecording, MissionState> {
    private final double m_x;
    private final double m_y;

    public SetCurrentLocationCmd (MissionStateModelInstance model, String target, String x,
                                  String y) {
        super ("setCurrentLocation", model, target, x, y);
        m_x = Double.parseDouble (x);
        m_y = Double.parseDouble (y);
    }

    @Override
    public MissionState.LocationRecording getResult () throws IllegalStateException {
        return getModelContext ().getModelInstance ().getCurrentLocation ();
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, "setCurrentLocation");
    }

    @Override
    protected void subExecute () throws RainbowException {
        getModelContext ().getModelInstance ().setCurrentLocation (m_x, m_y);
    }

    @Override
    protected void subRedo () throws RainbowException {
        getModelContext ().getModelInstance ().setCurrentLocation (m_x, m_y);
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
