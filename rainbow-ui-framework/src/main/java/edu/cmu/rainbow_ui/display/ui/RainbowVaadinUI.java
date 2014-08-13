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
package edu.cmu.rainbow_ui.display.ui;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.ClientConnector;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UIDetachedException;
import edu.cmu.rainbow_ui.common.ISystemConfiguration;
import edu.cmu.rainbow_ui.display.AcmeSystemViewProvider;
import edu.cmu.rainbow_ui.display.ApplicationCore;
import edu.cmu.rainbow_ui.display.ISystemViewProvider;
import edu.cmu.rainbow_ui.display.config.IViewConfigurationLoader;
import edu.cmu.rainbow_ui.display.config.ViewConfiguration;
import edu.cmu.rainbow_ui.display.config.YamlViewConfigurationLoader;
import edu.cmu.rainbow_ui.display.viewcontrol.ViewControl;
import edu.cmu.rainbow_ui.ingestion.IRuntimeAggregator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

/**
 * This class initializes the UI
 *
 * <p>
 * The application UI is initialized and started from this class
 * </p>
 *
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
@Theme("rainbow-theme")
@SuppressWarnings("serial")
@Push
// Allow push
public class RainbowVaadinUI extends AbstractRainbowVaadinUI implements ClientConnector.DetachListener {

    /**
     * Main Layout
     */
    protected MainLayout mainLayout;
    /**
     * System view provider to be used in the current UI session
     */
    private ISystemViewProvider systemViewProvider;
    /**
     * View control
     */
    private ViewControl viewControl;
    /**
     * View configuration for current UI session
     */
    private ViewConfiguration viewConfig;

    /**
     * Location of system settings file
     */
    private final static String SYSTEM_CONFIG_FILE = "system.properties";

    /**
     * Default view config file
     */
    private final static String DEFAULT_VIEW_CONFIG = "default_view_config.yml";

    /**
     * View Configuration loader
     */
    private IViewConfigurationLoader viewConfigLoader;

    /**
     * Servlet definition
     *
     * The application is deployed to the servlet specified
     */
    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = RainbowVaadinUI.class, widgetset = "edu.cmu.rainbow_ui.display.AppWidgetSet", heartbeatInterval = 10)
    public static class Servlet extends VaadinServlet {

        /**
         * Override the servlet initialization to add Application Core startup
         *
         * @throws ServletException
         */
        @Override
        protected void servletInitialized() throws ServletException {
            super.servletInitialized();
            ApplicationCore appCore = ApplicationCore.getInstance();

            try {
                appCore.startup(SYSTEM_CONFIG_FILE);
            } catch (Exception ex) {
                throw new ServletException(ex);
            }
        }
    }

    @Override
    public ISystemViewProvider getSystemViewProvider() {
        return systemViewProvider;
    }

    @Override
    public ViewControl getViewControl() {
        return viewControl;
    }

    @Override
    public ViewConfiguration getViewConfiguraion() {
        return viewConfig;
    }

    /**
     * Initializes the application.
     *
     * Sets the layout and launches all needed components
     *
     * @param request handles the request to open the application
     */
    @Override
    protected void init(VaadinRequest request) {
        /**
         * Add UI detach listener
         */
        addDetachListener(this);
        
        /* Get the application core instance */
        ApplicationCore appCore = ApplicationCore.getInstance();

        ISystemConfiguration systemConfig = appCore.getSystemConfiguration();
        IRuntimeAggregator runtimeAggr = appCore.getRuntimeAggregator();

        /**
         * Load default view configuration.
         *
         * <ol>
         * <li> Try to load the latest view config from the database </li>
         * <li> Try to load the default one </li>
         * <li> Use empty view configuration </li>
         * </ol>
         */
        String latestViewConfigName = appCore.getLatestViewConfigurationName();

        if (latestViewConfigName != null) {
            viewConfig = appCore.getViewConfiguration(latestViewConfigName);
        }
        if (viewConfig == null) {
            try {
                viewConfigLoader = new YamlViewConfigurationLoader(systemConfig);
                viewConfig = viewConfigLoader.readFromFile(DEFAULT_VIEW_CONFIG);
                /* If there is no view configuration - construct an empty one */
                if (viewConfig == null) {
                    viewConfig = new ViewConfiguration();
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(RainbowVaadinUI.class.getName()).log(Level.SEVERE,
                        "Cannot load the view configuration", ex);
            }
        } else {
            if (Page.getCurrent() != null) {
                Notification.show("Loaded the latest view configuration: " + latestViewConfigName, 
                        Notification.Type.HUMANIZED_MESSAGE);
            }
        }

        /**
         * Create view control
         */
        viewControl = new ViewControl(this);

        /**
         * Create system view provider
         */
        systemViewProvider = new AcmeSystemViewProvider(runtimeAggr, this,
                systemConfig);

        if (appCore.isAttached()) {
            systemViewProvider.setSession(appCore.getWriteSession());
            systemViewProvider.setUseCurrent();
        }

        /**
         * Create the UI hierarchy
         */
        mainLayout = new MainLayout(this);
        setContent(mainLayout);
    }

    /**
     * Handle the UI expiration. When the UI is expired it should release its acquired resource.
     */
    @Override
    public void detach(DetachEvent event) {
        systemViewProvider.stop();
    }
    
    @Override
    public void saveViewConfiguration() {
        try {
            this.viewConfigLoader.writeToFile(this.viewConfig,
                    "system_view_configuration" + new Date().toString());
        } catch (IOException e) {
            Logger.getLogger(RainbowVaadinUI.class.getName()).log(Level.SEVERE,
                    "saveViewConfiguration", e);
        }
    }

    @Override
    public void pushUpdate() {
        try {
            getUI().push();
        } catch (UIDetachedException ex) {
            Logger.getLogger(RainbowVaadinUI.class.getName()).log(Level.WARNING,
                    "The UI is detached", ex);
        }
    }

    @Override
    public void setViewConfiguration(ViewConfiguration viewConfig) {
        this.viewConfig = viewConfig;
    }
}
