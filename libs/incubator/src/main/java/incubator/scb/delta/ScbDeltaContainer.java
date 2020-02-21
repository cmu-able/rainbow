package incubator.scb.delta;

import incubator.pval.Ensure;
import incubator.scb.MergeableScb;

import java.util.ArrayList;
import java.util.List;

/**
 * SCB delta that applies multiple deltas.
 * @param <T> type of SCB
 */
public class ScbDeltaContainer<T extends MergeableScb<T>>
		extends AbstractScbDelta<T> implements ScbDelta<T> {
	/**
	 * The deltas.
	 */
	private List<ScbDelta<T>> m_deltas;
	
	/**
	 * Creates a new container.
	 * @param target the target SCB
	 * @param source the source SCB
	 * @param deltas the deltas to apply
	 */
	public ScbDeltaContainer(T target, T source, List<ScbDelta<T>> deltas) {
		super(target, source);
		
		Ensure.not_null(deltas, "deltas == null");
		m_deltas = new ArrayList<>(deltas);
	}
	
	@Override
	public void apply() {
		for (ScbDelta<T> d : m_deltas) {
			d.apply();
		}
	}
	
	@Override
	public void revert() {
		for (int i = m_deltas.size() - 1; i >= 0; i++) {
			m_deltas.get(i).apply();
		}
	}
	
	/**
	 * Obtains the list of deltas.
	 * @return the list
	 */
	public List<ScbDelta<T>> deltas() {
		return new ArrayList<>(m_deltas);
	}
}
