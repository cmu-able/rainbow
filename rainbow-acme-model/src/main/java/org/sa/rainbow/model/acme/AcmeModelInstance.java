package org.sa.rainbow.model.acme;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.core.exception.AcmeException;
import org.acmestudio.acme.element.IAcmeFamily;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.model.DefaultAcmeModel;
import org.acmestudio.acme.model.IAcmeModel;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.command.IAcmeCompoundCommand;
import org.acmestudio.acme.model.command.IAcmeElementCopyCommand;
import org.acmestudio.armani.ArmaniLanguagePack;
import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.models.IModelInstance;

public abstract class AcmeModelInstance implements IModelInstance<IAcmeSystem> {

    private IAcmeSystem m_system;


    public AcmeModelInstance (IAcmeSystem system) {
        m_system = system;
    }

    @Override
    public String getModelType () {
        return "Acme";
    }

    @Override
    public IAcmeSystem getModelInstance () {
        return m_system;
    }

    @Override
    public void setModelInstance (IAcmeSystem model) {
        m_system = model;
    }

    @Override
    public IModelInstance<IAcmeSystem> copyModelInstance (String newName) throws RainbowCopyException {
        synchronized (m_system) {
            ArmaniLanguagePack alp = new ArmaniLanguagePack ();
            IAcmeModel model = alp.getModel ();
            List<IAcmeCommand<?>> cmds = new LinkedList<> ();
            // Copy all families into this system, whether imported or local
            Set<IAcmeFamily> types = ModelHelper.gatherSuperFamilies (m_system);
            types.remove (DefaultAcmeModel.defaultFamily ());
            for (IAcmeFamily f : types) {
                cmds.add (model.getCommandFactory ().copyElementCommand (model, f));
            }

            // Copy the system itself
            IAcmeElementCopyCommand cmd = model.getCommandFactory ().copyElementCommand (model, m_system);
            cmds.add (cmd);
            cmds.add (model.getCommandFactory ().elementRenameCommand (cmd, newName));
            // Execute the command to create a new system
            try {
                if (cmds.size () == 1)
                    return generateInstance ((IAcmeSystem )cmd.execute ());
                else {
                    IAcmeCompoundCommand cc = model.getCommandFactory ().compoundCommand (cmds);
                    List<Object> execute = cc.execute ();

                    return generateInstance ((IAcmeSystem )execute.get (execute.size () - 1));
                }
            }
            catch (IllegalStateException | AcmeException e) {
                RainbowCopyException exc = new RainbowCopyException (MessageFormat.format (
                        "Could not copy Acme system {0}", m_system.getName ()));
                exc.addSuppressed (e);
                throw exc;
            }
        }
    }

    @Override
    public String getModelName () {
        return m_system.getName ();
    }

    protected abstract AcmeModelInstance generateInstance (IAcmeSystem sys);

}
