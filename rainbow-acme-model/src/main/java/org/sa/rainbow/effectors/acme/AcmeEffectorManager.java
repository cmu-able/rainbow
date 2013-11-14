package org.sa.rainbow.effectors.acme;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeElementInstance;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.translator.effectors.EffectorManager;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.util.Util;

/**
 * An effector manager that deals with operations intended for Acme models. It assumes: 1. That commands are Acme
 * commands 2. That the target of a command refers to an Acme object
 * 
 * It manages the calling of effectors associated with the command,
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public class AcmeEffectorManager extends EffectorManager {

    public AcmeEffectorManager () {
        super ("Acme Global Effector Manager");
    }

    @Override
    public OperationResult publishOperation (IRainbowOperation cmd) {
        OperationResult badResult = new OperationResult ();
        badResult.result = Result.UNKNOWN;
        if (cmd.getModelType ().equals ("Acme")) {
            AcmeModelInstance ami = (AcmeModelInstance )Rainbow.instance ().getRainbowMaster ().modelsManager ()
                    .<IAcmeSystem> getModelInstance (cmd.getModelType (), cmd.getModelName ());
            if (ami == null) {
                String errMsg = MessageFormat.format ("Could not find the model reference ''{0}'' for command {1}",
                        Util.genModelRef (cmd.getModelName (), cmd.getModelType ()), cmd.getName ());
                m_reportingPort.error (getComponentType (), errMsg);
                badResult.reply = errMsg;
                return badResult;
            }
            try {
                Object object = ami.resolveInModel (cmd.getTarget (), Object.class);
                if (object instanceof IAcmeElementInstance) {
                    IAcmeElementInstance instance = (IAcmeElementInstance )object;
                    String location = ami.getElementLocation (instance);
                    if (location != null) {
                        // look for an effector registered at this location that understands the command
                        cmd = resolveElementReferences (cmd, ami);
                        Set<EffectorAttributes> effectors = getEffectorsAtLocation (location);
                        effectors.addAll (getEffectorsInterestedInLocation (location, m_effectors.effectors));
                        filterEffectorsBasedOnCommandName (cmd, effectors);
                        filterEffectorsBasedOnCommandParameters (cmd, effectors);
                        if (!effectors.isEmpty ()) {
                            OperationResult result = new OperationResult ();
                            result.result = Result.SUCCESS;
                            StringBuffer errMsg = new StringBuffer ();
                            for (EffectorAttributes ea : effectors) {
                                Outcome outcome = executeEffector (ea.name, ea.location, cmd.getParameters ());
                                if (outcome != Outcome.SUCCESS) {
                                    errMsg.append (MessageFormat.format (
                                            "E[{0}@{1}]: Failed to execute command {2} - ", ea.name, location,
                                            Arrays.toString (cmd.getParameters ())));

                                    switch (outcome) {
                                    case CONFOUNDED:
                                        errMsg.append ("CONFOUNDED");
                                        result.result = Result.FAILURE;
                                        break;
                                    case TIMEOUT:
                                        errMsg.append ("TIMED OUT");
                                        result.result = Result.FAILURE;
                                        break;
                                    case UNKNOWN:
                                        errMsg.append ("UNKNOWN");
                                        if (result.result != Result.FAILURE) {
                                            result.result = Result.UNKNOWN;
                                        }
                                        break;
                                    }
                                    errMsg.append ("\n");
                                }
                            }
                            if (result.result == Result.FAILURE) {
                                result.reply = errMsg.toString ();
                            }
                            return result;
                        }
                        else {
                            badResult.reply = MessageFormat.format ("No effectors at {0} understand the command {1}",
                                    location, cmd.getName ());
                            return badResult;
                        }
                    }
                }
            }
            catch (Exception e) {
                badResult.reply = e.getMessage ();
                return badResult;
            }

        }
        else {
            badResult.reply = "Currently, I only know how to effect Acme models";
            return badResult;
        }
        return badResult;

    }



    private IRainbowOperation resolveElementReferences (IRainbowOperation cmd, AcmeModelInstance ami) {
        String target = cmd.getTarget ();
        target = resolveAcmeReference (target, ami);
        String[] args = cmd.getParameters ();
        for (int i = 0; i < args.length; i++) {
            if (!"".equals (args[i])) {
                args[i] = resolveAcmeReference (args[i], ami);
            }
        }
        OperationRepresentation or = new OperationRepresentation (cmd.getName (), cmd.getModelName (),
                cmd.getModelType (), target, args);
        return or;

    }

    String resolveAcmeReference (String target, AcmeModelInstance ami) {
        try {
            Object modelObject = ami.resolveInModel (target, Object.class);
            if (modelObject instanceof IAcmeElementInstance) {
                String location = ami.getElementLocation ((IAcmeElementInstance<?, ?> )modelObject);
                if (location != null) {
                    target = location;
                }
            }
            else if (modelObject instanceof IAcmeProperty) {
                IAcmeProperty prop = (IAcmeProperty )modelObject;
                if (prop.getValue () != null) {
                    target = ModelHelper.propertyValueAsString (prop.getValue ());
                }
            }
        }
        catch (RainbowModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
        }
        return target;
    }

    private Collection<? extends EffectorAttributes> getEffectorsInterestedInLocation (String location,
            Set<EffectorAttributes> effectors) {
        Set<EffectorAttributes> interestedIn = new HashSet<> ();
        for (EffectorAttributes ea : effectors) {
            if (ea.commandPattern != null && location.equals (ea.commandPattern.getTarget ())) {
                interestedIn.add (ea);
            }
        }
        return interestedIn;
    }

    private void filterEffectorsBasedOnCommandName (IRainbowOperation cmd,
            Set<EffectorAttributes> effectors) {
        for (Iterator iterator = effectors.iterator (); iterator.hasNext ();) {
            EffectorAttributes ea = (EffectorAttributes )iterator.next ();
            if (ea.commandPattern == null || !ea.commandPattern.getName ().equals (cmd.getName ())) {
                iterator.remove ();
            }
        }
    }

    private void filterEffectorsBasedOnCommandParameters (IRainbowOperation cmd, Set<EffectorAttributes> effectors) {
        for (Iterator iterator = effectors.iterator (); iterator.hasNext ();) {
            EffectorAttributes ea = (EffectorAttributes )iterator.next ();
            OperationRepresentation commandPattern = ea.commandPattern;
            if (commandPattern != null) {
                String target = commandPattern.getTarget ();
                int idxStart = 0;
                if (target == null /*|| !target.equals (ea.location)*/) {
                    target = commandPattern.getParameters ()[0];
                    idxStart = 1;
                }
                if (target != null && !target.contains ("$<")) {
                    if (!target.equals (cmd.getTarget ())) {
                        iterator.remove ();
                        break;
                    }
                }
                String[] parameters = commandPattern.getParameters ();
                if (parameters != null) {
                    boolean removed = false;
                    for (int i = idxStart; i < parameters.length && !removed; i++) {
                        if (!parameters[i].contains ("$<"))
                            if (i - idxStart < cmd.getParameters ().length
                                    && !parameters[i].equals (cmd.getParameters ()[i - idxStart])) {
                                iterator.remove ();
                                break;
                            }
                    }
                }
            }
        }

    }
}
