package incubator.scb.sdl.generators;

import incubator.jcodegen.JavaClass;
import incubator.jcodegen.JavaCode;
import incubator.jcodegen.JavaMethod;
import incubator.jcodegen.JavaPackage;
import incubator.pval.Ensure;
import incubator.scb.sdl.GenerationInfo;
import incubator.scb.sdl.GenerationResult;
import incubator.scb.sdl.SdlBean;
import incubator.scb.sdl.SdlBeanGenerator;
import incubator.scb.sdl.SdlGenerationException;

import java.util.Map;

/**
 * Generator that creates a no-argument constructor.
 */
public class NoArgumentConstructorGenerator implements SdlBeanGenerator {
	/**
	 * Suggested name for the generator.
	 */
	public static final String NAME = "no_arg_constructor";
	
	/**
	 * Constructor.
	 */
	public NoArgumentConstructorGenerator() {
		/*
		 * Nothing to do.
		 */
	}
	
	@Override
	public GenerationInfo generate(SdlBean b, JavaCode jc, JavaPackage jp,
			Map<String, String> properties) throws SdlGenerationException {
		Ensure.not_null(b, "b == null");
		Ensure.not_null(jc, "jc == null");
		Ensure.not_null(jp, "jp == null");
		Ensure.not_null(properties, "properties == null");
		
		JavaClass cls = b.property(JavaClass.class,
				ClassBeanGenerator.SDL_PROP_CLASS);
		if (cls == null) {
			return new GenerationInfo(GenerationResult.CANNOT_RUN,
					NoArgumentConstructorGenerator.class.getCanonicalName()
					+ ": no class found for bean");
		}
		
		JavaMethod m = b.property(JavaMethod.class,
				SdlBean.SDL_PROP_DEFAULT_CONSTRUCTOR);
		if (m == null) {
			m = cls.make_method(cls.name(), null);
			b.property(SdlBean.SDL_PROP_DEFAULT_CONSTRUCTOR, m);
			return new GenerationInfo(GenerationResult.GENERATED_CODE);
		} else {
			return new GenerationInfo(GenerationResult.NOTHING_TO_DO);
		}
	}
}
