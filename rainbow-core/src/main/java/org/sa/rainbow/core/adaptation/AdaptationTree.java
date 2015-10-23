package org.sa.rainbow.core.adaptation;


import java.util.ArrayList;
import java.util.Collection;

/**
 * Implements an adaptation tree that specifies how to execute adaptations. Leaf nodes of the tree have evaluable
 * adaptations stored as data, and no children. Subtrees may be added as children, and the execution of these trees
 * specified in operator.
 * 
 * @author Bradley Schmerl: schmerl
 *
 * @param <T>
 */
public class AdaptationTree<T extends IEvaluable> {

    /** The data associated with this LEAF node. data != null iff operator==LEAF **/

    final private T data;

    /** How to execute the leaves of this treee. !leafs.isEmpty iff operator != LEAF **/
    final private AdaptationExecutionOperatorT operator;

    /** The children of the tree **/
    private final ArrayList<AdaptationTree<T>> branches = new ArrayList<> ();

    /** The parent of this tree **/

    private AdaptationTree<T> parent = null;

    public AdaptationTree (T head) {
        this.data = head;
        operator = AdaptationExecutionOperatorT.LEAF;
    }

    public AdaptationTree (AdaptationExecutionOperatorT operator) {
        this.operator = operator;
        this.data = null;
    }

    public void addLeaf (T root, T leaf) {
        addLeaf (root).addLeaf (leaf);
    }


    public AdaptationTree<T> addLeaf (T leaf) {
        AdaptationTree<T> t = new AdaptationTree<> (leaf);
        branches.add (t);
        t.parent = this;
        return t;
    }


    public AdaptationTree<T> setAsParent (T parentRoot) {
        AdaptationTree<T> t = new AdaptationTree<> (parentRoot);
        t.branches.add (this);
        this.parent = t;
        return t;
    }


    public T getHead () {
        return data;
    }


    public AdaptationTree<T> getParent () {
        return parent;
    }


    public Collection<AdaptationTree<T>> getSubTrees () {
        return branches;
    }

    public AdaptationExecutionOperatorT getOperator () {
        return operator;
    }


    @Override
    public String toString () {
        return printTree (0);
    }

    private static final int indent = 2;


    private String printTree (int increment) {
        String s;
        String inc = "";
        for (int i = 0; i < increment; ++i) {
            inc = inc + " ";
        }
        s = inc + data;
        for (AdaptationTree<T> child : branches) {
            s += "\n" + child.printTree (increment + indent);
        }
        return s;
    }

    /**
     * Visits this tree, calling the visitor based on the kind of tree it is and its operator.
     * 
     * @param visitor
     *            the visitor to visit the tree
     * @return
     */
    public boolean visit (IAdaptationVisitor<T> visitor) {
        switch (operator) {
        case LEAF:
            return visitor.visitLeaf (this);
        case SEQUENCE:
            return visitor.visitSequence (this);
        case SEQUENCE_STOP_FAILURE:
            return visitor.visitSequenceStopFailure (this);
        case SEQUENCE_STOP_SUCCESS:
            return visitor.visitSequenceStopSuccess (this);
        case PARALLEL:
            return visitor.visitParallel (this);
        }
        return false;
    }
}