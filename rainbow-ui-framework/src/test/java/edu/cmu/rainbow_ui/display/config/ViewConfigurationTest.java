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

import edu.cmu.rainbow_ui.common.SystemConfiguration;
import edu.cmu.rainbow_ui.display.ApplicationCore;
import edu.cmu.rainbow_ui.display.ui.Dashboard;
import edu.cmu.rainbow_ui.display.ui.DummyTestUI;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for view configuration.
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class ViewConfigurationTest {

    /* Config locations */
    private static final String SYSTEM_CONFIG = "src/test/resources/system_default.properties";
    private static final String DEFAULT_CONFIG = "src/test/resources/view_config.yml";
    private static final String DASH_CONFIG = "src/test/resources/dashboard_test_view_config.yml";
    private static final String CANONICAL_CONFIG = "src/test/resources/view_config_output.yml";
    /* Test-wide configuration */
    private SystemConfiguration systemConfig;
    /* Test-wide loader */
    private YamlViewConfigurationLoader loader;
    private DummyTestUI ui;

    public ViewConfigurationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws IOException {
        systemConfig = new SystemConfiguration(SYSTEM_CONFIG);
        loader = new YamlViewConfigurationLoader(systemConfig);
        ui = new DummyTestUI();
        ui.init(null);
    }

    @After
    public void tearDown() {
    }

    /**
     * Read default configuration file and check the result.
     *
     * @throws java.io.FileNotFoundException
     */
    @Test
    public void testRead() throws FileNotFoundException {
        ViewConfiguration config = loader.readFromFile(DEFAULT_CONFIG);
        assertNotNull(config);
        assertNotNull(config.dashboard);
        List<DashboardPageConfiguration> pages = config.dashboard.pages;
        assertNotNull(pages);
        assertEquals(pages.size(), 2);
        DashboardPageConfiguration page1 = pages.get(0);
        DashboardPageConfiguration page2 = pages.get(1);
        assertNotNull(page1);
        assertNotNull(page2);
        assertEquals("BarChart", page1.widgets.get(0).id);
        assertEquals("Server0.load", page1.widgets.get(0).mapping);
        /* Check properties map */
        String name = (String) page1.widgets.get(0).properties.get("name");
        assertNotNull(name);
        assertEquals("Server0 Load", name);
        Double max = (Double) page1.widgets.get(0).properties.get("max");
        assertNotNull(max);
        assertEquals(new Double(100.0), max);
    }

    /**
     * Read default file and write it to temporary, them compare it with canonical.
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test
    public void testWrite() throws FileNotFoundException, IOException {
        ViewConfiguration config = loader.readFromFile(DEFAULT_CONFIG);
        /* Expected file is in more compact format than original
         * due to dumping formating */
        File expected = new File(systemConfig.getConfigDir() + "/" + CANONICAL_CONFIG);
        File tmpDir = new File(systemConfig.getConfigDir() + "/tmp/");
        tmpDir.mkdirs();

        /* Write and compare */
        loader.writeToFile(config, "tmp/view_config.yml");
        File result = new File(tmpDir, "view_config.yml");
        assertEquals(FileUtils.readLines(expected), FileUtils.readLines(result));

        /* Cleanup */
        result.delete();
        tmpDir.delete();
    }

    /**
     * Read default configuration file and check the result.
     *
     * @throws java.io.FileNotFoundException
     */
    @Test
    public void dashboardTestRead() throws FileNotFoundException {
        ViewConfiguration config = loader.readFromFile(DASH_CONFIG);
        assertNotNull(config);
        assertNotNull(config.dashboard);
        List<DashboardPageConfiguration> pages = config.dashboard.pages;
        assertNotNull(pages);
        assertEquals(pages.size(), 2);
        DashboardPageConfiguration page1 = pages.get(0);
        DashboardPageConfiguration page2 = pages.get(1);
        assertNotNull(page1);
        assertNotNull(page2);
        assertEquals("Test", page1.widgets.get(0).id);
        assertEquals("Test", page1.widgets.get(1).id);
        assertEquals("Server0.load", page1.widgets.get(0).mapping);
        assertEquals("Test", page2.widgets.get(0).id);
        assertEquals("Server0.load", page2.widgets.get(0).mapping);
    }

    /**
     * Write configuration file and check the result.
     *
     * @throws java.io.FileNotFoundException
     */
    @Test
    public void dashboardTestWrite() throws FileNotFoundException {
        ApplicationCore.registerWidgets();
        ui.setViewConfiguraion(loader.readFromFile(DASH_CONFIG));
        Dashboard dash = new Dashboard(ui);
        dash.saveViewConfiguration();
        ViewConfiguration config = ui.getViewConfiguraion();
        assertNotNull(config);
        assertNotNull(config.dashboard);
        List<DashboardPageConfiguration> pages = config.dashboard.pages;
        assertNotNull(pages);
        assertEquals(pages.size(), 2);
        DashboardPageConfiguration page1 = pages.get(0);
        DashboardPageConfiguration page2 = pages.get(1);
        assertNotNull(page1);
        assertNotNull(page2);
        assertEquals("Test", page1.widgets.get(0).id);
        assertEquals("Test", page1.widgets.get(1).id);
        assertEquals("Server0.load", page1.widgets.get(0).mapping);
        assertEquals("Test", page2.widgets.get(0).id);
        assertEquals("Server0.load", page2.widgets.get(0).mapping);
    }
}
