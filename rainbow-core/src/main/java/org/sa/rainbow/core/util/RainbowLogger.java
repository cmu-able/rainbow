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
package org.sa.rainbow.core.util;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.ports.IRainbowReportingPort;

public class RainbowLogger {

    public static void error (RainbowComponentT compT,
            String msg,
            Throwable e,
                              IRainbowReportingPort port,
                              Logger logger) {
        if (logger != null) {
            logger.error (msg, e);
        }
        if (port != null) {
            port.error (compT, msg, e);
        }
    }

    public static void error (RainbowComponentT compT, String msg, IRainbowReportingPort port, Logger logger) {
        if (logger != null) {
            logger.error (msg);
        }
        if (port != null) {
            port.error (compT, msg);
        }
    }

    public static void
    warn (RainbowComponentT compT, String msg, Throwable e, IRainbowReportingPort port, Logger logger) {
        if (logger != null) {
            logger.warn (msg, e);
        }
        if (port != null) {
            port.warn (compT, msg, e);
        }
    }

    public static void warn (RainbowComponentT compT, String msg, IRainbowReportingPort port, Logger logger) {
        if (logger != null) {
            logger.warn (msg);
        }
        if (port != null) {
            port.warn (compT, msg);
        }
    }

    public static void fatal (RainbowComponentT compT,
            String msg,
            Throwable e,
                              IRainbowReportingPort port,
                              Logger logger) {
        if (logger != null) {
            logger.fatal (msg, e);
        }
        if (port != null) {
            port.fatal (compT, msg, e);
        }
    }

    public static void info (RainbowComponentT compT, String msg, IRainbowReportingPort port, Logger logger) {
        if (logger != null) {
            logger.info (msg);
        }
        if (port != null) {
            port.info (compT, msg);
        }
    }

}
