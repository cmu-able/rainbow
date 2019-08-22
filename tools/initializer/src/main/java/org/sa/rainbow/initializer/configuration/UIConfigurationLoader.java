package org.sa.rainbow.initializer.configuration;

import org.sa.rainbow.initializer.models.Variable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * This class is used to load configuration from interactive inputs.
 *
 * @author Liwen Feng
 * @since 1.0
 */
public class UIConfigurationLoader extends ConfigurationLoader {
    private List<Variable> variables;

    public UIConfigurationLoader(List<Variable> variables) {
        super(variables);
        this.variables = variables;
    }

    /**
     * Writes default values to a given configuration file, with descriptions as comments.
     *
     * @param configFile config file to write.
     * @throws IOException if an I/O exception occur while writing to config file.
     */
    private void writeDefaultConfiguration(File configFile) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            for (Variable variable : this.variables) {
                writer.write("# " + variable.getDescription());
                writer.newLine();
                yaml.dump(Collections.singletonMap(variable.getName(), variable.getValue()), writer);
                writer.newLine();
            }
        }
    }

    /**
     * load variables from interactive inputs
     *
     * @return true if default values are used, false otherwise.
     * @throws IOException if an I/O exception occur during interactive I/O
     *                     or writing config file.
     */
    public boolean loadConfiguration() throws IOException {
        for (Map.Entry<String, Object> entry : defaultConfig.entrySet()) {
            System.out.printf("'%s' will be initiated as: (%s)%n", entry.getKey(), entry.getValue());
        }
        System.out.println("Is it OK(y/n)?");

        Scanner scanner = new Scanner(System.in);
        String answer = scanner.nextLine();

        // if answer is invalid, try three times
        int cnt = 1;
        while (!answer.equals("yes") && !answer.equals("y") && !answer.equals("no") && !answer.equals("n")) {
            System.out.println(answer);
            System.out.println("Please enter yes/y or no/n.");
            System.out.println("Is it OK(Y/N)?");
            answer = scanner.nextLine();
            cnt++;
            if (cnt == 3) {
                break;
            }
        }

        // if answer is yes, we will generate configuration using default values.
        if (answer.equals("yes") || answer.equals("y")) {
            System.out.println("We will use the default values to initialize.");
            return true;
        }

        // if answer is no, we will call an editor for users
        else if (answer.equals("no") || answer.equals("n")) {
            System.out.println("Please set your own configurations in 'config.yml' and run again.");
            File file = new File("config.yml");
            writeDefaultConfiguration(file);
            return false;
        } else {
            System.out.println("Abort!");
            return false;
        }
    }

    public Map<String, Object> getDefaultConfig() {
        return defaultConfig;
    }

    public Set<String> getVariableNames() {
        return variableNames;
    }

}
