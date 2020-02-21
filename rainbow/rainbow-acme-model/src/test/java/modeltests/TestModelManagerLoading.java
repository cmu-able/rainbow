/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
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
package modeltests;

import java.io.File;

import junit.framework.TestCase;

import org.acmestudio.acme.element.IAcmeSystem;
import org.junit.Test;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;

public class TestModelManagerLoading extends TestCase {

    @Test
    public void test () throws Throwable {

        File basePath = new File (System.getProperty ("user.dir"));
        File testMasterDir = new File (basePath, "src/test/resources/RainbowTest/eseb");
        System.setProperty ("user.dir", testMasterDir.getCanonicalPath ());

        RainbowMaster master = new RainbowMaster ();
        master.initialize ();
        assertTrue (master.modelsManager () != null);
        assertTrue (!master.modelsManager ().getRegisteredModelTypes ().isEmpty ());
        assertTrue (master.modelsManager ().getRegisteredModelTypes ().contains ("Acme"));

        IModelInstance<IAcmeSystem> modelInstance = master.modelsManager ().<IAcmeSystem> getModelInstance (
                new ModelReference ("ZNewsSys", "Acme"));
        assertNotNull (modelInstance);
        IAcmeSystem system = modelInstance.getModelInstance ();
        assertNotNull (system);

    }

}
