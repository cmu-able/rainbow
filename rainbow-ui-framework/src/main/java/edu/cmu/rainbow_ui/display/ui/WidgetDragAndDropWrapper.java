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

import com.vaadin.event.dd.DropHandler;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;

import edu.cmu.rainbow_ui.display.widgets.IWidget;

/**
 * Drag and Drop wrapper tailored for usage for widgets.
 * 
 * <p>
 * Allows to access its whole wrapped component, which should be widget.
 * </p>
 * 
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class WidgetDragAndDropWrapper extends DragAndDropWrapper {

    private final DropHandler dropHandler;

    public WidgetDragAndDropWrapper (Component widget) {
        super(widget);
        this.dropHandler = null;
        setDragStartMode(DragStartMode.WRAPPER);
    }

    public WidgetDragAndDropWrapper (Component widget, DropHandler dropHandler) {
        super(widget);
        this.dropHandler = dropHandler;
        setDragStartMode(DragStartMode.WRAPPER);
    }

    @Override
    public DropHandler getDropHandler() {
        return dropHandler;
    }

    public IWidget getWidget () {
        return (IWidget )super.getCompositionRoot ();
    }
}
