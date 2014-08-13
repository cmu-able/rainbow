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
package edu.cmu.rainbow_ui.display.widgets;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import edu.cmu.cs.able.typelib.jconv.ValueConversionException;
import edu.cmu.rainbow_ui.common.DataValueSupport;

/**
 * Simple text widget.
 *
 * <p>
 * Shows a property as a string.
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class SimpleText extends Widget {

    private static final WidgetDescription widgetDescription;
    private final Component displayComponent;
    private final Label label;
    private final Label text;
    private String visualName = "";

    /**
     * Initialize widget description
     */
    static {
        String name = "SimpleText";
        String description = "Shows mapping name and displays a value as string.";
        String type = "string";
        WidgetFactory factory = new WidgetFactory() {

            @Override
            public Widget getInstance(String mapping) {
                return new SimpleText(mapping);
            }
        };
        Map<String, WidgetPropertyDescription> propDescr = new HashMap<>();
        propDescr.put("name", new WidgetPropertyDescription(String.class, "Visual name of the property", ""));
        widgetDescription = new WidgetDescription(name, description, type, factory, propDescr);
    };

    static WidgetDescription register() {
        return widgetDescription;
    }

    private SimpleText(String mapping) {
        super(mapping);
        active = false;

        label = new Label(mapping);
        text = new Label();
        displayComponent = new VerticalLayout(label, text);
        this.setCompositionRoot(displayComponent);
    }

    @Override
    public WidgetDescription getWidgetDescription() {
        return widgetDescription;
    }

    @Override
    public IWidget getClone() {
        SimpleText clone = new SimpleText (mapping);
        clone.setProperties(this.getProperties());
        return clone;
    }

    @Override
    public void update() {
        try {
            text.setValue(DataValueSupport.converter.to_java(value, String.class));
        } catch (ValueConversionException ex) {
            Logger.getLogger(SimpleText.class.getName()).log(Level.SEVERE, "Cannot convert value to string.", ex);
        }
    }

    @Override
    protected void onPropertyUpdate() {
        String vName = (String) properties.get("name");
        if (vName != null) {
            visualName = vName;
            label.setValue(visualName);
        }
    }

}
