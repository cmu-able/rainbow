package org.sa.rainbow.model.acme.znn.commands;

import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.DefaultAcmeModel;
import org.acmestudio.acme.model.IAcmeCommandFactory;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.util.core.UMFloatingPointValue;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by schmerl on 9/10/2015.
 */
public class SetSystemPropertiesCmd extends ZNNAcmeModelCommand<IAcmeSystem> {


    public static final String AVERAGE_CLIENT_RESPONSE_TIME = "averageClientResponseTime";
    public static final String PERCENTAGE_MALICIOUS = "percentageMalicious";
    private String m_avgRt;
    private String m_maliciousness;
    private String m_aboveRT;
    private String m_aboveMal;

    public SetSystemPropertiesCmd (AcmeModelInstance model, String target, String avgRt, String maliciousness, String aboveRT, String aboveMal) {
        super ("setSystemProperties", model, target, avgRt, maliciousness, aboveRT, aboveMal);
        m_avgRt = avgRt;
        m_maliciousness = maliciousness;
        m_aboveRT = aboveRT;
        m_aboveMal = aboveMal;
    }

    @Override
    protected List<IAcmeCommand<?>> doConstructCommand () throws RainbowModelException {
        IAcmeSystem system = getModelContext ().getModelInstance ();

        List<IAcmeCommand<?>> cmds = new LinkedList<> ();
        IAcmeProperty rtProp = system.getProperty (AVERAGE_CLIENT_RESPONSE_TIME);
        IAcmeCommandFactory cf = system.getCommandFactory ();
        UMFloatingPointValue rtAcme = new UMFloatingPointValue (Float.valueOf (m_avgRt));
        if (rtProp == null) {
            cmds.add (cf.propertyCreateCommand (system, AVERAGE_CLIENT_RESPONSE_TIME, DefaultAcmeModel.defaultFloatType (), rtAcme));
        } else {
            cmds.add (cf.propertyValueSetCommand (rtProp, rtAcme));
        }

        IAcmeProperty malProp = system.getProperty (PERCENTAGE_MALICIOUS);
        UMFloatingPointValue malAcme = new UMFloatingPointValue (Float.valueOf (m_maliciousness));
        if (malProp == null) {
            cmds.add (cf.propertyCreateCommand (system, PERCENTAGE_MALICIOUS, DefaultAcmeModel.defaultFloatType (), malAcme));
        } else {
            cmds.add (cf.propertyValueSetCommand (malProp, malAcme));
        }
        return cmds;
    }

    @Override
    public IAcmeSystem getResult () throws IllegalStateException {
        return getModel ();
    }
}
