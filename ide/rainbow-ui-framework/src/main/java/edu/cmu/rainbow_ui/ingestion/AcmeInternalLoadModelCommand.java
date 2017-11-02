/*
 * The MIT License
 *
 * Copyright 2014 CMU MSIT-SE Rainbow Team.
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
package edu.cmu.rainbow_ui.ingestion;

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

/**
 * Command to load Acme model from the given input stream.
 * 
 * <p>
 * 
 * @see AcmeInternalCommandFactory </p>
 * 
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
class AcmeInternalLoadModelCommand extends AbstractLoadModelCmd<IAcmeSystem> {

    private final String systemName;
    private final InputStream inputStream;
    private String modelName;
    private AcmeInternalModelInstance result;

    /**
     * Construct the load command.
     * 
     * @see org.sa.rainbow.core.models.commands.AbstractLoadModelCmd
     * 
     * @param sysName Acme system name
     * @param is input stream to read model from
     */
    public AcmeInternalLoadModelCommand(String sysName, IModelsManager mm,
            InputStream is, String source) {
        super("LoadAcmeModel", mm, sysName, is, source);
        systemName = sysName;
        inputStream = is;
    }

    @Override
    protected void subExecute() throws RainbowException {
        try {
            IAcmeResource resource = StandaloneResourceProvider.instance()
                    .acmeResourceForObject(inputStream);
            result = new AcmeInternalModelInstance(resource.getModel()
                    .getSystem(systemName), getOriginalSource());

            doPostExecute();
        } catch (ParsingFailureException | IOException e) {
            throw new RainbowException(e);
        }
    }

    @Override
    protected void subRedo() throws RainbowException {
        doPostExecute();
    }

    @Override
    protected void subUndo() throws RainbowException {
        doPostUndo();
    }

    @Override
    protected boolean checkModelValidForCommand(Object model) {
        return true;
    }

    @Override
    public IModelInstance<IAcmeSystem> getResult() throws IllegalStateException {
        return result;
    }

    @Override
    public ModelReference getModelReference () {
        return new ModelReference (modelName, "Acme");
    }

}
