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

import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.DragStartMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import edu.cmu.rainbow_ui.common.SystemConfiguration;
import edu.cmu.rainbow_ui.display.components.CloneableLabel;
import edu.cmu.rainbow_ui.display.config.ViewConfiguration;
import edu.cmu.rainbow_ui.display.config.YamlViewConfigurationLoader;
import edu.cmu.rainbow_ui.display.ui.AbstractRainbowVaadinUI;
import edu.cmu.rainbow_ui.display.ui.Dashboard;
import edu.cmu.rainbow_ui.display.ui.MainContentType;
import edu.cmu.rainbow_ui.display.ui.SearchAndFilter;
import edu.cmu.rainbow_ui.display.viewcontrol.ViewControl;
import edu.cmu.rainbow_ui.display.timeline.TimelineView;

import java.io.IOException;

/**
 * This class creates the main layout for the application
 * 
 * <p>
 * The main layout consists of a menu bar, main content area, time line and
 * dashboard.
 * </p>
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class TestMainLayout extends VerticalLayout {

    private final HorizontalLayout menuBar;
    private final MenuBar mainMenu;
    private final ComboBox viewSelector;
    private final MenuBar settingsMenu;
    private final Button currentTime;
    private final Button alertsNotifications;
    private final SearchAndFilter search;
    private final HorizontalLayout mainContentArea;
    private final TimelineView timeline;
    private Dashboard dashboard;
    private final AbstractRainbowVaadinUI ui;
    private Component mainContentFill;
    private ViewConfiguration viewConfiguration;
    private ViewControl viewControl;
    private ISystemViewProvider systemViewProvider;

    /**
     * Constructor
     * 
     * Creates a new main layout
     */
    public TestMainLayout(AbstractRainbowVaadinUI ui) {
        this.ui = ui;
        this.setWidth(100, Sizeable.Unit.PERCENTAGE);
        this.setHeight("-1px");
        this.viewControl = new ViewControl(ui);

        menuBar = new HorizontalLayout();

        mainMenu = new MenuBar();
        MenuItem menu = mainMenu.addItem("Menu", null);
        // TODO Add menu items
        menuBar.addComponent(mainMenu);

        viewSelector = new ComboBox();
        viewSelector.setInputPrompt("Select View");
        viewSelector.setNullSelectionAllowed(false);
        menuBar.addComponent(viewSelector);

        settingsMenu = new MenuBar();
        MenuItem settings = settingsMenu.addItem("Settings", null);
        // TODO Add menu items
        menuBar.addComponent(settingsMenu);

        Label spacer = new Label("&nbsp;", ContentMode.HTML);
        spacer.setSizeUndefined();
        menuBar.addComponent(spacer);
        menuBar.setExpandRatio(spacer, 1.0f);

        currentTime = new Button("Time Displayed");
        // TODO Add Button OnClickListener
        menuBar.addComponent(currentTime);

        alertsNotifications = new Button("Alerts: ");
        // TODO Add Button OnClickListener
        menuBar.addComponent(alertsNotifications);

        Label spacer2 = new Label("&nbsp;", ContentMode.HTML);
        spacer2.setSizeUndefined();
        menuBar.addComponent(spacer2);
        menuBar.setExpandRatio(spacer2, 1.0f);

        search = new SearchAndFilter();
        search.setInputPrompt("Search");
        menuBar.addComponent(search);

        menuBar.setWidth(100, Sizeable.Unit.PERCENTAGE);
        this.addComponent(menuBar);

        mainContentArea = new HorizontalLayout();
        mainContentArea.setWidth(100, Sizeable.Unit.PERCENTAGE);
        this.addComponent(mainContentArea);
        this.setExpandRatio(mainContentArea, 1.0f);

        CloneableLabel test = new CloneableLabel("TEST LABEL");
        DragAndDropWrapper testWrap = new DragAndDropWrapper(test);
        testWrap.setDragStartMode(DragStartMode.COMPONENT);
        testWrap.setSizeUndefined();
        mainContentArea.addComponent(testWrap);

        CloneableLabel test2 = new CloneableLabel("OTHER LABEL");
        DragAndDropWrapper testWrap2 = new DragAndDropWrapper(test2);
        testWrap2.setDragStartMode(DragStartMode.COMPONENT);
        testWrap2.setSizeUndefined();
        mainContentArea.addComponent(testWrap2);
        mainContentArea.setSpacing(true);

        timeline = new TimelineView(ui.getSystemViewProvider());
        timeline.setSizeUndefined();
        this.addComponent(timeline);

        ApplicationCore.registerWidgets();
        dashboard = new Dashboard(this.ui);
        this.addComponent(dashboard);
        this.setExpandRatio(dashboard, 0.25f);
    }

    public void setMainContentArea(MainContentType type) {
        switch (type) {
        default:
            mainContentArea.removeAllComponents();
        }
    }
}
