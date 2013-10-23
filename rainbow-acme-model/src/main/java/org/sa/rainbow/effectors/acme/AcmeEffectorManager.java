package org.sa.rainbow.effectors.acme;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Set;

import org.acmestudio.acme.element.IAcmeElementInstance;
import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;
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
    public OperationResult publishOperation (IRainbowModelCommandRepresentation cmd) {
        OperationResult badResult = new OperationResult ();
        badResult.result = Result.UNKNOWN;
        if (cmd.getModelType ().equals ("Acme")) {
            AcmeModelInstance ami = (AcmeModelInstance )Rainbow.instance ().getRainbowMaster ().modelsManager ()
                    .<IAcmeSystem> getModelInstance (cmd.getModelType (), cmd.getModelName ());
            if (ami == null) {
                String errMsg = MessageFormat.format ("Could not find the model reference ''{0}'' for command {1}",
                        Util.genModelRef (cmd.getModelName (), cmd.getModelType ()), cmd.getCommandName ());
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
                        Set<EffectorAttributes> effectors = getEffectorsAtLocation (location);
                        filterEffectorsBasedOnCommand (cmd, effectors);
                        if (!effectors.isEmpty ()) {
                            OperationResult result = new OperationResult ();
                            result.result = Result.SUCCESS;
                            StringBuffer errMsg = new StringBuffer ();
                            for (EffectorAttributes ea : effectors) {
                                Outcome outcome = executeEffector (ea.name, location, cmd.getParameters ());
                                if (outcome != Outcome.SUCCESS) {
                                    errMsg.append (MessageFormat.format (
                                            "E[{0}@{1}]: Failed to execute command {2} - ", ea.name, location,
                                            cmd.getParameters ()));

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
                                    location, cmd.getCommandName ());
                            return badResult;
                        }
                    }
                }
            }
            catch (RainbowModelException e) {
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

    private void filterEffectorsBasedOnCommand (IRainbowModelCommandRepresentation cmd,
            Set<EffectorAttributes> effectors) {
        for (Iterator iterator = effectors.iterator (); iterator.hasNext ();) {
            EffectorAttributes ea = (EffectorAttributes )iterator.next ();
            if (ea.commandPattern == null || !ea.commandPattern.getCommandName ().equals (cmd.getCommandName ())) {
                iterator.remove ();
            }
        }

    }

}
