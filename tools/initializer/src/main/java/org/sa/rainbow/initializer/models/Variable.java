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
     * @param name the name of the variable.
     */
    public Variable(String name, String description) {
        this.name = name;
        this.description = description;
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
}
