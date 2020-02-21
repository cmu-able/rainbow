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
package org.sa.rainbow.translator.znn.gauges;

import org.acmestudio.acme.core.type.IAcmeBooleanValue;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.model.acme.AcmeModelInstance;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This gauge reports on enablement/disablement of servers. If the setup parameter "mode" is "enabled", then the
 * commands issued will be enabling/disabling servers by property. If the "mode" is "new" then the commands issues will
 * delete/add new servers to the model.
 * <p/>
 * Note that this gauge needs access to the mode, through the model port. For now, it should be run on the same machine
 * as the rainbow master.
 *
 * @author Bradley Schmerl: schmerl
 */
public class ServerEnablementGauge extends RegularPatternGauge {

    /** Uses existing back up servers as new servers, and just enables them in the model **/
    private static final String  ENABLE_MODE = "enable";
    /** Finds disconnected servers in the model and connects them to the right load balancer and db **/
    private static final String  CONNECT_MODE = "connect";
    /** adding a server means a new server was created, so issue the create server command **/
    private static final String CREATE_MODE = "create";

    private static final String  MODE_PARAM  = "mode";
    private static final String  OFF         = "f";
    private static final String  ON          = "o";
    private static final String  INIT_ON     = "i";
    public static final  String  NAME        = "G - Server Enablement";
    public static final  String  DEFAULT     = "DEFAULT";
    public static final  Pattern PATTERN     = Pattern
            .compile ("(([o,f,i]) ((?:\\d{1,3}\\.){3}\\d{1,3}:\\d{1,4}))"
/*"(([o,f]) ((?:\\d{1,3}\\.){3}\\d{1,3}:\\d{1,4}))(?:\\s(([o,f]) ((?:\\d{1,3}\\.){3}\\d{1,3}:\\d{1,4})))*"*/);

    private IModelsManagerPort m_modelsPort;
    private AcmeModelInstance  m_model;

    public ServerEnablementGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
                                  List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings)
            throws RainbowException {
        super (NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
        addPattern (DEFAULT, PATTERN);
        m_modelsPort = RainbowPortFactory.createModelsManagerRequirerPort ();
    }

    @Override
    protected void doMatch (String matchName, Matcher m) {
        if (matchName == DEFAULT) {
            Set<String> enabled = new HashSet<> ();
            Set<String> disabled = new HashSet<> ();
            int groupCount = m.groupCount ();
            int c = groupCount / 3;
//            String group = m.group (0);
//            group = m.group (1);
//            group = m.group (2);
//            group = m.group (3);
            for (int i = 1; i < c + 1; i++) {
                String enablement = m.group (i * 3 - 1);
                String ip = m.group (i * 3);
                if (ON.equals (enablement) || INIT_ON.equals (enablement)) {
                    enabled.add (ip);
                } else if (OFF.equals (enablement)) {
                    disabled.add (ip);
                }
            }

            switch ((String )m_configParams.get (MODE_PARAM).getValue ()) {
                case ENABLE_MODE:
                    enableOrDisableServers (enabled, disabled);
                    break;
                case CONNECT_MODE:
                    connectOrRemoveServers (enabled, disabled);
                    break;
                case CREATE_MODE:
                    createOrRemoveServers (enabled, disabled);
                    break;

                }
            }
        }

    private void createOrRemoveServers (Set<String> enabled, Set<String> disabled) {
        IRainbowOperation addOp = getCommand ("newServer");
        IRainbowOperation delOp = getCommand ("removeServer");
        IRainbowOperation connOp = getCommand ("connectServer");
        List<IRainbowOperation> ops = new ArrayList<> (enabled.size () + disabled.size ());
        List<Map<String, String>> params = new ArrayList<> (ops.size ());
        for (String ip : enabled) {
            String [] location = ip.split (":");
            final IAcmeComponent server = getServerCompForIP (location[0]);
            if (server != null) {
                if (((IAcmeBooleanValue )server.getProperty ("isArchEnabled").getValue ()).getValue ())
                    continue;
                ops.add (connOp);
                Map<String, String> p = new HashMap<> ();
                p.put (connOp.getParameters ()[0], server.getQualifiedName ());
                p.put (connOp.getParameters ()[1], location[0]);
                p.put (connOp.getParameters ()[2], location[1]);
                params.add (p);
            }
            else {
                ops.add (addOp);
                Map<String, String> p = new HashMap<> ();
                p.put (addOp.getParameters ()[0], "Server");
                p.put (addOp.getParameters ()[1], location[0]);
                p.put (addOp.getParameters ()[2], location[1]);
                params.add (p);
            }
        }
        for (String ip : disabled) {
            ops.add (delOp);
            Map<String, String> p = new HashMap<> ();
            p.put (delOp.getTarget (), getServerForIP (ip));
            params.add (p);
        }
        if (ops.size () > 0)
            issueCommands(ops, params);
    }

    private void connectOrRemoveServers (Set<String> enabled, Set<String> disabled) {
        IRainbowOperation addOp = getCommand ("connectServer");
        IRainbowOperation delOp = getCommand ("removeServer");
        List<IRainbowOperation> ops = new ArrayList<> (enabled.size () + disabled.size ());
        List<Map<String, String>> params = new ArrayList<> (ops.size ());
        for (String ip : enabled) {
            String[] location = ip.split (":");

            ops.add (addOp);
            Map<String, String> p = new HashMap<> ();
//                    p.put (addOp.getParameter, getServerForIP (ip));
            p.put (addOp.getParameters ()[1], location[0]);
            p.put (addOp.getParameters ()[2], location[1]);
            params.add (p);
        }
        for (String ip : disabled) {
            ops.add (delOp);
            Map<String, String> p = new HashMap<> ();
            p.put (delOp.getTarget (), getServerForIP (ip));
            params.add (p);
        }
        log ("-----> Server Enablement Gauge reporting commands");
        issueCommands (ops, params);
    }

    private void enableOrDisableServers (Set<String> enabled, Set<String> disabled) {
        IRainbowOperation op = getCommand ("enableServer");
        List<IRainbowOperation> ops = new ArrayList<> (enabled.size () + disabled.size ());
        List<Map<String, String>> params = new ArrayList<> (ops.size ());
        for (String ip : enabled) {
            ops.add (op);
            Map<String, String> p = new HashMap<> ();
            p.put (op.getParameters ()[0], getServerForIP (ip));
            p.put (op.getParameters ()[1], "true");
            p.put (op.getTarget (), "LB0");
            params.add (p);
        }
        for (String ip : disabled) {
            ops.add (op);
            Map<String, String> p = new HashMap<> ();
            p.put (op.getParameters ()[0], getServerForIP (ip));
            p.put (op.getParameters ()[1], "false");
            p.put (op.getTarget (), "LB0");
            params.add (p);
        }
        issueCommands (ops, params);
    }



    private AcmeModelInstance getModel () {
        if (m_model == null) {
            m_model = (AcmeModelInstance) m_modelsPort.<IAcmeSystem>getModelInstance (new ModelReference (m_modelDesc
                                                                                                                  .getName (), m_modelDesc.getType ()));
        }
        return m_model;
    }

    private String getServerForIP (String ip) {
        IAcmeComponent server = getModel ().getElementForLocation (ip, "ServerT");
        if (server != null) return server.getQualifiedName ();
        return null;
    }

    private IAcmeComponent getServerCompForIP (String ip) {
        IAcmeComponent server = getModel ().getElementForLocation (ip, "ServerT");
        return server;
    }


}
