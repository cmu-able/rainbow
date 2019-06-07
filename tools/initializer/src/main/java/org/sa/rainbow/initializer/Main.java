package org.sa.rainbow.initializer;

import org.apache.commons.cli.*;
import org.apache.commons.validator.routines.UrlValidator;
import org.sa.rainbow.initializer.configuration.ConfigurationLoader;
import org.sa.rainbow.initializer.models.TemplateSet;
import org.sa.rainbow.initializer.scaffolder.Scaffolder;
import org.sa.rainbow.initializer.template.FileTemplateSetLoader;
import org.sa.rainbow.initializer.template.TemplateSetLoader;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;


/**
 * The entry point of initializer.
 */
public class Main {
    public static void main(String[] args) throws Exception {

        // initializer, as a command line tool, could take in two flags, -t for template and -c for config
        Options options = new Options();
        Option option_template = Option.builder("t")
                .argName("path_or_uri_to_template")
                .hasArg()
                .desc("load template from either path to local file or uri")
                .longOpt("template")
                .build();
        Option option_config = Option.builder("c")
                .argName("path_or_uri_to_config")
                .hasArg()
                .desc("load configuration from either path to local file or uri")
                .longOpt("config")
                .build();
        options.addOption(option_template);
        options.addOption(option_config);

        // help info can be displayed when using the -h or --help flag
        Option option_help = Option.builder("h")
                .longOpt("help")
                .desc("help menu")
                .build();
        options.addOption(option_help);

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

            UrlValidator urlValidator = new UrlValidator();

            // if user provides a template flag, parse template path
            if (cmd.hasOption("t")) {

                // if path is a valid uri, read and parse it
                if (urlValidator.isValid(cmd.getOptionValue("t"))) {

                    // TODO: read and parse template as URI here
                    System.out.print("Valid template uri: ");
                    System.out.println(cmd.getOptionValue("t"));

                    // otherwise, check if it is a valid local file
                } else {

                    // if path cannot be instantiated into a Java Path object, print error and return
                    try {
                        Path file = Paths.get(cmd.getOptionValue("t"));
                    } catch (InvalidPathException e) {
                        System.out.println("Parse error: the input template path is invalid.");
                        return;
                    }

                    // if path does not point to a local file, print error and return
                    Path file = Paths.get(cmd.getOptionValue("t"));
                    if (!Files.isDirectory(file)) {
                        System.out.println("Parse error: the input template path does not point to a local file.");
                        return;
                    }

                    // TODO: read and parse template as local file here
                    System.out.print("Valid template path: ");
                    System.out.println(cmd.getOptionValue("t"));

                    TemplateSetLoader templateSetLoader = new FileTemplateSetLoader(file.toFile());
                    templateSet = templateSetLoader.load();
                }
            }

            // if user provides a config flag, parse the config path
            if (cmd.hasOption("c")) {

                // if path is a valid uri, read and parse it
                if (urlValidator.isValid(cmd.getOptionValue("c"))) {

                    // TODO: read and parse template as URI here
                    System.out.print("Valid config uri: ");
                    System.out.println(cmd.getOptionValue("c"));

                    // otherwise, check if it is a valid local file
                } else {

                    // if path cannot be instantiated into a Java Path object, print error and return
                    try {
                        Path file = Paths.get(cmd.getOptionValue("c"));
                    } catch (InvalidPathException e) {
                        System.out.println("Parse error: the input configuration path is invalid.");
                        return;
                    }

                    // if path does not point to a local file, print error and return
                    Path file = Paths.get(cmd.getOptionValue("c"));
                    if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
                        System.out.println("Parse error: the input configuration path does not point to a local file.");
                        return;
                    }

                    // TODO: read and parse configuration as local file here
                    System.out.print("Valid config path: ");
                    System.out.println(cmd.getOptionValue("c"));

                    ConfigurationLoader configurationLoader = new ConfigurationLoader(templateSet.getVariables());
                    Map<String, String> configuration = configurationLoader.loadConfiguration(file.toFile());

                    Scaffolder scaffolder = new Scaffolder(templateSet, configuration);
                    scaffolder.setBaseDirectory(Paths.get(".")); //TODO: make it a parameter
                    scaffolder.scaffold();
                }
            }
        }

        // print error if parse fails
        catch (ParseException exception) {
            System.out.print("Parse error: ");
            System.out.println(exception.getMessage());
        }
    }
}
