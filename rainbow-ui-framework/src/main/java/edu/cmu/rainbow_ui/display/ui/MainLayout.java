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

import com.vaadin.server.Sizeable;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import edu.cmu.rainbow_ui.display.config.ViewConfiguration;
import edu.cmu.rainbow_ui.display.event.EventTable;
import edu.cmu.rainbow_ui.display.model.AcmeGraph;
import edu.cmu.rainbow_ui.display.timeline.TimelineView;

/**
 * This class creates the main layout for the application
 *
 * <p>
 * The main layout consists of a menu bar, main content area, time line and dashboard.
 * </p>
 *
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public final class MainLayout extends VerticalLayout {

    private final Panel mainContentArea;
    private final TimelineView timeline;
    private final Dashboard dashboard;
    private final AbstractRainbowVaadinUI ui;
    private final MainMenuBar bar;

    private final Label blankLabel = new Label("There is nothing to display");

    /**
     * Main content views
     */
    private final AcmeGraph acmeGraph;
    private final EventTable eventTable;

    /**
     * Constructor.
     *
     * Creates a new main layout. Gets access to the main UI to access UI session-scoped objects
     *
     * @param rui Rainbow Vaadin UI
     */
    public MainLayout(AbstractRainbowVaadinUI rui) {
        this.setWidth(100, Sizeable.Unit.PERCENTAGE);
        this.setHeight("100%");
        this.ui = rui;
        bar = new MainMenuBar(rui, this);

        bar.setWidth(100, Sizeable.Unit.PERCENTAGE);
        this.addComponent(bar);

        mainContentArea = new Panel();
        mainContentArea.setContent(blankLabel);
        mainContentArea.setWidth(100, Sizeable.Unit.PERCENTAGE);
        mainContentArea.setHeight(100, Sizeable.Unit.PERCENTAGE);
        this.addComponent(mainContentArea);
        this.setExpandRatio(mainContentArea, 1.0f);

        timeline = new TimelineView(this.ui.getSystemViewProvider());
        timeline.setWidth(100, Sizeable.Unit.PERCENTAGE);
        this.addComponent(timeline);
        ui.getViewControl().addUpdatableWidget(timeline);

        dashboard = new Dashboard(this.ui);
        this.addComponent(dashboard);

        acmeGraph = new AcmeGraph(ui);
        eventTable = new EventTable(ui.getSystemViewProvider());
        ui.getViewControl().addUpdatableWidget(acmeGraph);
        ui.getViewControl().addUpdatableWidget(eventTable);

        setMainContentArea(MainContentType.ACME_GRAPH);
    }

    public void setMainContentArea(MainContentType type) {
        switch (type) {
            case ACME_GRAPH:
                mainContentArea.setContent(acmeGraph);
                acmeGraph.activate();
                eventTable.deactivate();
                break;
           case EVENT_TABLE:
                mainContentArea.setContent(eventTable);
                eventTable.activate();
                eventTable.loadEvents();
                acmeGraph.deactivate();
                break;
        }
    }

    public void saveViewConfiguration() {
        this.dashboard.saveViewConfiguration();
    }

    public void updateConfiguration() {
        this.dashboard.resetDashboard();
        this.dashboard.loadViewConfiguration();
        /**
         * View configuration affects the whole graph, so just reset it.
         */
        this.acmeGraph.resetGraph();
    }

    public ViewConfiguration getViewConfiguration() {
        return this.ui.getViewConfiguraion();
    }
    
    public void setViewConfiguration(ViewConfiguration viewConfig) {
        this.ui.setViewConfiguration(viewConfig);
    }
    
    /**
     * Set UI components act as in attached mode.
     */
    public void setUIattached() {
        timeline.setAttached();
    }
    
    /**
     * Set UI components act as in detached mode.
     */
    public void setUIdetached() {
        timeline.setDetached();
    }
}
