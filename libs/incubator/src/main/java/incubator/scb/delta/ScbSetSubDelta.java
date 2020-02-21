package incubator.scb.delta;

import incubator.pval.Ensure;
import incubator.scb.MergeableScb;
import incubator.scb.ScbField;

import java.util.Set;

/**
 * SCB sub delta that performs changes in a sub-SCB of a SCB that is part
 * of a set of the <em>target</em>.
 * @param <T> the type of SCB
 * @param <ST> the type of the sub SCB
 */
public class ScbSetSubDelta<T extends MergeableScb<T>,
		ST extends MergeableScb<ST>> extends AbstractScbDelta<T>
		implements ScbSubDelta<T, ST> {
	/**
	 * The field to access the sub set.
	 */
	private ScbField<T, Set<ST>> m_f;
	
	/**
	 * The sub delta.
	 */
	private ScbDelta<ST> m_sub_delta;
	
	/**
	 * Creates a new sub delta.
	 * @param target the target SCB
	 * @param source the source SCB
	 * @param f the field
	 * @param sd the delta in the sub SCB
	 */
	public ScbSetSubDelta(T target, T source, ScbField<T, Set<ST>> f,
			ScbDelta<ST> sd) {
		super(target, source);
		
		Ensure.not_null(f, "f == null");
		Ensure.not_null(sd, "sd == null");
		m_f = f;
		m_sub_delta = sd;
	}
	
	@Override
	public void apply() {
		Ensure.is_true(m_f.get(target()).contains(m_sub_delta.target()),
				"Sub target not found in SCB set");
		m_sub_delta.apply();
	}
	
	@Override
	public void revert() {
		Ensure.is_true(m_f.get(target()).contains(m_sub_delta.target()),
				"Sub target not found in SCB set");
		m_sub_delta.revert();
	}
	
	@Override
	public ScbDelta<ST> sub_delta() {
		return m_sub_delta;
	}
}
