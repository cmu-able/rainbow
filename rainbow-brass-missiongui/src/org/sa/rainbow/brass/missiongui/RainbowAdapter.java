package org.sa.rainbow.brass.missiongui;

import java.util.EnumSet;

import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IMasterCommandPort;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort.IRainbowReportingSubscriberCallback;
import org.sa.rainbow.core.ports.RainbowPortFactory;

public class RainbowAdapter {
    private IModelChangeBusSubscriberPort   m_modelPort;
    private IRainbowReportingSubscriberPort m_reportingPort;
    private IBRASSOperations                m_brass;
    private IMasterCommandPort              m_master;

    public RainbowAdapter (IBRASSOperations op) throws RainbowConnectionException {
        m_brass = op;
        initConnections ();
        initSubscriptions ();
    }

    private void initConnections () throws RainbowConnectionException {
        m_master = RainbowPortFactory.createMasterCommandPort ();
        m_reportingPort = RainbowPortFactory.createReportingSubscriberPort (new IRainbowReportingSubscriberCallback () {

            @Override
            public void report (RainbowComponentT component, ReportType type, String message) {
                if (component == RainbowComponentT.ANALYSIS || component == RainbowComponentT.ADAPTATION_MANAGER) {
                    m_brass.reportFromDAS (component.toString () + ": " + message);
                }
            }
        });
        m_reportingPort.subscribe (EnumSet.of (RainbowComponentT.ADAPTATION_MANAGER, RainbowComponentT.ANALYSIS),
                EnumSet.allOf (ReportType.class));
        m_modelPort = RainbowPortFactory.createModelChangeBusSubscriptionPort ();
    }

    private void initSubscriptions () {
        m_modelPort.subscribe (m_setLocationsSubscription, new IRainbowModelChangeCallback () {

            @Override
            public void onEvent (ModelReference reference, IRainbowMessage message) {
                String command = (String )message.getProperty (IModelChangeBusPort.COMMAND_PROP);
                if (isSetCurrentLocation (command)) {
                    Double x = Double.valueOf ((String )message.getProperty (IModelChangeBusPort.PARAMETER_PROP + "0"));
                    Double y = Double.valueOf ((String )message.getProperty (IModelChangeBusPort.PARAMETER_PROP + "1"));
                    m_brass.setRobotLocation (x, y);
                }
                else if (isSetObstructed (command)) {
                    Boolean obstructed = Boolean
                            .valueOf ((String )message.getProperty (IModelChangeBusPort.PARAMETER_PROP + "0"));
                    m_brass.setRobotObstructed (obstructed);
                }
            }
        });
        m_modelPort.subscribe (m_newInstructionGraphSubscription, new IRainbowModelChangeCallback () {

            @Override
            public void onEvent (ModelReference reference, IRainbowMessage message) {
                String command = (String )message.getProperty (IModelChangeBusPort.COMMAND_PROP);
                if (isSetInstructions (command)) {
                    String ig = (String )message.getProperty (IModelChangeBusPort.PARAMETER_PROP + "0");
                    InstructionGraphProgress igModel = new InstructionGraphProgress (
                            new ModelReference ("local", InstructionGraphModelInstance.INSTRUCTION_GRAPH_TYPE));
                    igModel.setInstructions (InstructionGraphProgress.parseFromString (ig));
                    m_brass.newInstructionGraph (igModel);
                }
                else if (isSetExecutingInstruction (command)) {
                    String label = (String )message.getProperty (IModelChangeBusPort.PARAMETER_PROP + "0");
                    m_brass.setExecutingInstruction (label);
                }
                else if (isFailedInstruction (command)) {
                    Boolean result = Boolean
                            .valueOf ((String )message.getProperty (IModelChangeBusPort.PARAMETER_PROP + "0"));
                    m_brass.setInstructionFailed (result);
                }
            }
        });

        m_modelPort.subscribe (m_mapSubscription, new IRainbowModelChangeCallback () {

            @Override
            public void onEvent (ModelReference reference, IRainbowMessage message) {
                String command = (String )message.getProperty (IModelChangeBusPort.COMMAND_PROP);
                if (isInsertNode (command)) {
                    String n = (String )message.getProperty (IModelChangeBusPort.PARAMETER_PROP + "0");
                    String na = (String )message.getProperty (IModelChangeBusPort.PARAMETER_PROP + "1");
                    String nb = (String )message.getProperty (IModelChangeBusPort.PARAMETER_PROP + "2");
                    Double x = (Double )message.getProperty (IModelChangeBusPort.PARAMETER_PROP + "3");
                    Double y = (Double )message.getProperty (IModelChangeBusPort.PARAMETER_PROP + "4");
                    m_brass.insertMapNode (n, na, nb, x, y);
                }

            }
        });
    }

    public void dispose () {
        if (m_reportingPort != null) {
            m_reportingPort.dispose ();
        }
        if (m_modelPort != null) {
            m_modelPort.dispose ();
        }
    }

    boolean isSetInstructions (String command) {
        return "setInstructions".equals (command);
    }

    boolean isSetExecutingInstruction (String command) {
        return "setExecutingInstruction".equals (command);
    }

    boolean isFailedInstruction (String command) {
        return "setExecutionFailed".equals (command);
    }

    boolean isSetCurrentLocation (String command) {
        return "setCurrentLocation".equals (command);
    }

    boolean isSetObstructed (String command) {
        return "setRobotObstructed".equals (command);
    }

    boolean isInsertNode (String command) {
        return "insertNode".equals (command);
    }

    private IRainbowChangeBusSubscription m_setLocationsSubscription = new IRainbowChangeBusSubscription () {

        @Override
        public boolean matches (IRainbowMessage message) {
            String command = (String )message.getProperty (IModelChangeBusPort.COMMAND_PROP);
            return isSetCurrentLocation (command) || isSetObstructed (command);
        }
    };

    private IRainbowChangeBusSubscription m_newInstructionGraphSubscription = new IRainbowChangeBusSubscription () {

        @Override
        public boolean matches (IRainbowMessage message) {
            String command = (String )message.getProperty (IModelChangeBusPort.COMMAND_PROP);
            return isFailedInstruction (command) || isSetExecutingInstruction (command) || isSetInstructions (command);
        }
    };

    private IRainbowChangeBusSubscription m_mapSubscription = new IRainbowChangeBusSubscription () {

        @Override
        public boolean matches (IRainbowMessage message) {
            String command = (String )message.getProperty (IModelChangeBusPort.COMMAND_PROP);
            return isInsertNode (command);
        }

    };

}
