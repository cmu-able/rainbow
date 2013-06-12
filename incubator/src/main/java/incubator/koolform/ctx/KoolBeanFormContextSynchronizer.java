package incubator.koolform.ctx;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ActionContextListener;
import incubator.koolform.KoolBeanForm;

import org.apache.commons.lang.ObjectUtils;

public class KoolBeanFormContextSynchronizer<T> {
	private Class<T> beanClass;
	private KoolBeanForm<T> form;
	private ActionContext ctx;
	private String beanContextKey;
	private String editableContextKey;
	
	public KoolBeanFormContextSynchronizer(Class<T> beanClass,
			KoolBeanForm<T> form, ActionContext ctx, String beanContextKey,
			String editableContextKey) {
		if (form == null) {
			throw new IllegalArgumentException("form == null");
		}
		
		if (ctx == null) {
			throw new IllegalArgumentException("ctx == null");
		}
		
		if (beanContextKey == null) {
			throw new IllegalArgumentException("beanContextKey == null");
		}
		
		this.beanClass = beanClass;
		this.form = form;
		this.ctx = ctx;
		this.beanContextKey = beanContextKey;
		this.editableContextKey = editableContextKey;
		
		ctx.addActionContextListener(new ActionContextListener() {
			@Override
			public void contextChanged(ActionContext context) {
				reviewFormData();
			}
		});
		
		reviewFormData();
	}
	
	private void reviewFormData() {
		T t = ctx.get(beanContextKey, beanClass);
		if (!ObjectUtils.equals(t, form.getEditing())) {
			form.load(t);
		}
		
		if (editableContextKey != null) {
			Boolean editable = ctx.get(editableContextKey, Boolean.class);
			if (editable == null) {
				editable = false;
			}
			
			form.setFormEditable(editable);
		}
	}
}
