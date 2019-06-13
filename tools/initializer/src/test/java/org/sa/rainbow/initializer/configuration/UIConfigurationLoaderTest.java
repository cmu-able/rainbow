package org.sa.rainbow.initializer.configuration;

import org.junit.Before;
import org.junit.Test;
import org.sa.rainbow.initializer.models.TemplateSet;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UIConfigurationLoaderTest {
    UIConfigurationLoader cfg;

    @Before
    public void prepare() {
       cfg  = spy(UIConfigurationLoader.class);
    }

    @Test
    public void loadY() throws Exception {
        when(cfg.mockInput(0)).thenReturn("yes");
        try {
            cfg.loadConfiguration();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void loadN() throws Exception {
        when(cfg.mockInput(0)).thenReturn("no");
        try {
            cfg.loadConfiguration();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void loadOther() throws Exception {
        when(cfg.mockInput(2)).thenReturn("oth");
        try {
            cfg.loadConfiguration();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void loadOtherY() throws Exception {
        try {
            cfg.loadConfiguration();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}