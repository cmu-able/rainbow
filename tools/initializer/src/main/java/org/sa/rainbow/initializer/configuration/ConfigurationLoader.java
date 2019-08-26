package org.sa.rainbow.initializer.configuration;

import org.sa.rainbow.initializer.models.Variable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * The Configuration class represent a set of key-values of required variables.
 */

public class ConfigurationLoader {
    /**
     * A map of key values for the required variables.
     */
    protected final Map<String, Object> defaultConfig;

    /**
     * A set of known variable names.
     * Please note that we may not want to use defaultConfig directly
     * because some variables may be required but have no default values.
     */
    protected final Set<String> variableNames;

    public ConfigurationLoader(List<Variable> variables) {
        defaultConfig = new HashMap<>();
        variableNames = new HashSet<>();
        for (Variable variable : variables) {
            variableNames.add(variable.getName());
            defaultConfig.put(variable.getName(), variable.getValue());
        }
    }

    /**
     * Loads configuration setting from a Yaml file ( .yml file)
     *
     * @param file the properties file to load.
     * @throws IOException if the file cannot be loaded as Yaml file.
     */
    public Map<String, Object> loadConfiguration(File file) throws IOException {
        Map<String, Object> config = new HashMap<>(defaultConfig);
        Map<String, Object> tmpConfig;
        try (InputStream input = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            tmpConfig = yaml.load(input);
            if (tmpConfig == null) {
                return config;
            }
            for (Map.Entry element : tmpConfig.entrySet()) {
                String key = (String) element.getKey();
                if (!variableNames.contains(key)) {
                    throw new InvalidVariableException("unknown variable: " + key);
                } else {
                    config.put(key, element.getValue());
                }
            }

        }
        return config;
    }

}
