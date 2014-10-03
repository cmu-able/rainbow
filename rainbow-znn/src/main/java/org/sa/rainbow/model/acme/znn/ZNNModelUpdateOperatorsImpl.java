/*
 * The MIT License
 *
 * Copyright 2import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.znn.commands.ZNNCommandFactory;
oftware"), to deal
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
package org.sa.rainbow.model.acme.znn;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.znn.commands.ZNNCommandFactory;

/**
 * An Acme model that embodies the ZNN architecture and the associated style-specific operations
 * 
 * @author Bradley Schmerl: schmerl
 *
 */
public class ZNNModelUpdateOperatorsImpl extends AcmeModelInstance {

    private ZNNCommandFactory m_commandFactory;

    public ZNNModelUpdateOperatorsImpl (IAcmeSystem system, String source) {
        super (system, source);
        // Make sure it is the right family
    }

    @Override
    public ZNNCommandFactory getCommandFactory () {
        if (m_commandFactory == null) {
            m_commandFactory = new ZNNCommandFactory (this);
        }
        return m_commandFactory;
    }

    @Override
    protected AcmeModelInstance generateInstance (IAcmeSystem sys) {
        return new ZNNModelUpdateOperatorsImpl (sys, getOriginalSource ());
    }


}
