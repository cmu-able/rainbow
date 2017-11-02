package org.sa.rainbow.brass.model.mission;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.AbstractLoadModelCmd;

import java.io.InputStream;

import static org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance.INSTRUCTION_GRAPH_TYPE;

/**
 * Created by schmerl on 12/28/2016.
 */
public class MissionStateLoadCmd extends AbstractLoadModelCmd<MissionState> {
    private final String m_modelName;
    private final InputStream m_stream;
    private MissionStateModelInstance m_result;

    public MissionStateLoadCmd (IModelsManager mm, String resource, InputStream is, String source) {
        super ("loadMissionState", mm, resource, is, source);
        m_modelName = resource;
        m_stream = is;
    }

    @Override
    public IModelInstance<MissionState> getResult () throws IllegalStateException {
        return m_result;
    }
    public ModelReference getModelReference () {
        return new ModelReference (m_modelName, MissionStateModelInstance.MISSION_STATE_TYPE);
    }

    @Override
    protected void subExecute () throws RainbowException {
        if (m_stream == null) {
            MissionState m = new MissionState (getModelReference ());
            m_result = new MissionStateModelInstance (m, getOriginalSource ());
            doPostExecute ();
        }
    }

    @Override
    protected void subRedo () throws RainbowException {
        doPostExecute ();
    }

    @Override
    protected void subUndo () throws RainbowException {
        doPostUndo ();
    }

    @Override
    protected boolean checkModelValidForCommand (Object o) {
        return true;
    }
}
