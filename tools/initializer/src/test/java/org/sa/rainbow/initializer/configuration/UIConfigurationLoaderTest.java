package org.sa.rainbow.initializer.configuration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;


public class UIConfigurationLoaderTest {
    UIConfigurationLoader cfg;

    @Before
    public void prepare() {
        cfg  = new UIConfigurationLoader();
    }

    @Test
    public void loadY() throws Exception {
        String input = "yes";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        try {
            cfg.loadConfiguration();
        } catch (IOException e) {
            throw e;
        }
    }

    @Test
    public void loadN() throws Exception {
        String input = "no";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        try {
            cfg.loadConfiguration();
            tearDown();
        } catch (IOException e) {
            throw e;
        }
    }

    @Test
    public void loadOther() throws Exception {
        String input = "other\nother\nother";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        try {
            cfg.loadConfiguration();
        } catch (IOException e) {
            throw e;
        }
    }

    @Test
    public void loadOtherY() throws Exception {
        String input = "other\nother\nyes";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        try {
            cfg.loadConfiguration();
        } catch (IOException e) {
            throw e;
        }
    }

    public void tearDown() throws Exception {
        try {
            Path path = Paths.get("config.yml");
            Files.delete(path);
        } catch (IOException e) {
            throw e;
        }
    }
}