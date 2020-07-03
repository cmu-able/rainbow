/*
 * The MIT License
 *
 * Copyright 2014 CMUimport java.io.File;

import org.sa.rainbow.translator.probes.AbstractRunnableProbe;
import org.sa.rainbow.util.Util;
 associated documentation files (the "Software"), to deal
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
package org.sa.rainbow.translator.znn.probes;

import java.io.File;

import org.sa.rainbow.translator.probes.AbstractRunnableProbe;
import org.sa.rainbow.util.Util;

public class CaptchaProbe extends AbstractRunnableProbe {

    private static final String PROBE_TYPE = "captchaprobe";
    private String              m_captchaFile;

    public CaptchaProbe (String id, long sleepTime) {
        super (id, PROBE_TYPE, Kind.JAVA, sleepTime);
    }

    public CaptchaProbe (String id, long sleepTime, String[] args) {
        this (id, sleepTime);
        if (args.length == 1) {
            m_captchaFile = args[0];
        }
    }

    @Override
    public void run () {
        Thread currentThread = Thread.currentThread ();
        if (m_captchaFile == null) {
            LOGGER.error ("No captcha file specified");
            tallyError ();
        }
        while (thread () == currentThread && isActive ()) {
            try {
                Thread.sleep (sleepTime ());
            }
            catch (InterruptedException e) {
            }
            File f = new File (m_captchaFile);
            boolean captchaOn = f.exists ();
            String rpt = captchaOn ? "on" : "off";
            reportData (rpt);
            Util.dataLogger ().info (rpt);
        }
    }

}
