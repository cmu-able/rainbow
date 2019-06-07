package org.sa.rainbow.initializer.configuration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.initializer.models.Variable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.assertEquals;


public class ConfigurationLoaderTest {
    private Path tempPath;
    private File testProperties;

    private void createConfigProperties(File configFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            Properties properties = new Properties();
            properties.setProperty("probe", "DimmerProbe");
            properties.setProperty("mode", "continual");
            properties.store(writer, "test");
        }
    }

    @Before
    public void setup() throws Exception {
        tempPath = Files.createTempDirectory("config");
        testProperties = tempPath.resolve("test.properties").toFile();
        createConfigProperties(testProperties);
    }

    @After
    public void tearDown() throws Exception {
        // delete everything under tempPath
        Files.walk(tempPath).sorted(Comparator.reverseOrder()).forEach(path -> {
            try {
                Files.delete(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void load() throws Exception {
        List<Variable> list = new ArrayList<>();
        list.add(new Variable("probe"));
        list.add(new Variable("mode"));
        list.add(new Variable("should-have-default", "should have default value", "default"));
        ConfigurationLoader config = new ConfigurationLoader(list);
        try {
            Map<String, String> result = config.loadConfiguration(testProperties);
            assertEquals("DimmerProbe", result.get("probe"));
            assertEquals("continual", result.get("mode"));
            assertEquals("default", result.get("should-have-default"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test(expected = InvalidVariableException.class)
    public void invalidVariable() throws Exception {
        List<Variable> variables = Collections.singletonList(new Variable("allowed"));
        (new ConfigurationLoader(variables)).loadConfiguration(testProperties); // should throw an exception
    }
}
