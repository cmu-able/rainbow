/*
 * The MIT License
 *
 * Copyright 2014 Cimport java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.ports.eseb.ESEBProvider;
import org.sa.rainbow.translator.effectors.IEffector;

import edu.cmu.cs.able.eseb.participant.ParticipantException;
ding without limitation the rights
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
package org.sa.rainbow.core.ports.eseb.rpc;

import edu.cmu.cs.able.eseb.participant.ParticipantException;
import org.sa.rainbow.core.ports.eseb.ESEBProvider;
import org.sa.rainbow.translator.effectors.IEffector;

import java.io.IOException;
import java.util.List;

public class ESEBEffectorExecutionProviderPort extends AbstractESEBDisposableRPCPort implements
IESEBEffectorExecutionRemoteInterface {

    private IEffector        m_effector;

    public ESEBEffectorExecutionProviderPort (IEffector effector) throws IOException, ParticipantException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                effector.id ());
        m_effector = effector;
        try {
            getConnectionRole().createRegistryWrapper (IESEBEffectorExecutionRemoteInterface.class, this, effector.id ()
                    + IESEBEffectorExecutionRemoteInterface.class.getSimpleName ());
        }
        catch (Exception e) {
        }

    }

    @Override
    public Outcome execute (List<String> args) {
        return m_effector.execute (args);
    }

}
