/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.rainbow_ui.ingestion;

import java.io.InputStream;

import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.model.acme.AcmeModelCommandFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;

/**
 * Internal implementation Acme Command Factory.
 * 
 * <p>
 * Defines the load model command..
 * </p>
 * 
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
class AcmeInternalCommandFactory extends AcmeModelCommandFactory {

    /**
     * The command used to load Acme model from the given input stream.
     * 
     * @param modelsManager model manager, is not used, could be null. Left for
     *        compatibility
     * @param modelName name of the model to load
     * @param stream input stream to read from
     * @param source name of the source of the model
     * @return command to load the model
     */
    public static AcmeInternalLoadModelCommand loadCommand(
            ModelsManager modelsManager, String modelName, InputStream stream,
            String source) {
        return new AcmeInternalLoadModelCommand(modelName, modelsManager,
                stream, source);
    }

    /**
     * @see org.sa.rainbow.model.acme.AcmeModelCommandFactory#AcmeModelCommandFactory(org.sa.rainbow.model.acme.AcmeModelInstance)
     */
    public AcmeInternalCommandFactory(AcmeModelInstance model) {
        super(model);
    }
}
