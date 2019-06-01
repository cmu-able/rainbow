package org.sa.rainbow.initializer.models;

import java.util.List;

/**
 * The Template class represents a set of variables and the content to create a single file of the new target.
 */
public class Template {
    /**
     * A list of variables in this specific template.
     */
    private List<Variable> localVariables;
    /**
     * The content of the template, with placeholders for variables.
     */
    private String content;

    public List<Variable> getLocalVariables() {
        return localVariables;
    }

    public void setLocalVariables(List<Variable> localVariables) {
        this.localVariables = localVariables;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
