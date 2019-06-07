package org.sa.rainbow.initializer.configuration;

import org.sa.rainbow.initializer.models.Variable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

/**
 * The Configuration class represent a set of key-values of required variables.
 */

public class ConfigurationLoader {
    /**
     * A map of key values for the required variables.
     */
    private final Map<String, String> defaultConfig;

    /**
     * A set of known variable names.
     * Please note that we may not want to use defaultConfig directly
     * because some variables may be required but have no default values.
     */
    private final Set<String> variableNames;

    public ConfigurationLoader(List<Variable> variables) {
        defaultConfig = new HashMap<>();
        variableNames = new HashSet<>();
        for (Variable variable : variables) {
            variableNames.add(variable.getName());
            defaultConfig.put(variable.getName(), variable.getValue());
        }
    }

    /**
     * Loads configuration setting from a file (java .properties file)
     *
     * @param file the properties file to load.
     * @throws IOException if the file cannot be loaded as properties file.
     */
    public Map<String, String> loadConfiguration(File file) throws IOException {
        Map<String, String> config = new HashMap<>(defaultConfig);
        try (InputStream input = new FileInputStream(file)) {
            Properties prop = new Properties();
            prop.load(input);
            Set<Entry<Object, Object>> entries = prop.entrySet();
            for (Entry<Object, Object> entry : entries) {
                String name = (String) entry.getKey();
                String value = (String) entry.getValue();
                // The name was not known as the name of a variable.
                if (!variableNames.contains(name)) {
                    throw new InvalidVariableException("unknown variable: " + name);
                }
                config.put(name, value);
            }
        }
        return config;
    }

}
