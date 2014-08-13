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
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import edu.cmu.cs.able.typelib.jconv.ValueConversionException;
import edu.cmu.rainbow_ui.common.DataValueSupport;

/**
 * Simple bar widget.
 *
 * <p>
 * This widget displays a float value as a percentage of a maximum value.
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class SimpleBar extends Widget {

    private static final WidgetDescription widgetDescription;
    /* Visual name of the monitored property */
    private String visualName = "";
    /* Maximum value */
    private Double maxValue = 1.0;
    private final Label label;
    /* barValue displays numercial value */
    private final Label barValue;
    /* barFill displays value as a bar */
    private final Label barFill;

    /**
     * Initialize widget description
     */
    static {
        String name = "SimpleBar";
        String description = "A bar that displays float value in percentage of the maximum value.";
        String type = "float";
        WidgetFactory factory = new WidgetFactory() {
            @Override
            public Widget getInstance(String mapping) {
                return new SimpleBar(mapping);
            }
        };
        Map<String, WidgetPropertyDescription> propDescr = new HashMap<>();
        propDescr.put("name", new WidgetPropertyDescription(String.class, "Visual name of the property", ""));
        propDescr.put("max", new WidgetPropertyDescription(Double.class, "Maximum widget value", new Double(1.0)));
        widgetDescription = new WidgetDescription(name, description, type, factory, propDescr);
    }

    static WidgetDescription register() {
        return widgetDescription;
    }

    /**
     * Main constructor
     *
     * @param mapping a property mapping to display
     */
    private SimpleBar(String mapping) {
        super(mapping);

        label = new Label(visualName);

        /* Using CSS layout to get divs displayed full width on top of each other */
        CssLayout bar = new CssLayout() {
            @Override
            protected String getCss(Component c) {
                if (c instanceof Label) return "position: absolute;";
                return null;
            }
        };
        bar.addStyleName("bar");
        bar.setHeight(20, Unit.PIXELS);
        bar.setWidth(100, Unit.PERCENTAGE);

        barValue = new Label();
        barValue.addStyleName("barvalue");
        barValue.setWidth(100, Unit.PERCENTAGE);
        barValue.setHeight(20, Unit.PIXELS);

        barFill = new Label();
        barFill.addStyleName("barfill");
        barFill.setHeight(20, Unit.PIXELS);

        bar.addComponent(barValue);
        bar.addComponent(barFill);

        this.setCompositionRoot(new VerticalLayout(label, bar));
    }

    @Override
    public void update() {
        try {
            Float val;

            try {
                val = DataValueSupport.converter.to_java(value, Float.class
                        );
            } catch (ValueConversionException ex) {
                /* Sometimes float values are reported as ints */
                val = (float) DataValueSupport.converter.to_java(value, Integer.class);
            }

            /* We dont want the bar to overflow */
            float fillVal = val;
            if (val > maxValue) {
                fillVal = (float) (double) maxValue;
            }
            float percentage = (float) (fillVal / maxValue * 100);
            barValue.setValue(Float.toString(val));
            barFill.setWidth(percentage, Unit.PERCENTAGE);

        } catch (ValueConversionException ex) {
            Logger.getLogger(SimpleBar.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void onPropertyUpdate() {
        String vName = (String) properties.get("name");
        visualName = vName;
        label.setValue(visualName);

        Double max = (Double) properties.get("max");
        maxValue = max;
    }

    @Override
    public IWidget getClone() {
        SimpleBar clone = new SimpleBar (mapping);
        clone.setProperties(this.getProperties());
        return clone;
    }

    @Override
    public WidgetDescription getWidgetDescription() {
        return widgetDescription;
    }

}
