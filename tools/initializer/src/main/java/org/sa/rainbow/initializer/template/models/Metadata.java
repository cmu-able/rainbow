package org.sa.rainbow.initializer.template.models;

import org.sa.rainbow.initializer.models.Variable;

import java.util.List;

/**
 * The Metadata of a set of templates.
 */
public class Metadata {
    /**
     * A list of variables to be filled in the templates.
     */
    private List<Variable> variables;
    /**
     * A list of files to be loaded as templates.
     */
    private List<String> files;

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
