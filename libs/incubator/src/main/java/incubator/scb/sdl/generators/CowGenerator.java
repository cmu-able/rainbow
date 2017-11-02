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
 * Generator for COW (Copy-On-Write) setters.
 */
public class CowGenerator implements SdlBeanGenerator {
	/**
	 * Suggested generator name.
	 */
	public static final String NAME = "cow_setters";
	
	/**
	 * Property attached to the attribute with the COW setter method.
	 */
	public static final String SDL_PROP_COW_SETTER = "cow_setter";
	
	/**
	 * Creates a new generator.
	 */
	public CowGenerator() {
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
					CowGenerator.class.getCanonicalName()
					+ ": bean class not found");
		}
		
		JavaMethod cc = b.property(JavaMethod.class,
				SdlBean.SDL_PROP_COPY_CONSTRUCTOR);
		if (cc == null) {
			return new GenerationInfo(GenerationResult.CANNOT_RUN,
					CowGenerator.class.getCanonicalName()
					+ ": copy constructor not found");
		}
		
		List<SdlAttribute> attrs = new ArrayList<>();
		List<JavaField> fields = new ArrayList<>();
		List<JavaMethod> setters = new ArrayList<>();
		for (String an : b.attribute_names()) {
			SdlAttribute a = b.attribute(an);
			JavaField f = a.property(JavaField.class,
					AttributesAsFieldsGenerator.SDL_PROP_FIELD);
			if (f == null) {
				return new GenerationInfo(GenerationResult.CANNOT_RUN,
						CowGenerator.class.getCanonicalName()
						+ ": field not found for attribute '" + an + "'");
			}
			
			JavaMethod m = a.property(JavaMethod.class,
					SimpleAttributeAccessorsGenerator.SDL_PROP_SETTER);
			if (m == null) {
				return new GenerationInfo(GenerationResult.CANNOT_RUN,
						CowGenerator.class.getCanonicalName()
						+ ": setter not found for attribute '" + an + "'");
			}
			
			attrs.add(a);
			fields.add(f);
			setters.add(m);
		}
		
		boolean any = false;
		for (int i = 0; i < attrs.size(); i++) {
			JavaMethod m = attrs.get(i).property(JavaMethod.class,
					SDL_PROP_COW_SETTER);
			if (m == null) {
				m = cls.make_method("cow_" + attrs.get(i).name(),
						b.type().generate_type());
				m.make_parameter("v", fields.get(i).type());
				m.append_contents(b.name() + " copy = new " + b.name()
						+ "(this);\n");
				m.append_contents("copy." + setters.get(i).name() + "(v);\n");
				m.append_contents("return copy;\n");
				attrs.get(i).property(SDL_PROP_COW_SETTER, m);
				any = true;
			}
		}
		
		if (any) {
			return new GenerationInfo(GenerationResult.GENERATED_CODE);
		} else {
			return new GenerationInfo(GenerationResult.NOTHING_TO_DO);
		}
	}
}
