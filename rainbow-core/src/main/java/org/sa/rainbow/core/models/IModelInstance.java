package org.sa.rainbow.core.models;

import org.sa.rainbow.core.error.RainbowCopyException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.commands.ModelCommandFactory;

/**
 * Captures an abstract Rainbow model, for example a model of the architecture. A model in Rainbow should provide the
 * following things:
 * 
 * 1. A way to get a model instance keyed by some value (e.g., the filename) <br>
 * 2. A command factory that can be used to make changes to the model -- not sure what the abstract representation of a
 * command factory should be <br>
 * 3. Lock seeking methods for a model instance? <br>
 * 4. Get a snapshot of a model instance (a snapshot will not be updated by any changes through the command interface)
 * (p.s., this is needed at least by the tactic executor, which requires no changes to the model)
 * 
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public interface IModelInstance<T> {

    /**
     * 
     * @return the Model instance being managed (e.g., an Acme system, and impact model, ...)
     */
    public T getModelInstance ();

    /**
     * Sets the model to be managed by this instance
     * 
     * @param model
     */
    public void setModelInstance (T model);

    /**
     * Creates a copy of this model instance, giving it the new name if appropriate
     * 
     * @param newName
     * @return
     * @throws RainbowCopyException
     */
    public IModelInstance<T> copyModelInstance (String newName) throws RainbowCopyException;

    /**
     * 
     * @return The type of this model (e.g., Acme, computation, ...)
     */
    public String getModelType ();

    public String getModelName ();

    public ModelCommandFactory<T> getCommandFactory ();

    public void setOriginalSource (String source);

    public String getOriginalSource ();

    public void dispose () throws RainbowException;
}
