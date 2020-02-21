/**
 * <p>The <code>scbset</code> package provides a set which contains SCBs.
 * The set, implemented in the {@link incubator.scb.scbset.ScbSet} class,
 * provides an interface to add and remove SCBs as well as informing listeners
 * of changes in the set and in the SCBs themselves.</p>
 * 
 * <p>An important feature of the set is the ability to keep a <em>change
 * log</em> ({@link incubator.scb.scbset.ChangeLog}). A change log keeps
 * track of changes in a set and is able to apply these changes to another
 * set. This can be used to incrementally keep sets synchronized.</p>
 * 
 * <p>The SCBs that can be used with the <code>scbset</code> package have to
 * fulfill several interfaces: the {@link incubator.scb.Scb}, the
 * {@link incubator.scb.MergeableIdScb} and the
 * {@link incubator.scb.CloneableScb}.</p>
 * 
 * <p>Keeping an SCB set in sync with another SCB set is done with an
 * {@link incubator.scb.scbset.ChangeLogApplier}. This class receives data
 * from a change log and uses it to update an SCB set. A filter may be
 * provided to keep the destination set as a sub set of the original set.</p>
 */
package incubator.scb.scbset;
