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
package org.sa.rainbow.util;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.IRainbowMaster;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.gauges.GaugeDescription;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.gauges.GaugeTypeDescription;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.EffectorDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.ProbeDescription;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;
import org.sa.rainbow.translator.probes.IProbe.Kind;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;

public class RainbowConfigurationChecker implements IRainbowReportingPort {

    public enum ProblemT {
        WARNING, ERROR
    }

    public class Problem {

        public Problem (ProblemT p, String msg) {
            problem = p;
            this.msg = msg;
        }

        public Problem () {
        }

        public ProblemT problem;
        public String   msg;
    }

    final List<Problem> m_problems = new LinkedList<> ();
    private final IRainbowMaster m_master;
    final Set<String> m_referredToProbes = new HashSet<> ();

    public RainbowConfigurationChecker (IRainbowMaster master) {
        m_master = master;
    }

    public void checkRainbowConfiguration () {
        checkGaugeConfiguration ();
        checkProbeConfiguration ();
        checkEffectorConfiguration ();
//        checkPreferencesConfiguration ();
    }

    private void checkPreferencesConfiguration () {
//        UtilityPreferenceDescription preferenceDesc = m_master.preferenceDesc ();
//        Pattern p = Pattern.compile ("(?:\\[(.*)\\])?(.*)");
//        for (Entry<String, UtilityAttributes> av : preferenceDesc.utilities.entrySet ()) {
//            UtilityAttributes value = av.getValue ();
//            Matcher matcher = p.matcher (value.mapping);
//            if (matcher.matches ()) {
//                String type = matcher.group (1);
//                String expr = matcher.group (2);
//                switch (type) {
//                case "[EXPR]":
//                    break;
//                case 
//                }
//            }
//
//        }
    }

    private void checkEffectorConfiguration () {
        EffectorDescription effectorDesc = m_master.effectorDesc ();
        for (EffectorAttributes effector : effectorDesc.effectors) {
            checkEffector (effector);
        }
    }

    private void checkEffector (EffectorAttributes effector) {

        if (effector.effectorType != null) {
            if (m_master.effectorDesc ().effectorTypes.get (effector.effectorType.name) == null) {
                m_problems.add (new Problem (ProblemT.ERROR,
                        MessageFormat.format ("{0}: Refers to an effector type that does not exist: {1}", effector.name,
                                effector.effectorType.name)));
            }
        }

        if (effector.getLocation() == null || "".equals (effector.getLocation())) {
            m_problems.add (new Problem (ProblemT.ERROR, MessageFormat.format ("{0}: Does not have a location",
                    effector.name)));
        }

        if (effector.getCommandPattern () == null) {
            m_problems.add (new Problem (ProblemT.ERROR, MessageFormat.format (
                    "{0}: does not have a command and so cannot be called.", effector.name)));
        }

        if (effector.getKind () == IEffectorIdentifier.Kind.JAVA) {
            String effClass = effector.getInfo().get ("class");
            if (effClass == null) {
                m_problems.add (new Problem (ProblemT.ERROR, MessageFormat.format (
                        "{0}: Is a JAVA effector without a 'class' attribute", effector.name)));
            }
            else {
                try {
                    Class.forName (effClass);
                }
                catch (ClassNotFoundException e) {
                    m_problems.add (new Problem (ProblemT.WARNING, MessageFormat.format (
                            "{0}: Cannot find the class ''{1}'' for the effector", effector.name, effClass)));
                }
            }
        }
        else if (effector.getKind () == IEffectorIdentifier.Kind.SCRIPT) {
            String path = effector.getInfo().get ("path");
            if (path == null) {
                m_problems.add (new Problem (ProblemT.ERROR, MessageFormat.format (
                        "{0}: Is a SCRIPT effector without a 'path' attribute", effector.name)));
            }

        }
    }

    private void checkProbeConfiguration () {
        ProbeDescription probeDesc = m_master.probeDesc ();
        for (ProbeDescription.ProbeAttributes probe : probeDesc.probes) {
            checkProbe (probe);
        }
    }

