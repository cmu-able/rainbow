package org.sa.rainbow.initializer.configuration;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.sa.rainbow.initializer.models.TemplateSet;
import org.sa.rainbow.initializer.models.Variable;
import java.io.File;
import java.io.*;
import java.util.*;

public class ConfigurationTest {
    @Test
    public void load(){
        ConfigurationLoader config = new ConfigurationLoader();
        List<Variable> list = new ArrayList<Variable>();
        Variable v = new Variable("probe");
        list.add(v);
        config.setLocalVariables(list);
        File testfile = new File("src/test/java/org/sa/rainbow/initializer/configuration/example.properties");
        config.loadConfiguration(testfile);
        Map<String, String> result = config.getConfig();
        assertEquals("DimmerProbe", result.get("probe"));
    }

}
