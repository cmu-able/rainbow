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
package org.sa.rainbow.core.adaptation;

import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IRainbowReportingPort;

/**
 * THis interface specifies the API for adaptation managers in Rainbow
 * 
 * @author Bradley Schmerl: schmerl
 *
 * @param <S>
 */
public interface IAdaptationManager<S extends IEvaluable> extends IRainbowRunnable {

    /**
     * Which model in the models manager are the adaptations in this manager for.
     * 
     * @param modelRef
     *            The model to manage
     */
    void setModelToManage (ModelReference modelRef);

    /**
     * Marks a particular adaptation as executed by an adaptation executor
     * 
     * @param strategy
     *            The strategy (managed by this manager) that was executor
     */
    void markStrategyExecuted (AdaptationTree<S> strategy);

    /**
     * The interface for initializing this component
     * 
     * @param port
     * @throws RainbowConnectionException
     */
    void initialize (IRainbowReportingPort port) throws RainbowConnectionException;

    /**
     * Sets whether this manager is enabled. Unenabled managers are not suggesting adaptations.
     * 
     * @param enabled
     */
    void setEnabled (boolean enabled);

    boolean isEnabled ();

}
