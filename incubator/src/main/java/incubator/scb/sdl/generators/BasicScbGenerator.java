package incubator.scb.sdl.generators;

import incubator.dispatch.Dispatcher;
import incubator.dispatch.DispatcherOp;
import incubator.dispatch.LocalDispatcher;
import incubator.jcodegen.JavaClass;
import incubator.jcodegen.JavaCode;
import incubator.jcodegen.JavaField;
import incubator.jcodegen.JavaMethod;
import incubator.jcodegen.JavaPackage;
import incubator.jcodegen.JavaType;
import incubator.jcodegen.ProtectionLevel;
import incubator.pval.Ensure;
import incubator.scb.Scb;
import incubator.scb.ScbBooleanField;
import incubator.scb.ScbDateField;
import incubator.scb.ScbEnumField;
import incubator.scb.ScbField;
import incubator.scb.ScbIntegerField;
import incubator.scb.ScbLongField;
import incubator.scb.ScbTextField;
import incubator.scb.ScbUpdateListener;
import incubator.scb.sdl.GenerationResult;
import incubator.scb.sdl.SdlAttribute;
import incubator.scb.sdl.SdlBean;
import incubator.scb.sdl.SdlBeanGenerator;
import incubator.scb.sdl.SdlEnumerationType;
import incubator.scb.sdl.SdlGenerationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generator that creates the basic methods and fields to support
 * {@link Scb}.
 */
public class BasicScbGenerator implements SdlBeanGenerator {
	/**
	 * Suggested name for the generator.
	 */
	public static final String NAME = "basic_scb";
	
	/**
	 * Name of property with dispatcher field.
	 */
	public static final String SDL_PROP_DISPATCHER_FIELD = "dispatcher_f";
	
	/**
	 * Name of property with dispatcher method.
	 */
	public static final String SDL_PROP_DISPATCHER_METHOD = "dispatcher_m";
	
	/**
	 * Name of property with the SCB field method.
	 */
	public static final String SDL_PROP_SCB_FIELD_METHOD = "scb_field";
	
	/**
	 * Has the dispatcher been initialized in the simple constructor?
	 */
	public static final String SDL_PROP_INIT_IN_SC = "init_in_sc";
	
	/**
	 * Has the dispatcher been initialized in the copy constructor?
	 */
	public static final String SDL_PROP_INIT_IN_CC = "init_in_cc";
	
	/**
	 * Name of property with the method that returns all fields.
	 */
	public static final String SDL_PROP_ALL_FIELDS_METHOD = "all_fields";
	
	/**
	 * Name of property with the notify update method.
	 */
	public static final String SDL_PROP_NOTIFY_UPDATE_METHOD = "notify_update";
	
