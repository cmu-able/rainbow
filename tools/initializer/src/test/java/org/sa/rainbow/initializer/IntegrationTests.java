package org.sa.rainbow.initializer;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.initializer.configuration.ConfigurationLoader;
import org.sa.rainbow.initializer.models.TemplateSet;
import org.sa.rainbow.initializer.scaffolder.Scaffolder;
import org.sa.rainbow.initializer.template.FileTemplateSetLoader;
import org.sa.rainbow.initializer.template.TemplateSetLoader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.assertNotEquals;

public class IntegrationTests {

    private Path tempDirectory;

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
}
