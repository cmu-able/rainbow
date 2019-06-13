package org.sa.rainbow.initializer.configuration;

import freemarker.template.Template;
import org.sa.rainbow.initializer.models.TemplateSet;
import org.sa.rainbow.initializer.models.Variable;
import org.sa.rainbow.initializer.template.FileTemplateSetLoader;
import org.sa.rainbow.initializer.template.InvalidMetadataException;
import org.sa.rainbow.initializer.template.TemplateSetLoader;
import org.sa.rainbow.initializer.template.models.Metadata;
import org.yaml.snakeyaml.Yaml;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class UIConfigurationLoader {
    /*
     * The default configuration file.
     */
    protected static final String METADATA_FILENAME = "metadata.yml";

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

    public UIConfigurationLoader() {
        this.defaultConfig = new HashMap<>();
        this.variableNames = new HashSet<>();
    }

    public void defaultConfiguration(List<Variable> variables) {
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
    public Map<String, String> fileConfiguration(File file) throws IOException {
        Map<String, String> config = new HashMap<>(defaultConfig);
        try (InputStream input = new FileInputStream(file)) {
            Properties prop = new Properties();
            prop.load(input);
            Set<Map.Entry<Object, Object>> entries = prop.entrySet();
            for (Map.Entry<Object, Object> entry : entries) {
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

    /**
     * Loads the metadata from a reader.
     *
     * @param reader the reader to read metadata from.
     * @return the parsed metadata.
     */
    protected Metadata loadMetadata(Reader reader) throws InvalidMetadataException {
        Yaml yaml = new Yaml();
        Metadata metadata = yaml.loadAs(reader, Metadata.class);
        if (metadata.getFiles() == null) {
            throw new InvalidMetadataException("field 'files' is required in metadata.");
        }
        if (metadata.getVariables() == null) {
            metadata.setVariables(Collections.emptyList());
        }
        return metadata;
    }

    /**
     * Loads the metadata from the root directory.
     *
     * @return the parsed Metadata
     * @throws IOException if metadata cannot be read or parsed.
     */
    private Metadata loadMetadata() throws IOException {
        Metadata metadata;
        try (Reader reader = new BufferedReader(new FileReader(new File("templates", METADATA_FILENAME)))) {
            metadata = loadMetadata(reader);
        }
        return metadata;
    }

    /**
     * load variables from interactive inputs
     *
     * @return TemplateSet
     * @throws IOException
     */
    public void loadConfiguration() throws IOException {
        TemplateSetLoader loader = new FileTemplateSetLoader(new File("templates"));
        TemplateSet templateSet = loader.load();
        for (Variable var : templateSet.getVariables()) {
            System.out.println("\'"+var.getName()+"\'"+" will be initiated as: ("+var.getValue()+")");
        }
        System.out.println("Is it OK(y/n)?");

        Scanner scanner = new Scanner(System.in);
        String answer = scanner.nextLine();

        // String answer = mockInput(0);

        // if answer is invalid, try three times
        int cnt = 1;
        while(!answer.equals("yes") && !answer.equals("y") && !answer.equals("no") && !answer.equals("n")) {
            System.out.println(answer);
            System.out.println("Please enter yes/y or no/n.");
            System.out.println("Is it OK(Y/N)?");
            answer = scanner.nextLine();
            cnt++;
            if(cnt == 3)
                break;
        }
        System.out.println(answer);

        // if answer is yes, we will generate configuration using default values.
        if(answer.equals("yes") || answer.equals("y")) {
            System.out.println("We will use the default values to initialize.");
            defaultConfiguration(templateSet.getVariables());
        }
        // if answer is no, we will call an editor for users
        else if(answer.equals("no") || answer.equals("n")){
            System.out.println("Please set your own configurations and run again.");
            try {
                Desktop desktop = Desktop.getDesktop();
                File file = new File("config.yml");
                file.createNewFile();
                desktop.open(file);
                // fileConfiguration(file);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
        else {
            System.out.println("Abort!");
        }

    }

    public Map<String, String> getDefaultConfig() {
        return defaultConfig;
    }

    public Set<String> getVariableNames() {
        return variableNames;
    }

    /**
     * This is for unit test mocking
     *
     * @param cnt
     * @return String
     */
//    public String mockInput(int cnt) {
//        if(cnt < 2)
//            return "other";
//        else
//            return "yes";
//    }
}
