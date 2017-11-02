package a;

public class C implements incubator.scb.Scb<C>, incubator.scb.MergeableIdScb<a.C> {
	private B m_b;
	private incubator.dispatch.LocalDispatcher<incubator.scb.ScbUpdateListener<C>> m_update_dispatcher;
	private int m_id;

	public B b() {
		return m_b;
	}

	public void b(B v) {
		if (org.apache.commons.lang.ObjectUtils.equals(m_b, v)) {
			return;
		}
		this.m_b = v;
		notify_update();
	}

	public incubator.dispatch.Dispatcher<incubator.scb.ScbUpdateListener<C>> dispatcher() {
		return m_update_dispatcher;
	}

	protected void notify_update() {
		this.m_update_dispatcher.dispatch(new incubator.dispatch.DispatcherOp<incubator.scb.ScbUpdateListener<C>>() {
					@Override
					public void dispatch(incubator.scb.ScbUpdateListener<C> l) {
						incubator.pval.Ensure.not_null(l, "l == null");
						l.updated(C.this);
					}
				});
	}

	public static incubator.scb.ScbField<C,B> c_b() {
		return new incubator.scb.ScbField<C,B>("b", true, null, B.class) {
			@Override
			public B get(C bean) {
				incubator.pval.Ensure.not_null(bean, "bean == null");
				return bean.b();
			}

			@Override
			public void set(C bean, B v) {
				incubator.pval.Ensure.not_null(bean, "bean == null");
				bean.b(v);
			}
		};
	}

	public static java.util.List<incubator.scb.ScbField<C, ?>> c_fields() {
		java.util.List<incubator.scb.ScbField<C, ?>> fields = new java.util.ArrayList<>();
		fields.add(c_b());
		return fields;
	}

	public java.util.List<incubator.scb.ScbField<C, ?>> fields() {
		return c_fields();
	}

	public int id() {
		return m_id;
	}

	public void merge(a.C v) {
		incubator.pval.Ensure.not_null(v, "v == null");
		incubator.pval.Ensure.equals(m_id, v.m_id, "Objects to merge do not have the same ID.");
		if (!org.apache.commons.lang.ObjectUtils.equals(m_b, v.m_b)) {
			if (m_b == null || v.m_b == null) {
				b(v.m_b);
			} else {
				m_b.merge(v.m_b);
			}
		}
	}

	public incubator.scb.delta.ScbDelta<a.C> diff_from(a.C old) {
		incubator.pval.Ensure.not_null(old, "old == null");
		java.util.List<incubator.scb.delta.ScbDelta<a.C>> delta = new java.util.ArrayList<>();
		if (!(java.util.Objects.equals(m_b, old.m_b))) {
			delta.add(new incubator.scb.delta.ScbFieldDelta(this, old, c_b(), c_b().get(old), c_b().get(this)));
		}
		
		return new incubator.scb.delta.ScbDeltaContainer(this, old, delta);
	}
}
