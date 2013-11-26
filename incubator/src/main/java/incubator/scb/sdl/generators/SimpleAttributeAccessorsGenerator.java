package incubator.scb.sdl.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import incubator.jcodegen.JavaClass;
import incubator.jcodegen.JavaCode;
import incubator.jcodegen.JavaField;
import incubator.jcodegen.JavaMethod;
import incubator.jcodegen.JavaPackage;
import incubator.jcodegen.JavaType;
import incubator.jcodegen.ProtectionLevel;
import incubator.pval.Ensure;
import incubator.scb.sdl.GenerationResult;
import incubator.scb.sdl.SdlAttribute;
import incubator.scb.sdl.SdlBean;
import incubator.scb.sdl.SdlBeanGenerator;
import incubator.scb.sdl.SdlGenerationException;

/**
 * Generator that generates simple attribute accessors.
 */
public class SimpleAttributeAccessorsGenerator implements SdlBeanGenerator {
	/**
	 * Suggested generator name.
	 */
	public static final String NAME = "simple_attribute_accessors";
	
	/**
	 * Property name for SDL getter method.
	 */
	public static final String SDL_PROP_GETTER = "getter";
	
	/**
	 * Property name for SDL setter method.
	 */
	public static final String SDL_PROP_SETTER = "setter";
	
	/**
	 * Generation property for setter protection level.
	 */
	public static final String GEN_PROP_SETTER_PROT = "setter_protection";
	
	/**
	 * Public protection level.
	 */
	public static final String GEN_PROPVAL_PUBLIC = "public";
	
	/**
	 * Public protection level.
	 */
	public static final String GEN_PROPVAL_PRIVATE = "private";
	
	/**
	 * Public protection level.
	 */
	public static final String GEN_PROPVAL_PROTECTED = "protected";
	
	/**
	 * Public protection level.
	 */
	public static final String GEN_PROPVAL_PACKAGE = "package";
	
	/**
	 * Creates a new generator.
	 */
	public SimpleAttributeAccessorsGenerator() {
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
		
		ProtectionLevel setter_protection = null;
		for (String k : properties.keySet()) {
			String v = properties.get(k);
			switch (k) {
			case GEN_PROP_SETTER_PROT:
				switch (v) {
				case GEN_PROPVAL_PUBLIC:
					setter_protection = ProtectionLevel.PUBLIC;
					break;
				case GEN_PROPVAL_PACKAGE:
					setter_protection = ProtectionLevel.PACKAGE;
					break;
				case GEN_PROPVAL_PROTECTED:
					setter_protection = ProtectionLevel.PROTECTED;
					break;
				case GEN_PROPVAL_PRIVATE:
					setter_protection = ProtectionLevel.PRIVATE;
					break;
				default:
					throw new SdlGenerationException("Unknown value for "
							+ GEN_PROP_SETTER_PROT + " property: " + v + ".");
				
				}
				break;
				
			default:
				throw new SdlGenerationException("Uknown generation "
						+ "property: " + k + ".");
			}
		}
		
		JavaClass cls = b.property(JavaClass.class,
				ClassBeanGenerator.SDL_PROP_CLASS);
		if (cls == null) {
			return GenerationResult.CANNOT_RUN;
		}
		
		List<JavaField> fields = new ArrayList<>();
		List<SdlAttribute> attrs = new ArrayList<>();
		for (String an : b.attribute_names()) {
			SdlAttribute attr = b.attribute(an);
			attrs.add(attr);
			
			JavaField jf = attr.property(JavaField.class,
					AttributesAsFieldsGenerator.SDL_PROP_FIELD);
			if (jf == null) {
				return GenerationResult.CANNOT_RUN;
			}
			
			fields.add(jf);
		}
		
		boolean done_any = false;
		for (int i = 0; i < fields.size(); i++) {
			SdlAttribute a = attrs.get(i);
			JavaField f = fields.get(i);
			
			JavaMethod m = a.property(JavaMethod.class, SDL_PROP_GETTER);
			if (m == null) {
				m = cls.make_method(a.name(), a.type().generate_type());
				m.append_contents("return "
							+ f.type().copy_expression(f.name()) + ";\n");
				a.property(SDL_PROP_GETTER, m);
				
				if (!b.read_only() || setter_protection != null) {
					m = cls.make_method(a.name(), new JavaType("void"));
					if (setter_protection != null) {
						m.protection(setter_protection);
					}
					
					a.property(SDL_PROP_SETTER, m);
					m.make_parameter("v", a.type().generate_type());
					m.append_contents(a.generate_invariants("v"));
					m.append_contents("if (org.apache.commons.lang.ObjectUtils"
							+ ".equals(" + f.name() + ", v)) {\n");
					m.append_contents("return;\n");
					m.append_contents("}\n");
					m.append_contents("this." + f.name() + " = "
							+ f.type().copy_expression("v") + ";\n");
				}
				
				done_any = true;
			}
		}
		
		if (done_any) {
			return GenerationResult.GENERATED_CODE;
		} else {
			return GenerationResult.NOTHING_TO_DO;
		}
	}
}
