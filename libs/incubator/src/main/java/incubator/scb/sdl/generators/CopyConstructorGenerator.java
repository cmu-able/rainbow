package incubator.scb.sdl.generators;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generator that creates a copy constructor.
 */
public class CopyConstructorGenerator implements SdlBeanGenerator {
	/**
	 * Suggested name for the generator.
	 */
	public static final String NAME = "copy_constructor";
	
	/**
	 * Constructor.
	 */
	public CopyConstructorGenerator() {
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
					CopyConstructorGenerator.class.getCanonicalName()
					+ ": bean class not found");
		}
		
		List<JavaField> fields = new ArrayList<>();
		for (String an : b.attribute_names()) {
			SdlAttribute attr = b.attribute(an);
			
			JavaField jf = attr.property(JavaField.class,
					AttributesAsFieldsGenerator.SDL_PROP_FIELD);
			if (jf == null) {
				return new GenerationInfo(GenerationResult.CANNOT_RUN,
						CopyConstructorGenerator.class.getCanonicalName()
						+ ": no field found for attribute '" + an + "'");
			}
			
			fields.add(jf);
		}
		
		JavaMethod m = b.property(JavaMethod.class,
				SdlBean.SDL_PROP_COPY_CONSTRUCTOR);
		if (m == null) {
			m = cls.make_method(cls.name(), null);
			m.make_parameter("src", b.type().generate_type());
			m.append_contents("incubator.pval.Ensure.not_null(src,"
					+ " \"src == null\");\n");
			for (int i = 0; i < fields.size(); i++) {
				JavaField f = fields.get(i);
				
				m.append_contents("this." + f.name() + " = "
						+ f.type().copy_expression("src." + f.name())
						+ ";\n");
			}
			
			b.property(SdlBean.SDL_PROP_COPY_CONSTRUCTOR, m);
			return new GenerationInfo(GenerationResult.GENERATED_CODE);
		} else {
			return new GenerationInfo(GenerationResult.NOTHING_TO_DO);
		}
	}
}
