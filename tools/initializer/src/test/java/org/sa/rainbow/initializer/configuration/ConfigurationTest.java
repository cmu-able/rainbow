package org.sa.rainbow.initializer.configuration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.sa.rainbow.initializer.models.Variable;
import java.io.File;
import java.io.*;
import java.util.*;
import java.nio.file.Path;
import java.nio.file.Files;


public class ConfigurationTest {
    private Path tempPath;

    private void createConfigProperties(File configFile) throws IOException{
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))){
            Properties properties = new Properties();
            properties.setProperty("probe", "DimmerProbe");
            properties.setProperty("mode", "continual");

            properties.store(writer, "test");
        }
    }
    @Before
    public void setup() throws Exception{
        tempPath = Files.createTempDirectory("config");
        createConfigProperties(tempPath.resolve("test.properties").toFile());
    }
    @After
    public void tearDown() throws Exception{
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
    public void load() throws Exception{
        ConfigurationLoader config = new ConfigurationLoader();
        List<Variable> list = new ArrayList<>();
        Variable v = new Variable("probe");
        list.add(v);
        list.add(new Variable("mode"));
        config.setLocalVariables(list);
        try {
            config.loadConfiguration(tempPath.resolve("test.properties").toFile());
            Map<String, String> result = config.getConfig();
            assertEquals("DimmerProbe", result.get("probe"));
            assertEquals("continual", result.get("mode"));
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
