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
package edu.cmu.rainbow_ui.storage;

import edu.cmu.rainbow_ui.ingestion.AcmeInternalModelInstance;
import java.io.IOException;
import org.acmestudio.acme.core.resource.ParsingFailureException;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.basicmodel.element.AcmeSystem;
import org.acmestudio.standalone.resource.StandaloneResource;
import org.acmestudio.standalone.resource.StandaloneResourceProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.sa.rainbow.core.models.IModelInstance;

/**
 * Unit test for AcmeModelSerializer
 * 
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class AcmeModelSerializerTest {

    public AcmeModelSerializerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of serialize method, of class AcmeModelSerializer.
     * @throws java.io.IOException
     * @throws org.acmestudio.acme.core.resource.ParsingFailureException
     */
    @Test
    public void testSerialize() throws IOException, ParsingFailureException {
        System.out.println("serialize");
        StandaloneResource resource = StandaloneResourceProvider.instance().acmeResourceForString(
                "ZNewsSys.acme");
        AcmeSystem sys = resource.getModel().getSystems().iterator().next();
        IModelInstance<IAcmeSystem> instance = new AcmeInternalModelInstance(sys, "Acme");
        AcmeModelSerializer serializer = new AcmeModelSerializer();
        String result = serializer.serialize(instance);
        
        IModelInstance<IAcmeSystem> instanceResult = serializer.deserialize(result);

        assertEquals(instance.getModelName(), instanceResult.getModelName());
        assertEquals(instance.getModelType(), instanceResult.getModelType());
        assertEquals(instance.getOriginalSource(), instanceResult.getOriginalSource());
        
        String result2 = serializer.serialize(instance);
        
        assertEquals(result, result2);
    }

}
