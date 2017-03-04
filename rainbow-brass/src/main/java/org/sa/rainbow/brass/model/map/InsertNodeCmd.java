package org.sa.rainbow.brass.model.map;

import java.util.List;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.ports.IRainbowMessageFactory;

public class InsertNodeCmd extends AbstractRainbowModelOperation<EnvMap.NodeInsertion, EnvMap> {

    private final String m_n;
    private final String m_na;
    private final String m_nb;
    private final float m_x;
    private final float m_y;
    private boolean      m_blocked;

    public InsertNodeCmd (EnvMapModelInstance model, String target, String n, String na, String nb, String x, String y,
            String blocked) {
        super ("insertNode", model, target, n, na, nb, x, y, blocked);
        m_n = n;
        m_na = na;
        m_nb = nb;
        m_x = Float.parseFloat(x);
        m_y = Float.parseFloat(y);
        m_blocked = Boolean.parseBoolean (blocked);
    }

    @Override
    public EnvMap.NodeInsertion getResult () throws IllegalStateException {
        return getModelContext ().getModelInstance().getNodeInsertionResult();
    }

    @Override
    protected List<? extends IRainbowMessage> getGeneratedEvents (IRainbowMessageFactory messageFactory) {
        return generateEvents (messageFactory, "insertNode");
    }

    @Override
    protected void subExecute () throws RainbowException {
        getModelContext ().getModelInstance ().insertNode (m_n, m_na, m_nb, m_x, m_y, m_blocked);
    }

    @Override
    protected void subRedo () throws RainbowException {
        getModelContext ().getModelInstance ().insertNode (m_n, m_na, m_nb, m_x, m_y, m_blocked);
    }

    @Override
    protected void subUndo () throws RainbowException {
        // To be implemented
    }

    @Override
    protected boolean checkModelValidForCommand (EnvMap envMap) {
        return envMap == getModelContext ().getModelInstance ();
    }

}
