package org.sa.rainbow.initializer.configuration;

import freemarker.template.Template;
import org.sa.rainbow.initializer.models.TemplateSet;
import org.sa.rainbow.initializer.models.Variable;
import org.sa.rainbow.initializer.template.InvalidMetadataException;
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
        Metadata metadata = loadMetadata();
        Map<String, Template> templates = new HashMap<>();
        for (Variable var : metadata.getVariables()) {
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
            answer = mockInput(cnt);
            cnt++;
            if(cnt == 3)
                break;
        }
        System.out.println(answer);

        // if answer is yes, we will generate configuration using default values.
        if(answer.equals("yes") || answer.equals("y")) {
            System.out.println("We will use the default values to initialize.");
            // TODO: need other operations?
        }
        // if answer is no, we will call an editor for users
        else if(answer.equals("no") || answer.equals("n")){
            System.out.println("Please set your own configurations and run again.");
            try {
                Desktop desktop = Desktop.getDesktop();
                File file = new File("config.properties");
                file.createNewFile();
                desktop.open(file);
                // TODO: need other operations?
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
        else {
            System.out.println("Abort!");
        }

    }

    /**
     * This is for unit test mocking
     *
     * @param cnt
     * @return String
     */
    public String mockInput(int cnt) {
        if(cnt < 2)
            return "other";
        else
            return "yes";
    }
}
