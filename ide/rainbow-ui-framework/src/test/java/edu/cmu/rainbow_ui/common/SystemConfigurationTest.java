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
package edu.cmu.rainbow_ui.common;

import java.io.IOException;
import javax.naming.NamingException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for System Configuration
 *
 * @author Denis Anisimov <dbanisimov@cmu.edu>
 */
public class SystemConfigurationTest {

    private SystemConfiguration instance;

    public SystemConfigurationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws IOException, NamingException {
        instance = new SystemConfiguration("src/test/resources/system_default.properties");
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getRainbowHost method, of class SystemConfiguration.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testGetRainbowHost() throws IOException {
        System.out.println("getRainbowHost");
        String expResult = "127.0.0.1";
        String result = instance.getRainbowHost();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRainbowPort method, of class SystemConfiguration.
     */
    @Test
    public void testGetRainbowPort() {
        System.out.println("getRainbowPort");
        short expResult = 1100;
        short result = instance.getRainbowPort();
        assertEquals(expResult, result);
    }

    /**
     * Test of getModelPath method, of class SystemConfiguration.
     */
    @Test
    public void testGetModelPath() {
        System.out.println("getModelPath");
        String expResult = "ZNewsSys.acme";
        String result = instance.getModelPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNode method, of class SystemConfiguration.
     */
    @Test
    public void testGetNode() {
        System.out.println("getNode");
        String expResult = "127.0.0.1";
        String result = instance.getNode();
        assertEquals(expResult, result);
    }

    /**
     * Test of isAttached method, of class SystemConfiguration.
     */
    @Test
    public void testIsAttached() {
        System.out.println("isAttached");
        boolean expResult = true;
        boolean result = instance.isAttached();
        assertEquals(expResult, result);
    }

    /**
     * Test of setAttached method, of class SystemConfiguration.
     */
    @Test
    public void testSetAttached() {
        System.out.println("setAttached");
        boolean attached = false;
        instance.setAttached(attached);
        boolean result = instance.isAttached();
        assertEquals(attached, result);
    }

    /**
     * Test of setRainbowHost method, of class SystemConfiguration.
     */
    @Test
    public void testSetRainbowHost() {
        System.out.println("setRainbowHost");
        String host = "192.168.0.1";
        instance.setRainbowHost(host);
        String result = instance.getRainbowHost();
        assertEquals(host, result);
    }

    /**
     * Test of setRainbowPort method, of class SystemConfiguration.
     */
    @Test
    public void testSetRainbowPort() {
        System.out.println("setRainbowPort");
        short port = 2200;
        instance.setRainbowPort(port);
        short result = instance.getRainbowPort();
        assertEquals(port, result);
    }

    /**
     * Test of setModelPath method, of class SystemConfiguration.
     */
    @Test
    public void testSetModelPath() {
        System.out.println("setModelPath");
        String path = "Model.acme";
        instance.setModelPath(path);
        String result = instance.getModelPath();
        assertEquals(path, result);
    }

    /**
     * Test of setNode method, of class SystemConfiguration.
     */
    @Test
    public void testSetNode() {
        System.out.println("setNode");
        String node = "";
        instance.setNode(node);
        String result = instance.getNode();
        assertEquals(result, result);
    }

    /**
     * Test of getConfigDir method, of class SystemConfiguration.
     */
    @Test
    public void testGetConfigDir() {
        System.out.println("getConfigDir");
        String expResult = "./";
        String result = instance.getConfigDir();
        assertEquals(expResult, result);
    }

    /**
     * Test of setConfigDir method, of class SystemConfiguration.
     */
    @Test
    public void testSetConfigDir() {
        System.out.println("setConfigDir");
        String path = "dir";
        instance.setConfigDir(path);
        String result = instance.getConfigDir();
        assertEquals(path, result);
    }

    /**
     * Test of getSnapshotRate method, of class SystemConfiguration.
     */
    @Test
    public void testGetSnapshotRate() {
        System.out.println("getSnapshotRate");
        int expResult = 10;
        int result = instance.getSnapshotRate();
        assertEquals(expResult, result);
    }

    /**
     * Test of setSnapshotRate method, of class SystemConfiguration.
     */
    @Test
    public void testSetSnapshotRate() {
        System.out.println("setSnapshotRate");
        int rate = 100;
        instance.setSnapshotRate(rate);
        int result = instance.getSnapshotRate();
        assertEquals(rate, result);
    }

    /**
     * Test of isModelLocal method, of class SystemConfiguration.
     */
    @Test
    public void testIsModelLocal() {
        System.out.println("isModelLocal");
        boolean expResult = true;
        boolean result = instance.isModelLocal();
        assertEquals(expResult, result);
        instance.remove("ingestion.model.local");
        /* Default value should be false */
        expResult = false;
        result = instance.isModelLocal();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of setModelLocal method, of class SystemConfiguration.
     */
    @Test
    public void testSetModelLocal() {
        System.out.println("setModelLocal");
        boolean local = false;
        instance.setModelLocal(local);
        boolean result = instance.isModelLocal();
        assertEquals(local, result);
    }

    /**
     * Test of getModelName method, of class SystemConfiguration.
     */
    @Test
    public void testGetModelName() {
        System.out.println("getModelName");
        String expResult = "ZNewsSys";
        String result = instance.getModelName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setModelName method, of class SystemConfiguration.
     */
    @Test
    public void testSetModelName() {
        System.out.println("setModelName");
        String name = "ZNN";
        instance.setModelName(name);
        String result = instance.getModelName();
        assertEquals(name, result);
    }

    /**
     * Test of getModelFactoryClass method, of class SystemConfiguration.
     */
    @Test
    public void testGetModelFactoryClass() {
        System.out.println("getModelFactoryClass");
        String expResult = "edu.cmu.rainbow_ui.ingestion.AcmeInternalCommandFactory";
        String result = instance.getModelFactoryClass();
        assertEquals(expResult, result);
    }

    /**
     * Test of setModelFactoryClass method, of class SystemConfiguration.
     */
    @Test
    public void testSetModelFactoryClass() {
        System.out.println("setModelFactoryClass");
        String cls = "org.sa.rainbow.model.acme.znn.commands.ZNNCommandFactory";
        instance.setModelFactoryClass(cls);
        String result = instance.getModelFactoryClass();
        assertEquals(cls, result);

    }
    
    
}
