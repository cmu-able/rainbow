/*
 * The MIT License
 *
 * Copyrigimport org.sa.rainbow.core.Rainbow.ExitState;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
ftware and associated documentation files (the "Software"), to deal
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
package org.sa.rainbow.core.ports;

import java.util.List;

import org.sa.rainbow.core.globals.ExitState;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;

public interface IMasterCommandPort {
    void startProbes ();

    void killProbes ();

    void enableAdaptation (boolean enabled);

    Outcome testEffector (String target, String effName, List<String> args);

    void sleep ();

    void terminate (ExitState exitState);

    void restartDelegates ();

    void sleepDelegates ();

    void destroyDelegates ();

    boolean allDelegatesOK ();

    void killDelegate (String ipOfDelegate);

    List<String> getExpectedDelegateLocations ();

}
