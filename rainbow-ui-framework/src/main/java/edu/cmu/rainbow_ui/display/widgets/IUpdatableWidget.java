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
 * Interface describing updatable widgets.
 * 
 * <p>
 * The widget may update its state when active. Active means that the widget is
 * visible on the screen and should display the information. Sometimes the
 * widget may not be active even if it is displayed, for example when the data
 * it should display no longer exists in the current context.
 * </p>
 * 
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public interface IUpdatableWidget {
    /**
     * Update the display of the widget to reflect its value
     */
    public abstract void update();

    /**
     * Gets whether or not the widget is currently active
     * 
     * @return whether or not the widget is currently active
     */
    public boolean isActive();

    /**
     * Set a flag that indicates that the widget is currently active
     */
    public void activate();

    /**
     * Set a flag that indicates that the widget is currently not active
     */
    public void deactivate();
}
