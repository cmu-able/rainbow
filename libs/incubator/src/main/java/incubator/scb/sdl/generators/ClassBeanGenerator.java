package incubator.scb.sdl.generators;

import java.util.Map;

import incubator.jcodegen.JavaClass;
import incubator.jcodegen.JavaCode;
import incubator.jcodegen.JavaPackage;
import incubator.pval.Ensure;
import incubator.scb.sdl.GenerationInfo;
import incubator.scb.sdl.GenerationResult;
import incubator.scb.sdl.SdlBean;
import incubator.scb.sdl.SdlBeanGenerator;
import incubator.scb.sdl.SdlGenerationException;

/**
 * Bean generator that creates a class for the bean.
 */
public class ClassBeanGenerator implements SdlBeanGenerator {
	/**
	 * Suggested name for the generator.
	 */
	public static final String NAME = "class";
	
	/**
	 * SDL property attached to the bean with the {@link JavaClass} that
	 * was generated.
	 */
	public static final String SDL_PROP_CLASS =
			ClassBeanGenerator.class.getCanonicalName() + ":class";
	
	/**
	 * Creates a new generator.
	 */
	public ClassBeanGenerator() {
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
		
		JavaClass cls = b.property(JavaClass.class, SDL_PROP_CLASS);
		if (cls != null) {
			return new GenerationInfo(GenerationResult.NOTHING_TO_DO);
		}
		
		cls = jp.child_class(b.name());
		if (cls != null) {
			throw new SdlGenerationException("Class '" + b.name() + "' "
					+ "already exists in package '" + jp.fqn() + "'.");
		}
		
		JavaClass pclass = null;
		if (b.parent() != null) {
			pclass = b.parent().property(JavaClass.class, SDL_PROP_CLASS);
			if (pclass == null) {
				return new GenerationInfo(GenerationResult.CANNOT_RUN,
						ClassBeanGenerator.class.getCanonicalName()
						+ ": class found but is not what was expected");
			}
		}
		
		cls = jp.make_class(b.name(), pclass);
		b.property(SDL_PROP_CLASS, cls);
		
		return new GenerationInfo(GenerationResult.GENERATED_CODE);
	}
}
