package org.sa.rainbow.brass.model.acme;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.model.acme.AcmeModelInstance;

public class RosModelUpdateOperatorsImpl extends AcmeModelInstance {

    private RosModelCommandFactory m_commandFactory;

    public RosModelUpdateOperatorsImpl (IAcmeSystem system, String source) {
        super (system, source);
    }

    @Override
    protected AcmeModelInstance generateInstance (IAcmeSystem sys) {
        return new RosModelUpdateOperatorsImpl (sys, getOriginalSource ());
    }

    @Override
    public RosModelCommandFactory getCommandFactory () {
        if (m_commandFactory == null) {
            m_commandFactory = new RosModelCommandFactory (this);
        }
        return m_commandFactory;
    }

}
