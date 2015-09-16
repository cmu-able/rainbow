package org.sa.rainbow.core.adaptation;


public interface IAdaptationVisitor<T2 extends IEvaluable> {
    /**
     * Visits a tree that is a leaf node. The adaptation in the leaf node is evaluated. The result of the adaptation
     * evaluation is returned. Precondition: tree.getOperator () == LEAF
     * 
     * @param tree
     * @return
     */
    boolean visitLeaf (AdaptationTree<T2> tree);

    /**
     * Visits each subtree of three in sequence. Returns true if all subtrees return true. False otherwise.
     * 
     * @param tree
     * @return
     */
    boolean visitSequence (AdaptationTree<T2> tree);

    /**
     * Visits each subtree of the tree in sequence, stopping when the first one is successful.
     * 
     * @param tree
     * @return true if a successful adpatation was executed, false otherwise.
     */
    boolean visitSequenceStopSuccess (AdaptationTree<T2> tree);

    /**
     * Visits each subtree in sequence, stopping if a failure is reached.
     * 
     * @param tree
     * @return true if all adaptations executed. False otherwise.
     */
    boolean visitSequenceStopFailure (AdaptationTree<T2> tree);

    /**
     * Spawns a thread for each subtree to execute in parallel.
     * 
     * @param tree
     * @return false if one of the subtrees fails.
     */
    boolean visitParallel (AdaptationTree<T2> tree);

}
