package org.sa.rainbow.initializer;

import org.apache.commons.cli.*;
import org.sa.rainbow.initializer.cli.OptionParser;
import org.sa.rainbow.initializer.configuration.ConfigurationLoader;
import org.sa.rainbow.initializer.configuration.UIConfigurationLoader;
import org.sa.rainbow.initializer.models.TemplateSet;
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
        TemplateSet templateSet;

        try {
            // parse command line arguments
            CommandLine cmd;
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);

            Map<String, Object> configuration;

            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("rainbow_initializer", options, true);
                return;
            }

            // if user provides a template flag, parse template path
            if (cmd.hasOption("t")) {
                Path file = optionParser.handleTemplateOption(cmd);
                TemplateSetLoader templateSetLoader = new FileTemplateSetLoader(file.toFile());
                templateSet = templateSetLoader.load();
            } else {
                // Should not happen
                System.out.println("Parse error: missing required option -t, please use -t <path_to_template>");
                return;
            }

            Path configPath = null;
            // if user provides a config flag, parse the config path
            if (cmd.hasOption("c")) {
                configPath = optionParser.handleConfigOption(cmd);
            } else {
                System.out.println("No configuration file provided, using default.");
            }

            if (configPath != null) {
                ConfigurationLoader configurationLoader = new ConfigurationLoader(templateSet.getVariables());
                configuration = configurationLoader.loadConfiguration(configPath.toFile());
            } else {
                UIConfigurationLoader configLoader = new UIConfigurationLoader(templateSet.getVariables());
                if (configLoader.loadConfiguration()) {
                    configuration = configLoader.getDefaultConfig();
                } else {
                    return;
                }
            }

            Scaffolder scaffolder = new Scaffolder(templateSet, configuration);

            if (cmd.hasOption("p")) {
                Path destinationPath = Paths.get(cmd.getOptionValue("p"));
                scaffolder.setBaseDirectory(destinationPath);
            } else {
                scaffolder.setBaseDirectory(Paths.get("."));
            }

            scaffolder.scaffold();
        }

        // print error if parse fails
        catch (ParseException exception) {
            System.out.print("Parse error: ");
            System.out.println(exception.getMessage());
        }
    }
}