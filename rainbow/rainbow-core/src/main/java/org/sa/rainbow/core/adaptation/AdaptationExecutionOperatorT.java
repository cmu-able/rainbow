package org.sa.rainbow.core.adaptation;

public enum AdaptationExecutionOperatorT {
    // Execute all children in sequence, waiting for one to finish
    // before executing the next. Execute them all, regardless of
    // exit state of the child.
    SEQUENCE,
    // Execute children in sequence, waiting for one to finish 
    // before executing the next. Execute until one of the children
    // is successful.
    SEQUENCE_STOP_SUCCESS,
    // Execute children in sequence, waiting for one to finish
    // before executing the next. Execute until one of the children
    // fails.
    SEQUENCE_STOP_FAILURE,
    // Execute children in separate threads, waiting until all
    // the children have finished.
    PARALLEL,
    // Execute this leaf node.
    LEAF
}
