/*
 * The MIT License
 *
 * Copyrigimport java.io.IOException;
import java.util.Properties;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.eseb.AbstractESEBDisposablePort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
r sell
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
package org.sa.rainbow.core.ports;

import java.io.IOException;
import java.util.Properties;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.eseb.AbstractESEBDisposablePort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;

public abstract class AbstractMasterConnectionPort extends AbstractESEBDisposablePort implements IMasterConnectionPort {

    final protected RainbowMaster m_master;

    public AbstractMasterConnectionPort (RainbowMaster master, short port, ChannelT channel) throws IOException {
        super (port, channel);
        m_master = master;
    }

    @Override
    public IDelegateManagementPort connectDelegate (String delegateID, Properties connectionProperties) throws RainbowConnectionException {
        IDelegateManagementPort port = m_master.connectDelegate (delegateID, connectionProperties);
        return port;
    }

    @Override
    public void report (String delegateID, ReportType type, RainbowComponentT compT, String msg) {
        m_master.report (delegateID, type, compT, msg);
    }



}
