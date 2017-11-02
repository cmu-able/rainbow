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

/**
 * Describes Rainbow UI Framework System Configuration file, defining
 * configuration properties access methods.
 * 
 * <p>
 * Methods describes typed safe configuration properties access methods.
 * </p>
 * 
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public interface ISystemConfiguration {

    /**
     * Get Rainbow host.
     * 
     * @return Rainbow host or IP as a String
     */
    public String getRainbowHost();

    /**
     * Set Rainbow host.
     * 
     * @param host string hostname or ip
     */
    public void setRainbowHost(String host);

    /**
     * Get Rainbow port.
     * 
     * @return port
     */
    public short getRainbowPort();

    /**
     * Set Rainbow port
     * 
     * @param port
     */
    public void setRainbowPort(short port);

    /**
     * Get initial model location.
     * 
     * @return path to model
     */
    public String getModelPath();

    /**
     * Set initial model location
     * 
     * @param path
     */
    public void setModelPath(String path);

    /* Other getters/setters */
    /**
     * Get the Cassandra node location
     * 
     * @return node
     */
    public String getNode();

    /**
     * Set the Cassandra node.
     * 
     * @param node Cassandra node
     */
    public void setNode(String node);

    /**
     * Get the attached status of the system
     * 
     * @return status
     */
    public boolean isAttached();

    /**
     * Set the attached status of the system
     * 
     * @param attached
     */
    public void setAttached(boolean attached);

    /**
     * Get the configuration directory for the application
     * 
     * @return path to the directory
     */
    public String getConfigDir();

    /**
     * Set the configuration directory
     * 
     * @param path the directory path
     */
    public void setConfigDir(String path);

    /**
     * Get the snapshot saving rate
     * 
     * @return the rate
     */
    public int getSnapshotRate();

    /**
     * Set the snapshot saving rate
     * 
     * @param rate saving rate
     */
    public void setSnapshotRate(int rate);

    /**
     * Check whether the model should be loaded from local file.
     * 
     * @return boolean whether model is local
     */
    public boolean isModelLocal();

    /**
     * Set whether the model should be loaded from local file.
     * 
     * @param local whether model is local
     */
    public void setModelLocal(boolean local);

    /**
     * Get the model name.
     * 
     * @return model name
     */
    public String getModelName();

    /**
     * Set the model name.
     * 
     * @param name new model name
     */
    public void setModelName(String name);

    /**
     * Get the class to be used for model factory.
     * 
     * @return model class
     */
    public String getModelFactoryClass();

    /**
     * Set the class to be used for model factory.
     * 
     * @param cls new model class
     */
    public void setModelFactoryClass(String cls);

    /**
     * Get the rate at which the ui should update.
     * 
     * @return the rate at which the UI should update
     */
    public int getUpdateRate();

    /**
     * set the rate at which the ui should update.
     * 
     * @param the rate at which the UI should update
     */
    public void setUpdateRate(int rate);

}
