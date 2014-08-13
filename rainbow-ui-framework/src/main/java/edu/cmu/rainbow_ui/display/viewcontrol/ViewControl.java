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
package edu.cmu.rainbow_ui.display.viewcontrol;

import com.vaadin.server.VaadinSession;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.able.typelib.type.DataValue;
import edu.cmu.rainbow_ui.display.AcmeConversionException;
import edu.cmu.rainbow_ui.display.AcmeSystemViewProvider;
import edu.cmu.rainbow_ui.display.ISystemViewProvider;
import edu.cmu.rainbow_ui.display.ui.AbstractRainbowVaadinUI;
import edu.cmu.rainbow_ui.display.widgets.IUpdatableWidget;
import edu.cmu.rainbow_ui.display.widgets.Widget;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Maintains a list of all widgets and pushes updates to their visual representation
 *
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class ViewControl {

    private final AbstractRainbowVaadinUI ui;

    /**
     * List of widgets under control. These widgets receives data through view control. They are
     * also updated through view control.
     */
    private final List<Widget> widgets;
    /**
     * List of widgets that should be updated, but not controlled.
     */
    private final List<IUpdatableWidget> updatableWidgets;

    /**
     * Constructor for the widget control
     *
     * @param ui Rainbow Vaadin UI
     */
    public ViewControl(AbstractRainbowVaadinUI ui) {
        widgets = new ArrayList<>();
        this.ui = ui;
        updatableWidgets = new ArrayList<>();
    }

    /**
     * Adds a widget to the list of widgets in the system
     *
     * @param w a widget to be added to the list of widgets
     */
    public void addWidget(Widget w) {
        widgets.add(w);
    }

    /**
     * Removes a widget from the list of widgets in the system
     *
     * @param w a widget to be removed from the list of widgets
     */
    public void removeWidget(Widget w) {
        // ArrayList allows removal by reference
        widgets.remove(w);
    }

    /**
     * List all widgets
     *
     * @return the list of known widgets
     */
    public List<Widget> getWidgetList() {
        return widgets;
    }

    /**
     * Add a new widget to the list of updatables.
     *
     * @param w widget to add
     */
    public void addUpdatableWidget(IUpdatableWidget w) {
        updatableWidgets.add(w);
    }

    /**
     * Remove a widget from the list of updatables.
     *
     * @param w widget to remove
     */
    public void removeUpdatableWidget(IUpdatableWidget w) {
        // ArrayList allows removal by reference
        updatableWidgets.remove(w);
    }

    /**
     * Return a list of widgets which are updated by the view control
     *
     * @return list of updatable widgets
     */
    public List<IUpdatableWidget> getUpdatableWidgetsList() {
        return updatableWidgets;
    }

    /**
     * Updates the values for all widgets in the list based on the view in the system view provider
     * and updates the visual display for widgets currently visible on the screen
     */
    public void pushUpdate() {
        /**
         * Widgets update modifies the UI state. Since it is done in the background thread 
         * the UI session state should be locked to avoid concurrent modifications
         */
        VaadinSession session = ui.getSession();
        if (session != null) {
            session.lock();
            try {
                /* Pass the data to all widgets under control */
                for (Widget widget : widgets) {
                    DataValue value;
                    try {
                        value = ui.getSystemViewProvider().getValue(widget.getMapping());
                        widget.setValue(value);
                    } catch (Exception ex) {
                        Logger.getLogger(ViewControl.class.getName()).log(Level.SEVERE,
                                null, ex);
                    }
                }

                /* Update all widgets */
                for (Widget widget : widgets) {
                    if (widget.isActive()) {
                        widget.update();
                    }
                }
                for (IUpdatableWidget widget : updatableWidgets) {
                    if (widget.isActive()) {
                        widget.update();
                    }
                }

                /* Push updates */
                ui.pushUpdate();
            } finally {
                session.unlock();
            }
        }
    }
}
