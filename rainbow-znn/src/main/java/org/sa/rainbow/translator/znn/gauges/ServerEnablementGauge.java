package org.sa.rainbow.translator.znn.gauges;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.model.acme.AcmeModelInstance;

/**
 * This gauge reports on enablement/disablement of servers. If the setup parameter "mode" is "enabled", then the
 * commands issued will be enabling/disabling servers by property. If the "mode" is "new" then the commands issues will
 * delete/add new servers to the model.
 * 
 * Note that this gauge needs access to the mode, through the model port. For now, it should be run on the same machine
 * as the rainbow master.
 * 
 * @author Bradley Schmerl: schmerl
 *
 */
public class ServerEnablementGauge extends RegularPatternGauge {

    private static final String ENABLE_MODE = "enable";
    private static final String MODE_PARAM  = "mode";
    private static final String OFF         = "f";
    private static final String ON          = "o";
    public static final String  NAME        = "G - Server Enablement";
    public static final String  DEFAULT     = "DEFAULT";
    public static final Pattern PATTERN     = Pattern
            .compile ("(([o,f]) ((?:\\d{1,3}\\.){3}\\d{1,3}:\\d{1,4}))"
/*"(([o,f]) ((?:\\d{1,3}\\.){3}\\d{1,3}:\\d{1,4}))(?:\\s(([o,f]) ((?:\\d{1,3}\\.){3}\\d{1,3}:\\d{1,4})))*"*/);

    private IModelsManagerPort  m_modelsPort;
    private AcmeModelInstance   m_model;

    public ServerEnablementGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings) throws RainbowException {
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
                if (ON.equals (enablement)) {
                    enabled.add (ip);
                }
                else if (OFF.equals (enablement)) {
                    disabled.add (ip);
                }
            }

            if (m_configParams.get (MODE_PARAM).equals (ENABLE_MODE)) {
                IRainbowOperation op = m_commands.get ("enableServer(x)");
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
            else {
                IRainbowOperation addOp = m_commands.get ("connectServer(x)");
                IRainbowOperation delOp = m_commands.get ("removeServer(x)");
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
        }

    }

    private AcmeModelInstance getModel () {
        if (m_model == null) {
            m_model = (AcmeModelInstance )m_modelsPort.<IAcmeSystem> getModelInstance (m_modelDesc.getType (),
                    m_modelDesc.getName ());
        }
        return m_model;
    }

    private String getServerForIP (String ip) {
        IAcmeComponent server = getModel ().getElementForLocation (ip, "ServerT");
        if (server != null) return server.getQualifiedName ();
        return null;
    }

    @Override
    protected void initProperty (String name, Object value) {
        // TODO Auto-generated method stub

    }

}