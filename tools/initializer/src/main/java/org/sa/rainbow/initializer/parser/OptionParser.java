package org.sa.rainbow.initializer.parser;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Parser that parses the option flags of rainbow initializer
 */
public class OptionParser {

    private Options options;

    /**
     * Constructor for an option parser that takes -t, -c and -h flags.
     */
    public OptionParser () {
        // initializer, as a command line tool, could take in two flags, -t for template and -c for config
        options = new Options();
        Option option_template = Option.builder("t")
                .argName("path_to_template")
                .hasArg()
                .desc("load template from path to local directory")
                .longOpt("template")
                .required()
                .build();
        Option option_config = Option.builder("c")
                .argName("path_to_config")
                .hasArg()
                .desc("load configuration from path to local file")
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
    }

    /**
     * Getter for options.
     *
     * @return all options created by the OptionParser
     */
    public Options getOptions() {
        return options;
    }

    /**
     * Handler of the template flag.
     *
     * @param cmd the commnd line input
     * @return path to the directory containing templates
     */
    public Path handleTemplateOption(CommandLine cmd) throws InvalidPathException {
        // if path cannot be instantiated into a Java Path object, print error and return
        try {
            Path file = Paths.get(cmd.getOptionValue("t"));
        } catch (InvalidPathException e) {
            System.out.println("Parse error: the input template path is invalid.");
        }

        // if path does not point to a local file, print error and return
        Path file = Paths.get(cmd.getOptionValue("t"));
        if (!Files.isDirectory(file)) {
            System.out.println("Parse error: the input template path does not point to a local directory.");
            return file;
        }

        System.out.print("Valid template path: ");
        System.out.println(cmd.getOptionValue("t"));

        return file;
    }

    /**
     * Handler of the configuration flag.
     *
     * @param cmd command line input
     * @return path to the file containing configurations
     */
    public Path handleConfigOption(CommandLine cmd) throws InvalidPathException{
        // if path cannot be instantiated into a Java Path object, print error and return
        try {
            Path file = Paths.get(cmd.getOptionValue("c"));
        } catch (InvalidPathException e) {
            System.out.println("Parse error: the input configuration path is invalid.");
        }

        // if path does not point to a local file, print error and return
        Path file = Paths.get(cmd.getOptionValue("c"));
        if (!Files.isRegularFile(file)) {
            System.out.println("Parse error: the input configuration path does not point to a local file.");
            return file;
        }

        System.out.print("Valid config path: ");
        System.out.println(cmd.getOptionValue("c"));

        return file;
    }
}
