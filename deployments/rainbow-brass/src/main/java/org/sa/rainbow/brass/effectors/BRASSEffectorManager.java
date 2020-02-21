package org.sa.rainbow.brass.effectors;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.translator.effectors.EffectorManager;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;

/**
 * Created by schmerl on 12/27/2016.
 */
public class BRASSEffectorManager extends EffectorManager  {
    public BRASSEffectorManager () {
        super ("BRASS Effector Manager");
    }

    @Override
    public OperationResult publishOperation (IRainbowOperation cmd) {
        OperationResult badResult = new OperationResult ();
        badResult.result = Result.UNKNOWN;

        OperationResult actualResult = badResult;
        Set<EffectorAttributes> effectors = getEffectorsAtLocation (
                Rainbow.instance ().getProperty ("rainbow.master.location.host"));
        filterEffectorsForCommand (cmd, effectors);
        if (!effectors.isEmpty ()) {
            OperationResult result = new OperationResult ();
            result.result = Result.SUCCESS;
            StringBuilder errMsg = new StringBuilder ();
            for (EffectorAttributes ea : effectors) {
                Outcome outcome = executeEffector (ea.name, ea.getLocation (), cmd.getParameters ());
                if (outcome != Outcome.SUCCESS) {
                    errMsg.append (MessageFormat.format ("E[{0}@{1}]: Failed to execute command {2} - ", ea.name,
                            ea.getLocation (), Arrays.toString (cmd.getParameters ())));
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
            else {
                result.reply = cmd.toString ();
            }
            actualResult = result;
        }
        else {
            badResult.reply = "Could not find an effector for " + cmd.toString ();
        }

        return actualResult;

    }

    private void filterEffectorsForCommand (IRainbowOperation cmd, Set<EffectorAttributes> effectors) {
        for (Iterator i = effectors.iterator (); i.hasNext ();) {
            EffectorAttributes ea = (EffectorAttributes )i.next ();
            if (ea.getCommandPattern () == null || !ea.getCommandPattern ().getName ().equals (cmd.getName ())) {
                i.remove ();
            }

        }
    }

}
