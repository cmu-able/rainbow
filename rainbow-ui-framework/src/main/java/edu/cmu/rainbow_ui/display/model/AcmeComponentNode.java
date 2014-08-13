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
package edu.cmu.rainbow_ui.display.model;

import edu.cmu.rainbow_ui.display.ui.AbstractRainbowVaadinUI;
import org.acmestudio.acme.element.IAcmeComponent;

/**
 * Visual component to display an Acme component in the graph.
 *
 * Package-scoped to be used only in the AcmeGraph class
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
class AcmeComponentNode extends AcmeElementNode {

    private static final int COMPONENT_WIDTH = 120;
    private static final int COMPONENT_LABEL_HEIGHT = 30;
    private static final int COMPONENT_WIDGET_PANEL_HEIGHT = 120;

    AcmeComponentNode(final IAcmeComponent component, final AbstractRainbowVaadinUI ui, final AcmeGraph graphView) {
        super(component, ui, graphView);
        this.setWidth(COMPONENT_WIDTH, Unit.PIXELS);
        this.setLabelHeight(COMPONENT_LABEL_HEIGHT);
        this.setWidgetsPanelHeight(COMPONENT_WIDGET_PANEL_HEIGHT);
    }
}
