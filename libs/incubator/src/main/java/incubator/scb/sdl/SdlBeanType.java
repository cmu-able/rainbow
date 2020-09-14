package incubator.scb.sdl;

import incubator.jcodegen.JavaClassWithCopyConstructorType;
import incubator.jcodegen.JavaMethod;
import incubator.jcodegen.JavaType;
import incubator.pval.Ensure;

/**
 * Sdl type that refers to an SDL bean.
 */
public class SdlBeanType extends SdlType {
	/**
	 * The bean.
	 */
	private SdlBean m_bean;
	
	/**
	 * Creates a new type.
	 * @param b the bean it refers to
	 */
	public SdlBeanType(SdlBean b) {
		super(Ensure.not_null(b, "b == null").name());
		m_bean = b;
	}
	
	/**
	 * Obtains the bean.
	 * @return the bean
	 */
	public SdlBean bean() {
		return m_bean;
	}
	
	@Override
	public JavaType generate_type() {
		JavaMethod cc = m_bean.property(JavaMethod.class,
				SdlBean.SDL_PROP_COPY_CONSTRUCTOR);
		if (cc == null) {
			return super.generate_type();
		} else {
			return new JavaClassWithCopyConstructorType(name());
		}
	}
}
