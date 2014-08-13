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

/**
 * Class to describe a widget property.
 *
 * <p>
 * Contains information about property type, default value and its textual description.
 * </p>
 *
 * <p>
 * The property description allows to have strongly typed properties of the widgets for 
 * verification of user-created properties. Default value may be used as a placeholder or
 * when creating a default widget instance. Textual description provides human-friendly
 * description of widget developer intentions.
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class WidgetPropertyDescription {

    private final Class valueClass;
    private final String description;
    private final Object defaultValue;

    /**
     * Construct a property description.
     *
     * @param valueTypeClass class of the value this property may hold
     * @param description textual description of the property
     */
    public WidgetPropertyDescription(Class valueTypeClass, String description, Object defaultValue) {
        this.valueClass = valueTypeClass;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    /**
     * Get class of the property value.
     *
     * @return class of the value
     */
    public Class getValueClass() {
        return valueClass;
    }

    /**
     * Get textual description of the property.
     *
     * @return textual description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the property default value.
     * 
     * @return value Object
     */
    public Object getDefaultValue() {
        return defaultValue;
    }
}
