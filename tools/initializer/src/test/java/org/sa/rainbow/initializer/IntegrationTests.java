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
import java.util.Collections;
import java.util.Map;

public class IntegrationTests {

    private Path tempDirectory;

    @Before
    public void setUp() throws Exception {
        Assume.assumeTrue(Files.isDirectory(Paths.get("templates")));

        tempDirectory = Files.createTempDirectory("integration");
    }

    @Test
    public void createDefaultTarget() throws Exception{
        TemplateSetLoader loader = new FileTemplateSetLoader(new File("templates"));
        TemplateSet templateSet = loader.load();
        Map<String, String> configuration = Collections.emptyMap();
        Scaffolder scaffolder = new Scaffolder(templateSet, configuration);
        scaffolder.scaffold();
    }
}
