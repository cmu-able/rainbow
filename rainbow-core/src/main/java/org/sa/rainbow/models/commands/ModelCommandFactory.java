package org.sa.rainbow.models.commands;

import java.io.InputStream;

import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.models.ModelsManager;

public abstract class ModelCommandFactory<T> {
    public static AbstractLoadModelCmd
    loadCommand (ModelsManager modelsManager,
            String modelName,
            InputStream stream,
            String source) {
        throw new UnsupportedOperationException ("This method should be implemented in all subclasses and wasn't");
    }

    public abstract IRainbowModelCommand generateCommand (String commandName,
            String... args)
                    throws RainbowModelException;

}
