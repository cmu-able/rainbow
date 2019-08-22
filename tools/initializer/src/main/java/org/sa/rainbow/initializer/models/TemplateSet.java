package org.sa.rainbow.initializer.models;

import freemarker.template.Template;

import java.util.List;
import java.util.Map;

/**
 * The TemplateSet class contains templates for each file to create and the list of variables.
 */
public class TemplateSet {
    /**
     * A mapping from paths of files to create, to their corresponding template.
     */
    private Map<String, Template> templates;
    /**
     * A list of all needed variables.
     */
    private List<Variable> variables;


    public TemplateSet(Map<String, Template> templates, List<Variable> variables) {
        this.templates = templates;
        this.variables = variables;
    }

    public Map<String, Template> getTemplates() {
        return templates;
    }

    public void setTemplates(Map<String, Template> templates) {
        this.templates = templates;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

}
