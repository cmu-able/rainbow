package org.sa.rainbow.brass.model.p2_cp3.mission;

import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

/**
 * Created by schmerl on 12/27/2016.
 */
public class MissionStateModelInstance implements IModelInstance<MissionState> {

    public static final String MISSION_STATE_TYPE = "MissionState";
    private MissionState m_missionState;
    private MissionCommandFactory m_commandFactory;
    private String m_source;

    public MissionStateModelInstance (MissionState s, String source) {
        setModelInstance (s);
        setOriginalSource (source);
    }

    @Override
    public MissionState getModelInstance () {
        return m_missionState;
    }

    @Override
    public void setModelInstance (MissionState model) {
        m_missionState = model;
    }

    @Override
    public IModelInstance<MissionState> copyModelInstance (String newName) throws RainbowCopyException {
        return new MissionStateModelInstance (getModelInstance ().copy (), getOriginalSource ());
    }

    @Override
    public String getModelType () {
        return MISSION_STATE_TYPE;
    }

    @Override
    public String getModelName () {
        return getModelInstance ().getModelReference ().getModelName ();
    }

    @Override
    public MissionCommandFactory getCommandFactory () throws RainbowException {
        if (m_commandFactory == null) {
            m_commandFactory = new MissionCommandFactory (this);
        }
        return m_commandFactory;
    }

    @Override
    public void setOriginalSource (String source) {
        m_source = source;
    }

    @Override
    public String getOriginalSource () {
        return m_source;
    }

    @Override
    public void dispose () throws RainbowException {

    }
}
