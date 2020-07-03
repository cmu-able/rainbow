package a;

public class B implements incubator.scb.Scb<B>, incubator.scb.MergeableIdScb<a.B> {
	private int m_c;
	private int m_id;
	private incubator.dispatch.LocalDispatcher<incubator.scb.ScbUpdateListener<B>> m_update_dispatcher;

	public int c() {
		return m_c;
	}

	public void c(int v) {
		if (org.apache.commons.lang.ObjectUtils.equals(m_c, v)) {
			return;
		}

		this.m_c = v;
		notify_update();
	}

	public B(a.B src) {
		incubator.pval.Ensure.not_null(src, "src == null");
		this.m_c = src.m_c;
		this.m_id = src.m_id;
		this.m_update_dispatcher = new incubator.dispatch.LocalDispatcher<>();
	}

	public int id() {
		return m_id;
	}

	public B(int c,int id) {
		this.m_c = c;
		this.m_update_dispatcher = new incubator.dispatch.LocalDispatcher<>();
		incubator.pval.Ensure.greater_equal(id, 0, "id < 0");
		this.m_id = id;
	}

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

	public static incubator.scb.ScbIntegerField<B> c_c() {
		return new incubator.scb.ScbIntegerField<B>("c", true, null) {
			@Override
			public Integer get(B bean) {
				incubator.pval.Ensure.not_null(bean, "bean == null");
				return bean.c();
			}

			@Override
			public void set(B bean, Integer v) {
				incubator.pval.Ensure.not_null(bean, "bean == null");
				bean.c(v);
			}
		};
	}

	public static java.util.List<incubator.scb.ScbField<B, ?>> c_fields() {
		java.util.List<incubator.scb.ScbField<B, ?>> fields = new java.util.ArrayList<>();
		fields.add(c_c());
		return fields;
	}

	public java.util.List<incubator.scb.ScbField<B, ?>> fields() {
		return c_fields();
	}

	public void merge(a.B v) {
		incubator.pval.Ensure.not_null(v, "v == null");
		incubator.pval.Ensure.equals(m_id, v.m_id, "Objects to merge do not have the same ID.");
		if (!org.apache.commons.lang.ObjectUtils.equals(m_c, v.m_c)) {
			c(v.m_c);
		}
	}

	public incubator.scb.delta.ScbDelta<a.B> diff_from(a.B old) {
		incubator.pval.Ensure.not_null(old, "old == null");
		java.util.List<incubator.scb.delta.ScbDelta<a.B>> delta = new java.util.ArrayList<>();
		if (!(java.util.Objects.equals(m_c, old.m_c))) {
			delta.add(new incubator.scb.delta.ScbFieldDelta(this, old, c_c(), c_c().get(old), c_c().get(this)));
		}
		
		return new incubator.scb.delta.ScbDeltaContainer(this, old, delta);
	}
}
