/*
 * The MIT License
 *
 * Copyrigimport java.util.List;

import org.sa.rainbow.core.util.TypedAttributeWithValue;
 person obtaining a copy
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
package org.sa.rainbow.core.ports;

import org.sa.rainbow.core.util.TypedAttributeWithValue;

import java.util.List;

/**
 * This interface represents the API through which gauges might be configured at runtime
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public interface IGaugeConfigurationPort extends IDisposablePort {
    /**
     * Configures the parameters of this Gauge using the supplied configuration parameter values.
     * 
     * @param configParams
     *            list of type-name-value triples of configuration parameters
     * @return boolean <code>true</code> if configuration succeeds, <code>false</code> otherwise
     */
    boolean configureGauge (List<TypedAttributeWithValue> configParams);

    /**
     * Causes the IGauge to call configureGauge on itself using its existing config parameters. This method is currently
     * used to reconnect to IProbes.
     * 
     * @return boolean <code>true</code> if configure call succeed, <code>false</code> otherwise
     */
    boolean reconfigureGauge ();


}
