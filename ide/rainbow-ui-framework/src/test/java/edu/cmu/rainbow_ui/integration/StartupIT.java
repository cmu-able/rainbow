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
package edu.cmu.rainbow_ui.integration;

import edu.cmu.rainbow_ui.common.ISystemConfiguration;
import edu.cmu.rainbow_ui.display.ISystemViewProvider;
import edu.cmu.rainbow_ui.display.AccessibleApplicationCore;
import edu.cmu.rainbow_ui.display.config.ViewConfiguration;
import edu.cmu.rainbow_ui.display.ui.MainLayout;
import edu.cmu.rainbow_ui.display.ui.MockRainbowVaadinUI;
import edu.cmu.rainbow_ui.display.viewcontrol.ViewControl;
import edu.cmu.rainbow_ui.ingestion.IRuntimeAggregator;
import edu.cmu.rainbow_ui.storage.DatabaseConnector;
import org.junit.After;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * This class provides an integration test for system startup
 *
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class StartupIT {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Tests the startup of the system's UI and background via the applicationcore
     *
     * @throws java.lang.Exception
     */
    @Test
    public void startupTest() throws Exception {
        AccessibleApplicationCore mockAppCore = (AccessibleApplicationCore)AccessibleApplicationCore.getInstance();
        /* We do not want Mock Rainbow to interfere with these IT tests */
        mockAppCore.setUseMockRainbow(false);
        mockAppCore.startup("system.properties");

        IRuntimeAggregator<?> runtimeAggregator = mockAppCore.getRuntimeAggregator();
        assertNotNull(runtimeAggregator);
        ISystemConfiguration sysConfig = mockAppCore.getSystemConfiguration();
        assertNotNull(sysConfig);
        DatabaseConnector databaseConn = (DatabaseConnector) mockAppCore.getDBC();
        assertNotNull(databaseConn);

        MockRainbowVaadinUI ui = new MockRainbowVaadinUI();
        ui.init();

        ISystemViewProvider svp = ui.getSystemViewProvider();
        assertNotNull(svp);
        ViewControl viewControl = ui.getViewControl();
        assertNotNull(viewControl);
        ViewConfiguration viewConfiguration = ui.getViewConfiguraion();
        assertNotNull(viewConfiguration);
        MainLayout mainLayout = ui.getMainLayout();
        assertNotNull(mainLayout);

        assertEquals(sysConfig.isAttached(), mockAppCore.isAttached());
        assertEquals(ui.getContent(), mainLayout);
    }

}
