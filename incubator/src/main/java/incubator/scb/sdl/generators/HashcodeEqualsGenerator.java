package incubator.scb.sdl.generators;

import incubator.jcodegen.JavaClass;
import incubator.jcodegen.JavaCode;
import incubator.jcodegen.JavaField;
import incubator.jcodegen.JavaMethod;
import incubator.jcodegen.JavaPackage;
import incubator.jcodegen.JavaType;
import incubator.pval.Ensure;
import incubator.scb.sdl.GenerationResult;
import incubator.scb.sdl.SdlAttribute;
import incubator.scb.sdl.SdlBean;
import incubator.scb.sdl.SdlBeanGenerator;
import incubator.scb.sdl.SdlGenerationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generator that creates the hashcode and equals methods.
 */
public class HashcodeEqualsGenerator implements SdlBeanGenerator {
	/**
	 * Suggested name for the generator.
	 */
	public static final String NAME = "hashcode_equals";
	
	/**
	 * Hashcode method name.
	 */
	public static final String SDL_PROP_HASHCODE_METHOD = "hashcode";
	
	/**
	 * Creates a new generator.
	 */
	public HashcodeEqualsGenerator() {
		/*
		 * Nothing to do.
		 */
	}
	
	@Override
	public GenerationResult generate(SdlBean b, JavaCode jc, JavaPackage jp,
			Map<String, String> properties) throws SdlGenerationException {
		Ensure.not_null(b, "b == null");
		Ensure.not_null(jc, "jc == null");
		Ensure.not_null(jp, "jp == null");
		Ensure.not_null(properties, "properties == null");
		
		JavaClass cls = b.property(JavaClass.class,
				ClassBeanGenerator.SDL_PROP_CLASS);
		if (cls == null) {
			return GenerationResult.CANNOT_RUN;
		}
		
		List<SdlAttribute> attrs = new ArrayList<>();
		List<JavaField> fields = new ArrayList<>();
		
		for (String an : b.attribute_names()) {
			SdlAttribute a = b.attribute(an);
			JavaField jf = a.property(JavaField.class,
					AttributesAsFieldsGenerator.SDL_PROP_FIELD);
			if (jf == null) {
				return GenerationResult.CANNOT_RUN;
			}
			
			attrs.add(a);
			fields.add(jf);
		}
		
		JavaMethod m = b.property(JavaMethod.class, SDL_PROP_HASHCODE_METHOD);
		if (m == null) {
			m = cls.make_method("hashCode", new JavaType("int"));
			b.property(SDL_PROP_HASHCODE_METHOD, m);
			JavaMethod em = cls.make_method("equals", new JavaType("boolean"));
			em.make_parameter("obj", new JavaType("Object"));
			
			m.append_contents("final int prime = 31;\n");
			m.append_contents("int result = 1;\n");
			
			em.append_contents("if (this == obj) return true;\n");
			em.append_contents("if (obj == null) return false;\n");
			em.append_contents("if (getClass() != obj.getClass()) "
					+ "return false;\n");
			em.append_contents(b.type().generate_type().name() + " other = ("
					+ b.type().generate_type().name() + ") obj;\n");
			
			for (int i = 0; i < attrs.size(); i++) {
				String fn = fields.get(i).name();
				
				m.append_contents("result = prime * result + "
						+ "org.apache.commons.lang.ObjectUtils.hashCode("
						+ fn + ");\n");
				
				em.append_contents("if (!org.apache.commons.lang.ObjectUtils"
						+ ".equals(" + fn + ", other." + fn + ")) return "
						+ "false;\n");
			}
			
			m.append_contents("return result;\n");
			
			em.append_contents("return true;\n");
			return GenerationResult.GENERATED_CODE;
		}
		
		return GenerationResult.NOTHING_TO_DO;
	}
}
