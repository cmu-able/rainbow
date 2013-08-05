package org.sa.rainbow.models.ports.eseb;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.management.ports.eseb.ESEBConnector;
import org.sa.rainbow.management.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.management.ports.eseb.ESEBConnector.IESEBListener;
import org.sa.rainbow.management.ports.eseb.ESEBConstants;
import org.sa.rainbow.management.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.models.IModelInstance;
import org.sa.rainbow.models.IModelUpdater;
import org.sa.rainbow.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.models.ports.IRainbowModelUSBusPort;

public class ESEBModelManagerModelUpdatePort implements ESEBConstants, IRainbowModelUSBusPort {

    static Logger         LOGGER = Logger.getLogger (ESEBModelManagerModelUpdatePort.class);

    private IModelUpdater m_mm;
    private ESEBConnector m_role;

    public ESEBModelManagerModelUpdatePort (IModelUpdater mm) throws IOException {
        m_mm = mm;

        String delegateHost = Rainbow.properties ().getProperty (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_HOST,
                "localhost");
        String delegatePort = Rainbow.properties ().getProperty (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_PORT,
                Rainbow.properties ().getProperty (Rainbow.PROPKEY_DEPLOYMENT_LOCATION, "1234"));
        Short port = Short.valueOf (delegatePort);
        m_role = new ESEBConnector (delegateHost, port, ChannelT.MODEL_US);
        m_role.addListener (new IESEBListener() {

            @Override
            public void receive (RainbowESEBMessage msg) {
                String msgType = (String )msg.getProperty (ESEBConstants.MSG_TYPE_KEY);
                if (ESEBConstants.MSG_TYPE_UPDATE_MODEL.equals (msgType)) {
                    String modelType = (String )msg.getProperty (MODEL_TYPE_KEY);
                    String modelName = (String )msg.getProperty (MODEL_NAME_KEY);

                    List<String> params = new LinkedList<> ();
                    params.add ((String )msg.getProperty (COMMAND_TARGET_KEY));
                    int i = 0;
                    String p;
                    while ((p = (String )msg.getProperty (COMMAND_PARAMETER_KEY + i++)) != null) {
                        params.add (p);
                    }

                    try {
                        IModelInstance model = getModelInstance (modelType, modelName);
                        IRainbowModelCommandRepresentation command = model.getCommandFactory ().generateCommand (
                                (String )msg.getProperty (COMMAND_NAME_KEY), params.toArray (new String[0]));
                        updateModel (command);
                    }
                    catch (RainbowModelException e) {
                        LOGGER.error ("Could not form the command from the ESEB message", e);
                    }
                }
            }
        });

    }

    @Override
    public void updateModel (IRainbowModelCommandRepresentation command) {
        try {
            m_mm.requestModelUpdate (command);
        }
        catch (IllegalStateException | RainbowException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
    }

    @Override
    public void updateModel (List<IRainbowModelCommandRepresentation> commands, boolean transaction) {
        try {
            m_mm.requestModelUpdate (commands, transaction);
        }
        catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
        catch (RainbowException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
    }

    @Override
    public IModelInstance getModelInstance (String modelType, String modelName) {
        return m_mm.getModelInstance (modelType, modelName);
    }

}
