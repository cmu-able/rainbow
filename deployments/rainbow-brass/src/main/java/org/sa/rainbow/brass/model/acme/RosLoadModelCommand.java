package org.sa.rainbow.brass.model.acme;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.acmestudio.acme.core.resource.IAcmeResource;
import org.acmestudio.acme.core.resource.ParsingFailureException;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.standalone.resource.StandaloneResourceProvider;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.AbstractLoadModelCmd;

public class RosLoadModelCommand extends AbstractLoadModelCmd<IAcmeSystem> {

    private InputStream                 m_inputStream;
    private String                      m_systemName;
    private RosModelUpdateOperatorsImpl m_result;

    public RosLoadModelCommand (String systemName, IModelsManager mm, InputStream is, String source) {
        super ("loadRosModel", mm, systemName, is, source);
        m_systemName = systemName;
        m_inputStream = is;
    }

    @Override
    public IModelInstance<IAcmeSystem> getResult () throws IllegalStateException {
        return m_result;
    }

    @Override
    public ModelReference getModelReference () {
        return new ModelReference (m_systemName, "Acme");
    }

    @Override
    protected void subExecute () throws RainbowException {
        try {
            IAcmeResource resource = StandaloneResourceProvider.instance ()
                    .acmeResourceForObject (new File (getOriginalSource ()));
            m_result = new RosModelUpdateOperatorsImpl (resource.getModel ().getSystem (m_systemName),
                    getOriginalSource ());
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
    protected boolean checkModelValidForCommand (Object model) {
        return true;
    }

    @Override
    public String getName () {
        return "LoadRosModel";
    }

}
