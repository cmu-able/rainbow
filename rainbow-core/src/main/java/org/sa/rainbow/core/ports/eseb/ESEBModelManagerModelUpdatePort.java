/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.sa.rainbow.core.ports.eseb;


import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelUpdater;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.IESEBListener;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

public class ESEBModelManagerModelUpdatePort extends AbstractESEBDisposablePort implements ESEBConstants,
IModelUSBusPort {

    private final IRainbowReportingPort LOGGER = new ESEBMasterReportingPort ();

    private IModelUpdater m_mm;

    public ESEBModelManagerModelUpdatePort (IModelUpdater mm) throws IOException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (), ChannelT.MODEL_US);
        m_mm = mm;
        // Runs on master
        getConnectionRole().addListener (new IESEBListener() {

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
                        IModelInstance model = getModelInstance (new ModelReference (modelName, modelType));
                        if (model != null) {
                            IRainbowOperation command = model.getCommandFactory ().generateCommand (
                                    commandName, params.toArray (new String[params.size ()]));
                            if (msg.hasProperty (COMMAND_ORIGIN)) {
                                command.setOrigin ((String )msg.getProperty (COMMAND_ORIGIN));
                            }
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
                else if ((ESEBConstants.MSG_TYPE_UPDATE_MODEL + "_multi").equals (msgType)) {
                    int i = 0;
                    IRainbowOperation cmd;
                    List<IRainbowOperation> ops = new LinkedList<> ();
                    do {
                        cmd = ESEBCommandHelper.msgToCommand (msg, "_" + i + "_");
                        if (cmd != null) {
                            try {
                                IModelInstance model = getModelInstance (cmd.getModelReference ());
                                if (model != null) {
                                    String[] params = new String[cmd.getParameters ().length + 1];
                                    params[0] = cmd.getTarget ();
                                    for (int j = 0; j < cmd.getParameters ().length; j++) {
                                        params[j + 1] = cmd.getParameters ()[j];
                                    }
                                    IRainbowOperation command = model.getCommandFactory ().generateCommand (
                                            cmd.getName (), params);
                                    if (msg.hasProperty (COMMAND_ORIGIN)) {
                                        command.setOrigin ((String )msg.getProperty (COMMAND_ORIGIN));
                                    }
                                    ops.add (command);

                                }
                            }
                            catch (Throwable e) {
                                LOGGER.error (RainbowComponentT.MODEL, MessageFormat.format (
                                        "Could not form the command ''{0}'' from the ESEB message", cmd.getName ()), e);
                            }
                        }
                        i++;
                    } while (cmd != null);
                    boolean b = (Boolean )msg.getProperty (ESEBConstants.MSG_TRANSACTION);
                    updateModel (ops, b);

                }
            }
        });

    }

    @Override
    public void updateModel (IRainbowOperation command) {
        try {
            m_mm.requestModelUpdate (command);
        } catch (IllegalStateException | RainbowException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
    }

    @Override
    public void updateModel (List<IRainbowOperation> commands, boolean transaction) {
        try {
            m_mm.requestModelUpdate (commands, transaction);
        } catch (IllegalStateException | RainbowException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
    }

    @Override
    public IModelInstance getModelInstance (ModelReference modelRef) {
        return m_mm.getModelInstance (modelRef);
    }

}
