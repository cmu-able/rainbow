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
package org.sa.rainbow.core.gauges;

import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import java.util.Collection;

/**
 * This interface defines the set of methods for returning the state of gauge. The state of the gauge is defined as all
 * of the setup parameters, the current configuration parameters, and the last set of commands that were issued.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public interface IGaugeState {
    /**
     * 
     * @return The collection of setup parameters that were used to set up this gauge.
     */
    Collection<? extends TypedAttributeWithValue> getSetupParams ();

    /**
     * 
     * @return The set of configuration parameters (and their current values) with which the gauge has been configured.
     */
    Collection<? extends TypedAttributeWithValue> getConfigParams ();

    /**
     * 
     * @return The set of commands that were most recently issued (one entry per command kind).
     */
    Collection<? extends IRainbowOperation> getGaugeReports ();
}
