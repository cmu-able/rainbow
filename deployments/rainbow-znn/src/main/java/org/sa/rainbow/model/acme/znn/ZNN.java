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
package org.sa.rainbow.model.acme.znn;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.acmestudio.acme.core.resource.RegionManager;
import org.acmestudio.acme.element.IAcmeElementInstance;
import org.acmestudio.acme.element.IAcmeElementType;
import org.acmestudio.acme.environment.error.AcmeError;
import org.acmestudio.acme.rule.AcmeSet;
import org.acmestudio.acme.rule.node.IExpressionNode;
import org.acmestudio.acme.type.verification.NodeScopeLookup;
import org.acmestudio.acme.type.verification.RuleTypeChecker;
import org.acmestudio.standalone.resource.StandaloneLanguagePackHelper;

/**
 * This class provides ZNN-specific operators that can be referred to in strategies and tactics. This class can be
 * imported into Stitch and the static members called from there.
 * 
 * @author Bradley Schmerl: schmerl
 *
 */
public class ZNN {
    // Acme expressions for evaluating the methods
    private static final String    FIND_SERVICES_EXPR      = "/self/components:T[!isArchEnabled]";
    private static final String    AVAILABLE_SERVICES_EXPR = "size (" + FIND_SERVICES_EXPR + ")";
    // Holder objects for caching parsed Acme expressions
    private static IExpressionNode s_availableServicesExpr = null;
    private static IExpressionNode s_findServicesExpression;

    /**
     * Returns the number services of a particular type that are available. Availability here means that they are not
     * yet marked in the architecture as available Assumes: exists property called isArchEnabled on elements of the type
     * 
     * @param model
     *            The model to find availabile services in
     * @param elemType
     *            The type of service to find
     * @return the number of services that are available.
     * @throws Exception
     */
    public static int availableServices (ZNNModelUpdateOperatorsImpl model, IAcmeElementType<?, ?> elemType)
            throws Exception {
        if (s_availableServicesExpr == null) {
            s_availableServicesExpr = StandaloneLanguagePackHelper.defaultLanguageHelper ()
                    .designRuleExpressionFromString (
                            AVAILABLE_SERVICES_EXPR, new RegionManager ());
        }

        // Add "T" as a name in scope, referring to the type that we are looking for
        NodeScopeLookup nameLookup = new NodeScopeLookup ();
        nameLookup.put ("T", elemType);
        return RuleTypeChecker.evaluateAsInt (model.getModelInstance (), null, s_availableServicesExpr,
                new Stack<AcmeError> (),
                nameLookup);

    }

    /**
     * Returns the services of a particular type that are available. Availability here means that they are not yet
     * marked in the architecture as available Assumes: exists property called isArchEnabled on elements of the type
     * 
     * @param model
     *            The model to find availabile services in
     * @param type
     *            The type of service to find
     * @return the number of services that are available.
     * @throws Exception
     */
    public static Set<IAcmeElementInstance<?, ?>> findServices (ZNNModelUpdateOperatorsImpl model,
            IAcmeElementType<?, ?> type)
                    throws Exception {
        if (s_findServicesExpression == null) {
            s_findServicesExpression = StandaloneLanguagePackHelper.defaultLanguageHelper ()
                    .designRuleExpressionFromString (FIND_SERVICES_EXPR, new RegionManager ());
        }
        NodeScopeLookup nameLookup = new NodeScopeLookup ();
        nameLookup.put ("T", type);
        AcmeSet set = RuleTypeChecker.evaluateAsSet (model.getModelInstance (), null, s_findServicesExpression,
                new Stack<AcmeError> (),
                nameLookup);

        Set<IAcmeElementInstance<?, ?>> retSet = new HashSet<> ();
        for (Object o : set.getValues ()) {
            if (o instanceof IAcmeElementInstance<?, ?>) {
                retSet.add ((IAcmeElementInstance<?, ?> )o);
            }
        }
        return retSet;
    }
}
