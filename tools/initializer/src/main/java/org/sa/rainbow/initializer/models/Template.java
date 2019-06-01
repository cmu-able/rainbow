package org.sa.rainbow.initializer.models;

import java.util.List;

/**
 * The Template class represents a set of variables and the content to create a single file of the new target.
 */
public class Template {
    /**
     * A list of variables in the specific template.
     */
    private List<Variable> variables;
    /**
     * The content of the template, with placeholders for variables.
     */
    private String content;

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
