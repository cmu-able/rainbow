package org.sa.rainbow.initializer.configuration;

import org.sa.rainbow.initializer.models.Variable;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.*;
import java.io.*;

/**
 * The Configuration class represent a set of key-values of required variables
 */

public class ConfigurationLoader {
    /**
     * A map of key values for the required variables.
     */
    private Map<String, String> config;
    /**
     * A list of variables required in the entire Model
     */
    private List<Variable> localVariables;
    private Set<String> variableSet;

    public ConfigurationLoader() {
        config = new HashMap<>();
        variableSet = new HashSet<>();
    }

    public List<Variable> getLocalVariables() {
        return localVariables;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setLocalVariables(List<Variable> localVariables) {
        this.localVariables = localVariables;
        for (Variable variable : this.localVariables) {
            variableSet.add(variable.getName());
        }
    }

    /* Load configuration setting from a file (java .properties file) */
    public void loadConfiguration(File file) throws IOException {

        try (InputStream input = new FileInputStream(file)) {

            Properties prop = new Properties();

            /*load a properties file*/
            prop.load(input);

            Set<Entry<Object, Object>> entries = prop.entrySet();
            for (Entry<Object, Object> entry : entries) {

                if (variableSet.contains(entry.getKey())) {
                    config.put((String) entry.getKey(), (String) entry.getValue());
                } else {
                    throw new InvalidVariableException("Invalid variable in property files");
                }
            }


        }
    }

}
