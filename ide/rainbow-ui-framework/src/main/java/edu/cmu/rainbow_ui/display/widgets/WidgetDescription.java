/* The MIT License
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

import java.util.Collections;
import java.util.Map;

/**
 * Provides information about a widget.
 * 
 * <p>
 * Dynamically found widgets are described using this class. Widgets are created using a
 * {@link WidgetFactory} accessible here.
 * </p>
 * 
 * @see WidgetFactory
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class WidgetDescription {
    private final String name;
    private final String description;
    private final String type;
    private final WidgetFactory factory;
    private final Map<String, WidgetPropertyDescription> properties;

    /**
     * Constructor for the widget description
     * 
     * @param name the name of the widget
     * @param description the description of the widget
     * @param type the type of data that can be displayed by the widget
     * @param factory a factory object which can create new instances of the
     *        widget
     * @param properties the value of properties
     */
    public WidgetDescription(String name, String description, String type, WidgetFactory factory, Map<String, WidgetPropertyDescription> properties) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.factory = factory;
        this.properties = properties;
    }

    /**
     * Getter for name of widget
     * 
     * @return the name of the widget
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for description of widget
     * 
     * @return the description of the widget
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter for type of widget
     * 
     * @return the type of the widget
     */
    public String getType() {
        return type;
    }

    /**
     * Getter for factory of widget
     * 
     * @return the factory of the widget
     */
    public WidgetFactory getFactory() {
        return factory;
    }
    
    /**
     * Return a map of properties descriptions.
     * 
     * @return map of properties
     */
    public Map<String, WidgetPropertyDescription> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

}
