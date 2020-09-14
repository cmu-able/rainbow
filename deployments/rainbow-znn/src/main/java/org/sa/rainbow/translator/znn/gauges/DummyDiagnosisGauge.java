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

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.gauges.RegularPatternGauge;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DummyDiagnosisGauge extends RegularPatternGauge {

    IModelsManagerPort m_modelPort;

    private final class CaptchaWatcher implements IRainbowModelChangeCallback {
        @Override
        public void onEvent (ModelReference mr, IRainbowMessage message) {
            Object property = message.getProperty (IModelChangeBusPort.PARAMETER_PROP + "0");
            Object target = message.getProperty (IModelChangeBusPort.TARGET_PROP);
            if (target instanceof String && property instanceof String) {
                Boolean captchaEnabled = Boolean.valueOf ((String )property);
                IModelInstance<IAcmeSystem> model = m_modelPort.getModelInstance (mr);
                IAcmeSystem system = model.getModelInstance ();
                Set<? extends IAcmeComponent> components = system.getComponents ();
                Set<IAcmeComponent> maliciousComponents = new HashSet<> ();
                for (IAcmeComponent c : components) {
                    if (c.declaresType ("ClientT") && c.getProperty ("maliciousness") != null) {
                        IAcmeProperty m = c.getProperty ("maliciousness");
                        float maliciousness = (float )PropertyHelper.toJavaVal (m.getValue ());
                        if (maliciousness >= 0.9) {
                            maliciousComponents.add (c);
                        }
                    }
                }
                getReportingPort ().info (RainbowComponentT.GAUGE, "Dummy gauge responding to captcha enablement");
                for (IAcmeComponent m : maliciousComponents) {
                    Map<String, String> pm = new HashMap<> ();
                    List<IRainbowOperation> ops = new ArrayList<> (2);
                    List<Map<String, String>> params = new ArrayList<> (2);
                    IRainbowOperation cmd = getCommand (valueNames[1]);
                    pm = new HashMap<> ();
                    pm.put (cmd.getParameters ()[0], captchaEnabled ? "-1" : "0");
                    pm.put (cmd.getTarget (), m.getQualifiedName ());
                    ops.add (cmd);
                    params.add (pm);

                    cmd = getCommand (valueNames[2]);
                    pm = new HashMap<> ();
                    pm.put (cmd.getParameters ()[0], captchaEnabled ? "-1" : "0");
                    pm.put (cmd.getTarget (), m.getQualifiedName ());
                    ops.add (cmd);
                    params.add (pm);

                    issueCommands (ops, params);
                }
            }
        }
    }

    private static final String   NAME                    = "G - Dummy Diagnosis Gauge";
    private static final String   DEFAULT                 = "default";
    private static final String[] valueNames              = { "maliciousness(x)", "captcha(x)", "authenticate(x)" };
    private static final String   AUTHENTICATION_ON       = "AUTH_ON";
    private static final String   AUTHENTICATION_OFF      = "AUTH_OFF";

    IModelChangeBusSubscriberPort m_modelChanges;
    private boolean               m_authenticationEnabled = false;

    public DummyDiagnosisGauge (String id, long beaconPeriod, TypedAttribute gaugeDesc, TypedAttribute modelDesc,
            List<TypedAttributeWithValue> setupParams, Map<String, IRainbowOperation> mappings) throws RainbowException {
        super (NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);

        addPattern (DEFAULT, Pattern.compile ("([\\w_]+)=([\\d]+(\\.[\\d]*))"));
        addPattern (AUTHENTICATION_ON, Pattern.compile ("^on$"));
        addPattern (AUTHENTICATION_OFF, Pattern.compile ("^off$"));

        m_modelPort = RainbowPortFactory.createModelsManagerRequirerPort ();

        m_modelChanges = RainbowPortFactory.createModelChangeBusSubscriptionPort ();

        m_modelChanges.subscribe (new IRainbowChangeBusSubscription () {

            @Override
            public boolean matches (IRainbowMessage message) {
                return message.getPropertyNames ().contains (IModelChangeBusPort.COMMAND_PROP)
                        && message.getProperty (IModelChangeBusPort.COMMAND_PROP).equals ("setCaptchaEnabled");
            }

        }, new CaptchaWatcher ());

    }

    @Override
    protected void doMatch (String matchName, Matcher m) {
        if (DEFAULT.equals (matchName)) {
            String LB = m.group (1);
            IRainbowOperation cmd = getCommand (valueNames[0]);
            Map<String, String> pm = new HashMap<> ();
            pm.put (cmd.getParameters ()[0], m.group (2));
            pm.put (cmd.getTarget (), LB);
            issueCommand (cmd, pm);

            // Issue the command that sets the captcha and authentication properties, in lieu of an actual gauge to do this
            // ClientX.maliciousness >= 0.9 -> ClientX.captcha = ClientX.authenticate = -1
            // ClientX.maliciousness >= 0.5 -> ClientX.captcha = ClientX.authentica = rand ()
            String mal = m.group (2);
            float maliciousness = Float.valueOf (mal);
//            if (maliciousness >= 0.9f) {
//                boolean captchaEnabled = false;
//                ModelsManager modelsManager = Rainbow.instance ().getRainbowMaster ().modelsManager ();
//                IModelInstance<IAcmeSystem> modelInstance = modelsManager.<IAcmeSystem> getModelInstance (modelDesc ()
//                        .getType (), modelDesc ().getName ());
//                if (modelInstance != null) {
//                    IAcmeProperty cProp = modelInstance.getModelInstance ().getComponent ("LB0")
//                            .getProperty ("captchaEnabled");
//                    if (cProp != null && cProp.getValue () != null
//                            && PropertyHelper.toJavaVal (cProp.getValue ()) == Boolean.TRUE) {
//                        captchaEnabled = true;
//                    }
//                }
//                if (captchaEnabled) {
//                    List<IRainbowOperation> ops = new ArrayList<> (2);
//                    List<Map<String, String>> params = new ArrayList<> (2);
//                    cmd = m_commands.get (valueNames[1]);
//                    pm = new HashMap<> ();
//                    pm.put (cmd.getParameters ()[0], "-1");
//                    pm.put (cmd.getTarget (), LB);
//                    ops.add (cmd);
//                    params.add (pm);
//
//                    cmd = m_commands.get (valueNames[2]);
//                    pm = new HashMap<> ();
//                    pm.put (cmd.getParameters ()[0], "-1");
//                    pm.put (cmd.getTarget (), LB);
//                    ops.add (cmd);
//                    params.add (pm);
//
//                    issueCommands (ops, params);
//                }
//            }
//            else if (maliciousness >= 0.5f) {
//
//                if (m_authenticationEnabled) {
//
//                    List<IRainbowOperation> ops = new ArrayList<> (2);
//                    List<Map<String, String>> params = new ArrayList<> (2);
//                    cmd = m_commands.get (valueNames[1]);
//                    pm = new HashMap<> ();
//                    String response = "-1";
//                    if (Math.random () < 0.5) {
//                        response = "1";
//                    }
//                    pm.put (cmd.getParameters ()[0], response);
//                    pm.put (cmd.getTarget (), LB);
//                    ops.add (cmd);
//                    params.add (pm);
//
//                    cmd = m_commands.get (valueNames[2]);
//                    pm = new HashMap<> ();
//                    response = "-1";
//                    if (Math.random () < 0.5) {
//                        response = "1";
//                    }
//                    pm.put (cmd.getParameters ()[0], response);
//                    pm.put (cmd.getTarget (), LB);
//                    ops.add (cmd);
//                    params.add (pm);
//
//                    issueCommands (ops, params);
//                }
//            }

//            String pClient = m_modelDesc.getName () + Util.DOT + m.group (1) + Util.DOT + "maliciousness";
//            eventHandler ().reportValue (new AttributeValueTriple (pClient, valueNames[0], m.group (2)));
        }
        else if (AUTHENTICATION_ON.equals (matchName)) {
            m_authenticationEnabled = true;
        }
        else if (AUTHENTICATION_OFF.equals (matchName)) {
            m_authenticationEnabled = false;
        }
    }


}
