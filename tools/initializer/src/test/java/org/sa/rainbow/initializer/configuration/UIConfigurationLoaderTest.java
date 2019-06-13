package org.sa.rainbow.initializer.configuration;

import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.initializer.models.TemplateSet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UIConfigurationLoaderTest {
    UIConfigurationLoader cfg;

    @Before
    public void prepare() {
        cfg = new UIConfigurationLoader();
    }

    @Test
    public void loadY() throws Exception {
        try {
            String input = "yes";
            InputStream in = new ByteArrayInputStream(input.getBytes());
            System.setIn(in);
            cfg.loadConfiguration();
        } catch (IOException e) {
            throw e;
        }
    }

    @Test
    public void loadN() throws Exception {
        try {
            String input = "no";
            InputStream in = new ByteArrayInputStream(input.getBytes());
            System.setIn(in);
            cfg.loadConfiguration();
        } catch (IOException e) {
            throw e;
        }
    }

    @Test
    public void loadOther() throws Exception {
        try {
            String input = "other";
            InputStream in = new ByteArrayInputStream(input.getBytes());
            System.setIn(in);
            cfg.loadConfiguration();
        } catch (IOException e) {
            throw e;
        }
    }

    @Test
    public void loadOtherY() throws Exception {
        try {
            cfg.loadConfiguration();
        } catch (IOException e) {
            throw new Exception(e);
        }
    }
}