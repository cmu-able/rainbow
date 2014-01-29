package incubator.scb.sdl.generators;

import java.util.Map;

import incubator.jcodegen.JavaClass;
import incubator.jcodegen.JavaCode;
import incubator.jcodegen.JavaField;
import incubator.jcodegen.JavaPackage;
import incubator.pval.Ensure;
import incubator.scb.sdl.GenerationInfo;
import incubator.scb.sdl.GenerationResult;
import incubator.scb.sdl.SdlAttribute;
import incubator.scb.sdl.SdlBean;
import incubator.scb.sdl.SdlBeanGenerator;
import incubator.scb.sdl.SdlGenerationException;

/**
 * Generator that creates fields for the bean attributes.
 */
public class AttributesAsFieldsGenerator implements SdlBeanGenerator {
	/**
	 * Suggested name for the generator.
	 */
	public static final String NAME = "attributes_as_fields";
	
	/**
	 * SDL property attached to the SDL attribute with the field.
	 */
	public static final String SDL_PROP_FIELD = "field";
	
	/**
	 * Prefix used when generating field names.
	 */
	private static final String FIELD_PREFIX = "m_";
	
	/**
	 * Creates a new generator.
	 */
	public AttributesAsFieldsGenerator() {
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
					AttributesAsFieldsGenerator.class.getCanonicalName()
					+ ": bean class not found");
		}
		
		boolean done_any = false;
		
		for (String an : b.attribute_names()) {
			SdlAttribute a = b.attribute(an);
			if (a.property(JavaField.class, SDL_PROP_FIELD) == null) {
				JavaField jf = cls.make_field(FIELD_PREFIX + a.name(),
						a.type().generate_type());
				a.property(SDL_PROP_FIELD, jf);
				done_any = true;
			}
		}
		
		if (done_any) {
			return new GenerationInfo(GenerationResult.GENERATED_CODE);
		} else {
			return new GenerationInfo(GenerationResult.NOTHING_TO_DO);
		}
	}
}
