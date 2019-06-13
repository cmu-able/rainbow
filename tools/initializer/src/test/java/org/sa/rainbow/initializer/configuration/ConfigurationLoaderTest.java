package org.sa.rainbow.initializer.configuration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.initializer.models.Variable;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.*;


public class ConfigurationLoaderTest {
    private Path tempPath;
    private File testYML;

    private void createConfigProperties(File configFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("probe", new String[]{"dimmerProbe", "genericProbe"});
            data.put("mode", "continual");
            Yaml yaml = new Yaml();
            yaml.dump(data, writer);
        }
    }

    @Before
    public void setup() throws Exception {
        tempPath = Files.createTempDirectory("config");
        testYML = tempPath.resolve("test.yml").toFile();
        createConfigProperties(testYML);
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
            Map<String, Object> result = config.loadConfiguration(testYML);
            ArrayList<String> tmpList=new ArrayList<>();
            tmpList.add("dimmerProbe");
            tmpList.add("genericProbe");
            assertEquals(tmpList, result.get("probe"));
            assertTrue(result.get("probe") instanceof ArrayList);
            assertEquals("continual", result.get("mode"));
            assertFalse(result.get("mode") instanceof ArrayList);
            assertEquals("default", result.get("should-have-default"));
            assertFalse(result.get("default") instanceof ArrayList);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test(expected = InvalidVariableException.class)
    public void invalidVariable() throws Exception {
        List<Variable> variables = Collections.singletonList(new Variable("allowed"));
        (new ConfigurationLoader(variables)).loadConfiguration(testYML); // should throw an exception
    }
}
