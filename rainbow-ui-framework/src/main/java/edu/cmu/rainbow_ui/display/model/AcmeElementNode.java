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
package edu.cmu.rainbow_ui.display.model;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.acmestudio.acme.element.IAcmeElementInstance;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.element.representation.IAcmeRepresentation;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import edu.cmu.rainbow_ui.display.config.ElementConfiguration;
import edu.cmu.rainbow_ui.display.config.GraphConfiguration;
import edu.cmu.rainbow_ui.display.config.PropertyConfiguration;
import edu.cmu.rainbow_ui.display.config.WidgetConfiguration;
import edu.cmu.rainbow_ui.display.ui.AbstractRainbowVaadinUI;
import edu.cmu.rainbow_ui.display.ui.WidgetDragAndDropWrapper;
import edu.cmu.rainbow_ui.display.viewcontrol.WidgetLibrary;
import edu.cmu.rainbow_ui.display.widgets.IWidget;
import edu.cmu.rainbow_ui.display.widgets.WidgetDescription;

/**
 * Visual component to display an Acme element in the graph.
 *
 * <p>
 * Shows Acme element properties as widgets inside of the node. Presents additional functions for
 * modifying list of available properties or displaying internal representations of Acme element.
 * </p>
 *
 * Package-scoped to be used only in the AcmeGraph class
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
abstract class AcmeElementNode extends CustomComponent {

    private static final int ELEMENT_WIDTH = 120;
    private static final int ELEMENT_LABEL_HEIGHT = 30;
    private static final int ELEMENT_WIDGET_PANEL_HEIGHT = 120;

    private final IAcmeElementInstance element;
    private final AbstractRainbowVaadinUI ui;
    private final AcmeGraph graphView;

    protected final Map<String, IWidget>  propertiesWidgets           = new HashMap<> ();

    private final HorizontalLayout labelBar;
    private final Button expButton;

    private final Panel widgetsPanel;

    final VerticalLayout container;

    /**
     * Container to hold widgets for visual properties
     */
    private final VerticalLayout widgetsContainer;

    AcmeElementNode(IAcmeElementInstance element, AbstractRainbowVaadinUI ui, AcmeGraph graphView) {
        this.element = element;
        this.ui = ui;
        this.graphView = graphView;

        /* Create container */
        container = new VerticalLayout();
        this.addStyleName(AcmeGraph.GRAPH_WIDGET_STYLE);

        widgetsContainer = new VerticalLayout();
        widgetsPanel = new Panel(widgetsContainer);
        widgetsPanel.setHeight(ELEMENT_WIDGET_PANEL_HEIGHT, Unit.PIXELS);

        /* Add widgets */
        addConfiguredPropertiesWidgets();

        /* By default node is folded. The widgets panel is not visible */
        widgetsPanel.setVisible(false);

        labelBar = new HorizontalLayout();
        /* Make widget label */
        String name = element.getName();
        Label lab = new Label(name);

        /* Constructs the element bar */
        labelBar.setHeight(ELEMENT_LABEL_HEIGHT, Unit.PIXELS);
        labelBar.setWidth("100%");
        labelBar.addComponent(lab);
        labelBar.setComponentAlignment(lab, Alignment.MIDDLE_LEFT);
        labelBar.setExpandRatio(lab, 1.0f);

        /* Make internal representations button */
        if (element.getRepresentations().size() > 0) {
            Button reprButton = makeInternalRepresentationsButton();
            labelBar.addComponent(reprButton);
            labelBar.setComponentAlignment(reprButton, Alignment.MIDDLE_RIGHT);
        }

        /* Make configuration button */
        Button confButton = makeConfigurationButton();
        labelBar.addComponent(confButton);
        labelBar.setComponentAlignment(confButton, Alignment.MIDDLE_RIGHT);

        /* Make expansion button */
        expButton = makeExpansionButton();

        labelBar.addComponent(expButton);
        labelBar.setComponentAlignment(expButton, Alignment.MIDDLE_RIGHT);

        /**
         * If the widget is not configured or has no properties configured then the expansion button
         * should not be displayed.
         *
         * TODO: this button should be activated after manual widget addition.
         */
        expButton.setVisible(propertiesWidgets.size() > 0);

        container.addComponents(labelBar, widgetsPanel);

        /* Size is fixed and sums up to components size */
        this.setWidth(ELEMENT_WIDTH, Unit.PIXELS);
        this.setHeight(computeContainerHeight(container), Unit.PIXELS);

        this.setCompositionRoot(container);
    }

    /**
     * Add widgets to the properties mentioned in the view configuration.
     */
    private void addConfiguredPropertiesWidgets() {
        ElementConfiguration elemConfig = ui.getViewConfiguraion().graph.elements.get(element.getQualifiedName());
        boolean isConfigured = elemConfig != null;

        /**
         * Add interactive widgets to the element node. If the element is not configured in the view
         * configuration no widgets are added by default.\
         *
         * TODO: add manual addition of widgets to the element node.
         */
        if (isConfigured) {
            for (IAcmeProperty prop : element.getProperties()) {
                String type = prop.getType().getQualifiedName();
                PropertyConfiguration propConfig = elemConfig.properties.get(prop.getName());

                /* The property should be configured */
                if (propConfig != null) {
                    WidgetConfiguration widgetConfig = propConfig.widget;
                    WidgetDescription widgetDescr = WidgetLibrary.getWidget(widgetConfig.id);
                    if (widgetDescr == null) {
                        Logger.getLogger(AcmeGraph.class.getName()).log(Level.WARNING,
                                "Widget {0} isn't registered in the library",
                                widgetConfig.id
                                );
                        break;
                    }
                    String widgetType = widgetDescr.getType();

                    if (!widgetType.equals(type)) {
                        Logger.getLogger(AcmeGraph.class.getName()).log(Level.WARNING,
                                String.format("Incompatible widget type for the property.\n    "
                                        + "element: %s\n    property: %s\n    type: %s\n    "
                                        + "widget: %s\n widget type: %s",
                                        element.getQualifiedName(),
                                        prop.getName(),
                                        type,
                                        widgetDescr.getName(),
                                        widgetType
                                        )
                                );
                        break;
                    }

                    String widgetMapping = widgetConfig.mapping;

                    if (!widgetMapping.equals(prop.getQualifiedName())) {
                        Logger.getLogger(AcmeGraph.class.getName()).log(Level.WARNING,
                                String.format("Widget mapping doesn't correspond to the element property.\n"
                                        + "    element: %s\n    property: %s\n"
                                        + "    widget: %s\n"
                                        + "    mapping: %s",
                                        element.getQualifiedName(),
                                        prop.getName(),
                                        widgetDescr.getName(),
                                        widgetConfig.mapping
                                        )
                                );
                        break;
                    }

                    IWidget widget = widgetDescr.getFactory ().getInstance (widgetMapping);
                    for (String widgetPropName : widgetConfig.properties.keySet()) {
                        try {
                            widget.setProperty(widgetPropName, widgetConfig.properties.get(widgetPropName));
                        } catch (IllegalArgumentException ex) {
                            Logger.getLogger(AcmeGraph.class.getName()).log(Level.WARNING,
                                    "Cannot set widget property from configuration.",
                                    ex
                                    );
                        }
                    }
                    ui.getViewControl().addWidget(widget);
                    widget.getAsComponent ().addStyleName ("graph-widget-item");
                    WidgetDragAndDropWrapper dndWrapper = new WidgetDragAndDropWrapper (widget.getAsComponent ());
                    propertiesWidgets.put(prop.getName(), widget);
                    widgetsContainer.addComponent(dndWrapper);
                }
            }
        }
    }

    /**
     * Make button to access internal representations of the component.
     *
     * @return created button
     */
    private Button makeInternalRepresentationsButton() {

        final NativeButton reprButton = new NativeButton("R");

        reprButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (element.getRepresentations().size() > 1) {
                    final Window subWindow = new Window("Select representation");
                    subWindow.setClosable(false);
                    subWindow.center();

                    final ListSelect selectList = new ListSelect();
                    selectList.setMultiSelect(false);
                    for (IAcmeRepresentation repr : element.getRepresentations()) {
                        selectList.addItem(repr.getName());
                    }
                    selectList.setValue(element.getRepresentations().iterator().next().getName());
                    selectList.setSizeFull();

                    Button showButton = new Button("Show");
                    showButton.addClickListener(new Button.ClickListener() {

                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            graphView.setCurrentSystem(element.getRepresentation(
                                    selectList.getValue().toString()).getSystem()
                                    );
                            subWindow.close();
                        }
                    });

                    Button cancelButton = new Button("Cancel");
                    cancelButton.addClickListener(new Button.ClickListener() {

                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            subWindow.close();
                        }
                    });

                    VerticalLayout vlayout = new VerticalLayout();
                    HorizontalLayout hlayout = new HorizontalLayout();
                    hlayout.addComponents(cancelButton, showButton);
                    vlayout.addComponents(selectList, hlayout);

                    subWindow.setContent(vlayout);
                    subWindow.setResizable(false);

                    ui.addWindow(subWindow);
                } else if (element.getRepresentations().size() == 1) {
                    /* Only one representation exists */
                    graphView.setCurrentSystem(element.getRepresentations()
                            .iterator().next().getSystem());
                }
            }
        });

        return reprButton;
    }

    /**
     * Computer vertical layout container height based on its visible components.
     *
     * @param container vertical layout with components
     * @return total height
     */
    private int computeContainerHeight(VerticalLayout container) {
        int totalHeight = 0;
        for (int i = 0; i < container.getComponentCount(); i++) {
            Component comp = container.getComponent(i);
            if (comp.isVisible()) {
                int height = (int) comp.getHeight();
                totalHeight += (height < 0) ? 0 : height;
            }
        }
        return totalHeight;
    }

    /**
     * Make button to expand/collapse widgets panel
     *
     * @return created button
     */
    private Button makeExpansionButton() {
        final NativeButton button = new NativeButton("+");
        button.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                /**
                 * TODO: add changing of node state width/height on expansion/collapse
                 */
                if (widgetsPanel.isVisible()) {
                    collapseWidgetsPanel();
                } else {
                    expandWidgetsPanel();
                }
            }
        });
        return button;
    }

    /**
     * Expand(show) widgets panel.
     */
    private void expandWidgetsPanel() {
        widgetsPanel.setVisible(true);
        AcmeElementNode.this.setHeight(computeContainerHeight(container), Unit.PIXELS);
        expButton.setCaption("-");
        /* Activate all widgets */
        for (int i = 0; i < widgetsContainer.getComponentCount(); i++) {
            IWidget widget = ((WidgetDragAndDropWrapper) widgetsContainer.getComponent(i)).getWidget();
            widget.activate();
        }

        graphView.updateNodeWidthAndHeight(element.getQualifiedName());
    }

    /**
     * Collapse(hide) widgets panel.
     */
    private void collapseWidgetsPanel() {
        widgetsPanel.setVisible(false);
        AcmeElementNode.this.setHeight(computeContainerHeight(container), Unit.PIXELS);
        expButton.setCaption("+");
        /* Deactivate all widgets */
        for (int i = 0; i < widgetsContainer.getComponentCount(); i++) {
            IWidget widget = ((WidgetDragAndDropWrapper) widgetsContainer.getComponent(i)).getWidget();
            widget.deactivate();
        }

        graphView.updateNodeWidthAndHeight(element.getQualifiedName());
    }

    /**
     * Create a button to display element configuration.
     *
     * @return created button
     */
    private Button makeConfigurationButton() {
        Button confButton = new NativeButton("C");
        confButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                Window confWindow = new ElementConfigurationWindow();
                confWindow.center();
                ui.addWindow(confWindow);
            }
        });
        return confButton;
    }

    /**
     * Class to build element configuration window
     */
    private class ElementConfigurationWindow extends Window {

        /**
         * Properties list layout
         */
        VerticalLayout vlayout = new VerticalLayout();

        /**
         * Create new window
         */
        ElementConfigurationWindow() {
            super();
            this.setResizable(false);

            this.setCaption(element.getQualifiedName());

            vlayout.setWidth("100%");
            populateElementProps();

            this.setContent(vlayout);
        }

        /**
         * Update list of widget properties
         */
        private void populateElementProps() {
            vlayout.removeAllComponents();

            for (final IAcmeProperty prop : element.getProperties()) {
                HorizontalLayout hlayout = new HorizontalLayout();
                hlayout.setWidth("100%");
                Label propName = new Label(prop.getName());
                hlayout.addComponent(propName);
                hlayout.setComponentAlignment(propName, Alignment.MIDDLE_LEFT);
                if (propertiesWidgets.containsKey(prop.getName())) {
                    Button removeButton = new Button("Remove");
                    hlayout.addComponent(removeButton);
                    hlayout.setComponentAlignment(removeButton, Alignment.MIDDLE_RIGHT);
                    removeButton.addClickListener(new Button.ClickListener() {

                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            IWidget widget = propertiesWidgets.get (prop.getName ());
                            widget.deactivate();
                            ui.getViewControl().removeWidget(widget);
                            propertiesWidgets.remove(prop.getName());
                            /**
                             * Widgets are wrapped in the DnD wrapper, so remove its parent from the
                             * container.
                             */
                            widgetsContainer.removeComponent (widget.getAsComponent ().getParent ());

                            if (propertiesWidgets.isEmpty()) {
                                collapseWidgetsPanel();
                                expButton.setVisible(false);
                            }

                            removeWidgetFromViewConfiguration(prop, widget);

                            populateElementProps();
                        }
                    });
                } else {
                    // No widget for the property is set
                    Button addButton = new Button("Add");
                    hlayout.addComponent(addButton);
                    hlayout.setComponentAlignment(addButton, Alignment.MIDDLE_RIGHT);
                    addButton.addClickListener(new Button.ClickListener() {

                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            Window addWindow = new WidgetCreationDialog(prop, ui) {

                                @Override
                                void onCreate (IWidget widget) {
                                    ui.getViewControl().addWidget(widget);
                                    widgetsContainer.addComponent (new WidgetDragAndDropWrapper (widget
                                            .getAsComponent ()));
                                    propertiesWidgets.put(prop.getName(), widget);

                                    expButton.setVisible(true);
                                    if (widgetsPanel.isVisible()) {
                                        widget.activate();
                                    }

                                    addWidgetToViewConfiguration(prop, widget);

                                    populateElementProps();
                                }
                            };
                            ui.addWindow(addWindow);
                        }
                    });
                }
                vlayout.addComponent(hlayout);
            }
        }
    }

    /**
     * Add new widget to the view configuration of the element. This will override previously
     * defined widget for a property.
     *
     * @param prop widget property
     * @param widget created widget
     */
    private void addWidgetToViewConfiguration(IAcmeProperty prop, IWidget widget) {
        GraphConfiguration graphConfig = ui.getViewConfiguraion().graph;

        ElementConfiguration elemConfig = graphConfig.elements.get(element.getQualifiedName());

        if (elemConfig == null) {
            elemConfig = new ElementConfiguration();
            graphConfig.elements.put(element.getQualifiedName(), elemConfig);
        }

        PropertyConfiguration propConfig = elemConfig.properties.get(prop.getName());
        if (propConfig == null) {
            propConfig = new PropertyConfiguration();
            elemConfig.properties.put(prop.getName(), propConfig);
        }
        propConfig.widget = new WidgetConfiguration(widget);
    }

    /**
     * Remove a widget configuration for a property from
     *
     * @param prop
     * @param widget
     */
    private void removeWidgetFromViewConfiguration(IAcmeProperty prop, IWidget widget) {
        GraphConfiguration graphConfig = ui.getViewConfiguraion().graph;

        ElementConfiguration elemConfig = graphConfig.elements.get(element.getQualifiedName());

        if (elemConfig != null
                && elemConfig.properties.containsKey(prop.getName())
                && elemConfig.properties.get(prop.getName()).widget.id
                .equals(widget.getWidgetDescription().getName())) {
            elemConfig.properties.remove(prop.getName());
        } else {
            Logger.getLogger(AcmeElementNode.class.getName()).log(
                    Level.WARNING,
                    "No configuration found for an existing widget."
                            + "\n    widget: {0}\n    maping: {1}",
                            new Object[]{widget.getWidgetDescription().getName(), widget.getMapping()}
                    );
        }
    }

    /**
     * Set the height of the label and update total height.
     *
     * @param height new label height
     */
    protected void setLabelHeight(int height) {
        labelBar.setHeight(height, Unit.PIXELS);
        this.setHeight(computeContainerHeight(container), Unit.PIXELS);
    }

    /**
     * Set the height of the widgets panel and update the total height.
     *
     * @param height new widgets panel height
     */
    protected void setWidgetsPanelHeight(int height) {
        widgetsPanel.setHeight(height, Unit.PIXELS);
        this.setHeight(computeContainerHeight(container), Unit.PIXELS);
    }

}
