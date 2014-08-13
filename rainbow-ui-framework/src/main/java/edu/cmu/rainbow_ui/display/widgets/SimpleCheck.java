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

import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

import edu.cmu.cs.able.typelib.jconv.ValueConversionException;
import edu.cmu.rainbow_ui.common.DataValueSupport;

/**
 * Simple checkbox widget.
 *
 * <p>
 * Displays boolean property as a checkbox.
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class SimpleCheck extends Widget {

    private static final WidgetDescription widgetDescription;

    private final Label label;
    private final CheckBox checkBox;

    public SimpleCheck(String mapping) {
        super(mapping);

        label = new Label();
        checkBox = new CheckBox();
        /**
         * TODO: The checkbox is actually clickable. But the disabled checkbox looks too
         * gray. Probably it should be replaced with something else.
         */

        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("100%");
        layout.addComponent(label);
        layout.addComponent(checkBox);
        layout.setExpandRatio(label, 1.0f);
        layout.setComponentAlignment(checkBox, Alignment.MIDDLE_RIGHT);

        setCompositionRoot(layout);
    }

    static {
        String name = "SimpleCheck";
        String description = "A widget that displays boolean value as a checkbox.";
        String type = "boolean";
        WidgetFactory factory = new WidgetFactory() {
            @Override
            public Widget getInstance(String mapping) {
                return new SimpleCheck(mapping);
            }
        };
        Map<String, WidgetPropertyDescription> propDescr = new HashMap<>();
        propDescr.put("name", new WidgetPropertyDescription(String.class, "Visual name of the property", ""));
        widgetDescription = new WidgetDescription(name, description, type, factory, propDescr);
    }

    static WidgetDescription register() {
        return widgetDescription;
    }

    @Override
    public WidgetDescription getWidgetDescription() {
        return widgetDescription;
    }

    @Override
    public void update() {
        try {
            Boolean val = DataValueSupport.converter.to_java(value, Boolean.class);
            checkBox.setValue(val);
        } catch (ValueConversionException ex) {
            Logger.getLogger(SimpleCheck.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public IWidget getClone() {
        SimpleCheck clone = new SimpleCheck (mapping);
        clone.setProperties(this.getProperties());
        return clone;
    }

    @Override
    protected void onPropertyUpdate() {
        String vName = (String) properties.get("name");
        label.setValue(vName);
    }
}
