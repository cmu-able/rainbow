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
package edu.cmu.rainbow_ui.display.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import edu.cmu.rainbow_ui.common.ISystemConfiguration;

/**
 * View configuration loader for Yaml format.
 * 
 * <p>
 * Implements write and read for Yaml representation of view configuration.
 * </p>
 * 
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class YamlViewConfigurationLoader implements IViewConfigurationLoader {
    /**
     * System configuration used to locate configuration directory
     */
    private final ISystemConfiguration systemConfig;
    /**
     * The Yaml parser
     */
    private final Yaml yaml;

    /**
     * Loader constructor.
     * 
     * @param config system configuration
     */
    public YamlViewConfigurationLoader(ISystemConfiguration config) {
        systemConfig = config;
        yaml = new Yaml(new Constructor(ViewConfiguration.class));
    }

    @Override
    public ViewConfiguration readFromFile(String filename)
            throws FileNotFoundException {
        File configFile = new File(systemConfig.getConfigDir() + "/" + filename);
        InputStream is = new FileInputStream(configFile);
        return (ViewConfiguration) yaml.load(is);
    }

    @Override
    public void writeToFile(ViewConfiguration config, String filename)
            throws IOException {
        FileWriter writer = new FileWriter(systemConfig.getConfigDir() + "/"
                + filename);
        yaml.dump(config, writer);
    }
    
    public ViewConfiguration readFromString(String config) {
        return (ViewConfiguration) yaml.load(config);
    }
    
    public String writeToString(ViewConfiguration config) {
        String yamlOut = yaml.dump(config);
        int header = yamlOut.indexOf("\n");
        yamlOut = yamlOut.substring(header+1);
        return yamlOut;
    }
}
