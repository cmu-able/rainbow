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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.DropTarget;
import com.vaadin.event.dd.TargetDetails;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.Not;
import com.vaadin.event.dd.acceptcriteria.SourceIsTarget;
import com.vaadin.shared.ui.dd.HorizontalDropLocation;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.DragStartMode;
import com.vaadin.ui.DragAndDropWrapper.WrapperTransferable;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import edu.cmu.rainbow_ui.display.config.DashboardConfiguration;
import edu.cmu.rainbow_ui.display.config.DashboardPageConfiguration;
import edu.cmu.rainbow_ui.display.config.ViewConfiguration;
import edu.cmu.rainbow_ui.display.config.WidgetConfiguration;
import edu.cmu.rainbow_ui.display.viewcontrol.ViewControl;
import edu.cmu.rainbow_ui.display.viewcontrol.WidgetLibrary;
import edu.cmu.rainbow_ui.display.widgets.IWidget;
import edu.cmu.rainbow_ui.display.widgets.IWidget.IHandler;
import edu.cmu.rainbow_ui.display.widgets.WidgetDescription;

/**
 * This class defines the dashboard
 * 
 * <p>
 * The dashboard will allow widgets to be added and removed for quick viewing
 * </p>
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
@SuppressWarnings("serial")
public class Dashboard extends VerticalLayout {

    /**
     * Holds the dashboard pages
     * 
     * <p>
     * Package private for testing
     * </p>
     */
    TabSheet pageContentArea;
    /**
     * Creates a new page
     * 
     * <p>
     * Package private for testing
     * </p>
     */
    Button newPageBtn;
    /**
     * deletes the current page
     * 
     * <p>
     * Package private for testing
     * </p>
     */
    Button removePageBtn;
    /**
     * confirms the name of the new page on the popup window
     * 
     * <p>
     * Package private for testing
     * </p>
     */
    Button btnCreate;
    /**
     * Lets the user enter the name of the new page
     * 
     * <p>
     * Package private for testing
     * </p>
     */
    TextField txtPageName;
    protected final AbstractRainbowVaadinUI ui;
    protected List<DragAndDropWrapper> tabs;
    private List<List<IWidget>>             pageWidgets;
    private ViewControl viewControl;

    private final static int DASHBOARD_WIDGET_WIDTH = 120;

    /**
     * Constructor for a dashboard component
     * 
     * @param rui Rainbow Vaadin UI
     */
    public Dashboard(AbstractRainbowVaadinUI rui) {
        this.ui = rui;
        this.viewControl = ui.getViewControl();
        tabs = new ArrayList<>();
        pageWidgets = new ArrayList<>();

        pageContentArea = new TabSheet();
        this.addComponent(pageContentArea);

        removePageBtn = new Button("Remove Page");
        removePageBtn.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                removePage();
            }

        });

        newPageBtn = new Button("New Page");
        newPageBtn.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                newPage();
            }

        });

        HorizontalLayout buttonBar = new HorizontalLayout();
        buttonBar.setSpacing(true);
        buttonBar.addComponent(newPageBtn);
        buttonBar.addComponent(removePageBtn);

        Label spacer1 = new Label("&nbsp;", ContentMode.HTML);
        spacer1.setSizeUndefined();
        buttonBar.addComponent(spacer1);
        buttonBar.setExpandRatio(spacer1, 1.0f);

        Label trashLabel = new Label("Trash", ContentMode.HTML);
        spacer1.setSizeUndefined();

        DragAndDropWrapper trash = new DragAndDropWrapper(trashLabel);
        trash.setDropHandler(new DropHandler() {

            @Override
            public void drop(DragAndDropEvent event) {
                WrapperTransferable t = (WrapperTransferable) event
                        .getTransferable();
                Component draggedWrapper = t.getSourceComponent();
                if (draggedWrapper instanceof WidgetDragAndDropWrapper) {
                    Component layoutParent = draggedWrapper.getParent();
                    Component parent = draggedWrapper.getParent();
                    while (parent != null && !tabs.contains(parent)) {
                        parent = parent.getParent();
                    }

                    if (parent != null && tabs.contains(parent)) {
                        pageWidgets.get(tabs.indexOf(parent)).remove(
                                ((HorizontalLayout) layoutParent)
                                .getComponentIndex(draggedWrapper));
                        ((HorizontalLayout) layoutParent)
                        .removeComponent(draggedWrapper);
                        Dashboard.this.viewControl
                        .removeWidget(((WidgetDragAndDropWrapper) draggedWrapper)
                                .getWidget());
                    } else {
                        Notification.show(
                                "That component cannot be dropped here",
                                Type.WARNING_MESSAGE);
                    }
                }
            }

            @Override
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }

        });
        buttonBar.addComponent(trash);

        this.addComponent(buttonBar);

        loadViewConfiguration();
        if (pageContentArea.getComponentCount() <= 0) {
            addPage("Dashboard");
        }
    }

    /**
     * Adds a new page to the dashboard
     * 
     * @param pageName the name of the page that will be displayed on the tab
     */
    public void addPage(String pageName) {
        final HorizontalLayout newLayout = new HorizontalLayout();
        DragAndDropWrapper layoutWrapper = new DragAndDropWrapper(newLayout);

        setDropHandler(newLayout, layoutWrapper);

        tabs.add(layoutWrapper);
        pageContentArea.addTab(layoutWrapper, pageName);
        pageContentArea.setSelectedTab(layoutWrapper);
        pageWidgets.add (new ArrayList<IWidget> ());
        removePageBtn.setEnabled(true);
    }

    /**
     * Sets the displayed page to the page id provided
     * 
     * @param pageId the page id that will be set to display
     */
    public void setPage(int pageId) {
        pageContentArea.setSelectedTab(pageId);
    }

    /**
     * Removes the page with the id provided from the dashboard along with all
     * widgets on the page
     * 
     * @param pageId the page id that will be removed
     */
    public void removePage(int pageId) {
        for (int i = 0; i < pageWidgets.get(pageId).size(); i++) {
            viewControl.removeWidget(pageWidgets.get(pageId).get(i));
        }
        pageWidgets.remove(pageId);
        pageContentArea.removeTab(pageContentArea.getTab(pageContentArea
                .getSelectedTab()));
        tabs.remove(pageId);
    }

    /**
     * Loads the configuration from the view configuration, creates pages and
     * adds widgets to the pages as specified in the configuration
     */
    public void loadViewConfiguration() {
        ViewConfiguration viewConfiguration = ui.getViewConfiguraion();

        for (DashboardPageConfiguration page : viewConfiguration.dashboard.pages) {
            final HorizontalLayout newLayout = new HorizontalLayout();
            DragAndDropWrapper layoutWrapper = new DragAndDropWrapper(newLayout);
            setDropHandler(newLayout, layoutWrapper);
            ArrayList<IWidget> pageWidgetList = new ArrayList<> ();
            for (WidgetConfiguration widget1 : page.widgets) {
                WidgetDescription widgetDescription = WidgetLibrary
                        .getWidgetDescriptions().get(widget1.id);
                if (widgetDescription == null) {
                    Logger.getLogger(Dashboard.class.getName()).log(
                            Level.SEVERE, "loadViewConfiguration",
                            "Error in view configuration: Unknown Widget");
                } else {
                    IWidget widget = widgetDescription.getFactory ().getInstance (widget1.mapping);
                    for (String widgetPropName : widget1.properties.keySet()) {
                        try {
                            widget.setProperty(widgetPropName, widget1.properties.get(widgetPropName));
                        }catch (IllegalArgumentException ex) {
                            Logger.getLogger(Dashboard.class.getName())
                            .log(Level.WARNING,
                                    "Cannot  set widget property from configuration.",
                                    ex);
                        }
                    }
                    widget.activate();
                    this.ui.getViewControl().addWidget(widget);
                    widget.getAsComponent ().setWidth (DASHBOARD_WIDGET_WIDTH, Unit.PIXELS);
                    pageWidgetList.add(widget);
                    WidgetDragAndDropWrapper wrapper = new WidgetDragAndDropWrapper(
                            widget.getAsComponent (),
                            new ReorderLayoutDropHandler (newLayout));
                    wrapper.setDragStartMode(DragStartMode.WRAPPER);
                    wrapper.setSizeUndefined();
                    newLayout.addComponent(wrapper);
                }
            }
            tabs.add(layoutWrapper);
            pageWidgets.add(pageWidgetList);
            pageContentArea.addTab(layoutWrapper, page.name);
            pageContentArea.setSelectedTab(layoutWrapper);
        }
    }

    /**
     * Saves the configuration from the view configuration to a .yaml file
     */
    public void saveViewConfiguration() {
        DashboardConfiguration dashConfig = new DashboardConfiguration();
        for (int i = 0; i < pageWidgets.size(); i++) {
            DashboardPageConfiguration pageConfig = new DashboardPageConfiguration();
            pageConfig.name = pageContentArea.getTab(i).getCaption();
            List<IWidget> page = pageWidgets.get (i);
            for (IWidget page1 : page) {
                WidgetConfiguration widgetConfig = new WidgetConfiguration();
                widgetConfig.id = page1.getWidgetDescription().getName();
                widgetConfig.mapping = page1.getMapping();
                for (String propName : page1.getWidgetDescription().getProperties().keySet()) {
                    widgetConfig.properties.put(propName, page1.getProperty(propName));
                }
                pageConfig.widgets.add(widgetConfig);
            }
            dashConfig.pages.add(pageConfig);
        }
        ui.getViewConfiguraion().dashboard = dashConfig;
    }

    /**
     * Creates a new page based on the information gathered by the user via
     * subwindow prompts
     */
    private void newPage() {
        VerticalLayout getPageName = new VerticalLayout();
        txtPageName = new TextField("Page name:");
        getPageName.addComponent(txtPageName);
        btnCreate = new Button("Create");
        final Window subWindow = new Window("Page name");
        btnCreate.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                if (txtPageName.getValue().equals("")
                        || txtPageName.getValue() == null) {
                    Notification.show("Please enter a page name",
                            Type.ERROR_MESSAGE);
                } else {
                    addPage(txtPageName.getValue());
                    Dashboard.this.ui.removeWindow(subWindow);
                }
            }

        });

        getPageName.addComponent(btnCreate);
        getPageName.setMargin(true);
        subWindow.setContent(getPageName);
        Dashboard.this.ui.addWindow(subWindow);
    }

    /**
     * Removes the currently selected tab from the dashboard
     */
    private void removePage() {
        removePage(pageContentArea.getTabPosition(pageContentArea
                .getTab(pageContentArea.getSelectedTab())));
        if (tabs.size() <= 0) {
            removePageBtn.setEnabled(false);
        }
    }

    private void setDropHandler(final HorizontalLayout newLayout,
            DragAndDropWrapper layoutWrapper) {
        layoutWrapper.setDropHandler(new DropHandler() {
            @Override
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }

            @Override
            public void drop(DragAndDropEvent event) {
                WrapperTransferable t = (WrapperTransferable) event
                        .getTransferable();

                Component draggedWrapper = t.getSourceComponent();
                if (!(draggedWrapper instanceof WidgetDragAndDropWrapper)) return;
                boolean inDashAlready = false;
                Component c = draggedWrapper;
                while (c.getParent() != null) {
                    c = c.getParent();
                    if (c.equals(Dashboard.this)) {
                        inDashAlready = true;
                        break;
                    }
                }

                if (!inDashAlready) {
                    // Create the component clone
                    IWidget widgetClone = ((WidgetDragAndDropWrapper) draggedWrapper)
                            .getWidget().getClone();
                    widgetClone.activate();

                    Dashboard.this.ui.getViewControl().addWidget(widgetClone);

                    // Add the widget to the page widget list
                    pageWidgets.get(
                            pageContentArea.getTabPosition(pageContentArea
                                    .getTab(pageContentArea.getSelectedTab())))
                                    .add(widgetClone);

                    widgetClone.getAsComponent ().setWidth (DASHBOARD_WIDGET_WIDTH, Unit.PIXELS);

                    final WidgetDragAndDropWrapper wrapper = new WidgetDragAndDropWrapper (
                            (Component )widgetClone,
                            new ReorderLayoutDropHandler(newLayout));
                    wrapper.setDragStartMode(DragStartMode.WRAPPER);
                    wrapper.setSizeUndefined();
                    widgetClone.setCloseHandler (new IHandler () {

                        @Override
                        public void handle () {
                            newLayout.removeComponent (wrapper);
                        }

                    });
                    newLayout.addComponent(wrapper);
                }
            }
        });
    }

    /**
     * This class defines a drop handler that will allow widgets to be reorderd
     * in the dashboard
     * 
     * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
     */
    private static class ReorderLayoutDropHandler implements DropHandler {

        private final HorizontalLayout layout;

        /**
         * Constructor for a reorderlayoutdrophandler
         * 
         * @param layout a horizontal layout that will house the widgets
         */
        public ReorderLayoutDropHandler(HorizontalLayout layout) {
            this.layout = layout;
        }

        /**
         * @return acceptcriterion which will only accept if the source is not
         *         equal to the target
         */
        @Override
        public AcceptCriterion getAcceptCriterion() {
            return new Not(SourceIsTarget.get());
        }

        @Override
        public void drop(DragAndDropEvent dropEvent) {
            Transferable transferable = dropEvent.getTransferable();
            Component sourceComponent = transferable.getSourceComponent();
            if (sourceComponent instanceof WidgetDragAndDropWrapper) {
                TargetDetails dropTargetData = dropEvent.getTargetDetails();
                DropTarget target = dropTargetData.getTarget();

                // find the location where to move the dragged component
                int index = 0;
                Iterator<Component> componentIterator = layout.iterator();
                Component next = null;
                boolean found = false;
                while (componentIterator.hasNext()) {
                    next = componentIterator.next();
                    IWidget nextWidget = null;
                    if (next instanceof WidgetDragAndDropWrapper) {
                        nextWidget = ((WidgetDragAndDropWrapper) next)
                                .getWidget();
                    }
                    if (nextWidget != ((WidgetDragAndDropWrapper) target)
                            .getWidget()) {
                        index++;
                    } else {
                        found = true;
                        break;
                    }
                }
                if (next == null || !found)
                    // component not found - if dragging from another layout
                    return;
                else if (dropTargetData.getData("horizontalLocation").equals(
                        HorizontalDropLocation.LEFT.toString())) {
                    index--;
                    if (index < 0) {
                        index = 0;
                    }
                }

                // move component within the layout
                layout.removeComponent(sourceComponent);
                layout.addComponent(sourceComponent, index);
            }
        }
    };

    public void resetDashboard() {
        TabSheet newTabSheet = new TabSheet();
        this.replaceComponent(pageContentArea, newTabSheet);
        pageContentArea = newTabSheet;
        tabs = new ArrayList<>();
        pageWidgets = new ArrayList<>();
    }
}
