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
package edu.cmu.rainbow_ui.display;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Zachary Sweigart <zsweigar@cmu.edu>
 */
public class ApplicationCoreTest {

    MockApplicationCore instance;
    private final static String SYSTEM_CONFIG_FILE = "src/test/resources/system_test.properties";

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        instance = MockApplicationCore.getInstance();
        try {
            instance.startup(SYSTEM_CONFIG_FILE);
            instance.setUseMockRainbow(false); // just in case
        } catch (Exception e) {
            fail("could not start up application core");
        }
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of startup method, of class ApplicationCore.
     */
    @Test
    public void testStartup() throws Exception {
        assertEquals(instance.isAttached(), true);
    }

    /**
     * Test of attach method, of class ApplicationCore.
     */
    @Test
    public void testAttach() {
        instance.attach();
        assertEquals(instance.isAttached(), true);
    }

    /**
     * Test of detach method, of class ApplicationCore.
     */
    @Test
    public void testDetach() {
        instance.detach();
        assertEquals(instance.isAttached(), false);
    }

    /**
     * Test of getSessionList method, of class ApplicationCore.
     */
    @Test
    public void testGetSessionList() {
        ArrayList<String> result = instance.getSessionList();
        assertNotNull(result);
    }

    /**
     * Test of isAttached method, of class ApplicationCore.
     */
    @Test
    public void testIsAttached() {
        boolean expResult = true;
        instance.attach();
        boolean result = instance.isAttached();
        assertEquals(expResult, result);
    }

}
