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
/**
 * Created March 13, 2007.
 */
package org.sa.rainbow.stitch.adaptation;

import org.sa.rainbow.translator.effectors.IEffector;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;

import java.util.Map;


/**
 * This interface defines the generic architectural operators that an
 * "adaptation-enabled" system should, and would, at the minimum provide to
 * Rainbow to make effecting changes possible.  In this implementation, each
 * operator would be mapped by name to an effector implemented in the system.
 * More specifically, an <em>archElement.{operator}</em> would map to a
 * <em>sysTargetLocation.{effectorName}</em>: <ul>
 * <li> {@linkplain start()}:  starts, or activates, a component
 * <li> {@linkplain stop()}:   stops, or deactivates, a component
 * <li> {@linkplain changeState()}:  changes some designated state of an element, in the form of key:value pairs
 * <li> {@linkplain connect()}:  connects two components, essentially creating a connector
 * <li> {@linkplain disconnect()}:  disconnects two components, essentially removing the connector
 * <li> {@linkplain execute()}:  a generic execute statement, on an element, that accepts an array of String parameters
 * </ul>
 * <p>
 * For a similar idea, confer Mikik-Rakic and Medvidovic's 2002 WOSS paper.
 * <p>
 * Note:  This interface could perhaps be given a better name.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IGenericArchOperators {

    /** The NULL effect implementation of this interface */
    IGenericArchOperators NULL_OP = new IGenericArchOperators () {
        @Override
        public Outcome start (String effName, Object component, String[] optArgs) {
            return IEffector.Outcome.UNKNOWN;
        }
        @Override
        public Outcome stop (String effName, Object component, String[] optArgs) {
            return IEffector.Outcome.UNKNOWN;
        }
        @Override
        public Outcome changeState (String effName, Object element, Map<String,String> statePairs) {
            return IEffector.Outcome.UNKNOWN;
        }
        @Override
        public Outcome connect (String effName, Object initiatingComp, Object targetComp, String[] optArgs) {
            return IEffector.Outcome.UNKNOWN;
        }
        @Override
        public Outcome disconnect (String effName, Object comp1, Object comp2, Object conn, String[] optArgs) {
            return IEffector.Outcome.UNKNOWN;
        }
        @Override
        public IEffector.Outcome execute (String effName, Object element, String[] args) {
            return IEffector.Outcome.UNKNOWN;
        }
    };

    /**
     * Starts the system component represented by the <code>component</code>
     * instance, using the system-level Effector identified by <code>effName</code>.
     * A basic start operation is assumed not to require arguments, but this
     * interface allows String arguments to be supplied.
     * @param effName    the name of the system-level Effector to invoke that
     *     achieves the "start" effect
     * @param component  the architecture component instance, from which the
     *     implementing method should deduce the actual system target to invoke Effector
     * @param optArgs     the array of any optional String arguments to supply to the Effector
     * @return {@link IEffector.Outcome}  the "start" Effector execution outcome
     */
    Outcome start (String effName, Object component, String[] optArgs);

    /**
     * Stops the system component represented by the <code>component</code>
     * instance, using the system-level Effector identified by <code>effName</code>.
     * A basic stop operation is assumed not to require arguments, but this
     * interface allows String arguments to be supplied.
     * @param effName    the name of the system-level Effector to invoke that
     *     achieves the "stop" effect
     * @param component  the architecture component instance, from which the
     *     implementing method should deduce the actual system target to invoke Effector
     * @param optArgs     the array of any optional String arguments to supply to the Effector
     * @return {@link IEffector.Outcome}  the "stop" Effector execution outcome
     */
    Outcome stop (String effName, Object component, String[] optArgs);

    /**
     * Changes some known internal state of a system element represented by the
     * <code>element</code> instance, using the system-level Effector identified
     * by <code>effName</code>.
     * The basic changeState operation is assumed to take a pairs of values,
     * each of which consists of the name of the property, the "key", to alter,
     * and the new value to set the property (in String form).
     * @param effName     the name of the system-level Effector to invoke that
     *     achieves the "state-change" effect
     * @param element     the architecture element instance, which could be a
     *     component, connector, or an interfaces, from which the implementing
     *     method should deduce the actual system target to invoke Effector
     * @param statePairs  the key-value pairs for updaing states
     * @return {@link IEffector.Outcome}  the "changeState" Effector execution outcome
     */
    Outcome changeState (String effName, Object element, Map<String, String> statePairs);

    /**
     * Connects two system components, identified by <code>initiatingComp</code>
     * and <code>targetComp</code>, to form a connector, using the system-level
     * Effector identified by <code>effName</code>.
     * A basic connect operation would only make sense initiated from one of the
     * two involved component entities, so we've imposed the convention that the
     * first component argument would be the initiating component, and the second
     * the target component.  This assumption implies that the connect Effector
     * would be invoked on <code>initiatingComp</code>, parametrized using
     * <code>targetComp</code> as the connection target.
     * @param effName         the name of the system-level Effector to invoke
     *     that achieves the "connect" effect
     * @param initiatingComp  the initiating architecture component instance,
     *     from which the implementing method should deduce the actual system
     *     target to invoke Effector
     * @param targetComp      the connection-target architecture component instance
     * @param optArgs     the array of any optional String arguments to supply to the Effector
     * @return {@link IEffector.Outcome}  the "connect" Effector execution outcome
     */
    Outcome connect (String effName, Object initiatingComp, Object targetComp,
                     String[] optArgs);

    /**
     * Disconnects the connector connecting two system components,
     * <code>comp1</code> and <code>comp2</code>, optionally identified by
     * <code>conn</code> in case more than one connector exists, using the
     * system-level Effector identified by <code>effName</code>.
     * A basic disconnect operation would only make sense from one of the two
     * involved component entities, so we've imposed the convention that the
     * first component argument would initiate the disconnect.
     * This assumption implies that the disconnect Effector would be invoked on
     * <code>comp1</code>, parametrized using <code>comp2</code> as the target.
     * @param effName  the name of the system-level Effector to invoke
     *     that achieves the "disconnect" effect
     * @param comp1    the initiating architecture component instance,
     *     from which the implementing method should deduce the actual system
     *     target to invoke Effector
     * @param comp2    the disconnection-target architecture component instance
     * @param conn     an optional connector instance for the purpose of distinction,
     *     so this argument may be null
     * @param optArgs     the array of any optional String arguments to supply to the Effector
     * @return {@link IEffector.Outcome}  the "disconnect" Effector execution outcome
     */
    Outcome disconnect (String effName, Object comp1, Object comp2,
                        Object conn, String[] optArgs);

    /**
     * Executes an Effector identified by <code>effName</code> on the architecture
     * element identified by <code>element</code>, using the supplied array of
     * String arguments.  This is the generic effect operator.
     * @param effName  the name of the system-level Effector to invoke
     * @param element  the architecture element instance, which could be a
     *     component, connector, or an interfaces, from which the implementing
     *     method should deduce the actual system target to invoke Effector
     * @param args     the array of String arguments to supply to the Effector
     * @return {@link IEffector.Outcome}  the generic Effector execution outcome
     */
    Outcome execute (String effName, Object element, String[] args);

}
