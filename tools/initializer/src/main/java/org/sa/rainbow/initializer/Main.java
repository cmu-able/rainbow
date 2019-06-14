package org.sa.rainbow.initializer;

import org.apache.commons.cli.*;
import org.sa.rainbow.initializer.configuration.ConfigurationLoader;
import org.sa.rainbow.initializer.models.TemplateSet;
import org.sa.rainbow.initializer.parser.OptionParser;
import org.sa.rainbow.initializer.scaffolder.Scaffolder;
import org.sa.rainbow.initializer.template.FileTemplateSetLoader;
import org.sa.rainbow.initializer.template.TemplateSetLoader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * The entry point of initializer.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        OptionParser optionParser = new OptionParser();
        Options options = optionParser.getOptions();
        TemplateSet templateSet = null;

        try {

            // parse command line arguments
            CommandLine cmd;
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("rainbow_initializer", options, true);
            }

            // if user provides a template flag, parse template path
            if (cmd.hasOption("t")) {
                Path file = optionParser.handleTemplateOption(cmd);
                TemplateSetLoader templateSetLoader = new FileTemplateSetLoader(file.toFile());
                templateSet = templateSetLoader.load();
            }

            // if user provides a config flag, parse the config path
            if (cmd.hasOption("c")) {
                Path file = optionParser.handleConfigOption(cmd);
                ConfigurationLoader configurationLoader = new ConfigurationLoader(templateSet.getVariables());
                Map<String, Object> configuration = configurationLoader.loadConfiguration(file.toFile());
                Scaffolder scaffolder = new Scaffolder(templateSet, configuration);
                scaffolder.setBaseDirectory(Paths.get("."));
                scaffolder.scaffold();
            }
        }

        // print error if parse fails
        catch (ParseException exception) {
            System.out.print("Parse error: ");
            System.out.println(exception.getMessage());
        }
    }
}