	/**
	 * Creates a new generator.
	 */
	public BasicScbGenerator() {
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
		
		JavaClass bc = b.property(JavaClass.class,
				ClassBeanGenerator.SDL_PROP_CLASS);
		if (bc == null) {
			return GenerationResult.CANNOT_RUN;
		}
		
		boolean done_any = false;
		
		List<SdlAttribute> attrs = new ArrayList<>();
		List<JavaField> fields = new ArrayList<>();
		for (String an : b.attribute_names()) {
			SdlAttribute sa = b.attribute(an);
			JavaField af = sa.property(JavaField.class,
					AttributesAsFieldsGenerator.SDL_PROP_FIELD);
			if (af == null) {
				return GenerationResult.CANNOT_RUN;
			}
			
			attrs.add(sa);
			fields.add(af);
		}
		
		JavaType update_dispatcher_type = new JavaType(
				LocalDispatcher.class.getCanonicalName() + "<"
				+ ScbUpdateListener.class.getCanonicalName() + "<"
				+ bc.name() + ">>");
		
		JavaField df = b.property(JavaField.class, SDL_PROP_DISPATCHER_FIELD);
		if (df == null) {
			df = bc.make_field("m_update_dispatcher", update_dispatcher_type);
			bc.add_implements(Scb.class.getCanonicalName() + "<" + bc.name()
					+ ">");
			JavaMethod dm = bc.make_method("dispatcher",
					new JavaType(Dispatcher.class.getCanonicalName() + "<"
					+ ScbUpdateListener.class.getCanonicalName() + "<"
					+ bc.name() + ">>"));
			b.property(SDL_PROP_DISPATCHER_FIELD, df);
			b.property(SDL_PROP_DISPATCHER_METHOD, dm);
			dm.append_contents("return m_update_dispatcher;\n");
			done_any = true;
		}
		
		JavaMethod sc_method = b.property(JavaMethod.class,
				SimpleConstructorGenerator.SDL_PROP_SIMPLE_CONSTRUCTOR);
		if (sc_method != null) {
			Boolean init = b.property(Boolean.class, SDL_PROP_INIT_IN_SC);
			if (init == null || !init) {
				sc_method.append_contents("this.m_update_dispatcher = "
						+ "new incubator.dispatch.LocalDispatcher<>();\n");
				b.property(SDL_PROP_INIT_IN_SC, true);
			}
		}
		
		JavaMethod cc_method = b.property(JavaMethod.class,
				SdlBean.SDL_PROP_COPY_CONSTRUCTOR);
		if (cc_method != null) {
			Boolean init = b.property(Boolean.class, SDL_PROP_INIT_IN_CC);
			if (init == null || !init) {
				cc_method.append_contents("this.m_update_dispatcher = "
						+ "new incubator.dispatch.LocalDispatcher<>();\n");
				b.property(SDL_PROP_INIT_IN_CC, true);
			}
		}
		
		JavaMethod nu_method = b.property(JavaMethod.class,
				SDL_PROP_NOTIFY_UPDATE_METHOD);
		if (nu_method == null) {
			nu_method = bc.make_method("notify_update", new JavaType("void"));
			b.property(SDL_PROP_NOTIFY_UPDATE_METHOD, nu_method);
			nu_method.protection(ProtectionLevel.PROTECTED);
			nu_method.append_contents("this.m_update_dispatcher."
					+ "dispatch(new "
					+ DispatcherOp.class.getCanonicalName()
					+ "<" + ScbUpdateListener.class.getCanonicalName()
					+ "<" + bc.name() + ">>() {\n");
			nu_method.append_contents("@Override\n");
			nu_method.append_contents("public void dispatch("
					+ ScbUpdateListener.class.getCanonicalName()
					+ "<" + bc.name() + "> l) {\n");
			nu_method.append_contents("incubator.pval.Ensure.not_null("
					+ "l, \"l == null\");\n");
			nu_method.append_contents("l.updated(" + bc.name()
					+ ".this);\n");
			nu_method.append_contents("}\n");
			nu_method.append_contents("});\n");
		}
		
		for (int i = 0; i < attrs.size(); i++) {
			JavaType t = null;
			boolean needs_type;
			if (attrs.get(i).type().name().equals("String")) {
				t = new JavaType(ScbTextField.class.getCanonicalName()
						+ "<" + bc.name() + ">");
				needs_type = false;
			} else if (attrs.get(i).type().name().equals("int")
					|| attrs.get(i).type().name().equals("Integer")) {
				t = new JavaType(ScbIntegerField.class.getCanonicalName()
						+ "<" + bc.name() + ">");
				needs_type = false;
			} else if (attrs.get(i).type().name().equals("boolean")
					|| attrs.get(i).type().name().equals("Boolean")) {
				t = new JavaType(ScbBooleanField.class.getCanonicalName()
						+ "<" + bc.name() + ">");
				needs_type = false;
			} else if (attrs.get(i).type().name().equals("long")
					|| attrs.get(i).type().name().equals("Long")) {
				t = new JavaType(ScbLongField.class.getCanonicalName()
						+ "<" + bc.name() + ">");
				needs_type = false;
			} else if (attrs.get(i).type().name().equals("java.util.Date")) {
				t = new JavaType(ScbDateField.class.getCanonicalName()
						+ "<" + bc.name() + ">");
				needs_type = false;
			} else if(attrs.get(i).type() instanceof SdlEnumerationType) {
				t = new JavaType(ScbEnumField.class.getCanonicalName() + "<"
						+ bc.name() + ", " + attrs.get(i).type().name() + ">");
				needs_type = true;
			} else {
				t = new JavaType(ScbField.class.getCanonicalName() + "<"
						+ bc.name() + "," + attrs.get(i).type().name() + ">");
				needs_type = true;
			}
			
			JavaMethod getter = attrs.get(i).property(JavaMethod.class,
					SimpleAttributeAccessorsGenerator.SDL_PROP_GETTER);
			if (getter == null) {
				return GenerationResult.CANNOT_RUN;
			}
			
			JavaMethod setter = attrs.get(i).property(JavaMethod.class,
					SimpleAttributeAccessorsGenerator.SDL_PROP_SETTER);
			
			JavaMethod jm = attrs.get(i).property(JavaMethod.class,
					SDL_PROP_SCB_FIELD_METHOD);
			if (jm == null) {
				jm = bc.make_method("c_" + attrs.get(i).name(), t);
				jm.set_static();
				jm.append_contents("return new " + t.name() + "(\""
						+ attrs.get(i).name() + "\", "
						+ (setter != null? "true" : "false") + ", null"
						+ (needs_type?
						", " + attrs.get(i).type().name() + ".class" : "")
						+ ") {\n");
				jm.append_contents("@Override\n");
				String ftype = fields.get(i).type().name();
				if (ftype.equals("int")) {
					ftype = "Integer";
				} else if (ftype.equals("boolean")) {
					ftype = "Boolean";
				} else if (ftype.equals("long")) {
					ftype = "Long";
				}
				
				jm.append_contents("public " + ftype
						+ " get(" + bc.name() + " bean) {\n");
				jm.append_contents("incubator.pval.Ensure.not_null(bean, "
						+ "\"bean == null\");\n");
				jm.append_contents("return bean." + getter.name() + "();\n");
				jm.append_contents("}\n");
				jm.append_contents("@Override\n");
				jm.append_contents("public void set(" + bc.name() + " bean, "
						+ ftype + " v) {\n");
				if (setter != null) {
					jm.append_contents("incubator.pval.Ensure.not_null(bean, "
							+ "\"bean == null\");\n");
					jm.append_contents("bean." + setter.name() + "(v);\n");
					setter.append_contents("notify_update();\n");
				} else {
					jm.append_contents("incubator.pval.Ensure.unreachable("
							+ "\"Cannot set value in read only field\");\n");
				}
				jm.append_contents("}\n");
				jm.append_contents("};\n");
				
				attrs.get(i).property(SDL_PROP_SCB_FIELD_METHOD, jm);
				done_any = true;
			}
		}
		
		JavaMethod m = b.property(JavaMethod.class, SDL_PROP_ALL_FIELDS_METHOD);
		if (m == null) {
			JavaType fields_type = new JavaType("java.util.List<"
					+ ScbField.class.getCanonicalName() + "<" + bc.name()
					+ ", ?>>");
			m = bc.make_method("c_fields", fields_type);
			b.property(SDL_PROP_ALL_FIELDS_METHOD, m);
			m.set_static();
			m.append_contents("java.util.List<"
					+ ScbField.class.getCanonicalName() + "<" + bc.name()
					+ ", ?>> fields = new java.util.ArrayList<>();\n");
			
			for (int i = 0; i < attrs.size(); i++) {
				m.append_contents("fields.add(c_" + attrs.get(i).name()
						+ "());\n");
			}
			
			m.append_contents("return fields;\n");
			
			JavaMethod m2 = bc.make_method("fields", fields_type);
			m2.append_contents("return c_fields();\n");
		}
		
		if (done_any) {
			return GenerationResult.GENERATED_CODE;
		} else {
			return GenerationResult.NOTHING_TO_DO;
		}
	}
}
