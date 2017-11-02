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

import com.vaadin.ui.UI;

import edu.cmu.rainbow_ui.display.ISystemViewProvider;
import edu.cmu.rainbow_ui.display.config.ViewConfiguration;
import edu.cmu.rainbow_ui.display.viewcontrol.ViewControl;

/**
 * Abstract Rainbow Vaadin UI.
 * 
 * <p>
 * Is used to combine Vaadin abstract UI and Rainbow specific UI interfaces
 * </p>
 * 
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public abstract class AbstractRainbowVaadinUI extends UI {
    /**
     * Return the system view provider for the current UI session
     * 
     * @return system view provider
     */
    public abstract ISystemViewProvider getSystemViewProvider();

    /**
     * Return the view control for the current UI session
     * 
     * @return view control
     */
    public abstract ViewControl getViewControl();

    /**
     * Return the view configuration for the current UI session
     * 
     * @return view configuration
     */
    public abstract ViewConfiguration getViewConfiguraion();

    /**
     * Force the UI to update its state.
     */
    public abstract void pushUpdate();
    
    /**
     * Save the view configuration
     */
    public abstract void saveViewConfiguration();

    /**
     * Set the view configuration
     */
    public abstract void setViewConfiguration(ViewConfiguration viewConfig);
   
}
