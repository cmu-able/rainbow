/*
 * The MIT License
 *
 * Copyright 2014 CMU MSIT-SE Rainbow Team.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package edu.cmu.rainbow_ui.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * System Configuration class for Rainbow UI Framework.
 * 
 * <p>
 * Implements system configuration based on Java .properties format.
 * </p>
 * 
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class SystemConfiguration extends Properties implements
        ISystemConfiguration {
    /**
     * Location of configuration files.
     */
    private String configDir;

    private final String CONFIG_DIR_JNDI_NAME = "java:comp/env/rainbow-ui.configdir";
    private final String CONFIG_DIR_SYSTEM_PROP = "rainbow-ui.configdir";

    /**
     * Create system configuration and load it from file.
     * 
     * @param filename path to configuration file
     * @throws IOException
     */
    public SystemConfiguration(String filename) throws IOException {
        /* Look up for configuration directory location */
        /* Try the JNDI source first */
        String dir;
        try {
            InitialContext context = new InitialContext();
            dir = (String) context.lookup(CONFIG_DIR_JNDI_NAME);
        } catch (NamingException ex) {
            dir = null;
        }

        if (dir == null) {
            /* Look up in System Properties */
            dir = System.getProperty(CONFIG_DIR_SYSTEM_PROP);
        }

        if (dir == null) {
            /* Assign current directory */
            dir = "./";
        }

        configDir = dir;

        File configFile = new File(configDir + "/" + filename);
        InputStream is = new FileInputStream(configFile);
        this.load(is);
    }

    /**
     * {@inheritDoc}
     * <p>
     * rainbow.host = ip or hostname
     * </p>
     */
    @Override
    public String getRainbowHost() {
        String prop = getProperty("rainbow.host");
        return prop;
    }

    /**
     * {@inheritDoc}
     * <p>
     * rainbow.host = ip or hostname
     * </p>
     */
    @Override
    public void setRainbowHost(String host) {
        setProperty("rainbow.host", host);
    }

    /**
     * {@inheritDoc}
     * <p>
     * rainbow.port = numeric port
     * </p>
     */
    @Override
    public short getRainbowPort() {
        String prop = getProperty("rainbow.port");
        return Short.valueOf(prop);
    }

    /**
     * {@inheritDoc}
     * <p>
     * rainbow.port = numeric port
     * </p>
     */
    @Override
    public void setRainbowPort(short port) {
        setProperty("rainbow.port", Short.toString(port));
    }

    /**
     * {@inheritDoc}
     * <p>
     * ingestion.model.initial = path
     * </p>
     */
    @Override
    public String getModelPath() {
        String prop = getProperty("ingestion.model.path");
        return prop;
    }

    /**
     * {@inheritDoc}
     * <p>
     * ingestion.model.initial = path
     * </p>
     */
    @Override
    public void setModelPath(String path) {
        setProperty("ingestion.model.path", path);
    }

    /**
     * {@inheritDoc}
     * <p>
     * storage.cassandra.node = url
     * </p>
     */
    @Override
    public String getNode() {
        String prop = getProperty("storage.cassandra.node");
        return prop;
    }

    /**
     * {@inheritDoc}
     * <p>
     * storage.cassandra.node = url
     * </p>
     */
    @Override
    public void setNode(String node) {
        setProperty("storage.cassandra.node", node);
    }

    /**
     * {@inheritDoc}
     * <p>
     * ingestion.attach.onstartup = boolean
     * </p>
     */
    @Override
    public boolean isAttached() {
        String prop = getProperty("ingestion.attach.onstartup");
        return Boolean.valueOf(prop);
    }

    /**
     * {@inheritDoc}
     * <p>
     * ingestion.attach.onstartup = boolean
     * </p>
     */
    @Override
    public void setAttached(boolean attached) {
        setProperty("ingestion.attach.onstartup", Boolean.toString(attached));
    }

    @Override
    public String getConfigDir() {
        return configDir;
    }

    @Override
    public void setConfigDir(String path) {
        configDir = path;
    }

    @Override
    public int getSnapshotRate() {
        String prop = getProperty("ingestion.snapshot.rate");
        return Integer.valueOf(prop);
    };

    @Override
    public void setSnapshotRate(int rate) {
        setProperty("ingestion.snapshot.rate", Integer.toString(rate));
    };

    @Override
    public boolean isModelLocal() {
        String prop = getProperty("ingestion.model.local", "false");
        return Boolean.valueOf(prop);
    }

    @Override
    public void setModelLocal(boolean local) {
        setProperty("ingestion.model.local", Boolean.toString(local));
    }

    @Override
    public String getModelName() {
        return getProperty("ingestion.model.name");
    }

    @Override
    public void setModelName(String name) {
        setProperty("ingestion.model.name", name);
    }

    @Override
    public String getModelFactoryClass() {
        return getProperty("ingestion.model.factory");
    }

    @Override
    public void setModelFactoryClass(String cls) {
        setProperty("ingestion.model.factory", cls);
    }

    @Override
    public int getUpdateRate() {
        return Integer.parseInt(getProperty("display.update.rate"));
    }

    @Override
    public void setUpdateRate(int rate) {
        setProperty("display.update.rate", Integer.toString(rate));
    }
}
