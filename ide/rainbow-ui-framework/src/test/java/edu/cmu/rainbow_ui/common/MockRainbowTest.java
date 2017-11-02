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

import org.junit.Before;
import org.junit.Test;
import edu.cmu.rainbow_ui.ingestion.AcmeRuntimeAggregator;
import edu.cmu.rainbow_ui.storage.DummyDatabaseConnector;

/**
 * This class provides unit tests for the mock application rainbow component
 *
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class MockRainbowTest {

    private ISystemConfiguration systemConfig;

    @Before
    public void setUp() throws Exception {
        systemConfig = new SystemConfiguration("src/test/resources/system_default.properties");
    }

    /**
     * Test the creation of the mock rainbow component
     */
    @Test
    public void mockRainbowCreation() throws Exception {
        MockRainbow rainbow = new MockRainbow(new AcmeRuntimeAggregator(systemConfig, new DummyDatabaseConnector()));
    }

    /**
     * Test that the component can be stopped and restarted
     */
    @Test
    public void mockRainbowRestart() throws Exception {
        MockRainbow rainbow = new MockRainbow(new AcmeRuntimeAggregator(systemConfig, new DummyDatabaseConnector()));

        rainbow.run();
        rainbow.cancel();
        rainbow.run();
    }

}
