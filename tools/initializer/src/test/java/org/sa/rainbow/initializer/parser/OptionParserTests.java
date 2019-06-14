package org.sa.rainbow.initializer.parser;

import org.apache.commons.cli.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.Assert.*;

public class OptionParserTests {

    private Path templateDir;
    private Path configFile;
    private CommandLine cmd;

    @Before
    public void setUp() throws Exception {
        templateDir = Files.createTempDirectory("templates");
        configFile = Files.createTempFile("config", "yml");
        String args[] = new String[]{"-t", templateDir.toString(), "-c", configFile.toString()};
        OptionParser optionParser = new OptionParser();
        Options options = optionParser.getOptions();
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(options, args);
    }

    @After
    public void tearDown() throws Exception {
        try {
            Files.delete(configFile);
            Files.delete(templateDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void handleTemplate() {
        OptionParser optionParser = new OptionParser();
        Path dir = optionParser.handleTemplateOption(cmd);
        assertEquals(templateDir.toString(), dir.toString());
    }

    @Test
    public void handleConfig() {
        OptionParser optionParser = new OptionParser();
        Path file = optionParser.handleConfigOption(cmd);
        assertEquals(configFile.toString(), file.toString());
    }

}
