package org.sa.rainbow.core.models.commands;

public interface IRainbowOperation {

    public abstract String[] getParameters ();

    public abstract String getTarget ();

    public abstract String getName ();

    public String getModelName ();

    public String getModelType ();
}
