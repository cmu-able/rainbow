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

import edu.cmu.rainbow_ui.common.MockRainbow;
import edu.cmu.rainbow_ui.common.SystemConfiguration;
import edu.cmu.rainbow_ui.ingestion.RuntimeAggregatorException;
import edu.cmu.rainbow_ui.storage.MockDatabaseConnector;

/**
 * This class allows an ApplicationCore to be created with mock elements for
 * testing
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class MockApplicationCore extends AccessibleApplicationCore {
    static MockApplicationCore instance;

    public static MockApplicationCore getInstance() {
        if (instance == null) {
            instance = new MockApplicationCore();
        }
        return instance;
    }

    /**
     * Initializes the application and starts the background threads
     * 
     * @param filename contains the path to the file that contains the system
     *        configuration
     * @throws java.lang.Exception
     */
    public void startup(String filename) throws Exception {
        sysConfig = new SystemConfiguration(filename);

        try {
            startBackground();
        } catch (Exception e) {
            // Rethrow up to UI
            throw e;
        }

        registerWidgets();
    }

    /**
     * Starts the necessary background threads based on the configuration
     * 
     * @throws java.lang.Exception
     */
    protected void startBackground() throws Exception {

        databaseCon = new MockDatabaseConnector();

        try {
            runtimeAgg = new MockAcmeRuntimeAggregator(sysConfig, databaseCon);
            if (CREATE_MOCK_RAINBOW) {
                mockRainbow = new MockRainbow(this.runtimeAgg);
            }
        } catch (RuntimeAggregatorException ex) {
            // Rethrow up to UI
            throw ex;
        }

        if (sysConfig.isAttached()) {
            attach();
        }
    }
}
