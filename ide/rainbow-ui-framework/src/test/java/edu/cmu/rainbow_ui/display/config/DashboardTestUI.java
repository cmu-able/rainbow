package edu.cmu.rainbow_ui.display.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.server.VaadinRequest;

import edu.cmu.rainbow_ui.common.ISystemConfiguration;
import edu.cmu.rainbow_ui.common.SystemConfiguration;
import edu.cmu.rainbow_ui.display.ISystemViewProvider;
import edu.cmu.rainbow_ui.display.ui.AbstractRainbowVaadinUI;
import edu.cmu.rainbow_ui.display.ui.RainbowVaadinUI;
import edu.cmu.rainbow_ui.display.viewcontrol.ViewControl;

public class DashboardTestUI extends AbstractRainbowVaadinUI {
    /**
     * View configuration for current UI session
     */
    private ViewConfiguration viewConfig;

    /**
     * View Configuration loader
     */
    private IViewConfigurationLoader viewConfigLoader;

    /**
     * Location of system settings file
     */
    private final static String SYSTEM_CONFIG_FILE = "src/test/resources/system_default.properties";

    /**
     * Default view config file
     */
    private final static String DEFAULT_VIEW_CONFIG = "src/test/resources/dashboard_test_view_config.yml";

    @Override
    public ISystemViewProvider getSystemViewProvider() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ViewControl getViewControl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ViewConfiguration getViewConfiguraion() {
        return this.viewConfig;
    }

    @Override
    public void pushUpdate() {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveViewConfiguration() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void init(VaadinRequest request) {
        try {
            ISystemConfiguration systemConfig = new SystemConfiguration(
                    SYSTEM_CONFIG_FILE);
            viewConfigLoader = new YamlViewConfigurationLoader(systemConfig);
            viewConfig = viewConfigLoader.readFromFile(DEFAULT_VIEW_CONFIG);
            /* If there is no view configuration - construct an empty one */
            if (viewConfig == null) {
                viewConfig = new ViewConfiguration();
            }
        } catch (IOException ex) {
            Logger.getLogger(RainbowVaadinUI.class.getName()).log(Level.SEVERE,
                    "Cannot load the view configuration", ex);
        }

    }

    @Override
    public void setViewConfiguration(ViewConfiguration viewConfig) {
        // TODO Auto-generated method stub
        
    }

}
