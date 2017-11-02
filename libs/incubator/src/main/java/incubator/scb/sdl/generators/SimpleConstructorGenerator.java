package incubator.scb.sdl.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import incubator.jcodegen.JavaClass;
import incubator.jcodegen.JavaCode;
import incubator.jcodegen.JavaField;
import incubator.jcodegen.JavaMethod;
import incubator.jcodegen.JavaPackage;
import incubator.pval.Ensure;
import incubator.scb.sdl.GenerationInfo;
import incubator.scb.sdl.GenerationResult;
import incubator.scb.sdl.SdlAttribute;
import incubator.scb.sdl.SdlBean;
import incubator.scb.sdl.SdlBeanGenerator;
import incubator.scb.sdl.SdlGenerationException;

/**
 * Generator that creates a constructor that receives values for all attributes
 * and assigns them to the fields.
 */
public class SimpleConstructorGenerator implements SdlBeanGenerator {
	/**
	 * Suggested name for the generator.
	 */
	public static final String NAME = "simple_constructor";
	
	/**
	 * SDL property with the simple constructor.
	 */
	public static final String SDL_PROP_SIMPLE_CONSTRUCTOR =
			"simple_constructor"; 
	
	/**
	 * Constructor.
	 */
	public SimpleConstructorGenerator() {
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
					SimpleConstructorGenerator.class.getCanonicalName()
					+ ": no class found for bean");
		}
		
		List<JavaField> fields = new ArrayList<>();
		List<SdlAttribute> attrs = new ArrayList<>();
		for (String an : b.attribute_names()) {
			SdlAttribute attr = b.attribute(an);
			attrs.add(attr);
			
			JavaField jf = attr.property(JavaField.class,
					AttributesAsFieldsGenerator.SDL_PROP_FIELD);
			if (jf == null) {
				return new GenerationInfo(GenerationResult.CANNOT_RUN,
						SimpleConstructorGenerator.class.getCanonicalName()
						+ ": no field found for attribute '" + an + "'");
			}
			
			fields.add(jf);
		}
		
		JavaMethod m = b.property(JavaMethod.class,
				SDL_PROP_SIMPLE_CONSTRUCTOR);
		if (m == null) {
			m = cls.make_method(cls.name(), null);
			for (int i = 0; i < fields.size(); i++) {
				SdlAttribute a = attrs.get(i);
				JavaField f = fields.get(i);
				
				m.make_parameter(a.name(), f.type());
				m.append_contents(a.generate_invariants(a.name()));
				m.append_contents("this." + f.name() + " = "
						+ f.type().copy_expression(a.name()) + ";\n");
			}
			
			b.property(SDL_PROP_SIMPLE_CONSTRUCTOR, m);
			return new GenerationInfo(GenerationResult.GENERATED_CODE);
		}
		
		return new GenerationInfo(GenerationResult.NOTHING_TO_DO);
	}
}
