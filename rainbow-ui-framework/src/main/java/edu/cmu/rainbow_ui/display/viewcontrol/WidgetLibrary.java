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
package edu.cmu.rainbow_ui.display.viewcontrol;

import edu.cmu.rainbow_ui.display.widgets.WidgetDescription;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The widget library holds a list of all internal widget descriptions.
 *
 * <p>
 * Widget descriptions are added through the
 * {@link #register(edu.cmu.rainbow_ui.display.widgets.WidgetDescription)} method. Widget
 * description could be get by name via {@link #getWidget(java.lang.String)}. All widgets that
 * may hold a value of a certain type could be obtained by {@link #getWidgetByType(java.lang.String)}.
 * Full list of available widgets - {@link #getWidgetDescriptions()}
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class WidgetLibrary {

    private static final Map<String, WidgetDescription> widgetDescriptionsByName = new HashMap<>();
    private static final Map<String, List<WidgetDescription>> widgetDescriptionsByType = new HashMap<>();

    /**
     * Returns the library's list of widget descriptions
     *
     * @return the list of widget descriptions
     */
    public static Map<String, WidgetDescription> getWidgetDescriptions() {
        return (widgetDescriptionsByName);
    }

    /**
     * Adds a widget description to the widget library
     *
     * @param desc a widget description
     */
    public static void register(WidgetDescription desc) {
        widgetDescriptionsByName.put(desc.getName(), desc);
        registerByType(desc);
    }

    /**
     * Add to widget descriptions by type.
     *
     * @param desc widget description
     */
    private static void registerByType(WidgetDescription desc) {
        String type = desc.getType();
        List<WidgetDescription> descriptions = widgetDescriptionsByType.get(type);
        if (descriptions == null) {
            descriptions = new ArrayList<>();
            widgetDescriptionsByType.put(type, descriptions);
        }
        descriptions.add(desc);
    }

    /**
     * Gets a specific widget description from the widget library
     *
     * @param id the unique id of the widget, here it's name
     * @return a widget description
     */
    public static WidgetDescription getWidget(String id) {
        return widgetDescriptionsByName.get(id);
    }

    /**
     * Gets a list of widget descriptions that can display the given type of data.
     *
     * @param type the type of information the widget can display
     * @return a list of widget descriptions
     */
    public static List<WidgetDescription> getWidgetByType(String type) {

        return widgetDescriptionsByType.get(type);
    }
}
