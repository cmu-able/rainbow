package a;

public class B implements incubator.scb.Scb<B>, incubator.scb.MergeableIdScb<a.B> {
	private incubator.dispatch.LocalDispatcher<incubator.scb.ScbUpdateListener<B>> m_update_dispatcher;
	private int m_id;

	public incubator.dispatch.Dispatcher<incubator.scb.ScbUpdateListener<B>> dispatcher() {
		return m_update_dispatcher;
	}

	protected void notify_update() {
		this.m_update_dispatcher.dispatch(new incubator.dispatch.DispatcherOp<incubator.scb.ScbUpdateListener<B>>() {
					@Override
					public void dispatch(incubator.scb.ScbUpdateListener<B> l) {
						incubator.pval.Ensure.not_null(l, "l == null");
						l.updated(B.this);
					}
				});
	}

	public static java.util.List<incubator.scb.ScbField<B, ?>> c_fields() {
		java.util.List<incubator.scb.ScbField<B, ?>> fields = new java.util.ArrayList<>();
		return fields;
	}

	public java.util.List<incubator.scb.ScbField<B, ?>> fields() {
		return c_fields();
	}

	public int id() {
		return m_id;
	}

	public void merge(a.B v) {
		incubator.pval.Ensure.not_null(v, "v == null");
		incubator.pval.Ensure.equals(m_id, v.m_id, "Objects to merge do not have the same ID.");
	}

	public incubator.scb.delta.ScbDelta<a.B> diff_from(a.B old) {
		incubator.pval.Ensure.not_null(old, "old == null");
		java.util.List<incubator.scb.delta.ScbDelta<a.B>> delta = new java.util.ArrayList<>();
		return new incubator.scb.delta.ScbDeltaContainer(this, old, delta);
	}
}
