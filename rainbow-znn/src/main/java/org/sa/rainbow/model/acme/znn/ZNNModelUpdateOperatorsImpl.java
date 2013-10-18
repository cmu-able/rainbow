package org.sa.rainbow.model.acme.znn;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.znn.commands.ZNNCommandFactory;

public class ZNNModelUpdateOperatorsImpl extends AcmeModelInstance {

    private ZNNCommandFactory m_commandFactory;

    public ZNNModelUpdateOperatorsImpl (IAcmeSystem system, String source) {
        super (system, source);
        // Make sure it is the right family
    }

    @Override
    public ZNNCommandFactory getCommandFactory () {
        if (m_commandFactory == null) {
            m_commandFactory = new ZNNCommandFactory (this);
        }
        return m_commandFactory;
    }

    @Override
    protected AcmeModelInstance generateInstance (IAcmeSystem sys) {
        return new ZNNModelUpdateOperatorsImpl (sys, getOriginalSource ());
    }


}