    private void checkProbe (ProbeAttributes probe) {
        if (probe.alias == null || "".equals (probe.alias)) {
            m_problems.add (new Problem (ProblemT.ERROR, MessageFormat.format ("{0}: Does not have an alias",
                    probe.name)));
        }
        else {
            if (!m_referredToProbes.contains (probe.alias)) {
                m_problems.add (new Problem (ProblemT.WARNING, MessageFormat.format (
                        "{0}: The alias ''{1}'' is not referred to by any gauges.", probe.name, probe.alias)));
            }
        }

        if (probe.getLocation() == null || "".equals (probe.getLocation())) {
            m_problems.add (new Problem (ProblemT.ERROR, MessageFormat.format ("{0}: Does not have a location",
                    probe.name)));
        }

        if (probe.getLocation() != null && probe.getLocation().startsWith ("$")) {
            m_problems.add (new Problem (ProblemT.ERROR, MessageFormat.format (
                    "{0}: Has an unexpanded location ''{1}''", probe.name, probe.getLocation())));
        }

        if (probe.kind == Kind.JAVA) {
            String probeClazz = probe.getInfo().get ("class");
            if (probeClazz == null) {
                m_problems.add (new Problem (ProblemT.ERROR, MessageFormat.format (
                        "{0}: Is a JAVA probe without a 'class' attribute", probe.name)));
            }
            else {
                try {
                    this.getClass ().forName (probeClazz);
                }
                catch (ClassNotFoundException e) {
                    m_problems.add (new Problem (ProblemT.WARNING, MessageFormat.format (
                            "{0}: Cannot find the class ''{1}'' for the probe", probe.name, probeClazz)));
                }
            }
        }
        else if (probe.kind == Kind.SCRIPT) {
            String path = probe.getInfo().get ("path");
            if (path == null) {
                m_problems.add (new Problem (ProblemT.ERROR, MessageFormat.format (
                        "{0}: Is a SCRIPT probe without a 'path' attribute", probe.name)));
            }

        }


    }

    protected void checkGaugeConfiguration () {
        GaugeDescription gaugeDesc = m_master.gaugeDesc ();
        Collection<GaugeInstanceDescription> instSpecs = gaugeDesc.instSpec.values ();

        for (GaugeTypeDescription gtd : m_master.gaugeDesc ().typeSpec.values ()) {
            checkGaugeType (gtd);
        }

        for (GaugeInstanceDescription gid : instSpecs) {
            checkGaugeConsistent (gid);
        }
    }

    private void checkGaugeType (GaugeTypeDescription gtd) {
        checkSetupParam (gtd, "targetIP");
        checkSetupParam (gtd, "beaconPeriod");
    }

    void checkSetupParam (GaugeTypeDescription gtd, String param) {
        if (gtd.findSetupParam (param) == null) {
            Problem p = new Problem ();
            p.problem = ProblemT.ERROR;
            p.msg = MessageFormat.format ("{0}: does not specify a setup param ''{1}''.", gtd.gaugeType (), param);
            m_problems.add (p);
        }
    }

