package org.sa.rainbow.initializer.models;

/**
 * The Variable class represents a variable that the user can give a value to be filled in the resulting
 */
public class Variable {
    /**
     * The name of the variable.
     */
    private String name;
    /**
     * A short description of the variable.
     */
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
