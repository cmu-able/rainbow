package org.sa.rainbow.model.acme.znn.commands;

import java.io.IOException;
import java.io.InputStream;

import org.acmestudio.acme.core.resource.IAcmeResource;
import org.acmestudio.acme.core.resource.ParsingFailureException;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.standalone.resource.StandaloneResourceProvider;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.model.acme.znn.ZNNModelUpdateOperatorsImpl;
import org.sa.rainbow.models.IModelInstance;
import org.sa.rainbow.models.IModelsManager;
import org.sa.rainbow.models.commands.AbstractLoadModelCmd;

public class ZNNLoadModelCommand extends AbstractLoadModelCmd<IAcmeSystem> {

    private InputStream m_inputStream;
    private String                      m_systemName;
    private ZNNModelUpdateOperatorsImpl m_result;

    public ZNNLoadModelCommand (String systemName, IModelsManager mm, InputStream is, String source) {
        super ("loadZNNModel", mm, systemName, is, source);
        m_systemName = systemName;
        m_inputStream = is;
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
    protected boolean checkModelValidForCommand (Object model) {
        return true;
    }

    @Override
    protected void subExecute () throws RainbowException {
        try {
            IAcmeResource resource = StandaloneResourceProvider.instance ().acmeResourceForObject (m_inputStream);
            m_result = new ZNNModelUpdateOperatorsImpl (resource.getModel ().getSystem (m_systemName));
            doPostExecute ();
        }
        catch (ParsingFailureException | IOException e) {
            throw new RainbowException (e);
        }
    }

    @Override
    protected void subRedo () throws RainbowException {
        doPostExecute ();
    }

    @Override
    protected void subUndo () throws RainbowException {
        doPostUndo ();
    }

    @Override
    protected IModelInstance<IAcmeSystem> getResult () {
        return m_result;
    }

    @Override
    public String getCommandName () {
        return "LoadZNNModel";
    }


}