    private void checkGaugeConsistent (GaugeInstanceDescription gid) {
        // Errors
        // Check if gauge type exsits in gauge spec
        GaugeTypeDescription type = m_master.gaugeDesc ().typeSpec.get (gid.gaugeType ());
        if (type == null) {
            Problem p = new Problem ();
            p.problem = ProblemT.ERROR;
            p.msg = MessageFormat.format ("{0}: The gauge type ''{1}'' is unknown", gid.gaugeName (), gid.gaugeType ());
            m_problems.add (p);
        }
        // Check if model exists in Rainbow
        if (gid.modelDesc () == null || gid.modelDesc ().getName () == null || gid.modelDesc ().getType () == null) {
            Problem p = new Problem ();
            p.problem = ProblemT.ERROR;
            if (gid.modelDesc () == null) {
                p.msg = MessageFormat.format ("{0}: There is no model that the gauge is associated with",
                        gid.gaugeName ());
            }
            else {
                p.msg = MessageFormat.format (
                        "{0}: Neither the model name nor model type can be null: name=''{1}'', type=''{2}''",
                        gid.gaugeName (), gid.modelDesc ().getName (), gid.modelDesc ().getType ());
            }
            m_problems.add (p);
        }
        else {
            IModelInstance<Object> modelInstance = m_master.modelsManager ().getModelInstance (
                    new ModelReference (gid.modelDesc ().getName (), gid.modelDesc ().getType ()));
            if (modelInstance == null) {
                Problem p = new Problem ();
                p.problem = ProblemT.ERROR;
                p.msg = MessageFormat.format ("{0}: The model ''{1}:{2}'' is unknown.", gid.gaugeName (), gid
                        .modelDesc ().getName (), gid.modelDesc ().getType ());
                m_problems.add (p);
                return;
            }
            // Check if command exists in model

            List<Pair<String, OperationRepresentation>> commandSignatures = gid.commandSignatures ();
            Set<String> commandsFromType = new HashSet<> ();
            ModelCommandFactory<Object> cf = modelInstance.getCommandFactory ();
            for (Pair<String, OperationRepresentation> pair : commandSignatures) {
                String commandName = pair.secondValue ().getName ();
                try {
                    commandsFromType.add (commandName);
                    if (!findCommand (cf, commandName)) {
                        Problem p = new Problem ();
                        p.problem = ProblemT.ERROR;
                        p.msg = MessageFormat
                                .format (
                                        "{0}: Has a command that can''t be found in ''{1}:{2}''s command factory: {3}",
                                        gid.gaugeName (), gid.modelDesc ().getName (), gid.modelDesc ().getType (),
                                        commandName);
                        m_problems.add (p);
                    }
                }
                catch (RainbowModelException e) {
                    if (e.getCause () instanceof NoSuchMethodException || e.getCause () instanceof SecurityException) {
                        Problem p = new Problem ();
                        p.problem = ProblemT.ERROR;
                        p.msg = MessageFormat.format (
                                "{0}: Has a command that can''t be found in ''{1}:{2}''s command factory: {3}", gid
                                .gaugeName (), gid.modelDesc ().getName (), gid.modelDesc ().getType (), commandName);
                        m_problems.add (p);
                    }
                }
            }
            Collection<OperationRepresentation> mappings = gid.mappings ().values ();
            for (OperationRepresentation command : mappings) {
                boolean remove = commandsFromType.remove (command.getName ());
                if (!remove) {
                    Problem p = new Problem ();
                    p.problem = ProblemT.WARNING;
                    p.msg = MessageFormat.format (
                            "{0}: Specifiies the command ''{1}'' that is not referenced in the type ''{2}",
                            gid.gaugeName (), command.getName (), gid.gaugeType ());
                    m_problems.add (p);
                }
                try {
                    if (!findCommand (cf, command.getName ())) {
                        Problem p = new Problem ();
                        p.problem = ProblemT.ERROR;
                        p.msg = MessageFormat.format (
                                "{0}: Has a command that can''t be found in ''{1}:{2}''s command factory: {3}",
                                gid.gaugeName (), gid.modelDesc ().getName (), gid.modelDesc ().getType (),
                                command.getName ());
                        m_problems.add (p);
                    }

                }
                catch (RainbowModelException e) {
                    if (e.getCause () instanceof NoSuchMethodException || e.getCause () instanceof SecurityException) {
                        Problem p = new Problem ();
                        p.problem = ProblemT.ERROR;
                        p.msg = MessageFormat.format (
                                "{0}: Has a command that can''t be found in ''{1}:{2}''s command factory: {3}",
                                gid.gaugeName (), gid.modelDesc ().getName (), gid.modelDesc ().getType (),
                                command.getName ());
                        m_problems.add (p);
                    }
                }
            }
            if (!commandsFromType.isEmpty ()) {
                Problem p = new Problem ();
                p.problem = ProblemT.WARNING;
                StringBuilder cmd = new StringBuilder ();
                for (String c : commandsFromType) {
                    cmd.append (c);
                    cmd.append (", ");
                }
                cmd.delete (cmd.length () - 1, cmd.length ());
                p.msg = MessageFormat.format ("{0}: Does not refer to the following commands defined in the type: {1}",
                        gid.gaugeName (), cmd.toString ());
                m_problems.add (p);
            }

        }


        // Check if probe exists in probe desc
        TypedAttributeWithValue probe;
        if ((probe = gid.findConfigParam ("targetProbeType")) != null
                || (probe = gid.findConfigParam ("targetProbeList")) != null) {
            String[] probes = ((String )probe.getValue ()).split (",");
            for (String probe2 : probes) {
                probe2 = Util.decomposeID (probe2).firstValue ();
                m_referredToProbes.add (probe2);
                SortedSet<ProbeAttributes> probeDescs = m_master.probeDesc ().probes;
                boolean found = false;
                for (Iterator it = probeDescs.iterator (); it.hasNext () && !found;) {
                    ProbeAttributes pa  = (ProbeAttributes )it.next ();
                    found = probe2.equals (pa.alias);
                }
                if (!found) {
                    Problem p = new Problem ();
                    p.problem = ProblemT.ERROR;
                    p.msg = MessageFormat.format ("{0}: Refers to a probe ''{1}'' that is not found.",
                            gid.gaugeName (), probe2);
                    m_problems.add (p);
                }
            }
        }

        // Warnings
        // Check if class can be found and has a constructor
        TypedAttributeWithValue cls = gid.findSetupParam ("javaClass");
        if (cls == null) {
            Problem p = new Problem ();
            p.problem = ProblemT.WARNING;
            p.msg = MessageFormat.format ("{0}: does not have a ''javaClass'' setup parameter.", gid.gaugeName ());
            m_problems.add (p);

        }
        else {
            String className = (String )cls.getValue ();
            try {
                Class clazz = getClass ().forName (className);
                Class<?>[] paramTypes = new Class[6];
                paramTypes[0] = String.class;
                paramTypes[1] = long.class;
                paramTypes[2] = TypedAttribute.class;
                paramTypes[3] = TypedAttribute.class;
                paramTypes[4] = List.class;
                paramTypes[5] = Map.class;
                Constructor constructor = clazz.getConstructor (paramTypes);

            }
            catch (ClassNotFoundException e) {
                Problem p = new Problem ();
                p.problem = ProblemT.WARNING;
                p.msg = MessageFormat.format ("{0}: refers to a class ''{1}'' that cannot be found on the class path.",
                        gid.gaugeName (), className);
                m_problems.add (p);
            } catch (NoSuchMethodException | SecurityException e) {
                Problem p = new Problem ();
                p.problem = ProblemT.ERROR;
                p.msg = MessageFormat.format ("{0}: The class ''{1}'' does not seem to have a valid constructor.",
                        gid.gaugeName (), className);
                m_problems.add (p);
            }
        }

        // Check if all setupParams are found in type, and all configParams are found in type
        for (TypedAttributeWithValue s : gid.setupParams ()) {
            if (m_master.gaugeDesc ().typeSpec.get (gid.gaugeType ()).findSetupParam (s.getName ()) == null) {
                Problem p = new Problem ();
                p.problem = ProblemT.WARNING;
                p.msg = MessageFormat.format ("{0}: has a setup parameter ''{1}'' that is not declared in the type",
                        gid.gaugeName (), s.getName ());
                m_problems.add (p);
            }
        }
        for (TypedAttributeWithValue s : gid.configParams ()) {
            if (m_master.gaugeDesc ().typeSpec.get (gid.gaugeType ()).findConfigParam (s.getName ()) == null) {
                Problem p = new Problem ();
                p.problem = ProblemT.WARNING;
                p.msg = MessageFormat.format ("{0}: has a config parameter ''{1}'' that is not declared in the type",
                        gid.gaugeName (), s.getName ());
                m_problems.add (p);
            }
        }

    }

