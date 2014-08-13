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
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.communication.PushMode;
import edu.cmu.rainbow_ui.common.SystemConfiguration;
import edu.cmu.rainbow_ui.display.DummySystemViewProvider;
import edu.cmu.rainbow_ui.display.ISystemViewProvider;
import edu.cmu.rainbow_ui.display.config.IViewConfigurationLoader;
import edu.cmu.rainbow_ui.display.config.ViewConfiguration;
import edu.cmu.rainbow_ui.display.config.YamlViewConfigurationLoader;
import edu.cmu.rainbow_ui.display.viewcontrol.MockViewControl;
import edu.cmu.rainbow_ui.display.viewcontrol.ViewControl;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class initializes a dummy UI.
 *
 * <p>
 * A dummy UI is initialized and started from this class
 * </p>
 *
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
@Theme("rainbow-theme")
@SuppressWarnings("serial")
@Push(PushMode.MANUAL)
public class DummyTestUI extends AbstractRainbowVaadinUI {

    private final String SYSTEM_CONFIG_FILE = "src/test/resources/system_default.properties";
    private final String DEFAULT_VIEW_CONFIG = "src/test/resources/view_config_default.yml";
    private ViewConfiguration viewConfiguration;
    private SystemConfiguration systemConfiguration;
    private DummySystemViewProvider systemViewProvider;
    private ViewControl viewControl;
    /**
     * View Configuration loader
     */
    private IViewConfigurationLoader viewConfigLoader;

    /**
     * Initializes the application
     *
     * Sets the layout and launches all needed components
     *
     * @param request handles the request to open the application
     */
    @Override
    public void init(VaadinRequest request) {
        try {
            systemConfiguration = new SystemConfiguration(SYSTEM_CONFIG_FILE);
            viewConfigLoader = new YamlViewConfigurationLoader(systemConfiguration);
            viewConfiguration = viewConfigLoader.readFromFile(DEFAULT_VIEW_CONFIG);
            systemViewProvider = new DummySystemViewProvider();
            viewControl = new MockViewControl(this);
        } catch (IOException ex) {
            Logger.getLogger(DummyTestUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pushUpdate() {
        // Do nothing
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
        return viewConfiguration;
    }
    
    public void setViewConfiguraion(ViewConfiguration vc) {
        this.viewConfiguration = vc;
    }

    @Override
    public void saveViewConfiguration() {
        // Do nothing
    }

    @Override
    public void setViewConfiguration(ViewConfiguration viewConfig) {
        // Do nothing        
    }

}
