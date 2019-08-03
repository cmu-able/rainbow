package org.sa.rainbow.initializer.cli;

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
     * Constructor for an option cli that takes -t, -c and -h flags.
     */
    public OptionParser() {
        // initializer, as a command line tool, could take in three flags, -t for template,
        // -c for config, and -p for destination directory path
        options = new Options();
        Option templateOption = Option.builder("t")
                .argName("path_to_template")
                .hasArg()
                .desc("(REQUIRED) load template from path to local directory")
                .longOpt("template")
                .build();
        Option configOption = Option.builder("c")
                .argName("path_to_config")
                .hasArg()
                .desc("load configuration from path to local file")
                .longOpt("config")
                .build();
        Option pathOption = Option.builder("p")
                .argName("path_to_new_target")
                .hasArg()
                .desc("desired directory of which new target will locate")
                .longOpt("path")
                .build();
        options.addOption(templateOption);
        options.addOption(configOption);
        options.addOption(pathOption);

        // help info can be displayed when using the -h or --help flag
        Option helpOption = Option.builder("h")
                .longOpt("help")
                .desc("help menu")
                .build();
        options.addOption(helpOption);
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
     * @param cmd the command line input
     * @return path to the directory containing templates
     */
    public Path handleTemplateOption(CommandLine cmd) throws InvalidPathException {
        // if path does not point to a local file, print error and return
        Path file = Paths.get(cmd.getOptionValue("t"));
        if (!Files.isDirectory(file)) {
            throw new IllegalArgumentException("the input template path does not point to a local directory.");
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
    public Path handleConfigOption(CommandLine cmd) {
        // if path does not point to a local file, print error and return
        Path configPath = Paths.get(cmd.getOptionValue("c"));
        if (!Files.isRegularFile(configPath)) {
            throw new IllegalArgumentException("the input configuration path does not point to a local file.");
        }

        System.out.print("Valid config path: ");
        System.out.println(cmd.getOptionValue("c"));

        return configPath;
    }
}
