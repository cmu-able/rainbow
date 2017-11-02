package org.sa.rainbow.core.ports;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.ports.eseb.AbstractESEBDisposablePort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;

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
public abstract class AbstractDelegateConnectionPort extends AbstractESEBDisposablePort implements
        IDelegateMasterConnectionPort {

    protected RainbowDelegate m_delegate;

    protected AbstractDelegateConnectionPort (RainbowDelegate delegate, String host, short port, ChannelT channel)
            throws IOException {
        super (host, port, channel);
        m_delegate = delegate;
    }

    private void report (ReportType type, RainbowComponentT compType, String msg) {
        report (m_delegate.getId (), type, compType, msg);
    }

    private void report (ReportType type, RainbowComponentT compType, String msg, Throwable t) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        PrintStream ps = new PrintStream (baos);
        t.printStackTrace (ps);
        ps.close ();
        report (m_delegate.getId (), type, compType,
                MessageFormat.format ("{0}.\nException: {1}\n{2}", msg, t.getMessage (), baos.toString ()));
    }

    @Override
    public void info (RainbowComponentT type, String msg) {
        report (ReportType.INFO, type, msg);
    }

    @Override
    public void info (RainbowComponentT type, String msg, Logger logger) {
        logger.info (msg);
        report (ReportType.INFO, type, msg);
    }

    @Override
    public void warn (RainbowComponentT type, String msg) {
        report (ReportType.WARNING, type, msg);
    }

    @Override
    public void warn (RainbowComponentT type, String msg, Throwable e) {
        report (ReportType.WARNING, type, msg, e);
    }

    @Override
    public void warn (RainbowComponentT type, String msg, Logger logger) {
        logger.warn (msg);
        report (ReportType.WARNING, type, msg);
    }

    @Override
    public void warn (RainbowComponentT type, String msg, Throwable e, Logger logger) {
        logger.warn (msg, e);
        report (ReportType.WARNING, type, msg, e);
    }

    @Override
    public void error (RainbowComponentT type, String msg) {
        report (ReportType.ERROR, type, msg);
    }

    @Override
    public void error (RainbowComponentT type, String msg, Throwable e) {
        report (ReportType.ERROR, type, msg, e);
    }

    @Override
    public void error (RainbowComponentT type, String msg, Logger logger) {
        logger.error (msg);
        report (ReportType.ERROR, type, msg);
    }

    @Override
    public void error (RainbowComponentT type, String msg, Throwable e, Logger logger) {
        logger.error (msg, e);
        report (ReportType.ERROR, type, msg, e);
    }

    @Override
    public void fatal (RainbowComponentT type, String msg) {
        report (ReportType.FATAL, type, msg);
    }

    @Override
    public void fatal (RainbowComponentT type, String msg, Throwable e) {
        report (ReportType.FATAL, type, msg, e);
    }

    @Override
    public void fatal (RainbowComponentT type, String msg, Logger logger) {
        logger.fatal (msg);
        report (ReportType.FATAL, type, msg);
    }

    @Override
    public void fatal (RainbowComponentT type, String msg, Throwable e, Logger logger) {
        logger.fatal (msg, e);
        report (ReportType.FATAL, type, msg, e);
    }

}
