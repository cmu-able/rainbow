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

import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import edu.cmu.rainbow_ui.display.ui.AbstractRainbowVaadinUI;
import edu.cmu.rainbow_ui.display.viewcontrol.WidgetLibrary;
import edu.cmu.rainbow_ui.display.widgets.Widget;
import edu.cmu.rainbow_ui.display.widgets.WidgetDescription;
import edu.cmu.rainbow_ui.display.widgets.WidgetPropertyDescription;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.acmestudio.acme.element.property.IAcmeProperty;

/**
 * Dialog to add new widget for the specified property.
 *
 * <p>
 * Requires {@link #onCreate(edu.cmu.rainbow_ui.display.widgets.Widget) } to be defined - action to
 * be taken when widget is created successfully.
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
abstract class WidgetCreationDialog extends Window {

    private final IAcmeProperty property;
    /**
     * Layout to hold properties information
     */
    private final VerticalLayout propsLayout = new VerticalLayout();
    /**
     * Defined widget properties
     */
    private final Map<String, Object> widgetProperties = new HashMap<>();

    /**
     * Create new widget creation dialog for a given property.
     *
     * @param property property to create widget for
     * @param ui Rainbow Vaadin UI
     */
    WidgetCreationDialog(IAcmeProperty property, AbstractRainbowVaadinUI ui) {
        super();
        this.property = property;
        this.setModal(true);
        this.setClosable(false);
        this.setResizable(false);
        this.center();

        String type = property.getType().getQualifiedName();

        this.setCaption(property.getQualifiedName());

        Label propType = new Label(type);
        propType.setCaption("Type");

        VerticalLayout vlayout = new VerticalLayout();
        vlayout.addComponents(propType);

        List<WidgetDescription> suitableWidgets = WidgetLibrary.getWidgetByType(type);

        if (suitableWidgets != null
                && !suitableWidgets.isEmpty()) {
            final ComboBox widgetSel = new ComboBox();
            widgetSel.setCaption("Widget");
            widgetSel.setNullSelectionAllowed(false);
            for (WidgetDescription descr : suitableWidgets) {
                widgetSel.addItem(descr.getName());
            }
            widgetSel.setValue(suitableWidgets.get(0).getName());
            widgetSel.addValueChangeListener(new Property.ValueChangeListener() {

                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    WidgetCreationDialog.this.populateWidgetProps(WidgetLibrary.getWidget((String) widgetSel.getValue()));
                }
            });

            vlayout.addComponent(widgetSel);
            vlayout.addComponent(new Panel("Properties", propsLayout));
            this.populateWidgetProps(WidgetLibrary.getWidget((String) widgetSel.getValue()));

            Button cancelButton = new Button("Cancel");
            cancelButton.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    WidgetCreationDialog.this.close();
                }
            });

            Button createButton = new Button("Create");
            createButton.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    WidgetDescription descr = WidgetLibrary.getWidget((String) widgetSel.getValue());
                    Widget widget = descr.getFactory().getInstance(
                            WidgetCreationDialog.this.property.getQualifiedName()
                    );
                    for (String propName : widgetProperties.keySet()) {
                        widget.setProperty(propName, widgetProperties.get(propName));
                    }
                    WidgetCreationDialog.this.onCreate(widget);
                    WidgetCreationDialog.this.close();
                }
            });

            HorizontalLayout hlayout = new HorizontalLayout(cancelButton, createButton);
            vlayout.addComponent(hlayout);
        } else {
            Label notFound = new Label("No suitable widgets found");
            notFound.setCaption("Widget");
            vlayout.addComponent(notFound);

            Button okButton = new Button("OK");
            okButton.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    WidgetCreationDialog.this.close();
                }
            });
            vlayout.addComponent(okButton);
        }

        this.setContent(vlayout);
    }

    /**
     * Add widget properties selection components to the layout.
     *
     * @param descr description of the widget
     */
    private void populateWidgetProps(WidgetDescription descr) {
        propsLayout.removeAllComponents();

        for (final String propName : descr.getProperties().keySet()) {
            WidgetPropertyDescription propDescr = descr.getProperties().get(propName);

            final ObjectProperty textFieldValue;

            textFieldValue = new ObjectProperty(propDescr.getDefaultValue(), propDescr.getValueClass());
            final TextField propField = new TextField(propName, textFieldValue);
            propField.setImmediate(true);
            propField.setDescription(propDescr.getDescription());
            textFieldValue.addValueChangeListener(new Property.ValueChangeListener() {

                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    widgetProperties.put(propName, textFieldValue.getValue());
                }
            });

            propsLayout.addComponent(propField);
        }
    }

    /**
     * Action that should be done after widget has been created.
     *
     * @param widget created widget
     */
    abstract void onCreate(Widget widget);
}
