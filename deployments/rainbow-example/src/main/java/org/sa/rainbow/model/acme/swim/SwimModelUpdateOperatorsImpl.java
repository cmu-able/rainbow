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
package org.sa.rainbow.model.acme.swim;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.core.type.IAcmeFloatingPointValue;
import org.acmestudio.acme.core.type.IAcmeIntValue;
import org.acmestudio.acme.element.IAcmeElementInstance;
import org.acmestudio.acme.element.IAcmeElementType;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.DefaultAcmeModel;
import org.acmestudio.acme.type.AcmeTypeHelper;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.swim.commands.SwimCommandFactory;

import javassist.Modifier;

/**
 * An Acme model that embodies the SWIM architecture and the associated
 * style-specific operations
 * 
 * @author Bradley Schmerl: schmerl
 * @author gmoreno
 */
public class SwimModelUpdateOperatorsImpl extends AcmeModelInstance {

	private final Logger LOGGER = Logger.getLogger(this.getClass());

	private SwimCommandFactory m_commandFactory;

	public SwimModelUpdateOperatorsImpl(IAcmeSystem system, String source) {
		super(system, source);
		// Make sure it is the right family
	}

	@Override
	public SwimCommandFactory getCommandFactory() throws RainbowException { 
		if (m_commandFactory == null) {
			m_commandFactory = new SwimCommandFactory(this);
		}
		return m_commandFactory;
	}

	@Override
	protected AcmeModelInstance generateInstance(IAcmeSystem sys) {
		return new SwimModelUpdateOperatorsImpl(sys, getOriginalSource());
	}


}
