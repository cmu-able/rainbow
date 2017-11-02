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

import com.vaadin.ui.CustomComponent;
import edu.cmu.rainbow_ui.display.ISystemViewProvider;
import edu.cmu.rainbow_ui.display.widgets.IUpdatableWidget;
import org.acmestudio.acme.element.IAcmeAttachment;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeConnector;
import org.acmestudio.acme.element.IAcmeSystem;

/**
 * Abstract class to describe generic model view for Acme models.
 *
 * <p>
 * Provides methods that could be used by all model viewers for Acme models
 * </p>
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public abstract class AcmeModelView extends CustomComponent implements IUpdatableWidget {

    /**
     * Active state of the model view
     */
    private boolean active;

    /**
     * System view provider to access the model
     */
    protected final ISystemViewProvider systemViewProvider;

    /**
     * Currently displayed system model
     */
    protected IAcmeSystem model;

    /**
     * Hashes to identify model changes
     */
    private int componentHash = 0;
    private int connectorsHash = 0;
    private int attachmentsHash = 0;

    /**
     * Common Acme model viewer constructor.
     *
     * @param svp system view provider
     */
    AcmeModelView(ISystemViewProvider svp) {
        systemViewProvider = svp;
        active = false;
        model = (IAcmeSystem) systemViewProvider.getView().getModelInstance();
    }

    @Override
    public void update() {
        model = (IAcmeSystem) systemViewProvider.getView().getModelInstance();
        if (isModelChanged()) {
            subUpdate();
        }
    }

    /**
     * The update action of the concrete model view
     */
    protected abstract void subUpdate();

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void activate() {
        active = true;
    }

    @Override
    public void deactivate() {
        active = false;
    }

    /**
     * Check whether the model has changed. The method doesn't compare two models on equality, but
     * rather compare their magic numbers. It is guaranteed that if the model has not changed this
     * method will return false.
     * 
     * @return changed or not
     */
    private boolean isModelChanged() {
        int c_hash = 0;
        int n_hash = 0;
        int a_hash = 0;

        for (IAcmeComponent component : model.getComponents()) {
            c_hash += component.getName().hashCode();
        }

        for (IAcmeConnector connector : model.getConnectors()) {
            n_hash += connector.getName().hashCode();
        }

        for (IAcmeAttachment attachment : model.getAttachments()) {
            a_hash += attachment.getName().hashCode();
        }

        boolean res = !((c_hash == componentHash) && (n_hash == connectorsHash) && (a_hash == attachmentsHash));

        componentHash = c_hash;
        connectorsHash = n_hash;
        attachmentsHash = a_hash;

        return res;

    }
}
