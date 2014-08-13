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

import org.junit.After;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import edu.cmu.rainbow_ui.display.ApplicationCore;
import edu.cmu.rainbow_ui.display.ISystemViewProvider;
import edu.cmu.rainbow_ui.display.ui.Dashboard;

/**
 * This class contains unit tests for testing the Dashboard component
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class DashboardTest {
    Dashboard dashboard;
    ISystemViewProvider systemViewProvider;

    /**
     * Set up the tests by initializing a dummy ui, registering widgets, and
     * creating a new dashboard
     */
    @Before
    public void setUp() throws Exception {
        DummyTestUI ui = new DummyTestUI();
        ui.init(null);
        ApplicationCore.registerWidgets();
        dashboard = new Dashboard(ui);
        ui.setContent(dashboard);
    }

    /**
     * Cleanup after tests
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test adding a page with a name of "test"
     */
    @Test
    public void addPage() {
        dashboard.newPageBtn.click();
        dashboard.txtPageName.setValue("Test");
        dashboard.btnCreate.click();
        assertEquals(dashboard.tabs.size(), 2);
        boolean test = false;
        for (int i = 0; i < dashboard.pageContentArea.getComponentCount(); i++) {
            if (dashboard.pageContentArea.getTab(i).getCaption().equals("Test")) {
                test = true;
            }
        }
        assertEquals(test, true);
    }

    /**
     * Test removing a page
     */
    @Test
    public void removePage() {
        addPage();
        dashboard.removePageBtn.click();
        assertEquals(dashboard.tabs.size(), 1);
        boolean test = false;
        for (int i = 0; i < dashboard.pageContentArea.getComponentCount(); i++) {
            if (dashboard.pageContentArea.getTab(i).getCaption().equals("Test")) {
                test = true;
            }
        }
        assertEquals(test, false);
    }

    /**
     * Test setting a page
     */
    @Test
    public void setPage() {
        for (int i = 0; i < 5; i++) {
            dashboard.newPageBtn.click();
            dashboard.txtPageName.setValue("Test" + i);
            dashboard.btnCreate.click();
        }
        assertEquals(dashboard.tabs.size(), 6);
        dashboard.setPage(3);
        assertEquals(
                dashboard.pageContentArea.getTab(
                        dashboard.pageContentArea.getSelectedTab())
                        .getCaption(), "Test2");
    }

    /**
     * Test removing all pages, then trying to click remove page again
     */
    // Negative Tests
    @Test
    public void removeAllPages() {
        while (dashboard.tabs.size() > 0) {
            dashboard.removePageBtn.click();
        }
        assertEquals(dashboard.tabs.size(), 0);
        dashboard.removePageBtn.click();
    }

    /**
     * Test adding a page without a name, an error message is shown
     */
    @Test
    public void addPageWithoutName() {
        while (dashboard.tabs.size() > 0) {
            dashboard.removePageBtn.click();
        }
        assertEquals(dashboard.tabs.size(), 0);
        dashboard.newPageBtn.click();
        try {
            dashboard.btnCreate.click();
        } catch (com.vaadin.event.ListenerMethod.MethodException ex) {
            /*
             * Expected exception caused by trying to show a notification
             * without a visible UI
             */
        }
        assertEquals(dashboard.tabs.size(), 0);
    }

    /**
     * Test setting the page to a location outside of its bounds
     */
    @Test
    public void setPageOutsideBouds() {
        for (int i = 0; i < 5; i++) {
            dashboard.newPageBtn.click();
            dashboard.txtPageName.setValue("Test" + i);
            dashboard.btnCreate.click();
        }
        assertEquals(dashboard.tabs.size(), 6);
        dashboard.setPage(3);
        assertEquals(
                dashboard.pageContentArea.getTab(
                        dashboard.pageContentArea.getSelectedTab())
                        .getCaption(), "Test2");
        dashboard.setPage(10);
        assertEquals(
                dashboard.pageContentArea.getTab(
                        dashboard.pageContentArea.getSelectedTab())
                        .getCaption(), "Test2");
        dashboard.setPage(-1);
        assertEquals(
                dashboard.pageContentArea.getTab(
                        dashboard.pageContentArea.getSelectedTab())
                        .getCaption(), "Test2");
    }
}
