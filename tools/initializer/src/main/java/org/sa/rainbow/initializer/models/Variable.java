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
    private String description = "";

    /**
     * The default value of the variable.
     */
    private String value = "";

    /**
     * Constructs a new Variable with no name.
     */
    public Variable() {

    }

    /**
     * Constructs a new Variable with given name.
     *
     * @param name the name of the variable.
     */
    public Variable(String name) {
        this.name = name;
    }

    /**
     * Constructs a new Variable with given name and description.
     *
     * @param name        the name of the variable.
     * @param description the description of the variable.
     */
    public Variable(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Constructs a new Variable with given name, description, and default value.
     *
     * @param name        the name of the variable.
     * @param description the description of the variable.
     * @param value       the default value of the variable.
     */
    public Variable(String name, String description, String value) {
        this(name, description);
        this.value = value;
    }

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
