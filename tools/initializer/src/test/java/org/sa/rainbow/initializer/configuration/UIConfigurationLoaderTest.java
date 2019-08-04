package org.sa.rainbow.initializer.configuration;

import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.initializer.models.Variable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;


public class UIConfigurationLoaderTest {
    private UIConfigurationLoader configLoader;

    @Before
    public void prepare() {
        configLoader = new UIConfigurationLoader(Collections.singletonList(new Variable("foo", "foo")));
    }

    @Test
    public void loadY() throws Exception {
        String input = "yes";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        configLoader.loadConfiguration();
    }

    @Test
    public void loadN() throws Exception {
        String input = "no";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        configLoader.loadConfiguration();
        tearDown();
    }

    @Test
    public void loadOther() throws Exception {
        String input = "other\nother\nother";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        configLoader.loadConfiguration();
    }

    @Test
    public void loadOtherY() throws Exception {
        String input = "other\nother\nyes";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        configLoader.loadConfiguration();
    }

    public void tearDown() throws Exception {
        Path path = Paths.get("config.yml");
        Files.delete(path);
    }
}