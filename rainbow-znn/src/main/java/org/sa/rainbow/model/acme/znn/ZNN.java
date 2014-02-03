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

public class ZNN {
    private static final String    FIND_SERVICES_EXPR      = "/self/components:T[!isArchEnabled]";
    private static final String    AVAILABLE_SERVICES_EXPR = "size (" + FIND_SERVICES_EXPR + ")";
    private static IExpressionNode s_availableServicesExpr = null;
    private static IExpressionNode s_findServicesExpression;

    public static int availableServices (ZNNModelUpdateOperatorsImpl model, IAcmeElementType<?, ?> elemType)
            throws Exception {
        if (s_availableServicesExpr == null) {
            s_availableServicesExpr = StandaloneLanguagePackHelper.defaultLanguageHelper ()
                    .designRuleExpressionFromString (
                            AVAILABLE_SERVICES_EXPR, new RegionManager ());
        }

        NodeScopeLookup nameLookup = new NodeScopeLookup ();
        nameLookup.put ("T", elemType);
        return RuleTypeChecker.evaluateAsInt (model.getModelInstance (), null, s_availableServicesExpr,
                new Stack<AcmeError> (),
                nameLookup);

    }

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
