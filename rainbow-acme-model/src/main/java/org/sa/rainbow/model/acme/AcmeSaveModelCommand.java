package org.sa.rainbow.model.acme;

import java.io.BufferedOutputStream;
import java.io.OutputStream;

import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.model.IAcmeModel;
import org.acmestudio.armani.ArmaniExportVisitor;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;

public class AcmeSaveModelCommand extends AbstractSaveModelCmd<IAcmeSystem> {

    private String m_systemName;
    private AcmeModelInstance m_model;

    public AcmeSaveModelCommand (String systemName, AcmeModelInstance model, OutputStream os) {
        super ("SaveAcmeModel", null, systemName, os, "");
        m_systemName = systemName;
        m_model = model;
    }

    @Override
    public IModelInstance<IAcmeSystem> getResult () throws IllegalStateException {
        return null;
    }

    @Override
    public String getModelName () {
        return m_systemName;
    }

    @Override
    public String getModelType () {
        return "Acme";
    }

    @Override
    protected void subExecute () throws RainbowException {
        IAcmeModel model = getModelContext ().getModelInstance ().getContext ().getModel ();

        try {
            BufferedOutputStream stream = new BufferedOutputStream (getStream ());
            ArmaniExportVisitor visitor = new ArmaniExportVisitor (stream);
            model.visit (visitor, null);
            stream.flush ();
            stream.close ();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void subRedo () throws RainbowException {

    }

    @Override
    protected void subUndo () throws RainbowException {

    }

    @Override
    protected boolean checkModelValidForCommand (IAcmeSystem model) {
        return true;
    }

}
