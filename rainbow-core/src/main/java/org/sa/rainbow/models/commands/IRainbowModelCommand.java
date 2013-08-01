package org.sa.rainbow.models.commands;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.models.IModelInstance;
import org.sa.rainbow.models.ports.IEventAnnouncePort;

public interface IRainbowModelCommand<Type, Model> extends IRainbowModelCommandRepresentation {

    public boolean canExecute ();

    public boolean canUndo ();

    public boolean canRedo ();

    public Type execute (IModelInstance<Model> context) throws IllegalStateException, RainbowException;

    public Type redo () throws IllegalStateException, RainbowException;

    public Type undo () throws IllegalStateException, RainbowException;

    public void setEventAnnouncePort (IEventAnnouncePort announcPort);

    public void setModel (Model m);

}
