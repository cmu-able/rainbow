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
package edu.cmu.rainbow_ui.display;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.reflections.Reflections;

import edu.cmu.rainbow_ui.common.ISystemConfiguration;
import edu.cmu.rainbow_ui.common.MockRainbow;
import edu.cmu.rainbow_ui.common.SystemConfiguration;
import edu.cmu.rainbow_ui.display.config.ViewConfiguration;
import edu.cmu.rainbow_ui.display.config.YamlViewConfigurationLoader;
import edu.cmu.rainbow_ui.display.viewcontrol.WidgetLibrary;
import edu.cmu.rainbow_ui.display.widgets.IWidget;
import edu.cmu.rainbow_ui.display.widgets.WidgetDescription;
import edu.cmu.rainbow_ui.ingestion.AcmeRuntimeAggregator;
import edu.cmu.rainbow_ui.ingestion.IRuntimeAggregator;
import edu.cmu.rainbow_ui.ingestion.RuntimeAggregatorException;
import edu.cmu.rainbow_ui.storage.DatabaseConnector;
import edu.cmu.rainbow_ui.storage.IDatabaseConnector;

/**
 * This class provides system-wide functions by interfacing with various other components
 *
 * <p>
 * The Application core provides functionality that affects the system as a whole, such as setting
 * the session, starting the background threads, saving the configuration and attaching/detaching
 * the system from a Rainbow instance
 * </p>
 *
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class ApplicationCore {

    protected boolean attached = false; // attached or detached
    protected IRuntimeAggregator<?> runtimeAgg;
    protected IDatabaseConnector databaseCon;
    protected DatabaseConnector configDatabaseCon;
    protected ISystemConfiguration sysConfig;
    /*
     * Used for testing, if true a mock rainbow will be created to randomly feed
     * events to the runtime aggregator
     */
    protected boolean CREATE_MOCK_RAINBOW = false;
    protected MockRainbow mockRainbow;

    protected static ApplicationCore instance = null;

    /**
     * Create a singleton instance of Application Core
     *
     * @return application core instance
     */
    public static ApplicationCore getInstance() {
        if (instance == null) {
            instance = new ApplicationCore();
        }

        return instance;
    }

    /**
     * Protected default constructor to prevent direct creation.
     */
    protected ApplicationCore() {
    }

    /**
     * Initializes the application and starts the background threads
     *
     * @param filename contains the path to the file that contains the system configuration
     * @throws java.lang.Exception
     */
    public void startup(String filename) throws Exception {
        sysConfig = new SystemConfiguration(filename);

        try {
            startBackground();
        } catch (Exception e) {
            // Rethrow up to UI
            throw e;
        }

        registerWidgets();
    }

    /**
     * Starts the necessary background threads based on the configuration
     *
     * @throws java.lang.Exception
     */
    protected void startBackground() throws Exception {
        try {
            runtimeAgg = new AcmeRuntimeAggregator(sysConfig);
            if (CREATE_MOCK_RAINBOW) {
                mockRainbow = new MockRainbow(this.runtimeAgg);
            }
        } catch (RuntimeAggregatorException ex) {
            // Rethrow up to UI
            throw ex;
        }

        databaseCon = new DatabaseConnector(sysConfig);
        configDatabaseCon = new DatabaseConnector(sysConfig);
        configDatabaseCon.createConfigKeyspace();

        if (sysConfig.isAttached()) {
            attach();
        }
    }

    /**
     * Causes the runtime aggregator to attach to the Rainbow system
     */
    public void attach() {
        if (!attached) {
            try {
                runtimeAgg.start();
                attached = true;
                if (CREATE_MOCK_RAINBOW) {
                    mockRainbow.run();
                }
            } catch (RuntimeAggregatorException ex) {
                Logger.getLogger(ApplicationCore.class.getName()).log(Level.SEVERE,
                        "Cannot attach to the Rainbow system.", ex);
            }
        }
    }

    /**
     * Causes the runtime aggregator to detach from the Rainbow system
     */
    public void detach() // Disconnect from Rainbow
    {
        if (attached) {
            runtimeAgg.stop();
            attached = false;

            if (CREATE_MOCK_RAINBOW) {
                mockRainbow.cancel();
            }
        }
    }

    /**
     * Gets the current session's id
     *
     * @return a string that represents the session currently being written to
     */
    public String getWriteSession() {
        return databaseCon.getWriteSession();
    }

    /**
     * Gets a list of sessions that are saved in the database
     *
     * @return a list of strings representing the sessions saved
     */
    public ArrayList<String> getSessionList() {
        return databaseCon.getSessionList();
    }

    /**
     * Gives the status for the runtime aggregator on whether it is attached or not
     *
     * @return attached a boolean that is true if the runtime aggregator is attached to a Rainbow
     * system
     */
    public boolean isAttached() {
        return attached;
    }

    /**
     * Registers all widgets in the widget library. The registration may happen independently of
     * Application Core existence(e.g. in tests), so the method is static
     */
    public static void registerWidgets() {
        Reflections reflection = new Reflections(
                "edu.cmu.rainbow_ui.display.widgets");
        Set<Class<? extends IWidget>> allClasses = reflection.getSubTypesOf (IWidget.class);
        for (Class<? extends IWidget> classObj : allClasses) {
            try {
                Method method = classObj.getDeclaredMethod("register");
                method.setAccessible(true);
                WidgetDescription descr = (WidgetDescription) method.invoke(null);
                WidgetLibrary.register(descr);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                    NoSuchMethodException | SecurityException | ClassCastException e) {
                Logger.getLogger(ApplicationCore.class.getName()).log(
                        Level.WARNING, "Cannot register a widget.", e);
            }

        }
    }

    /**
     * Get the Runtime Aggregator
     *
     * @return runtime aggregator
     */
    public IRuntimeAggregator<?> getRuntimeAggregator() {
        return runtimeAgg;
    }

    /**
     * Get the system configuration.
     *
     * @return system configuration
     */
    public ISystemConfiguration getSystemConfiguration() {
        return sysConfig;
    }

    /**
     * Get the list of existing view configurations.
     *
     * @return list of configuration names
     */
    public ArrayList<String> getViewConfigurations() {
        return configDatabaseCon.getConfigurationList("viewConfig");
    }

    /**
     * Get a view configuration with the specific name
     *
     * @param name name of the configuration
     * @return view configuration
     */
    public ViewConfiguration getViewConfiguration(String name) {
        String configString = configDatabaseCon.getConfiguration("viewConfig", name);
        YamlViewConfigurationLoader yaml = new YamlViewConfigurationLoader(null);
        return yaml.readFromString(configString);
    }

    /**
     * Get the name of the latest view configuration.
     *
     * @return configuration name or null if no configuration exists
     */
    public String getLatestViewConfigurationName() {
        return configDatabaseCon.getLatestConfigurationName("viewConfig");
    }

    /**
     * Save the given view configuration under the given name
     *
     * @param config view configuration
     * @param name name of the configuration
     */
    public void saveViewConfiguration(ViewConfiguration config, String name) {
        YamlViewConfigurationLoader yaml = new YamlViewConfigurationLoader(null);
        String configuration = yaml.writeToString(config);
        configDatabaseCon.writeConfiguration(configuration, "viewConfig", name);
    }

}
