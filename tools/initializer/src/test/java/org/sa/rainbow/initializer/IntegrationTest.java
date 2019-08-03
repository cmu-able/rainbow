package org.sa.rainbow.initializer;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.initializer.configuration.ConfigurationLoader;
import org.sa.rainbow.initializer.models.TemplateSet;
import org.sa.rainbow.initializer.scaffolder.Scaffolder;
import org.sa.rainbow.initializer.template.FileTemplateSetLoader;
import org.sa.rainbow.initializer.template.TemplateSetLoader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class IntegrationTest {

    private Path tempDirectory;

    @After
    public void tearDown() throws Exception {
        Files.walk(tempDirectory)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Before
    public void setUp() throws Exception {
        Assume.assumeTrue(Files.isDirectory(Paths.get("templates")));

        tempDirectory = Files.createTempDirectory("integration");
    }

    @Test
    public void createDefaultTarget() throws Exception {
        TemplateSetLoader loader = new FileTemplateSetLoader(new File("templates"));
        TemplateSet templateSet = loader.load();
        File tempFile = File.createTempFile("integration", ".yml");
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), new byte[]{});
        ConfigurationLoader configurationLoader = new ConfigurationLoader(templateSet.getVariables());
        Map<String, Object> configuration = configurationLoader.loadConfiguration(tempFile);
        Scaffolder scaffolder = new Scaffolder(templateSet, configuration);
        scaffolder.setBaseDirectory(tempDirectory);
        scaffolder.scaffold();
    }

    @Test
    public void invokeMainDefault() throws Exception {
        // Print no templates provided
        Main.main(new String[]{});
        // Print help
        Main.main(new String[]{"-h"});
        ByteArrayInputStream ins = new ByteArrayInputStream("y\n".getBytes());
        System.setIn(ins);
        Main.main(new String[]{"-t", "templates", "-p", tempDirectory.toString()});
        assertTrue(Files.isRegularFile(tempDirectory.resolve("rainbow.properties.ftl")));
    }

    @Test
    public void invokeMainWithConfig() throws Exception {
        File tempFile = File.createTempFile("integration", ".yml");
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), new byte[]{});

        ByteArrayInputStream ins = new ByteArrayInputStream("y\n".getBytes());
        System.setIn(ins);
        Main.main(new String[]{"-t", "templates", "-p", tempDirectory.toString(), "-c", tempFile.toString()});
        assertTrue(Files.isRegularFile(tempDirectory.resolve("rainbow.properties.ftl")));
    }
}
