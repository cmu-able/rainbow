package org.sa.rainbow.core.models.commands;

import java.util.List;

public interface IRainbowModelCompoundCommand<Model> extends IRainbowModelCommand<List<Object>, Model> {
    public List<Object> getResults ();

}
