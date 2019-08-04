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

    private Map<String, String> createTestingProbe(String name, String description, String value) {
        Map<String, String> probe = new HashMap<>();
        probe.put("name", name);
        probe.put("description", description);
        probe.put("value", value);
        return probe;
    }

    private ArrayList<Map<String, String>> createTestingProbeList() {
        Map<String, String> probe1 = createTestingProbe("probe_1"
                , "the first probe in the system"
                , "default_probe_1");
        Map<String, String> probe2 = createTestingProbe("probe_2"
                , "the second probe in the system"
                , "default_probe_2");
        ArrayList<Map<String, String>> probeList = new ArrayList<>();
        probeList.add(probe1);
        probeList.add(probe2);
        return probeList;
    }

    private void createConfigProperties(File configFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            Map<String, Object> data = new HashMap<>();
            data.put("mode", "continual");
            data.put("probe", createTestingProbeList());
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
        Map<String, Object> result;
        try {
            result = config.loadConfiguration(testYML);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertEquals(createTestingProbeList(), result.get("probe"));
        assertTrue(result.get("probe") instanceof ArrayList);
        assertEquals("continual", result.get("mode"));
        assertFalse(result.get("mode") instanceof ArrayList);
        assertTrue(result.get("mode") instanceof String);
        assertEquals("default", result.get("should-have-default"));
        assertFalse(result.get("should-have-default") instanceof ArrayList);
        assertTrue(result.get("should-have-default") instanceof String);
    }

    @Test(expected = InvalidVariableException.class)
    public void invalidVariable() throws Exception {
        List<Variable> variables = Collections.singletonList(new Variable("allowed"));
        (new ConfigurationLoader(variables)).loadConfiguration(testYML); // should throw an exception
    }
}
