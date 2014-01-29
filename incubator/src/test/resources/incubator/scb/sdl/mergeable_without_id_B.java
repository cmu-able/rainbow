package a;

public class B implements incubator.scb.MergeableScb<a.B> {
	public void merge(a.B v) {
		incubator.pval.Ensure.not_null(v, "v == null");
	}

	public incubator.scb.delta.ScbDelta<a.B> diff_from(a.B old) {
		incubator.pval.Ensure.not_null(old, "old == null");
		java.util.List<incubator.scb.delta.ScbDelta<a.B>> delta = new java.util.ArrayList<>();
		return new incubator.scb.delta.ScbDeltaContainer(this, old, delta);
	}
}