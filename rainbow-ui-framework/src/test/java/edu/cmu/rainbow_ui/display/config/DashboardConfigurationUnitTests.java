package edu.cmu.rainbow_ui.display.config;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cmu.rainbow_ui.display.MockDashboard;

public class DashboardConfigurationUnitTests {
    DashboardTestUI ui;
    MockDashboard dashboard;
    private static final String CANONICAL_CONFIG = "src/test/resources/view_config_output.yml";

    @Before
    public void setUp() throws Exception {
        ui = new DashboardTestUI();
        ui.init(null);
        dashboard = new MockDashboard(ui);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void configurationLoadTest() {
        assertNotNull(dashboard.getDashboardUI());

        assertEquals(ui.getViewConfiguraion().dashboard.pages.size(),
                dashboard.getNumPages());
    }
    
    @Test
    public void saveConfiguration() {
        dashboard.saveViewConfiguration();
    }

}
