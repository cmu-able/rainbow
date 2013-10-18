package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelUpdater;
import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.IESEBListener;

public class ESEBModelManagerModelUpdatePort implements ESEBConstants, IModelUSBusPort {

    private IRainbowReportingPort LOGGER = new ESEBMasterReportingPort ();

    private IModelUpdater m_mm;
    private ESEBConnector m_role;

    public ESEBModelManagerModelUpdatePort (IModelUpdater mm) throws IOException {
        m_mm = mm;
        // Runs on master

        String delegateHost = ESEBProvider.getESEBClientHost ();
        Short port = ESEBProvider.getESEBClientPort ();
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

                    String commandName = (String )msg.getProperty (COMMAND_NAME_KEY);
                    try {
                        IModelInstance model = getModelInstance (modelType, modelName);
                        if (model != null) {
                            IRainbowModelCommandRepresentation command = model.getCommandFactory ().generateCommand (
                                    commandName, params.toArray (new String[0]));
                            updateModel (command);
                        }
                        else
                            throw new RainbowModelException (MessageFormat.format ("Could not find the referred model ''{0}'':''{1}''.", modelName,
                                    modelType));
                    }
                    catch (Throwable e) {
                        LOGGER.error (RainbowComponentT.MODEL, MessageFormat.format (
                                "Could not form the command ''{0}'' from the ESEB message",
                                commandName), e);
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
