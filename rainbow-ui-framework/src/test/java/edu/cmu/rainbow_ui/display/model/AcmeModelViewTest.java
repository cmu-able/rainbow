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

import edu.cmu.rainbow_ui.display.DummySystemViewProvider;
import edu.cmu.rainbow_ui.display.ISystemViewProvider;
import edu.cmu.rainbow_ui.ingestion.AcmeInternalModelInstance;
import java.io.IOException;
import org.acmestudio.acme.core.exception.AcmeException;
import org.acmestudio.acme.core.resource.ParsingFailureException;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.basicmodel.element.AcmeSystem;
import org.acmestudio.standalone.resource.StandaloneResource;
import org.acmestudio.standalone.resource.StandaloneResourceProvider;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.sa.rainbow.core.models.IModelInstance;

/**
 * Unit tests for AcmeModelView class.
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class AcmeModelViewTest {

    /**
     * System view provider used to test the AcmeGraph instance
     */
    private ISystemViewProvider svp;

    /**
     * Testing implementation of the abstract AcmeModelView
     */
    private class AcmeModelViewImpl extends AcmeModelView {

        /**
         * Count number of updates.
         */
        public int updateCnt = 0;

        public AcmeModelViewImpl(ISystemViewProvider svp) {
            super(svp);
        }

        @Override
        public void subUpdate() {
            updateCnt += 1;
        }
    }

    @Before
    public void setUp() throws IOException, ParsingFailureException {
        /**
         * Setup the dummy SVP that is able to supply a model
         */
        StandaloneResource resource = StandaloneResourceProvider.instance().acmeResourceForString(
                "ZNewsSys.acme");
        AcmeSystem sys = resource.getModel().getSystems().iterator().next();
        final IModelInstance<IAcmeSystem> instance = new AcmeInternalModelInstance(sys, "Acme");

        svp = new DummySystemViewProvider() {
            @Override
            public IModelInstance<?> getView() {
                return instance;
            }
        };
    }

    /**
     * Test of isActive, activate and deactivate methods, of class AcmeModelView.
     */
    @Test
    public void testActivation() {
        System.out.println("activation");
        AcmeModelView instance = new AcmeModelViewImpl(svp);
        boolean expResult = false;
        boolean result = instance.isActive();
        assertEquals(expResult, result);

        expResult = true;
        instance.activate();
        result = instance.isActive();
        assertEquals(expResult, result);

        expResult = false;
        instance.deactivate();
        result = instance.isActive();
        assertEquals(expResult, result);
    }

    /**
     * Test of update method, of class AcmeModelView.
     *
     * @throws org.acmestudio.acme.core.exception.AcmeException
     */
    @Test
    public void testUpdate() throws IllegalStateException, AcmeException {
        System.out.println("update");
        AcmeModelView instance = new AcmeModelViewImpl(svp);
        int expResult = 0;
        int result = ((AcmeModelViewImpl) instance).updateCnt;
        assertEquals(expResult, result);

        expResult = 1;
        instance.update();
        result = ((AcmeModelViewImpl) instance).updateCnt;
        assertEquals(expResult, result);

        /**
         * After the first update the next update should not happen since the model has not changed
         */
        expResult = 1;
        instance.update();
        result = ((AcmeModelViewImpl) instance).updateCnt;
        assertEquals(expResult, result);

        /**
         * Alter the model and update again. Note that in order to avoid initial model resource the
         * modification is undone after execution.
         */
        IAcmeSystem model = (IAcmeSystem) svp.getView().getModelInstance();
        IAcmeCommand<IAcmeComponent> command = model.getCommandFactory().componentDeleteCommand(model.getComponent("Server0"));
        command.execute();

        try {
            expResult = 2;
            instance.update();
            result = ((AcmeModelViewImpl) instance).updateCnt;
            assertEquals(expResult, result);

            expResult = 2;
            instance.update();
            result = ((AcmeModelViewImpl) instance).updateCnt;
            assertEquals(expResult, result);
        } finally {
            command.undo();
        }
    }

}