    protected boolean findCommand (ModelCommandFactory<?> cf, String commandName) throws RainbowModelException {
        Method[] methods = cf.getClass ().getMethods ();
        Method method = null;
        boolean found = false;
        for (int i = 0; i < methods.length && !found; i++) {
            if (methods[i].getName ().toLowerCase ().startsWith (commandName.toLowerCase ())) {
                method = methods[i];
                found = true;
            }
        }
        return found;
    }


    public Collection<Problem> getProblems () {
        return m_problems;
    }

    @Override
    public void fatal (RainbowComponentT type, String msg, Throwable e, Logger logger) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        PrintStream ps = new PrintStream (baos);
        e.printStackTrace (ps);
        ps.close ();
        fatal (type, MessageFormat.format ("{0}.\nException: {1}\n{2}", msg, e.getMessage (), baos.toString ()));

    }

    @Override
    public void fatal (RainbowComponentT type, String msg, Logger logger) {
        fatal (type, msg);
    }

    @Override
    public void fatal (RainbowComponentT type, String msg, Throwable e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        PrintStream ps = new PrintStream (baos);
        e.printStackTrace (ps);
        ps.close ();
        fatal (type, MessageFormat.format ("{0}.\nException: {1}\n{2}", msg, e.getMessage (), baos.toString ()));
    }

    @Override
    public void fatal (RainbowComponentT type, String msg) {
        Problem p = new Problem (ProblemT.ERROR, msg);
        m_problems.add (p);
    }

    @Override
    public void error (RainbowComponentT type, String msg, Throwable e, Logger logger) {
        error (type, msg, e);
    }

    @Override
    public void error (RainbowComponentT type, String msg, Logger logger) {
        error (type, msg);
    }

    @Override
    public void error (RainbowComponentT type, String msg, Throwable e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        PrintStream ps = new PrintStream (baos);
        e.printStackTrace (ps);
        ps.close ();
        error (type, MessageFormat.format ("{0}.\nException: {1}\n{2}", msg, e.getMessage (), baos.toString ()));
    }

    @Override
    public void error (RainbowComponentT type, String msg) {
        Problem p = new Problem (ProblemT.ERROR, msg);
        m_problems.add (p);
    }

    @Override
    public void warn (RainbowComponentT type, String msg, Throwable e, Logger logger) {
        warn (type, msg, e);
    }

    @Override
    public void warn (RainbowComponentT type, String msg, Logger logger) {
        warn (type, msg);
    }

    @Override
    public void warn (RainbowComponentT type, String msg, Throwable e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        PrintStream ps = new PrintStream (baos);
        e.printStackTrace (ps);
        ps.close ();
        warn (type, MessageFormat.format ("{0}.\nException: {1}\n{2}", msg, e.getMessage (), baos.toString ()));

    }

    @Override
    public void warn (RainbowComponentT type, String msg) {
        Problem p = new Problem (ProblemT.WARNING, msg);
        m_problems.add (p);
    }

    @Override
    public void info (RainbowComponentT type, String msg, Logger logger) {

    }

    @Override
    public void info (RainbowComponentT type, String msg) {

    }

    @Override
    public void trace (RainbowComponentT type, String msg) {

    }

    @Override
    public void dispose () {
        // TODO Auto-generated method stub

    }

}
